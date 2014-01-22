package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Provides method interfaces to manage people specific user concepts.
 * Implementing applications should extend and/or override the canonical
 * implementation in order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface UserManagementService {

	/* USERS */
	/**
	 * Returns the Node that will store all people specific information on a
	 * end-user using its username as a deterministic key
	 */
	public Node getPeopleProfile(Session session, String username);

	/* GROUPS */
	// /**
	// * Returns the base path for the groups
	// * **/
	// public String getUserGroupParentPath();

	/**
	 * Creates a new user group to be used in task assignation among other.
	 * Relies on the group ID if such a group already exists, existing one is
	 * returned.
	 * 
	 * @param session
	 * @param groupId
	 * @return
	 */
	public Node createGroup(Session session, String groupId, String title,
			String description);

	/**
	 * Simply returns existing default user group if such a one exists.
	 * Otherwise, it creates a default group with same id and add a reference in
	 * the user people:profile node.
	 * 
	 * @param session
	 * @param groupId
	 * @return
	 */
	public Node createDefaultGroupForUser(Session session, String username);

	/**
	 * Add a reference to the group to each user listed by its username.
	 * 
	 * @param userGroup
	 * @param usernames
	 * @return an error message listing the username where the reference was
	 *         already there
	 */
	public String addUsersToGroup(Session session, Node userGroup,
			List<String> usernames);

	/**
	 * Session is saved and userprofile checked in after addition.
	 * 
	 * @param userProfile
	 *            JCR Node of the profile in the Argeo Security model.
	 * @param groups
	 * @return
	 */
	public String addGroupsToUser(Session session, String username,
			List<Node> groups);

	public List<Node> getUserGroups(Session session, String username);

	/* MISCELLANEOUS */
}
