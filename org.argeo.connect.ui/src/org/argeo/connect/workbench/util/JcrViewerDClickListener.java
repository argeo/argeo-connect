package org.argeo.connect.workbench.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Canonical double-click listener for a viewer that displays JCR Rows or Nodes.
 * Calls the OpenEditor command retrieved via the AppWorkbenchService
 */
public class JcrViewerDClickListener implements IDoubleClickListener {

	private String selectorName;
	private SystemWorkbenchService systemWorkbenchService;

	public JcrViewerDClickListener() {
	}

	public JcrViewerDClickListener(String selectorName) {
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	@Deprecated
	public JcrViewerDClickListener(String selectorName, SystemWorkbenchService systemWorkbenchService) {
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
		this.systemWorkbenchService = systemWorkbenchService;
	}

	@Deprecated
	public JcrViewerDClickListener(SystemWorkbenchService systemWorkbenchService) {
		selectorName = null;
		this.systemWorkbenchService = systemWorkbenchService;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
		Node currNode = null;
		try {
			if (obj instanceof Row || obj instanceof Node)
				currNode = ConnectJcrUtils.getNodeFromElement(obj, selectorName);
			if (currNode != null) {
				String cmdId = OpenEntityEditor.ID;
				if (systemWorkbenchService != null)
					cmdId = systemWorkbenchService.getOpenEntityEditorCmdId();
				CommandUtils.callCommand(cmdId, OpenEntityEditor.PARAM_JCR_ID, currNode.getIdentifier());
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to open editor for node " + currNode, re);
		}
	}
}
