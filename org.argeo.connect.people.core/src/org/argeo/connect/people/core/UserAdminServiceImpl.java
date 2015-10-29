package org.argeo.connect.people.core;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.UsersUtils;
import org.argeo.osgi.useradmin.LdifName;
import org.argeo.osgi.useradmin.UserDirectoryException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Canonical implementation of the people {@link UserAdminService}. Wrap
 * interaction with users and groups in a *READ-ONLY* mode. We want to be able
 * to:
 * <ul>
 * <li>Retrieve my user and corresponding information (main info, groups...)</li>
 * <li>List all local groups (not the system roles)</li>
 * <li>If sufficient rights: retrieve a given user and its information</li>
 * </ul>
 */
public class UserAdminServiceImpl implements UserAdminService {

	private final static Log log = LogFactory
			.getLog(UserAdminServiceImpl.class);

	private final PeopleService peopleService;
	private final UserAdmin userAdmin;

	public UserAdminServiceImpl(PeopleService peopleService, UserAdmin userAdmin) {
		this.peopleService = peopleService;
		this.userAdmin = userAdmin;
	}

	// CURRENT USER

	/** Returns the current user */
	public User getMyUser() {
		String name = getMyUserName();
		return (User) userAdmin.getRole(name);
	}

	/** Returns the DN of the current user */
	public String getMyUserName() {
		Subject subject = Subject.getSubject(AccessController.getContext());
		String name = subject.getPrincipals(X500Principal.class).iterator()
				.next().toString();
		return name;
	}

	/** Lists all roles of the current user */
	public String[] getMyRoles() {
		return getUserRoles(getMyUserName());
	}

	/** Returns the local uid of the current connected user in this context */
	public String getMyLocalName() {
		return getMyUser().getName();
	}

	/** Returns the display name of the current logged in user */
	public String getMyDisplayName() {
		return getUserDisplayName(getMyUserName());
	}

	/** Returns true if the current user is in the specified role */
	@Override
	public boolean amIInRole(String rolename) {
		// FIXME clean this
		String dn;
		if (rolename.startsWith(LdifName.cn.name() + "="))
			dn = rolename;
		else
			dn = LdifName.cn.name() + "=" + rolename + "," + SYSTEM_ROLE_SUFFIX;

		Role role = userAdmin.getRole(dn);
		if (role == null)
			return false;

		String roledn = role.getName();

		for (String currRole : getMyRoles())
			if (roledn.equals(currRole))
				return true;
		return false;
	}

	// ALL USER: WARNING access to this will be later reduced

	/** Returns the current user */
	public User getUser(String dn) {
		Role role = null;
		try {
			role = userAdmin.getRole(dn);
		} catch (Exception e) {
			if (e instanceof InvalidNameException
					|| e instanceof UserDirectoryException)
				log.warn("Unable to retrieve user. Check if " + dn
						+ " is a valid dn");
			else
				throw new PeopleException("Unable to "
						+ "retrieve user with id " + dn, e);
		}
		if (role == null)
			return null;
		else
			return (User) role;
	}

	/** Can be a group or a user */
	public String getUserDisplayName(String dn) {

		// FIXME: during initialisation phase, the system logs "admin" as user
		// name rather than the corresponding dn
		if ("admin".equals(dn))
			return "System Adminitrator";

		User user = getUser(dn);
		if (user == null) {
			log.warn("No user found for dn " + dn
					+ " returning it instead of the display name");
			return dn;

		} else {
			if (user instanceof Group)
				return UsersUtils.getProperty(user, LdifName.cn.name());
			else {
				String dName = UsersUtils.getProperty(user,
						LdifName.displayName.name());
				if (CommonsJcrUtils.isEmptyString(dName))
					dName = UsersUtils.getProperty(user, LdifName.cn.name());
				if (CommonsJcrUtils.isEmptyString(dName))
					dName = dn;
				return dName;
			}
		}
	}

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn) {
		Authorization currAuth = userAdmin.getAuthorization(getUser(dn));
		return currAuth.getRoles();
	}

	private final String[] knownProps = { LdifName.cn.name(),
			LdifName.sn.name(), LdifName.uid.name() };

	public List<Group> listGroups(String filter) {
		Role[] roles;
		try {
			StringBuilder builder = new StringBuilder();
			StringBuilder tmpBuilder = new StringBuilder();
			if (UsersUtils.notNull(filter))
				for (String prop : knownProps) {
					tmpBuilder.append("(");
					tmpBuilder.append(prop);
					tmpBuilder.append("=*");
					tmpBuilder.append(filter);
					tmpBuilder.append("*)");
				}
			if (tmpBuilder.length() > 1) {
				builder.append("(&(objectclass=groupOfNames)(|");
				builder.append(tmpBuilder.toString());
				builder.append("))");
			} else
				builder.append("(objectclass=groupOfNames)");
			roles = userAdmin.getRoles(builder.toString());
		} catch (InvalidSyntaxException e) {
			throw new ArgeoException("Unable to get roles with filter: "
					+ filter, e);
		}
		List<Group> users = new ArrayList<Group>();
		for (Role role : roles)
			if (role.getType() == Role.GROUP)
				users.add((Group) role);
		return users;
	}

	public String getDistinguishedName(String localId, int type) {

		// TODO check if we already have the dn of an existing user. Clean this
		try {
			Role role = userAdmin.getRole(localId);
			if (role != null)
				return localId;
		} catch (Exception e) {
			// silent
		}

		if (Role.GROUP == type)
			return LdifName.cn.name() + "=" + localId + "," + GROUPS_SUFFIX
					+ "," + getDefaultDomainName();
		else if (Role.USER == type)
			return LdifName.uid.name() + "=" + localId + "," + USERS_SUFFIX
					+ "," + getDefaultDomainName();
		else if (Role.ROLE == type)
			return LdifName.cn.name() + "=" + localId + ","
					+ SYSTEM_ROLE_SUFFIX;
		else
			throw new ArgeoException("Unknown role type. "
					+ "Cannot deduce dn for " + localId);
	}

	public String getDefaultDomainName() {
		String defaultDN = "dc=example,dc=com";
		String dn = peopleService
				.getConfigProperty(PeopleConstants.PEOPLE_DEFAULT_DOMAIN_NAME);
		if (CommonsJcrUtils.isEmptyString(dn))
			return defaultDN;
		else
			return dn;
	}
}
