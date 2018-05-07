package org.argeo.connect;

import java.util.List;

import javax.jcr.Node;
import javax.transaction.UserTransaction;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Provide method interfaces to manage user concepts without accessing directly
 * the userAdmin.
 */
public interface UserAdminService {

	// CurrentUser
	/** Returns the e-mail of the current logged in user */
	public String getMyMail();

	// Other users
	/** Returns a {@link User} given a username */
	public User getUser(String username);

	/** Can be a group or a user */
	public String getUserDisplayName(String dn);

	/** Can be a group or a user */
	public String getUserMail(String dn);

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn);

	/** Checks if the passed user belongs to the passed role */
	public boolean isUserInRole(String userDn, String roleDn);

	// Search
	/** Returns a filtered list of roles */
	public Role[] getRoles(String filter) throws InvalidSyntaxException;

	/** Search among groups including system roles and users if needed */
	public List<User> listGroups(String filter, boolean includeUsers, boolean includeSystemRoles);

	/* MISCELLANEOUS */
	/** Returns the dn of a role given its local ID */
	public String buildDefaultDN(String localId, int type);

	/** Exposes the main default domain name for this instance */
	public String getDefaultDomainName();

	/**
	 * Search for a {@link User} (might also be a group) whose uid or cn is equals
	 * to localId within the various user repositories defined in the current
	 * context.
	 */
	public User getUserFromLocalId(String localId);

	void changeOwnPassword(char[] oldPassword, char[] newPassword);

	void resetPassword(String username, char[] newPassword);

	String addSharedSecret(String username, int hours);

	User createUserFromPerson(Node person);

	/* EXPOSE */
	public UserAdmin getUserAdmin();

	public UserTransaction getUserTransaction();
}