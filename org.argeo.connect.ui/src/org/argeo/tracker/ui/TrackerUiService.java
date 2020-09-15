package org.argeo.tracker.ui;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesService;
import org.argeo.cms.CmsUserManager;
import org.argeo.connect.ui.AppUiService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.ui.dialogs.ConfigureComponentWizard;
import org.argeo.tracker.ui.dialogs.ConfigureIssueWizard;
import org.argeo.tracker.ui.dialogs.ConfigureMilestoneWizard;
import org.argeo.tracker.ui.dialogs.ConfigureProjectWizard;
import org.argeo.tracker.ui.dialogs.ConfigureTaskWizard;
import org.argeo.tracker.ui.dialogs.ConfigureVersionWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerUiService implements AppUiService {

	private CmsUserManager cmsUserManager;
	private TrackerService trackerService;
	private ActivitiesService activitiesService;

	@Override
	public Wizard getCreationWizard(Node node) {
		if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_ISSUE))
			return new ConfigureIssueWizard(cmsUserManager, trackerService, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_TASK))
			return new ConfigureTaskWizard(cmsUserManager, activitiesService, trackerService, null, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_MILESTONE))
			return new ConfigureMilestoneWizard(cmsUserManager, trackerService, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_VERSION))
			return new ConfigureVersionWizard(trackerService, node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_COMPONENT))
			return new ConfigureComponentWizard(trackerService, node);
		// else if (ConnectJcrUtils.isNodeType(node,
		// TrackerTypes.TRACKER_IT_PROJECT))
		// return new ConfigureProjectWizard(userAdminService, trackerService,
		// node);
		else if (ConnectJcrUtils.isNodeType(node, TrackerTypes.TRACKER_PROJECT))
			return new ConfigureProjectWizard(cmsUserManager, trackerService, node);
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		return null;
	}

	public void setCmsUserManager(CmsUserManager userAdminService) {
		this.cmsUserManager = userAdminService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
