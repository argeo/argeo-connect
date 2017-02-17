package org.argeo.connect.people.workbench.rap.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Canonic double-click listener for a viewer that displays JCR Rows or Nodes.
 * Calls the OpenEditor command retrieved via the AppWorkbenchService
 */
public class PeopleJcrViewerDClickListener implements IDoubleClickListener {

	private String selectorName;
	private final AppWorkbenchService appWorkbenchService;

	public PeopleJcrViewerDClickListener(String selectorName, AppWorkbenchService appWorkbenchService) {
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
		this.appWorkbenchService = appWorkbenchService;
	}

	public PeopleJcrViewerDClickListener(AppWorkbenchService appWorkbenchService) {
		selectorName = null;
		this.appWorkbenchService = appWorkbenchService;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
		Node currNode = null;
		try {
			if (obj instanceof Row || obj instanceof Node)
				currNode = ConnectJcrUtils.getNodeFromElement(obj, selectorName);
			if (currNode != null)
				CommandUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
						currNode.getIdentifier());

		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node " + currNode, re);
		}
	}
}