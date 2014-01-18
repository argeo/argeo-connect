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
	public Node createDefaultGroupForUser(Session session, Node userProfile);

	/** Get Corresponding users with their IDs */
	public String addUsersToGroup(Node userGroup, List<Node> userProfiles);

	public String addGroupsToUser(Node peopleProfile, List<Node> groups);

	/* MISCELLANEOUS */
}
