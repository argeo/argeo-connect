package org.argeo.tracker.workbench;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.workbench.AllProjectsEditor;
import org.argeo.tracker.internal.workbench.CategoryEditor;
import org.argeo.tracker.internal.workbench.IssueEditor;
import org.argeo.tracker.internal.workbench.ProjectEditor;
import org.argeo.tracker.internal.workbench.TrackerImages;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerWorkbenchService implements AppWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return ProjectEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_COMPONENT)
					|| entity.isNodeType(TrackerTypes.TRACKER_VERSION))
				return CategoryEditor.ID;
			else if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return IssueEditor.ID;
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
	public Wizard getCreationWizard(Node node) {
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return TrackerImages.ICON_ISSUE;
			else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return TrackerImages.ICON_PROJECT;
			else
				return null;
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to get image for node" + entity, re);
		}
	}
}
