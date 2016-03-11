package org.argeo.connect.people;

import java.util.List;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Provide method interfaces to manage user concepts without accessing directly
 * the userAdmin.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface UserAdminService {

	/* USERS */
	/** Returns the current user */
	public User getMyUser();

	/** Returns the DN of the current user */
	public String getMyUsername();

	/** Lists all roles of the current user */
	public String[] getMyRoles();

	/** Returns the local uid of the current connected user */
	public String getMyLocalName();

	/** Returns the display name of the current logged in user */
	public String getMyDisplayName();

	/** Returns the e-mail of the current logged in user */
	public String getMyMail();

	/** Returns true if the current user is in the specified role */
	public boolean amIInRole(String role);

	// ALL USER: WARNING access to this will be later reduced
	/** Returns a {@link User} given a username */
	public User getUser(String username);

	/** Can be a group or a user */
	public String getUserDisplayName(String dn);

	/** Can be a group or a user */
	public String getUserMail(String dn);

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn);

	/** Search among defined groups */
	public List<Group> listGroups(String filter);

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
	public UserAdmin getUserAdmin();

}