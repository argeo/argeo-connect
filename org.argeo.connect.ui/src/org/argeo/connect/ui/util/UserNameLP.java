package org.argeo.connect.ui.util;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;

import org.argeo.cms.CmsUserManager;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Returns the assigned to display name given a row that contains a Task
 * selector
 */
public class UserNameLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 1L;

	private String selectorName;
	private CmsUserManager userAdminService;

	public UserNameLP(CmsUserManager userAdminService, String selectorName, String propertyName) {
		super(propertyName);
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		this.userAdminService = userAdminService;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		String userId = super.getText(currNode);
		String userName = null;
		if (notEmpty(userId))
			userName = userAdminService.getUserDisplayName(userId);
		if (notEmpty(userName) && !userName.equals(userId))
			return userName + " (" + userId + ")";
		else
			return userId;
	}
}
