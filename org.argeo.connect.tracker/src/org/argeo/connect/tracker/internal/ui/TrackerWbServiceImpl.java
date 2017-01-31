package org.argeo.connect.tracker.internal.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.workbench.rap.PeopleWorkbenchServiceImpl;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.tracker.ui.commands.OpenTrackerEntityEditor;
import org.argeo.connect.tracker.ui.commands.OpenTrackerSearchEntityEditor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralize here the definition of context specific parameters (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class TrackerWbServiceImpl extends PeopleWorkbenchServiceImpl {

	@Override
	public String getOpenEntityEditorCmdId() {
		return OpenTrackerEntityEditor.ID;
	}

	@Override
	public String getOpenSearchEntityEditorCmdId() {
		return OpenTrackerSearchEntityEditor.ID;
	}

	@Override
	public Wizard getCreationWizard(Node node) {
		return super.getCreationWizard(node);
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return TrackerImages.ICON_ISSUE;
			else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return TrackerImages.ICON_PROJECT;
			else
				return super.getIconForType(entity);
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to get image for node", re);
		}
	}
}
