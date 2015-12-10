package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.argeo.cms.auth.AuthConstants;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.Group;

/** Drop down that displays the list of existing groups */
public class ExistingGroupsDropDown extends PeopleAbstractDropDown {

	private final UserAdminService userService;
	private final boolean includeUsers;

	// We use a map: the displayed value is not the key we want to retrieve
	// use a linked map to keep ordered returned by the query
	private Map<String, Group> groupMap = new LinkedHashMap<String, Group>();

	public ExistingGroupsDropDown(Text text, PeopleService peopleService,
			boolean showSystemRoles) {
		super(text);
		this.userService = peopleService.getUserAdminService();
		this.includeUsers = showSystemRoles;
		init();
	}

	@Override
	public String getText() {
		String groupId = null;
		String dname = super.getText();
		if (EclipseUiUtils.notEmpty(dname))
			groupId = groupMap.get(dname).getName();
		return groupId;
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<Group> groups = userService.listGroups(filter);
		groupMap.clear();
		List<String> res = new ArrayList<String>();

		loop: for (Group group : groups) {
			String dn = group.getName();
			String groupDName = userService.getUserDisplayName(dn);

			if (EclipseUiUtils.notEmpty(filter))
				if (!dn.toLowerCase().contains(filter.toLowerCase()))
					continue loop;
			if (includeUsers || !dn.endsWith(AuthConstants.ROLES_BASEDN)) {
				groupMap.put(groupDName, group);
				res.add(groupDName);
			}
		}
		return res;
	}
}