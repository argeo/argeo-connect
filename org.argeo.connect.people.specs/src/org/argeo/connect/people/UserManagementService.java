package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.security.UserAdminService;

/**
 * Provides method interfaces to manage people specific user concepts.
 * Implementing applications should extend and/or override the canonical
 * implementation in order to provide business specific behaviors.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface UserManagementService {

	/* USERS */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(String userRole);

	/** returns the current user ID **/
	public String getCurrentUserId();

	/** Returns a human readable display name using the user ID */
	public String getUserDisplayName(String userId);

	/* USERS */
	/**
	 * Returns the Node that will store all people specific information on a
	 * end-user using its username as a deterministic key.
	 * 
	 * If a "argeo:profile" node exists but no "people:profile", the later is
	 * created, saved and checked-in in a distinct session
	 * 
	 * @param session
	 * @param username
	 * @return a people:profile node or null if the argeo:profile is not found
	 */
	@Deprecated
	public Node getPeopleProfile(Session session, String username);

	/** Centralize People Specific user creation. NAT API. will evolve. */
	@Deprecated
	public Node createUser(Session adminSession,
			UserAdminService userAdminService, String userName,
			char[] password, String firstName, String lastName, String email,
			String desc, List<String> roles);

	/**
	 * Retrieves the list of the defined user groups given its id
	 * 
	 * @param session
	 * @param filter
	 * @param includeSingleUserGroups
	 *            if false, it will returns only groups otherwise it will also
	 *            include all groups that have been created to enable assignment
	 *            to a single user
	 * @return
	 */
	@Deprecated
	public List<Node> getDefinedGroups(Session session, String filter,
			boolean includeSingleUserGroups);

	/**
	 * Creates a new user group to be used among others in task assignment. It
	 * relies on the group ID: if such a group already exists, this existing one
	 * is returned.
	 * 
	 * @param session
	 * @param groupId
	 * @return
	 */
	@Deprecated
	public Node createGroup(Session session, String groupId, String title,
			String description);

	/**
	 * Retrieves a user group given its id
	 * 
	 * @param session
	 * @param groupId
	 * @return
	 */
	@Deprecated
	public Node getGroupById(Session session, String groupId);

	/**
	 * Simply returns existing default user group if such a one exists.
	 * Otherwise, it creates a default group with same id and add a reference in
	 * the user people:profile node.
	 * 
	 * @param session
	 * @param groupId
	 * @return
	 */
	@Deprecated
	public Node createDefaultGroupForUser(Session session, String username);

	/**
	 * Add a reference to the group to each user listed by its username.
	 * 
	 * @param userGroup
	 * @param usernames
	 * @return an error message listing the username where the reference was
	 *         already there
	 */
	@Deprecated
	public String addUsersToGroup(Session session, Node userGroup,
			List<String> usernames);

	/**
	 * Session is saved and userprofile checked in after addition.
	 * 
	 * @param session
	 * @param username
	 * @param groups
	 * @return
	 */
	@Deprecated
	public String addGroupsToUser(Session session, String username,
			List<Node> groups);

	/**
	 * Returns the list of groups for this user
	 * 
	 * @param session
	 * @param username
	 */
	@Deprecated
	public List<Node> getUserGroups(Session session, String username);

	/**
	 * Returns the list of my groups for
	 * 
	 * @param session
	 */
	@Deprecated
	public List<Node> getMyGroups(Session session);

	/**
	 * Returns the list of my groups for
	 * 
	 * @param session
	 */
	@Deprecated
	public boolean amIInGroup(Session session, String groupId);

	/* MISCELLANEOUS */
}