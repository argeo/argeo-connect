package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
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
	private String propertyName;

	public JcrUserHtmlLabelProvider(PeopleService peopleService,
			String selectorName, String propertyName) {
		super(propertyName);
		this.propertyName = propertyName;
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
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		try {
			String userId = super.getText(currNode);
			String displayName = null;
			if (EclipseUiUtils.notEmpty(userId))
				displayName = userAdminService.getUserDisplayName(userId);
			if (EclipseUiUtils.isEmpty(displayName))
				displayName = userId;
			return ConnectUiUtils.replaceAmpersand(displayName);
		} catch (Exception e) {
			throw new PeopleException("Unable to get display name for prop: "
					+ propertyName + " of " + currNode, e);
		}
	}
}