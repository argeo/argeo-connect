package org.argeo.connect.people.core.imports;

@Deprecated
public class UsersCsvFileParser {
}
// /** Base utility to load users demo data **/
// public class UsersCsvFileParser extends AbstractPeopleCsvFileParser {
// // private final static Log log =
// // LogFactory.getLog(UsersCsvFileParser.class);
//
// private final Session adminSession;
// private final UserAdminService userAdminService;
// private final UserAdminService userManagementService;
//
// public UsersCsvFileParser(Session adminSession,
// PeopleService peopleService, UserAdminService userAdminService) {
// super(adminSession, peopleService);
// this.adminSession = adminSession;
// this.userAdminService = userAdminService;
// userManagementService = peopleService.getUserManagementService();
// }
//
// @Override
// protected void processLine(Integer lineNumber, Map<String, String> line) {
// String userName = line.get("people:username");
// String firstName = line.get("people:firstName");
// String lastName = line.get("people:lastName");
// String email = line.get("people:email");
// String desc = line.get("people:description");
// String role = line.get("people:role");
//
// throw new ArgeoException("Legacy class. do not use anymore");
//
// // userManagementService.createUser(adminSession, userAdminService,
// // userName, "demo".toCharArray(), firstName, lastName, email,
// // desc, Arrays.asList(role.split(", ")));
// }
// }