package org.argeo.connect.people.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Canonic double clic listener for a viewer that displays JCR Rows or Nodes. It
 * will call the OpenEditor command defined by the call back method.
 */
public class PeopleJcrViewerDClickListener implements IDoubleClickListener {

	private final String selectorName;
	private final String openEntityEditorCmdId;

	public PeopleJcrViewerDClickListener(String selectorName,
			PeopleUiService peopleUiService) {
		this.selectorName = selectorName;
		this.openEntityEditorCmdId = peopleUiService.getOpenEntityEditorCmdId();
	}

	/**
	 * Double click listener for NODES only, to manage JCR row use
	 * PeopleJcrViewerDClickListener(String selectorName)
	 **/
	public PeopleJcrViewerDClickListener(PeopleUiService peopleUiService) {
		selectorName = null;
		this.openEntityEditorCmdId = peopleUiService.getOpenEntityEditorCmdId();
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		Node currNode = null;
		try {
			if (obj instanceof Row && selectorName != null) {
				Row curRow = (Row) obj;
				currNode = curRow.getNode(selectorName);
			} else if (obj instanceof Node)
				currNode = (Node) obj;

			if (currNode != null) {
				CommandUtils
						.callCommand(openEntityEditorCmdId,
								OpenEntityEditor.PARAM_JCR_ID,
								currNode.getIdentifier());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}
}