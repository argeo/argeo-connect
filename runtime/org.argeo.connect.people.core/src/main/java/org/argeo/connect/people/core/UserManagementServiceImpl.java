package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/** Canonical implementation of the people {@link UserManagementService} */
public class UserManagementServiceImpl implements UserManagementService {

	//
	// GROUP MANAGEMENT
	//

	protected String getUserGroupParentPath() {
		return PeopleConstants.PEOPLE_USER_GROUPS_BASE_PATH;
	}

	@Override
	public Node getGroupById(Session session, String groupId) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_USER_GROUP,
					PeopleTypes.PEOPLE_USER_GROUP);
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_GROUP_ID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(groupId));
			Constraint defaultC = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 0)
				return null;
			else if (ni.getSize() > 1) {
				throw new PeopleException("Problem retrieving group " + groupId
						+ " by ID, we found " + ni.getSize()
						+ " correspnding entity(ies)");
			} else
				return ni.nextNode();
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create group " + groupId, re);
		}
	}

	@Override
	public Node createGroup(Session session, String groupId, String title,
			String description) {
		return createGroup(session, groupId, title, description, false);
	}

	private Node createGroup(Session session, String groupId, String title,
			String description, boolean isUserGroup) {
		try {
			String relPath = JcrUtils.firstCharsToPath(groupId, 2);
			String fullPath = getUserGroupParentPath() + "/" + relPath + "/"
					+ groupId;
			if (session.nodeExists(fullPath))
				return session.getNode(fullPath);
			else {
				Node newGroup = JcrUtils.mkdirs(session, fullPath,
						PeopleTypes.PEOPLE_USER_GROUP);

				newGroup.setProperty(PeopleNames.PEOPLE_GROUP_ID, groupId);
				if (CommonsJcrUtils.checkNotEmptyString(title))
					newGroup.setProperty(Property.JCR_TITLE, title);
				if (CommonsJcrUtils.checkNotEmptyString(description))
					newGroup.setProperty(Property.JCR_DESCRIPTION, description);

				if (isUserGroup) {
					newGroup.setProperty(
							PeopleNames.PEOPLE_IS_SINGLE_USER_GROUP, true);
				}
				CommonsJcrUtils.saveAndCheckin(newGroup);
				return newGroup;
			}
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create group " + groupId, re);
		}
	}

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		// Do nothing
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		// Do nothing
	}

	//
	// USER MANAGEMENT
	//
	/**
	 * If a userprofile exists but no peopleprofile, it creates, saves and
	 * checks-in the corresponding Node in a distinct session
	 * 
	 * @param session
	 * @param username
	 * @return a peopleprofile node or null if the userprofile is not found
	 */
	@Override
	public Node getPeopleProfile(Session session, String username) {
		Node userProfile = UserJcrUtils.getUserProfile(session, username);
		if (userProfile == null)
			return null;
		try {
			Node parent = userProfile.getParent();
			Node peopleProfile;
			if (!parent.hasNode(PeopleTypes.PEOPLE_PROFILE)) {
				Session tmpSession = null;
				try {
					String tmpParentPath = parent.getPath();
					tmpSession = session.getRepository().login();
					Node tmpParent = tmpSession.getNode(tmpParentPath);
					Node tmpProfile = tmpParent.addNode(
							PeopleTypes.PEOPLE_PROFILE,
							PeopleTypes.PEOPLE_PROFILE);
					tmpSession.save();

					String newPath = tmpProfile.getPath();
					tmpSession.getWorkspace().getVersionManager()
							.checkin(newPath);
					peopleProfile = session.getNode(newPath);
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to create people profile node for username "
									+ username, re);
				} finally {
					JcrUtils.logoutQuietly(tmpSession);
				}
			} else {
				peopleProfile = parent.getNode(PeopleTypes.PEOPLE_PROFILE);
			}
			return peopleProfile;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to get people profile node for username "
							+ username, re);
		}
	}

	public Node getMyPeopleProfile(Session session) {
		return getPeopleProfile(session, session.getUserID());
	}

	@Override
	public String addUsersToGroup(Session session, Node userGroup,
			List<String> usernames) {
		Session tmpSession = null;
		try {
			tmpSession = session.getRepository().login();
			StringBuilder builder = new StringBuilder();
			for (String currUserName : usernames) {
				Node peopleProfile = getPeopleProfile(tmpSession, currUserName);
				CommonsJcrUtils.checkout(peopleProfile);
				String msg = CommonsJcrUtils.addRefToMultiValuedProp(
						peopleProfile, PeopleNames.PEOPLE_USER_GROUPS,
						userGroup);
				if (msg != null) {
					builder.append(msg).append("\n");
					CommonsJcrUtils.cancelAndCheckin(peopleProfile);
				} else
					CommonsJcrUtils.saveAndCheckin(peopleProfile);
			}

			if (builder.length() > 0)
				return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("unable to add users to group "
					+ userGroup, re);
		} finally {
			JcrUtils.logoutQuietly(tmpSession);
		}
		return null;
	}

	// TODO manage adding without overwriting
	@Override
	public String addGroupsToUser(Session session, String username,
			List<Node> groups) {
		try {
			Node peopleProfile = getPeopleProfile(session, username);
			CommonsJcrUtils.checkout(peopleProfile);
			// FIXME it overrides all values at each time.
			CommonsJcrUtils.setMultipleReferences(peopleProfile,
					PeopleNames.PEOPLE_USER_GROUPS, groups);
			CommonsJcrUtils.saveAndCheckin(peopleProfile);

		} catch (RepositoryException re) {
			throw new PeopleException("unable to add groups to user "
					+ username, re);
		}
		return null;
	}

	@Override
	public List<Node> getUserGroups(Session session, String username) {
		List<Node> groups = new ArrayList<Node>();
		try {
			Node peopleProfile = getPeopleProfile(session, username);

			if (peopleProfile == null)
				return groups;

			if (!peopleProfile.hasProperty(PeopleNames.PEOPLE_USER_GROUPS))
				return groups;

			Value[] values = peopleProfile.getProperty(
					PeopleNames.PEOPLE_USER_GROUPS).getValues();

			for (Value val : values) {
				Node currNode = session.getNodeByIdentifier(val.getString());
				groups.add(currNode);
			}
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get groups for user "
					+ username, re);
		}
		return groups;
	}

	@Override
	public Node createDefaultGroupForUser(Session session, String username) {
		Node userGp = createGroup(session, username, username,
				"Default group for user " + username, true);
		List<Node> groups = new ArrayList<Node>();
		groups.add(userGp);
		addGroupsToUser(session, username, groups);
		return userGp;
	}

	/** returns true if the current user is in the specified role */
	@Override
	public boolean isUserInRole(String role) {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		for (GrantedAuthority ga : authen.getAuthorities()) {
			if (ga.getAuthority().equals(role))
				return true;
		}
		return false;
		// return currentUserService.getCurrentUser().getRoles().contains(role);
	}

	/** returns the current user ID **/
	@Override
	public String getCurrentUserId() {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		return authen.getName();
	}

	/** Returns a human readable display name using the user ID **/
	@Override
	public String getUserDisplayName(String userId) {
		// FIXME Must use a commons utils
		return userId;
	}

}
