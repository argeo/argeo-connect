package org.argeo.connect.tracker.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.tracker.internal.ui.parts.CategoryEditor;
import org.argeo.connect.tracker.internal.ui.parts.IssueEditor;
import org.argeo.connect.tracker.internal.ui.parts.ProjectEditor;
import org.argeo.connect.tracker.ui.TrackerUiPlugin;

/**
 * Open the corresponding editor given a node in a tracker context. Centralize
 * here mappings between a node type and a specific editor.
 */
public class OpenTrackerEntityEditor extends OpenEntityEditor {
	public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".openTrackerEntityEditor";

	/**
	 * Overwrite to open application specific editors depending on a given node
	 * type.
	 * 
	 * @param currNode
	 * @return
	 */
	@Override
	public String getEditorIdFromNode(Node currNode) {
		try {
			// Entities
			if (currNode.isNodeType(TrackerTypes.TRACKER_PROJECT))
				return ProjectEditor.ID;
			else if (currNode.isNodeType(TrackerTypes.TRACKER_COMPONENT)
					|| currNode.isNodeType(TrackerTypes.TRACKER_VERSION))
				return CategoryEditor.ID;
			else if (currNode.isNodeType(TrackerTypes.TRACKER_ISSUE))
				return IssueEditor.ID;
			else
				// No specific editor found
				return super.getEditorIdFromNode(currNode);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node " + currNode, re);
		}
	}
}
