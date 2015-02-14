package org.argeo.connect.people.core.imports;

import java.util.Arrays;
import java.util.Map;

import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.security.UserAdminService;

//import org.argeo.security.jcr.JcrSecurityModel;

/** Base utility to load users demo data **/
public class UsersCsvFileParser extends AbstractPeopleCsvFileParser {
	// private final static Log log =
	// LogFactory.getLog(UsersCsvFileParser.class);

	private final Session adminSession;
	private final UserAdminService userAdminService;
	private final UserManagementService userManagementService;

	// private final JcrSecurityModel jcrSecurityModel;

	public UsersCsvFileParser(Session adminSession,
			PeopleService peopleService, UserAdminService userAdminService) {
		super(adminSession, peopleService);
		this.adminSession = adminSession;
		this.userAdminService = userAdminService;
		userManagementService = peopleService.getUserManagementService();
		// this.jcrSecurityModel = jcrSecurityModel;

	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		final String userName = line.get("people:username");
		final String firstName = line.get("people:firstName");
		final String lastName = line.get("people:lastName");
		final String email = line.get("people:email");
		final String desc = line.get("people:description");
		final String role = line.get("people:role");

		userManagementService.createUser(adminSession, userAdminService,
				userName, "demo".toCharArray(), firstName, lastName, email, desc,
				Arrays.asList(role.split(", ")));

		// try {
		// // Effective creation of the new user
		// // Node userProfile = jcrSecurityModel.sync(adminSession, userName,
		// // null);
		// //
		// // adminSession.getWorkspace().getVersionManager()
		// // .checkout(userProfile.getPath());
		// NewUserDetails userDetails = new NewUserDetails(userName,
		// "demo".toCharArray(), role.split(", ")) {
		//
		// @Override
		// public void mapToProfileNode(Node userProfile)
		// throws RepositoryException {
		// userProfile.setProperty(ArgeoNames.ARGEO_PRIMARY_EMAIL,
		// email);
		// userProfile.setProperty(ArgeoNames.ARGEO_FIRST_NAME,
		// firstName);
		// userProfile.setProperty(ArgeoNames.ARGEO_LAST_NAME,
		// lastName);
		// userProfile.setProperty(Property.JCR_TITLE, firstName + " "
		// + lastName);
		//
		// if (CommonsJcrUtils.checkNotEmptyString(desc))
		// userProfile.setProperty(Property.JCR_DESCRIPTION, desc);
		//
		// List<GrantedAuthority> authoritiesList = new
		// ArrayList<GrantedAuthority>();
		// JcrUserDetails jcrUserDetails = new JcrUserDetails(
		// userProfile, "demo", authoritiesList);
		// jcrUserDetails = jcrUserDetails.cloneWithNewRoles(Arrays
		// .asList(role.split(", ")));
		//
		// userProfile.setProperty(ArgeoNames.ARGEO_PRIMARY_EMAIL,
		// email);
		// userProfile.setProperty(ArgeoNames.ARGEO_FIRST_NAME,
		// firstName);
		// userProfile.setProperty(ArgeoNames.ARGEO_LAST_NAME,
		// lastName);
		// userProfile.setProperty(Property.JCR_TITLE, firstName + " "
		// + lastName);
		//
		// if (CommonsJcrUtils.checkNotEmptyString(desc))
		// userProfile.setProperty(Property.JCR_DESCRIPTION, desc);
		// }
		//
		// };
		//
		// // List<GrantedAuthority> authoritiesList = new
		// // ArrayList<GrantedAuthority>();
		// // JcrUserDetails jcrUserDetails = new JcrUserDetails(userProfile,
		// // "demo", authoritiesList);
		// // jcrUserDetails = jcrUserDetails.cloneWithNewRoles(Arrays
		// // .asList(role.split(", ")));
		//
		// // adminSession.save();
		// // adminSession.getWorkspace().getVersionManager()
		// // .checkin(userProfile.getPath());
		//
		// userAdminService.createUser(userDetails);
		// } catch (Exception e) {
		// JcrUtils.discardQuietly(adminSession);
		// Node userHome = UserJcrUtils.getUserHome(adminSession, userName);
		// if (userHome != null) {
		// try {
		// userHome.remove();
		// adminSession.save();
		// } catch (RepositoryException e1) {
		// JcrUtils.discardQuietly(adminSession);
		// log.warn("Error when trying to clean up failed new user "
		// + userName, e1);
		// }
		// }
		// }
	}
}