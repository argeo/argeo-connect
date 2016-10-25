package org.argeo.connect.people;

import java.util.List;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Provide method interfaces to manage user concepts without accessing directly
 * the userAdmin.
 */
public interface UserAdminService {

	/* SELF */
	/** Returns the e-mail of the current logged in user */
	public String getMyMail();

	// /** Returns the absolute path to the home node of the current user */
	// public String getCurrentUserHomePath();
	//
	// /** Returns the absolute path to the home node of the current user */
	// public String getUserHomePath(String dn);

	// /** Lists all roles of the current user */
	// public String[] getMyRoles();
	//
	// /** Returns the local id of the current connected user */
	// public String getMyLocalName();
	//
	//
	// /** Returns true if the current user is in the specified role */
	// public boolean amIInRole(String role);

	// ALL USER: WARNING access to this will be later reduced
	/** Returns a {@link User} given a username */
	public User getUser(String username);

	//
	/** Can be a group or a user */
	public String getUserDisplayName(String dn);

	/** Can be a group or a user */
	public String getUserMail(String dn);

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn);

	// /** Search among defined groups */
	// public List<Group> listGroups(String filter);

	/** Returns a filtered list of roles */
	public Role[] getRoles(String filter) throws InvalidSyntaxException;

	/**
	 * Search among defined groups including system roles and users if needed
	 */
	public List<User> listGroups(String filter, boolean includeUsers, boolean includeSystemRoles);

	/* MISCELLANEOUS */
	/** Simply returns the dn of a role given its local ID */
	public String buildDefaultDN(String localId, int type);

	/** Exposes the main default domain name for this instance */
	public String getDefaultDomainName();

	/**
	 * Search for a {@link User} (might also be a group) whose uid or cn is
	 * equals to localId within the various user repositories defined in the
	 * current context.
	 */
	public User getUserFromLocalId(String localId);

	/* EXPOSE */
	@Deprecated
	public UserAdmin getUserAdmin();
}