package org.argeo.activities.e4;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.ActivitiesUiService;
import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.util.ConnectJcrUtils;

public class ActivitiesE4Service extends ActivitiesUiService implements AppE4Service {
	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_RATE))
			return null;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK))
			return "org.argeo.suite.e4.partdescriptor.taskEditor";
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return null;
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (ActivitiesTypes.ACTIVITIES_TASK.equals(nodeType) || ActivitiesTypes.ACTIVITIES_ACTIVITY.equals(nodeType))
			return null;
		return null;
	}

	
}
