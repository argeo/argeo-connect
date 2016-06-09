package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import javax.naming.ldap.LdapName;

import org.argeo.ArgeoException;
import org.argeo.cms.auth.AuthConstants;
import org.argeo.cms.util.useradmin.UserAdminUtils;
import org.argeo.cms.util.useradmin.UserAdminWrapper;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.util.UsersUtils;
import org.argeo.osgi.useradmin.LdifName;
import org.argeo.osgi.useradmin.UserAdminConf;
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
		return (User) UserAdminUtils.getRole(getUserAdmin(), ln);
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
			LdifName.sn.name(), LdifName.givenName.name(), LdifName.uid.name() };

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

	public List<User> listGroups(String filter, boolean includeUsers,
			boolean includeSystemRoles) {
		Role[] roles;
		try {
			StringBuilder tmpBuilder = new StringBuilder();

			// Build filter String
			String filterStr = null;
			if (UsersUtils.notNull(filter)) {
				for (String prop : knownProps) {
					tmpBuilder.append("(");
					tmpBuilder.append(prop);
					tmpBuilder.append("=*");
					tmpBuilder.append(filter);
					tmpBuilder.append("*)");
				}
				filterStr = "(|" + tmpBuilder.toString() + ")";
			}

			String typeStr = null;
			if (!includeUsers)
				typeStr = "(" + LdifName.objectClass.name() + "="
						+ LdifName.groupOfNames.name() + ")";

			// Construct statement
			String statement = null;
			if (typeStr != null && filterStr != null) {
				statement = "(&" + typeStr + filterStr + ")";
			} else if (typeStr != null)
				statement = typeStr;
			else if (filterStr != null)
				statement = filterStr;

			roles = getUserAdmin().getRoles(statement);
		} catch (InvalidSyntaxException e) {
			throw new ArgeoException("Unable to get roles with filter: "
					+ filter, e);
		}
		List<User> users = new ArrayList<User>();
		for (Role role : roles)
			if ((role.getType() == Role.USER || role.getType() == Role.GROUP)
					&& !users.contains(role)
					&& (includeSystemRoles || !role.getName().toLowerCase()
							.endsWith(AuthConstants.ROLES_BASEDN)))
				users.add((User) role);
		return users;
	}

	@Override
	public User getUserFromLocalId(String localId) {
		User user = getUserAdmin().getUser(LdifName.uid.name(), localId);
		if (user == null)
			user = getUserAdmin().getUser(LdifName.cn.name(), localId);
		return user;
	}

	public String buildDefaultDN(String localId, int type) {
		return buildDistinguishedName(localId, getDefaultDomainName(), type);
	}

	public String buildDistinguishedName(String localId, String baseDn, int type) {
		Map<String, String> dns = getKnownBaseDns(true);
		Dictionary<String, ?> props = UserAdminConf.uriAsProperties(dns
				.get(baseDn));
		String dn = null;
		if (Role.GROUP == type)
			dn = LdifName.cn.name() + "=" + localId + ","
					+ UserAdminConf.groupBase.getValue(props) + "," + baseDn;
		else if (Role.USER == type)
			dn = LdifName.uid.name() + "=" + localId + ","
					+ UserAdminConf.userBase.getValue(props) + "," + baseDn;
		else
			throw new ArgeoException("Unknown role type. "
					+ "Cannot deduce dn for " + localId);
		return dn;
	}

	public String getDefaultDomainName() {
		Map<String, String> dns = getKnownBaseDns(true);
		if (dns.size() == 1)
			return dns.keySet().iterator().next();
		else
			throw new ArgeoException("Current context contains " + dns.size()
					+ " base dns: " + dns.keySet().toString()
					+ ". Unable to chose a default one.");
	}

}