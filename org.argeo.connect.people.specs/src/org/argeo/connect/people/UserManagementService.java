package org.argeo.connect.people;

import java.util.List;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;

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
	/** Returns the current user */
	public User getMyUser();

	/** Returns the DN of the current user */
	public String getMyUserName();

	/** Lists all roles of the current user */
	public String[] getMyRoles();

	/** Returns the local uid of the current connected user in this context */
	public String getMyLocalName();

	/** Returns the display name of the current logged in user */
	public String getMyDisplayName();

	/** Returns true if the current user is in the specified role */
	public boolean amIInRole(String role);

	// ALL USER: WARNING access to this will be later reduced

	/** Returns the current user */
	public User getUser(String dn);

	/** Can be a group or a user */
	public String getUserDisplayName(String dn);

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn);

	public List<Group> listGroups(String filter);
	/* MISCELLANEOUS */
}