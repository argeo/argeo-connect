package org.argeo.connect.people.rap.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

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
			if (obj instanceof Row || obj instanceof Node)
				currNode = JcrUiUtils.getNodeFromElement(obj, selectorName);
			if (currNode != null)
				CommandUtils
						.callCommand(peopleWorkbenchService
								.getOpenEntityEditorCmdId(),
								OpenEntityEditor.PARAM_JCR_ID, currNode
										.getIdentifier());

		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node "
					+ currNode, re);
		}
	}
}