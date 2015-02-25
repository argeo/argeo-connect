package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.NewUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/** Canonical implementation of the people {@link UserManagementService} */
public class UserManagementServiceImpl implements UserManagementService {

	private final static Log log = LogFactory
			.getLog(UserManagementServiceImpl.class);

	private final PeopleService peopleService;

	public UserManagementServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	//
	// USERS MANAGEMENT
	//
	public Node createUser(Session adminSession,
			UserAdminService userAdminService, final String userName,
			final char[] password, final String firstName,
			final String lastName, final String email, final String desc,
			final List<String> roles) {
		try {
			NewUserDetails userDetails = new NewUserDetails(userName, password,
					roles.toArray(new String[0])) {
				private static final long serialVersionUID = 1L;

				@Override
				public void mapToProfileNode(Node userProfile)
						throws RepositoryException {
					userProfile.setProperty(ArgeoNames.ARGEO_PRIMARY_EMAIL,
							email);
					userProfile.setProperty(ArgeoNames.ARGEO_FIRST_NAME,
							firstName);
					userProfile.setProperty(ArgeoNames.ARGEO_LAST_NAME,
							lastName);
					userProfile.setProperty(Property.JCR_TITLE, firstName + " "
							+ lastName);
					if (CommonsJcrUtils.checkNotEmptyString(desc))
						userProfile.setProperty(Property.JCR_DESCRIPTION, desc);
				}

			};
			userAdminService.createUser(userDetails);
			return UserJcrUtils.getUserHome(adminSession, userName);
		} catch (Exception e) {
			JcrUtils.discardQuietly(adminSession);
			Node userHome = UserJcrUtils.getUserHome(adminSession, userName);
			if (userHome != null) {
				try {
					userHome.remove();
					adminSession.save();
				} catch (RepositoryException e1) {
					JcrUtils.discardQuietly(adminSession);
					log.warn("Error when trying to clean up failed new user "
							+ userName, e1);
				}
			}
		}
		return null;
	}

	//
	// GROUP MANAGEMENT
	//
	@Override
	public Node createGroup(Session session, String groupId, String title,
			String description) {
		return createGroup(session, groupId, title, description, false);
	}

	@Override
	public Node getGroupById(Session session, String groupId) {
		try {
			String fullPath = getPathFromGroupId(groupId);
			if (session.nodeExists(fullPath))
				return session.getNode(fullPath);
			else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve group of ID "
					+ groupId, re);
		}
	}

	private String getPathFromGroupId(String groupId) {
		String relPath = JcrUtils.firstCharsToPath(groupId, 2);
		String fullPath = peopleService
				.getBasePath(PeopleTypes.PEOPLE_USER_GROUP)
				+ "/"
				+ relPath
				+ "/" + groupId;
		return fullPath;
	}

	private Node createGroup(Session session, String groupId, String title,
			String description, boolean isUserGroup) {
		try {
			String fullPath = getPathFromGroupId(groupId);
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

	//
	// USER MANAGEMENT
	//
	/**
	 * If a "argeo:profile" node exists but no "people:profile", the later is
	 * created, saved and checked-in in a distinct session
	 * 
	 * @param session
	 * @param username
	 * @return a people:profile node or null if the argeo:profile is not found
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
					tmpSession = session.getRepository().login(
							session.getWorkspace().getName());
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
			tmpSession = session.getRepository().login(
					session.getWorkspace().getName());
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

			// it overrides all values at each time.
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

	/** Returns true if the current user is in the specified role */
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

	/** Returns the current user ID **/
	@Override
	public String getCurrentUserId() {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		return authen.getName();
	}

	/** Returns a human readable display name using the user ID **/
	@Override
	public String getUserDisplayName(String userId) {
		// TODO Must use a commons utils
		return userId;
	}
}