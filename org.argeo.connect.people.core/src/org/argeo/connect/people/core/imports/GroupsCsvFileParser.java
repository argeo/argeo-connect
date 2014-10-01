package org.argeo.connect.people.core.imports;

import java.util.Arrays;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;

/**
 * Base utility to load user groups form a .CSV file in a People repository
 **/
public class GroupsCsvFileParser extends AbstractPeopleCsvFileParser {

	private final Session adminSession;

	public GroupsCsvFileParser(Session adminSession, PeopleService peopleService) {
		super(adminSession, peopleService);
		this.adminSession = adminSession;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {

		String groupName = line.get("people:groupName");
		String title = line.get("jcr:title");
		String desc = line.get("jcr:description");
		String members = line.get("people:members");

		// Effective creation of the new user
		UserManagementService userManagementService = getPeopleService()
				.getUserManagementService();
		Node currGroup = userManagementService.createGroup(adminSession,
				groupName, title, desc);

		userManagementService.addUsersToGroup(adminSession, currGroup,
				Arrays.asList(members.split(", ")));
	}
}