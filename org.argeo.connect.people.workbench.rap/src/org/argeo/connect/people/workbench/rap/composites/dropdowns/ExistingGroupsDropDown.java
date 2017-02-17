package org.argeo.connect.people.workbench.rap.composites.dropdowns;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.argeo.connect.UserAdminService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

/** Drop down that displays the list of existing groups */
public class ExistingGroupsDropDown extends PeopleAbstractDropDown {
	private final UserAdminService userService;
	private final boolean includeUsers;
	private final boolean includeSystemRoles;

	// We use a map: the displayed value is not the key we want to retrieve
	// use a linked map to keep ordered returned by the query
	private Map<String, User> userMap = new LinkedHashMap<String, User>();

	/**
	 * @param text
	 * @param peopleService
	 * @param includeUsers
	 * @param includeSystemRoles
	 */
	public ExistingGroupsDropDown(Text text, UserAdminService userAdminService, boolean includeUsers,
			boolean includeSystemRoles) {
		super(text);
		this.userService = userAdminService;
		this.includeUsers = includeUsers;
		this.includeSystemRoles = includeSystemRoles;
		init();
	}

	@Override
	public String getText() {
		String groupId = null;
		String dname = super.getText();
		if (EclipseUiUtils.notEmpty(dname))
			groupId = userMap.get(dname).getName();
		return groupId;
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<User> users = userService.listGroups(filter, includeUsers, includeSystemRoles);
		userMap.clear();
		List<String> res = new ArrayList<String>();

		for (User user : users) {
			String dn = user.getName();
			String userDName = userService.getUserDisplayName(dn);
			userMap.put(userDName, user);
			res.add(userDName);
		}
		return res;
	}
}