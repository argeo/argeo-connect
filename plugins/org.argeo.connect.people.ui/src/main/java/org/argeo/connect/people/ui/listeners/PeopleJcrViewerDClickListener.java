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

	public PeopleJcrViewerDClickListener(String selectorName) {
		this.selectorName = selectorName;
	}

	/**
	 * Double click listener for NODES only, to manage JCR row use
	 * PeopleJcrViewerDClickListener(String selectorName)
	 **/
	public PeopleJcrViewerDClickListener() {
		selectorName = null;
	}

	/**
	 * Overwrite to provide a plugin specific open editor command and thus be
	 * able to open plugin specific editors
	 */
	protected String getOpenEditorCommandId() {
		return OpenEntityEditor.ID;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		Node currNode = null;
		try {
			if (obj instanceof Row) {
				Row curRow = (Row) obj;
				currNode = curRow.getNode(selectorName);
			} else if (obj instanceof Node)
				currNode = (Node) obj;

			if (currNode != null)
				CommandUtils.callCommand(getOpenEditorCommandId(),
						OpenEntityEditor.PARAM_ENTITY_UID,
						CommonsJcrUtils.get(currNode, PeopleNames.PEOPLE_UID));
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}
}