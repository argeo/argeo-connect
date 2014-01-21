package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;

/** Canonical implementation of the people {@link UserManagementService} */
public class UserManagementServiceImpl implements UserManagementService {

	protected String getUserGroupParentPath() {
		return PeopleConstants.PEOPLE_USER_GROUPS_BASE_PATH;
	}

	@Override
	public Node createGroup(Session session, String groupId, String title,
			String description) {
		try {
			String relPath = JcrUtils.firstCharsToPath(groupId, 2);
			String fullPath = getUserGroupParentPath() + "/" + relPath + "/"
					+ groupId;
			if (session.nodeExists(fullPath))
				return session.getNode(fullPath);
			else {
				Node newGroup = JcrUtils.mkdirs(session, fullPath,
						PeopleTypes.PEOPLE_USER_GROUP);
				if (CommonsJcrUtils.checkNotEmptyString(title))
					newGroup.setProperty(Property.JCR_TITLE, title);
				if (CommonsJcrUtils.checkNotEmptyString(description))
					newGroup.setProperty(Property.JCR_DESCRIPTION, description);
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
	@Override
	public String addUsersToGroup(Node userGroup, List<Node> userProfiles) {
		try {
			StringBuilder builder = new StringBuilder();
			for (Node currProfile : userProfiles) {
				Node parent = currProfile.getParent();
				Node peopleProfile;
				if (!parent.hasNode(PeopleTypes.PEOPLE_PROFILE))
					peopleProfile = parent.addNode(PeopleTypes.PEOPLE_PROFILE,
							PeopleTypes.PEOPLE_PROFILE);
				else {
					peopleProfile = parent.getNode(PeopleTypes.PEOPLE_PROFILE);
					CommonsJcrUtils.checkout(peopleProfile);
				}
				String msg = CommonsJcrUtils.addRefToMultiValuedProp(
						peopleProfile, PeopleNames.PEOPLE_USER_GROUPS,
						userGroup);
				if (msg != null)
					builder.append(msg).append("\n");
				CommonsJcrUtils.saveAndCheckin(peopleProfile);
			}

			if (builder.length() > 0)
				return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("unable to add users to group "
					+ userGroup, re);
		}
		return null;
	}

	@Override
	public String addGroupsToUser(Node userProfile, List<Node> groups) {
		try {
			Node parent = userProfile.getParent();
			Node peopleProfile;
			if (!parent.hasNode(PeopleTypes.PEOPLE_PROFILE))
				peopleProfile = parent.addNode(PeopleTypes.PEOPLE_PROFILE,
						PeopleTypes.PEOPLE_PROFILE);
			else {
				peopleProfile = parent.getNode(PeopleTypes.PEOPLE_PROFILE);
				CommonsJcrUtils.checkout(peopleProfile);
			}

			// FIXME it overrides all values at each time.
			CommonsJcrUtils.setMultipleReferences(peopleProfile,
					PeopleNames.PEOPLE_USER_GROUPS, groups);
			CommonsJcrUtils.saveAndCheckin(peopleProfile);

		} catch (RepositoryException re) {
			throw new PeopleException("unable to add groups to user "
					+ userProfile, re);
		}
		return null;
	}

	@Override
	public List<Node> getUserGroups(Node userProfile) {
		List<Node> groups = new ArrayList<Node>();
		try {
			Session session = userProfile.getSession();
			// Initialisation
			Node parent = userProfile.getParent();
			if (!parent.hasNode(PeopleTypes.PEOPLE_PROFILE))
				return groups;
			Node peopleProfile = parent.getNode(PeopleTypes.PEOPLE_PROFILE);
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
					+ userProfile, re);
		}
		return groups;
	}

	@Override
	public Node createDefaultGroupForUser(Session session, Node userProfile) {
		String currId = CommonsJcrUtils.get(userProfile,
				ArgeoNames.ARGEO_USER_ID);
		Node userGp = createGroup(session, currId, currId,
				"Default group for user " + currId);
		List<Node> groups = new ArrayList<Node>();
		groups.add(userGp);
		addGroupsToUser(userProfile, groups);
		return userGp;
	}

}
