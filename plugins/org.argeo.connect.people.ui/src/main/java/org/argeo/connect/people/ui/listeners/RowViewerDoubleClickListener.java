package org.argeo.connect.people.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;

/**
 * Centralizes the management of double click on a viewer table that displays
 * JCR Rows
 */
public class RowViewerDoubleClickListener implements IDoubleClickListener {

	private final PeopleUiService peopleUiService;
	private final String selectorName;

	public RowViewerDoubleClickListener(PeopleUiService peopleUiService,
			String selectorName) {
		this.selectorName = selectorName;
		this.peopleUiService = peopleUiService;
	}

	protected void openNodeEditor(String nodeId, String editorId) {
		try {
			EntityEditorInput eei = new EntityEditorInput(nodeId);
			PeopleUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.openEditor(eei, editorId);
		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (obj instanceof Row) {
			try {
				Row curRow = (Row) obj;
				Node currNode = curRow.getNode(selectorName);
				openNodeEditor(currNode.getIdentifier(),
						peopleUiService.getEditorIdFromNode(currNode));
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to open editor for node", re);
			}
		}
	}

}