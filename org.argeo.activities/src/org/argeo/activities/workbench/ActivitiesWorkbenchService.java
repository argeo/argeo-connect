package org.argeo.activities.workbench;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.ActivitiesUiService;
import org.argeo.activities.workbench.parts.ActivityEditor;
import org.argeo.activities.workbench.parts.RateEditor;
import org.argeo.activities.workbench.parts.SearchActivityEditor;
import org.argeo.activities.workbench.parts.TaskEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.LegacyWorkbenchService;

public class ActivitiesWorkbenchService extends ActivitiesUiService implements LegacyWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_RATE))
			return RateEditor.ID;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK))
			return TaskEditor.ID;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return ActivityEditor.ID;
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (ActivitiesTypes.ACTIVITIES_TASK.equals(nodeType) || ActivitiesTypes.ACTIVITIES_ACTIVITY.equals(nodeType))
			return SearchActivityEditor.ID;
		return null;
	}

}
