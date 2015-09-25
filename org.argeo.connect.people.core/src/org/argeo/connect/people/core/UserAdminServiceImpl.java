package org.argeo.connect.people.core;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.UsersUtils;
import org.argeo.osgi.useradmin.LdifName;
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
		if (rolename.startsWith("cn="))
			dn = rolename;
		else
			dn = "cn=" + rolename + ",ou=roles,ou=node";

		Role role = userAdmin.getRole(dn);
		String roledn = role.getName();

		for (String currRole : getMyRoles())
			if (roledn.equals(currRole))
				return true;
		return false;
	}

	// ALL USER: WARNING access to this will be later reduced

	/** Returns the current user */
	public User getUser(String dn) {
		return (User) userAdmin.getRole(dn);
	}

	/** Can be a group or a user */
	public String getUserDisplayName(String dn) {

		// FIXME: during initialisation phase, the system logs "admin" as user
		// name rather than the corresponding dn
		if ("admin".equals(dn))
			return "System Adminitrator";

		User user = getUser(dn);
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

	// //
	// // USERS MANAGEMENT
	// //
	// public Node createUser(Session adminSession,
	// UserAdminService userAdminService, final String userName,
	// final char[] password, final String firstName,
	// final String lastName, final String email, final String desc,
	// final List<String> roles) {
	// throw new ArgeoException("Legacy. Do not use");
	// // try {
	// // NewUserDetails userDetails = new NewUserDetails(userName, password,
	// // roles.toArray(new String[0])) {
	// // private static final long serialVersionUID = 1L;
	// //
	// // @Override
	// // public void mapToProfileNode(Node userProfile)
	// // throws RepositoryException {
	// // userProfile.setProperty(ArgeoNames.ARGEO_PRIMARY_EMAIL,
	// // email);
	// // userProfile.setProperty(ArgeoNames.ARGEO_FIRST_NAME,
	// // firstName);
	// // userProfile.setProperty(ArgeoNames.ARGEO_LAST_NAME,
	// // lastName);
	// // userProfile.setProperty(Property.JCR_TITLE, firstName + " "
	// // + lastName);
	// // if (CommonsJcrUtils.checkNotEmptyString(desc))
	// // userProfile.setProperty(Property.JCR_DESCRIPTION, desc);
	// // }
	// //
	// // };
	// // userAdminService.createUser(userDetails);
	// // return UserJcrUtils.getUserHome(adminSession, userName);
	// // } catch (Exception e) {
	// // JcrUtils.discardQuietly(adminSession);
	// // Node userHome = UserJcrUtils.getUserHome(adminSession, userName);
	// // if (userHome != null) {
	// // try {
	// // userHome.remove();
	// // adminSession.save();
	// // } catch (RepositoryException e1) {
	// // JcrUtils.discardQuietly(adminSession);
	// // log.warn("Error when trying to clean up failed new user "
	// // + userName, e1);
	// // }
	// // }
	// // }
	// // return null;
	// }
	//
	// //
	// // GROUP MANAGEMENT
	// //
	// @Override
	// public List<Node> getDefinedGroups(Session session, String filter,
	// boolean includeSingleUserGroups) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // try {
	// // QueryManager queryManager = session.getWorkspace()
	// // .getQueryManager();
	// //
	// // // XPath
	// // StringBuilder builder = new StringBuilder();
	// // builder.append("//element(*, ")
	// // .append(PeopleTypes.PEOPLE_USER_GROUP).append(")");
	// //
	// // String cond = "";
	// // if (!includeSingleUserGroups) {
	// // cond = "(not(@" + PeopleNames.PEOPLE_IS_SINGLE_USER_GROUP
	// // + "='true'))";
	// // }
	// // cond = XPathUtils.localAnd(
	// // XPathUtils.getFreeTextConstraint(filter), cond);
	// // if (cond.length() > 1)
	// // builder.append("[").append(cond).append("]");
	// // builder.append(" order by @");
	// // builder.append(PeopleNames.JCR_TITLE);
	// // builder.append(" ascending ");
	// // Query query = queryManager.createQuery(builder.toString(),
	// // PeopleConstants.QUERY_XPATH);
	// //
	// // // QOM
	// // // QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// // // Selector source = factory.selector(PeopleTypes.PEOPLE_USER_GROUP,
	// // // PeopleTypes.PEOPLE_USER_GROUP);
	// // // Constraint defaultC = null;
	// // // if (CommonsJcrUtils.checkNotEmptyString(filter)) {
	// // // String[] strs = filter.trim().split(" ");
	// // // for (String token : strs) {
	// // // StaticOperand so = factory.literal(session
	// // // .getValueFactory().createValue("*" + token + "*"));
	// // // Constraint currC = factory.fullTextSearch(
	// // // source.getSelectorName(), null, so);
	// // // if (defaultC == null)
	// // // defaultC = currC;
	// // // else
	// // // defaultC = factory.and(defaultC, currC);
	// // // }
	// // // }
	// // //
	// // // if (!includeSingleUserGroups) {
	// // // Constraint constraint = factory.propertyExistence(
	// // // source.getSelectorName(),
	// // // PeopleNames.PEOPLE_IS_SINGLE_USER_GROUP);
	// // // defaultC = CommonsJcrUtils.localAnd(factory, defaultC,
	// // // factory.not(constraint));
	// // // }
	// // //
	// // // Ordering order = factory.ascending(factory.propertyValue(
	// // // source.getSelectorName(), Property.JCR_TITLE));
	// // // Ordering[] orderings = { order };
	// // //
	// // // QueryObjectModel query = factory.createQuery(source, defaultC,
	// // // orderings, null);
	// //
	// // QueryResult result = query.execute();
	// // return JcrUtils.nodeIteratorToList(result.getNodes());
	// // } catch (RepositoryException e) {
	// // throw new PeopleException(
	// // "Unable to retrieve the list of defined user groups", e);
	// // }
	// }
	//
	// @Override
	// public Node createGroup(Session session, String groupId, String title,
	// String description) {
	// throw new ArgeoException("Legacy. Do not use");
	// // return createGroup(session, groupId, title, description, false);
	// }
	//
	// @Override
	// public Node getGroupById(Session session, String groupId) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // try {
	// // String fullPath = getPathFromGroupId(groupId);
	// // if (session.nodeExists(fullPath))
	// // return session.getNode(fullPath);
	// // else
	// // return null;
	// // } catch (RepositoryException re) {
	// // throw new PeopleException("Unable to retrieve group of ID "
	// // + groupId, re);
	// // }
	// }
	//
	// private String getPathFromGroupId(String groupId) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // String relPath = JcrUtils.firstCharsToPath(groupId, 2);
	// // String fullPath = peopleService
	// // .getBasePath(PeopleTypes.PEOPLE_USER_GROUP)
	// // + "/"
	// // + relPath
	// // + "/" + groupId;
	// // return fullPath;
	// }
	//
	// private Node createGroup(Session session, String groupId, String title,
	// String description, boolean isUserGroup) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // try {
	// // String fullPath = getPathFromGroupId(groupId);
	// // if (session.nodeExists(fullPath))
	// // return session.getNode(fullPath);
	// // else {
	// // Node newGroup = JcrUtils.mkdirs(session, fullPath,
	// // PeopleTypes.PEOPLE_USER_GROUP);
	// //
	// // newGroup.setProperty(PeopleNames.PEOPLE_GROUP_ID, groupId);
	// // if (CommonsJcrUtils.checkNotEmptyString(title))
	// // newGroup.setProperty(Property.JCR_TITLE, title);
	// // if (CommonsJcrUtils.checkNotEmptyString(description))
	// // newGroup.setProperty(Property.JCR_DESCRIPTION, description);
	// //
	// // if (isUserGroup) {
	// // newGroup.setProperty(
	// // PeopleNames.PEOPLE_IS_SINGLE_USER_GROUP, true);
	// // }
	// // // TODO save strategy
	// // CommonsJcrUtils.checkPoint(newGroup);
	// // // CommonsJcrUtils.saveAndCheckin(newGroup);
	// // return newGroup;
	// // }
	// // } catch (RepositoryException re) {
	// // throw new PeopleException("unable to create group " + groupId, re);
	// // }
	// }
	//
	// //
	// // USER MANAGEMENT
	// //
	// /**
	// * If a "argeo:profile" node exists but no "people:profile", the later is
	// * created, saved and checked-in in a distinct session
	// *
	// * @param session
	// * @param username
	// * @return a people:profile node or null if the argeo:profile is not found
	// */
	// @Override
	// public Node getPeopleProfile(Session session, String username) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // Node userProfile = UserJcrUtils.getUserProfile(session, username);
	// // if (userProfile == null)
	// // return null;
	// // try {
	// // Node parent = userProfile.getParent();
	// // Node peopleProfile;
	// // if (!parent.hasNode(PeopleTypes.PEOPLE_PROFILE)) {
	// // Session tmpSession = null;
	// // try {
	// // String tmpParentPath = parent.getPath();
	// // tmpSession = session.getRepository().login(
	// // session.getWorkspace().getName());
	// // Node tmpParent = tmpSession.getNode(tmpParentPath);
	// // Node tmpProfile = tmpParent.addNode(
	// // PeopleTypes.PEOPLE_PROFILE,
	// // PeopleTypes.PEOPLE_PROFILE);
	// // tmpSession.save();
	// //
	// // String newPath = tmpProfile.getPath();
	// // tmpSession.getWorkspace().getVersionManager()
	// // .checkin(newPath);
	// // peopleProfile = session.getNode(newPath);
	// // } catch (RepositoryException re) {
	// // throw new PeopleException(
	// // "Unable to create people profile node for username "
	// // + username, re);
	// // } finally {
	// // JcrUtils.logoutQuietly(tmpSession);
	// // }
	// // } else {
	// // peopleProfile = parent.getNode(PeopleTypes.PEOPLE_PROFILE);
	// // }
	// // return peopleProfile;
	// // } catch (RepositoryException re) {
	// // throw new PeopleException(
	// // "Unable to get people profile node for username "
	// // + username, re);
	// // }
	//
	// }
	//
	// public Node getMyPeopleProfile(Session session) {
	// throw new ArgeoException("Legacy. Do not use");
	// // return getPeopleProfile(session, session.getUserID());
	// }
	//
	// @Override
	// public String addUsersToGroup(Session session, Node userGroup,
	// List<String> usernames) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // Session tmpSession = null;
	// // try {
	// // tmpSession = session.getRepository().login(
	// // session.getWorkspace().getName());
	// // StringBuilder builder = new StringBuilder();
	// // for (String currUserName : usernames) {
	// // Node peopleProfile = getPeopleProfile(tmpSession, currUserName);
	// // CommonsJcrUtils.checkCOStatusBeforeUpdate(peopleProfile);
	// // String msg = CommonsJcrUtils.addRefToMultiValuedProp(
	// // peopleProfile, PeopleNames.PEOPLE_USER_GROUPS,
	// // userGroup);
	// // if (msg != null) {
	// // builder.append(msg).append("\n");
	// // JcrUtils.discardUnderlyingSessionQuietly(peopleProfile);
	// // } else
	// // CommonsJcrUtils.checkPoint(peopleProfile);
	// // }
	// //
	// // if (builder.length() > 0)
	// // return builder.toString();
	// // } catch (RepositoryException re) {
	// // throw new PeopleException("unable to add users to group "
	// // + userGroup, re);
	// // } finally {
	// // JcrUtils.logoutQuietly(tmpSession);
	// // }
	// // return null;
	// }
	//
	// // TODO manage adding without overwriting
	// @Override
	// public String addGroupsToUser(Session session, String username,
	// List<Node> groups) {
	// throw new ArgeoException("Legacy. Do not use");
	// //
	// // try {
	// // Node peopleProfile = getPeopleProfile(session, username);
	// // CommonsJcrUtils.checkCOStatusBeforeUpdate(peopleProfile);
	// //
	// // // it overrides all values at each time.
	// // CommonsJcrUtils.setMultipleReferences(peopleProfile,
	// // PeopleNames.PEOPLE_USER_GROUPS, groups);
	// // CommonsJcrUtils.checkPoint(peopleProfile);
	// //
	// // } catch (RepositoryException re) {
	// // throw new PeopleException("unable to add groups to user "
	// // + username, re);
	// // }
	// // return null;
	// }
	//
	// @Override
	// public List<Node> getMyGroups(Session session) {
	// throw new ArgeoException("Legacy. Do not use");
	// // return getUserGroups(session, session.getUserID());
	// }
	//
	// @Override
	// public boolean amIInGroup(Session session, String groupId) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // List<Node> myGroups = getMyGroups(session);
	// // for (Node group : myGroups) {
	// // String currGroupId = CommonsJcrUtils.get(group,
	// // PeopleNames.PEOPLE_GROUP_ID);
	// // if (groupId.equals(currGroupId))
	// // return true;
	// // }
	// // return false;
	// }
	//
	// @Override
	// public List<Node> getUserGroups(Session session, String username) {
	// throw new ArgeoException("Legacy. Do not use");
	// // List<Node> groups = new ArrayList<Node>();
	// // try {
	// // Node peopleProfile = getPeopleProfile(session, username);
	// //
	// // if (peopleProfile == null)
	// // return groups;
	// //
	// // if (!peopleProfile.hasProperty(PeopleNames.PEOPLE_USER_GROUPS))
	// // return groups;
	// //
	// // Value[] values = peopleProfile.getProperty(
	// // PeopleNames.PEOPLE_USER_GROUPS).getValues();
	// //
	// // for (Value val : values) {
	// // Node currNode = session.getNodeByIdentifier(val.getString());
	// // groups.add(currNode);
	// // }
	// // } catch (RepositoryException re) {
	// // throw new PeopleException("unable to get groups for user "
	// // + username, re);
	// // }
	// // return groups;
	// }
	//
	// @Override
	// public Node createDefaultGroupForUser(Session session, String username) {
	// throw new ArgeoException("Legacy. Do not use");
	//
	// // Node userGp = createGroup(session, username, username,
	// // "Default group for user " + username, true);
	// // List<Node> groups = new ArrayList<Node>();
	// // groups.add(userGp);
	// // addGroupsToUser(session, username, groups);
	// // return userGp;
	// }

}