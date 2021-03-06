package org.argeo.activities.ui;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.cms.CmsUserManager;
import org.argeo.connect.ui.AppUiService;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/** Activities specific implementation of AppUiService */
public class ActivitiesUiService implements AppUiService {
	private CmsUserManager cmsUserManager;
	private ActivitiesService activitiesService;

	@Override
	public Wizard getCreationWizard(Node node) {
		if (ConnectJcrUtils.isNodeType(node, ActivitiesTypes.ACTIVITIES_TASK))
			return new NewSimpleTaskWizard(cmsUserManager, activitiesService, node);
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_RATE))
			return ConnectImages.RATE;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK))
			return ConnectImages.TODO;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return ConnectImages.ACTIVITY;
		return null;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
