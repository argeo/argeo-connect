package org.argeo.activities.e4;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.ActivitiesUiService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class ActivitiesE4Service extends ActivitiesUiService implements AppE4Service {

	@Inject
	EPartService partService;

	@Override
	public void openEntityEditor(Node entity) {
		MPart part = partService.createPart(getEntityEditorId(entity));
		try {
			part.setLabel(entity.getName());
			part.getPersistedState().put("nodeWorkspace", entity.getSession().getWorkspace().getName());
			part.getPersistedState().put("nodePath", entity.getPath());
			part.getPersistedState().put("entityId", entity.getIdentifier());
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot open " + entity, e);
		}

		// the provided part is be shown
		partService.showPart(part, PartState.ACTIVATE);
	}

	@Override
	public void openSearchEntityView(String nodeType, String label) {
		// TODO Auto-generated method stub

	}

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
