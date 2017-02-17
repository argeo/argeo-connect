package org.argeo.connect.activities.workbench;

import javax.jcr.Node;

import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.eclipse.swt.graphics.Image;

public class ActivitiesWorkbenchService implements AppWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
//		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.PROJECTS_PROJECT))
//			return null;
//		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.PROJECTS_MILESTONE))
//			return null;
//		else
			return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		// if (ConnectJcrUtils.isNodeType(entity,
		// ActivitiesTypes.PROJECTS_PROJECT))
		// return ProjectsImages.ICON_PROJECT;
		// else if (ConnectJcrUtils.isNodeType(entity,
		// ActivitiesTypes.PROJECTS_MILESTONE))
		// return ProjectsImages.ICON_MILESTONE;
		// else
			return null;
	}
}
