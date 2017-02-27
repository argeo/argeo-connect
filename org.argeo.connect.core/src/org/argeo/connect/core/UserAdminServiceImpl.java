package org.argeo.connect.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.UserAdminService;
import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;
import org.argeo.osgi.useradmin.UserAdminConf;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Canonical implementation of the people {@link UserAdminService}. Wraps
 * interaction with users and groups.
 * 
 * In a *READ-ONLY* mode. We want to be able to:
 * <ul>
 * <li>Retrieve my user and corresponding information (main info,
 * groups...)</li>
 * <li>List all local groups (not the system roles)</li>
 * <li>If sufficient rights: retrieve a given user and its information</li>
 * </ul>
 */
public class UserAdminServiceImpl implements UserAdminService {

	private UserAdmin userAdmin;
	private ServiceReference<UserAdmin> userAdminServiceReference;
	private UserTransaction userTransaction;

	@Override
	public String getMyMail() {
		return getUserMail(CurrentUser.getUsername());
	}

	@Override
	public Role[] getRoles(String filter) throws InvalidSyntaxException {
		return userAdmin.getRoles(filter);
	}

	// ALL USER: WARNING access to this will be later reduced

	/** Retrieve a user given his dn */
	public User getUser(String dn) {
		return (User) getUserAdmin().getRole(dn);
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
	@Override
	public String[] getUserRoles(String dn) {
		Authorization currAuth = getUserAdmin().getAuthorization(getUser(dn));
		return currAuth.getRoles();
	}

	@Override
	public boolean isUserInRole(String userDn, String roleDn) {
		String[] roles = getUserRoles(userDn);
		for (String role : roles) {
			if (role.equalsIgnoreCase(roleDn))
				return true;
		}
		return false;
	}

	private final String[] knownProps = { LdapAttrs.cn.name(), LdapAttrs.sn.name(), LdapAttrs.givenName.name(),
			LdapAttrs.uid.name() };

	public List<User> listGroups(String filter, boolean includeUsers, boolean includeSystemRoles) {

		Role[] roles = null;
		try {
			roles = getUserAdmin().getRoles(null);
		} catch (InvalidSyntaxException e) {
			throw new ConnectException("Unable to get roles with filter: " + filter, e);
		}

		List<User> users = new ArrayList<User>();
		boolean doFilter = filter != null && !"".equals(filter);
		loop: for (Role role : roles) {
			if ((includeUsers && role.getType() == Role.USER || role.getType() == Role.GROUP) && !users.contains(role)
					&& (includeSystemRoles || !role.getName().toLowerCase().endsWith(NodeConstants.ROLES_BASEDN))) {
				if (doFilter) {
					for (String prop : knownProps) {
						Object currProp = null;
						try {
							currProp = role.getProperties().get(prop);
						} catch (Exception e) {
							throw e;
						}
						if (currProp != null) {
							String currPropStr = ((String) currProp).toLowerCase();
							if (currPropStr.contains(filter.toLowerCase())) {
								users.add((User) role);
								continue loop;
							}
						}
					}
				} else
					users.add((User) role);
			}
		}
		return users;
	}

	@Override
	public User getUserFromLocalId(String localId) {
		User user = getUserAdmin().getUser(LdapAttrs.uid.name(), localId);
		if (user == null)
			user = getUserAdmin().getUser(LdapAttrs.cn.name(), localId);
		return user;
	}

	@Override
	public String buildDefaultDN(String localId, int type) {
		return buildDistinguishedName(localId, getDefaultDomainName(), type);
	}

	@Override
	public String getDefaultDomainName() {
		Map<String, String> dns = getKnownBaseDns(true);
		if (dns.size() == 1)
			return dns.keySet().iterator().next();
		else
			throw new ConnectException("Current context contains " + dns.size() + " base dns: "
					+ dns.keySet().toString() + ". Unable to chose a default one.");
	}

	public Map<String, String> getKnownBaseDns(boolean onlyWritable) {
		Map<String, String> dns = new HashMap<String, String>();
		for (String uri : userAdminServiceReference.getPropertyKeys()) {
			if (!uri.startsWith("/"))
				continue;
			Dictionary<String, ?> props = UserAdminConf.uriAsProperties(uri);
			String readOnly = UserAdminConf.readOnly.getValue(props);
			String baseDn = UserAdminConf.baseDn.getValue(props);

			if (onlyWritable && "true".equals(readOnly))
				continue;
			if (baseDn.equalsIgnoreCase(NodeConstants.ROLES_BASEDN))
				continue;
			dns.put(baseDn, uri);
		}
		return dns;
	}

	public String buildDistinguishedName(String localId, String baseDn, int type) {
		Map<String, String> dns = getKnownBaseDns(true);
		Dictionary<String, ?> props = UserAdminConf.uriAsProperties(dns.get(baseDn));
		String dn = null;
		if (Role.GROUP == type)
			dn = LdapAttrs.cn.name() + "=" + localId + "," + UserAdminConf.groupBase.getValue(props) + "," + baseDn;
		else if (Role.USER == type)
			dn = LdapAttrs.uid.name() + "=" + localId + "," + UserAdminConf.userBase.getValue(props) + "," + baseDn;
		else
			throw new ConnectException("Unknown role type. " + "Cannot deduce dn for " + localId);
		return dn;
	}

	public UserAdmin getUserAdmin() {
		return userAdmin;
	}

	public UserTransaction getUserTransaction() {
		return userTransaction;
	}

	/* DEPENDENCY INJECTION */
	public void setUserAdmin(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}

	public void setUserTransaction(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	public void setUserAdminServiceReference(ServiceReference<UserAdmin> userAdminServiceReference) {
		this.userAdminServiceReference = userAdminServiceReference;
	}
}
