package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.UserAdminService;
import org.argeo.eclipse.ui.EclipseUiUtils;
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

	// // CURRENT USER
	// /** Returns the current user */
	// public User getMyUser() {
	// return UserAdminUtils.getCurrentUser(getUserAdmin());
	// }
	//
	// /** Returns the DN of the current user */
	// public String getMyUsername() {
	// return CurrentUser.getUsername();
	// }
	//
	@Override
	public String getMyMail() {
		return getUserMail(CurrentUser.getUsername());
	}
	//
	// /** Lists all roles of the current user */
	// public String[] getMyRoles() {
	// return getUserRoles(getMyUsername());
	// }
	//
	// /** Returns the local uid of the current connected user in this context
	// */
	// public String getMyLocalName() {
	// return getMyUser().getName();
	// }

	// @Override
	// public String getCurrentUserHomePath() {
	// return getHomeBasePath() + "/" +
	// UserAdminUtils.getCurrentUserHomeRelPath();
	// }
	//
	// @Override
	// public String getUserHomePath(String dn) {
	// return getHomeBasePath() + "/" + UserAdminUtils.getHomeRelPath(dn);
	// }

	protected String getHomeBasePath() {
		return "/home";
	}

	@Override
	public Role[] getRoles(String filter) throws InvalidSyntaxException {
		return userAdmin.getRoles(filter);
	}

	// /** Returns the display name of the current logged in user */
	// public String getMyDisplayName() {
	// return getUserDisplayName(getMyUsername());
	// }

	// /** Returns true if the current user is in the specified role */
	// @Override
	// public boolean amIInRole(String rolename) {
	// // FIXME clean this
	// String dn;
	// if (rolename.startsWith(LdapAttrs.cn.name() + "=") ||
	// rolename.startsWith(LdapAttrs.uid.name() + "="))
	// dn = rolename;
	// else
	// dn = LdapAttrs.cn.name() + "=" + rolename + "," +
	// NodeConstants.ROLES_BASEDN;
	//
	// Role role = getUserAdmin().getRole(dn);
	// if (role == null)
	// return false;
	//
	// String roledn = role.getName();
	//
	// for (String currRole : getMyRoles()) {
	// if (roledn.equals(currRole))
	// return true;
	// }
	// return false;
	// }

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
			throw new PeopleException("Unable to get roles with filter: " + filter, e);
		}

		List<User> users = new ArrayList<User>();
		boolean doFilter = EclipseUiUtils.notEmpty(filter);
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

	// @Override
	// public List<Group> listGroups(String filter) {
	// // TODO Auto-generated method stub
	// return null;
	// }

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
			throw new PeopleException("Current context contains " + dns.size() + " base dns: " + dns.keySet().toString()
					+ ". Unable to chose a default one.");
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
			throw new PeopleException("Unknown role type. " + "Cannot deduce dn for " + localId);
		return dn;
	}

	// public String buildDefaultDN(String localId, int type) {
	// return buildDistinguishedName(localId, getDefaultDomainName(), type);
	// }

	// public String buildDistinguishedName(String localId, String baseDn, int
	// type) {
	// Map<String, String> dns = getKnownBaseDns(true);
	// Dictionary<String, ?> props = UserAdminConf.uriAsProperties(dns
	// .get(baseDn));
	// String dn = null;
	// if (Role.GROUP == type)
	// dn = LdapAttrs.cn.name() + "=" + localId + ","
	// + UserAdminConf.groupBase.getValue(props) + "," + baseDn;
	// else if (Role.USER == type)
	// dn = LdapAttrs.uid.name() + "=" + localId + ","
	// + UserAdminConf.userBase.getValue(props) + "," + baseDn;
	// else
	// throw new PeopleException("Unknown role type. "
	// + "Cannot deduce dn for " + localId);
	// return dn;
	// }
	//
	// public String getDefaultDomainName() {
	// Map<String, String> dns = getKnownBaseDns(true);
	// if (dns.size() == 1)
	// return dns.keySet().iterator().next();
	// else
	// throw new PeopleException("Current context contains " + dns.size()
	// + " base dns: " + dns.keySet().toString()
	// + ". Unable to chose a default one.");
	// }

	// public Map<String, String> getKnownBaseDns(boolean onlyWritable) {
	// Map<String, String> dns = new HashMap<String, String>();
	// for (String uri : userAdminServiceReference.getPropertyKeys()) {
	// if (!uri.startsWith("/"))
	// continue;
	// Dictionary<String, ?> props = UserAdminConf.uriAsProperties(uri);
	// String readOnly = UserAdminConf.readOnly.getValue(props);
	// String baseDn = UserAdminConf.baseDn.getValue(props);
	//
	// if (onlyWritable && "true".equals(readOnly))
	// continue;
	// if (baseDn.equalsIgnoreCase(NodeConstants.ROLES_BASEDN))
	// continue;
	// dns.put(baseDn, uri);
	// }
	// return dns;
	// }

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
