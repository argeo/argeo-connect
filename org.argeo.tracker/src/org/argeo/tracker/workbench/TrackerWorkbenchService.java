package org.argeo.tracker.workbench;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.workbench.AllProjectsEditor;
import org.argeo.tracker.internal.workbench.CategoryEditor;
import org.argeo.tracker.internal.workbench.IssueEditor;
import org.argeo.tracker.internal.workbench.ItProjectEditor;
import org.argeo.tracker.internal.workbench.MilestoneEditor;
import org.argeo.tracker.internal.workbench.ProjectEditor;
import org.argeo.tracker.internal.workbench.TaskEditor;
import org.argeo.tracker.ui.TrackerUiService;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerWorkbenchService extends TrackerUiService implements AppWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_IT_PROJECT))
				return ItProjectEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return ProjectEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_MILESTONE))
				return MilestoneEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_COMPONENT)
					|| entity.isNodeType(TrackerTypes.TRACKER_VERSION))
				return CategoryEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return IssueEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_TASK))
				return TaskEditor.ID;
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to open editor for node " + entity, re);
		}
		// No specific editor found
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (nodeType.equals(TrackerTypes.TRACKER_PROJECT))
			return AllProjectsEditor.ID;
		// No specific editor found
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return ConnectImages.ISSUE;
			else if (entity.isNodeType(TrackerTypes.TRACKER_TASK))
				return ConnectImages.TASK;
			else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return ConnectImages.PROJECT;
			else if (entity.isNodeType(TrackerTypes.TRACKER_MILESTONE))
				return ConnectImages.MILESTONE;
			else
				return super.getIconForType(entity);
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to get image for node" + entity, re);
		}
	}
}
