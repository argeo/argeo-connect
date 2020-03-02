package org.argeo.connect.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.argeo.cms.CmsUserManager;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

/** Drop down that displays the list of existing groups */
public class GroupDropDown extends ConnectAbstractDropDown {
	private final CmsUserManager userService;
	private final String groupDn;

	private Map<String, User> userMap = new TreeMap<String, User>();

	/**
	 * @param text
	 * @param userAdminService
	 * @param groupDn
	 */
	public GroupDropDown(Text text, CmsUserManager userAdminService, String groupDn) {
		super(text);
		this.userService = userAdminService;
		this.groupDn = groupDn;
		init();
	}

	public void resetDN(String dn) {
		if (EclipseUiUtils.notEmpty(dn))
			reset(userService.getUserDisplayName(dn));
		else
			reset("");
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
		Set<User> users = userService.listUsersInGroup(groupDn, filter);
		userMap.clear();
		Set<String> res = new TreeSet<String>();

		for (User user : users) {
			String dn = user.getName();
			String userDName = userService.getUserDisplayName(dn);
			userMap.put(userDName, user);
			res.add(userDName);
		}
		// TODO make order configurable
		return new ArrayList<>(res);
	}
}
