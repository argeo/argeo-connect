package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;

/**
 * Wraps the getText() method of the JcrRowLabelProvider to retrieve a user ID.
 * It retrieves then the corresponding display name using the UserAdminService
 * exposed by the people service and then clean it to remove invalid characters
 * (typically the ampersand).
 */
public class JcrUserHtmlLabelProvider extends JcrRowLabelProvider {
	private static final long serialVersionUID = 2134911527741337612L;

	private final UserAdminService userAdminService;
	private String selectorName;

	public JcrUserHtmlLabelProvider(PeopleService peopleService,
			String selectorName, String propertyName) {
		super(propertyName);
		userAdminService = peopleService.getUserAdminService();
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	public JcrUserHtmlLabelProvider(PeopleService peopleService,
			String propertyName) {
		super(propertyName);
		userAdminService = peopleService.getUserAdminService();
	}

	@Override
	public String getText(Object element) {
		Node currNode = JcrUiUtils.getNodeFromElement(element, selectorName);
		String userId = super.getText(currNode);
		String displayName = null;
		if (EclipseUiUtils.notEmpty(userId))
			displayName = userAdminService.getUserDisplayName(userId);
		if (EclipseUiUtils.isEmpty(displayName))
			displayName = userId;
		return PeopleUiUtils.replaceAmpersand(displayName);
	}
}