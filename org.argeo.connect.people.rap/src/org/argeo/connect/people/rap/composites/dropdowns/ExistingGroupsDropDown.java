package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.widgets.Text;

/** Drop down that displays the list of existing groups */
public class ExistingGroupsDropDown extends PeopleAbstractDropDown {

	private final UserManagementService userService;
	private final Session session;
	private final boolean includeUsers;

	public ExistingGroupsDropDown(Text text, PeopleService peopleService,
			Session session, boolean includeUsers) {
		super(text);
		this.session = session;
		this.userService = peopleService.getUserManagementService();
		this.includeUsers = includeUsers;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<Node> groups = userService.getDefinedGroups(session, filter,
				includeUsers);
		List<String> values = new ArrayList<String>();
		for (Node group : groups) {
			values.add(CommonsJcrUtils.get(group, PeopleNames.PEOPLE_GROUP_ID));
		}
		return values;
	}
}