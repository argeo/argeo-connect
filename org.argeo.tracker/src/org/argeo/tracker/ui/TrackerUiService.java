package org.argeo.tracker.ui;

import javax.jcr.Node;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.AppUiService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.ui.dialogs.NewIssueWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerUiService implements AppUiService {

	private UserAdminService userAdminService;
	private TrackerService trackerService;

	@Override
	public Wizard getCreationWizard(Node node) {
		if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_ISSUE))
			return new NewIssueWizard(userAdminService, trackerService, node);
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		return null;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}
}
