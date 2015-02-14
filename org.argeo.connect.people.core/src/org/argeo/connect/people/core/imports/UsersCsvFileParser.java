package org.argeo.connect.people.core.imports;

import java.util.Arrays;
import java.util.Map;

import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.security.UserAdminService;

/** Base utility to load users demo data **/
public class UsersCsvFileParser extends AbstractPeopleCsvFileParser {
	// private final static Log log =
	// LogFactory.getLog(UsersCsvFileParser.class);

	private final Session adminSession;
	private final UserAdminService userAdminService;
	private final UserManagementService userManagementService;

	public UsersCsvFileParser(Session adminSession,
			PeopleService peopleService, UserAdminService userAdminService) {
		super(adminSession, peopleService);
		this.adminSession = adminSession;
		this.userAdminService = userAdminService;
		userManagementService = peopleService.getUserManagementService();
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
				userName, "demo".toCharArray(), firstName, lastName, email,
				desc, Arrays.asList(role.split(", ")));
	}
}