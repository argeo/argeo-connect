package org.argeo.connect.people.util;

public class UsersUtils {}



//import java.security.AccessController;
//
//import javax.naming.InvalidNameException;
//import javax.naming.ldap.LdapName;
//import javax.security.auth.Subject;
//import javax.security.auth.x500.X500Principal;
//
//import org.argeo.connect.people.PeopleException;
//import org.argeo.naming.LdapAttrs;
//import org.osgi.service.useradmin.Role;
//import org.osgi.service.useradmin.User;
//import org.osgi.service.useradmin.UserAdmin;
//
///** Centralise temporary helpers to query the user referential */
//public class UsersUtils {
//
//	/** returns the local name of the current connected user */
//	final static String getMyUserName(UserAdmin userAdmin) {
//		LdapName dn = getLdapName();
//		return getUsername(getUser(userAdmin, dn));
//	}
//
//	final static String getMyDisplayName(UserAdmin userAdmin) {
//		LdapName dn = getLdapName();
//		return getUsername(getUser(userAdmin, dn));
//	}
//
//	final static boolean isCurrentUser(User user) {
//		String userName = getProperty(user, LdapAttrs.uid.name());
//		try {
//			LdapName selfUserName = getLdapName();
//			LdapName userLdapName = new LdapName(userName);
//			if (userLdapName.equals(selfUserName))
//				return true;
//			else
//				return false;
//		} catch (InvalidNameException e) {
//			throw new PeopleException("User " + user + " has an unvalid dn: "
//					+ userName, e);
//		}
//	}
//
//	public final static LdapName getLdapName() {
//		Subject subject = Subject.getSubject(AccessController.getContext());
//		String name = subject.getPrincipals(X500Principal.class).iterator()
//				.next().toString();
//		LdapName dn;
//		try {
//			dn = new LdapName(name);
//		} catch (InvalidNameException e) {
//			throw new PeopleException("Invalid user dn " + name, e);
//		}
//		return dn;
//	}
//
//	public final static User getUser(UserAdmin userAdmin, LdapName dn) {
//		User user = userAdmin.getUser(LdapAttrs.uid.name(), dn.toString());
//		return user;
//	}
//
//	public final static String getUsername(User user) {
//		String cn = getProperty(user, LdapAttrs.cn.name());
//		if (isEmpty(cn))
//			cn = getProperty(user, LdapAttrs.uid.name());
//		return cn;
//	}
//
//	public final static String getProperty(Role role, String key) {
//		Object obj = role.getProperties().get(key);
//		if (obj != null)
//			return (String) obj;
//		else
//			return "";
//	}
//
//	/*
//	 * INTERNAL METHODS: Below methods are meant to stay here and are not part
//	 * of a potential generic backend to manage the useradmin
//	 */
//	public final static boolean notNull(String string) {
//		if (string == null)
//			return false;
//		else
//			return !"".equals(string.trim());
//	}
//
//	public final static boolean isEmpty(String string) {
//		if (string == null)
//			return true;
//		else
//			return "".equals(string.trim());
//	}
//}