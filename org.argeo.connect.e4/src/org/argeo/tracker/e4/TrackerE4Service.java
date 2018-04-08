package org.argeo.tracker.e4;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.ui.TrackerUiService;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerE4Service extends TrackerUiService implements AppE4Service {

	@Override
	public String getEntityEditorId(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_IT_PROJECT))
				return "org.argeo.suite.e4.partdescriptor.itProjectEditor";// ItProjectEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return "org.argeo.suite.e4.partdescriptor.projectEditor";// ProjectEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_MILESTONE))
				return "org.argeo.suite.e4.partdescriptor.milestoneEditor";// MilestoneEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_COMPONENT)
					|| entity.isNodeType(TrackerTypes.TRACKER_VERSION))
				return null;// CategoryEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return null;// IssueEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_TASK))
				return "org.argeo.suite.e4.partdescriptor.projectTaskEditor";// TaskEditor.ID;
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to open editor for node " + entity, re);
		}
		// No specific editor found
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (nodeType.equals(TrackerTypes.TRACKER_PROJECT))
			return null;// AllProjectsEditor.ID;
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
