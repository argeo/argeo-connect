package org.argeo.tracker.ui;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.AppUiService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.ui.dialogs.ConfigureIssueWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureMilestoneWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureProjectWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureTaskWizard;
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
	private ActivitiesService activitiesService;

	@Override
	public Wizard getCreationWizard(Node node) {
		if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_ISSUE))
			return new ConfigureIssueWizard(userAdminService, trackerService, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_TASK)
				|| ConnectJcrUtils.isNodeType(node, ActivitiesTypes.ACTIVITIES_TASK))
			return new ConfigureTaskWizard(userAdminService, activitiesService, trackerService, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_MILESTONE))
			return new ConfigureMilestoneWizard(userAdminService, trackerService, node);
		// else if (ConnectJcrUtils.isNodeType(node,
		// TrackerTypes.TRACKER_IT_PROJECT))
		// return new ConfigureProjectWizard(userAdminService, trackerService,
		// node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_PROJECT))
			return new ConfigureProjectWizard(userAdminService, trackerService, node);
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

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}