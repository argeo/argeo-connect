package org.argeo.connect.people.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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
			String openEntityEditorCmdId) {
		this.selectorName = selectorName;
		this.openEntityEditorCmdId = openEntityEditorCmdId;
	}

	/**
	 * Double click listener for NODES only, to manage JCR row use
	 * PeopleJcrViewerDClickListener(String selectorName)
	 **/
	public PeopleJcrViewerDClickListener(String openEntityEditorCmdId) {
		selectorName = null;
		this.openEntityEditorCmdId = openEntityEditorCmdId;
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

				String uid = CommonsJcrUtils.get(currNode,
						PeopleNames.PEOPLE_UID);

				if (CommonsJcrUtils.isEmptyString(uid))
					CommandUtils.callCommand(openEntityEditorCmdId,
							OpenEntityEditor.PARAM_JCR_ID,
							currNode.getIdentifier());
				else
					CommandUtils.callCommand(openEntityEditorCmdId,
							OpenEntityEditor.PARAM_ENTITY_UID, uid);
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}
}