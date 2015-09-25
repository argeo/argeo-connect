package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.Group;

/** Drop down that displays the list of existing groups */
public class ExistingGroupsDropDown extends PeopleAbstractDropDown {

	private final UserAdminService userService;
	private final Session session;
	private final boolean includeUsers;

	public ExistingGroupsDropDown(Text text, PeopleService peopleService,
			Session session, boolean includeUsers) {
		super(text);
		this.session = session;
		this.userService = peopleService.getUserAdminService();
		this.includeUsers = includeUsers;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<Group> groups = userService.listGroups(filter);
		List<String> values = new ArrayList<String>();
		for (Group group : groups) {
			values.add(group.getName());
		}
		return values;
	}
}