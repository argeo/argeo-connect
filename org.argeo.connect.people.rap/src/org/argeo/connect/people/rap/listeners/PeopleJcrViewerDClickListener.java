package org.argeo.connect.people.rap.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.workbench.CommandUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Canonic double-click listener for a viewer that displays JCR Rows or Nodes.
 * Calls the OpenEditor command retrieved via the PeopleWorkbenchService
 */
public class PeopleJcrViewerDClickListener implements IDoubleClickListener {

	private String selectorName;
	private final PeopleWorkbenchService peopleWorkbenchService;

	public PeopleJcrViewerDClickListener(String selectorName,
			PeopleWorkbenchService peopleWorkbenchService) {

		if (CommonsJcrUtils.checkNotEmptyString(selectorName))
			this.selectorName = selectorName;
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	/**
	 * Double click listener for NODES only, to manage JCR row use
	 * PeopleJcrViewerDClickListener(String selectorName)
	 **/
	public PeopleJcrViewerDClickListener(
			PeopleWorkbenchService peopleWorkbenchService) {
		selectorName = null;
		this.peopleWorkbenchService = peopleWorkbenchService;
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
				if (selectorName == null)
					currNode = curRow.getNode();
				else
					currNode = curRow.getNode(selectorName);
			} else if (obj instanceof Node)
				currNode = (Node) obj;

			if (currNode != null) {
				CommandUtils
						.callCommand(peopleWorkbenchService
								.getOpenEntityEditorCmdId(),
								OpenEntityEditor.PARAM_JCR_ID, currNode
										.getIdentifier());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}
}