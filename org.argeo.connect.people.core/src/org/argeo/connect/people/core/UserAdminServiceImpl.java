package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;

import javax.naming.ldap.LdapName;

import org.argeo.ArgeoException;
import org.argeo.cms.auth.AuthConstants;
import org.argeo.cms.util.useradmin.UserAdminUtils;
import org.argeo.cms.util.useradmin.UserAdminWrapper;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.util.UsersUtils;
import org.argeo.osgi.useradmin.LdifName;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Canonical implementation of the people {@link UserAdminService}. Wraps
 * interaction with users and groups.
 * 
 * In a *READ-ONLY* mode. We want to be able to:
 * <ul>
 * <li>Retrieve my user and corresponding information (main info, groups...)</li>
 * <li>List all local groups (not the system roles)</li>
 * <li>If sufficient rights: retrieve a given user and its information</li>
 * </ul>
 */
public class UserAdminServiceImpl extends UserAdminWrapper implements
		UserAdminService {

	// CURRENT USER
	/** Returns the current user */
	public User getMyUser() {
		return UserAdminUtils.getCurrentUser(getUserAdmin());
	}

	/** Returns the DN of the current user */
	public String getMyUsername() {
		return UserAdminUtils.getCurrentUsername();
	}

	/** Lists all roles of the current user */
	public String[] getMyRoles() {
		return getUserRoles(getMyUsername());
	}

	/** Returns the local uid of the current connected user in this context */
	public String getMyLocalName() {
		return getMyUser().getName();
	}

	@Override
	public String getMyMail() {
		return UserAdminUtils.getCurrentUserMail(getUserAdmin());
	}

	/** Returns the display name of the current logged in user */
	public String getMyDisplayName() {
		return getUserDisplayName(getMyUsername());
	}

	/** Returns true if the current user is in the specified role */
	@Override
	public boolean amIInRole(String rolename) {
		// FIXME clean this
		String dn;
		if (rolename.startsWith(LdifName.cn.name() + "="))
			dn = rolename;
		else
			dn = LdifName.cn.name() + "=" + rolename + ","
					+ AuthConstants.ROLES_BASEDN;

		Role role = getUserAdmin().getRole(dn);
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
		LdapName ln = UserAdminUtils.getLdapName(dn);
		return UserAdminUtils.getUser(getUserAdmin(), ln);
	}

	/** Can be a group or a user */
	public String getUserDisplayName(String dn) {
		// FIXME: during initialisation phase, the system logs "admin" as user
		// name rather than the corresponding dn
		if ("admin".equals(dn))
			return "System Administrator";
		else
			return UserAdminUtils.getUserDisplayName(getUserAdmin(), dn);
	}

	@Override
	public String getUserMail(String dn) {
		return UserAdminUtils.getUserMail(getUserAdmin(), dn);
	}

	/** Lists all roles of the given user */
	public String[] getUserRoles(String dn) {
		Authorization currAuth = getUserAdmin().getAuthorization(getUser(dn));
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
				builder.append("(&(").append(LdifName.objectClass.name())
						.append("=").append(LdifName.groupOfNames.name())
						.append(")(|");
				builder.append(tmpBuilder.toString());
				builder.append("))");
			} else
				builder.append("(").append(LdifName.objectClass.name())
						.append("=").append(LdifName.groupOfNames.name())
						.append(")");
			roles = getUserAdmin().getRoles(builder.toString());
		} catch (InvalidSyntaxException e) {
			throw new ArgeoException("Unable to get roles with filter: "
					+ filter, e);
		}
		List<Group> users = new ArrayList<Group>();
		for (Role role : roles)
			if (role.getType() == Role.GROUP
					&& !users.contains(role)
					&& !role.getName().toLowerCase()
							.endsWith(AuthConstants.ROLES_BASEDN))
				users.add((Group) role);
		return users;
	}

	public String getDistinguishedName(String localId, int type) {
		throw new ArgeoException("Implement this");
		// TODO check if we already have the dn of an existing user. Clean this
		// try {
		// Role role = userAdmin.getRole(localId);
		// if (role != null)
		// return localId;
		// } catch (Exception e) {
		// // silent
		// }
		//
		// if (Role.GROUP == type)
		// return LdifName.cn.name() + "=" + localId + "," + GROUPS_SUFFIX
		// + "," + getDefaultDomainName();
		// else if (Role.USER == type)
		// return LdifName.uid.name() + "=" + localId + "," + USERS_SUFFIX
		// + "," + getDefaultDomainName();
		// else if (Role.ROLE == type)
		// return LdifName.cn.name() + "=" + localId + ","
		// + SYSTEM_ROLE_SUFFIX;
		// else
		// throw new ArgeoException("Unknown role type. "
		// + "Cannot deduce dn for " + localId);
	}

	public String getDefaultDomainName() {
		throw new ArgeoException("Implement this");

		// String defaultDN = "dc=example,dc=com";
		//
		// // TODO rather retrieve this from the passed URIs
		// String dn = peopleService
		// .getConfigProperty(PeopleConstants.PEOPLE_DEFAULT_DOMAIN_NAME);
		// if (isEmpty(dn))
		// return defaultDN;
		// else
		// return dn;
	}

}