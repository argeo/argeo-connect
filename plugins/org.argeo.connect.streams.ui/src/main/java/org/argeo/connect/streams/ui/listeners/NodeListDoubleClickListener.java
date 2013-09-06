package org.argeo.connect.streams.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssService;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Centralizes the management of double click on a node
 */
public class NodeListDoubleClickListener implements IDoubleClickListener {

	private RssService rssService;
	private String parentNodeType = null;
	private String tableId = null;

	/**
	 * Set Rss service and table id to enable opening of the correct editor when
	 * displaying list of references
	 */
	public NodeListDoubleClickListener(RssService rssService) {
		this.rssService = rssService;
	}

	public NodeListDoubleClickListener(RssService rssService,
			String parentNodeType) {
		this.rssService = rssService;
		this.parentNodeType = parentNodeType;
	}

	public NodeListDoubleClickListener(RssService rssService,
			String parentNodeType, String tableId) {
		this.rssService = rssService;
		this.parentNodeType = parentNodeType;
		this.tableId = tableId;

	}

	protected void openNodeEditor(String nodeId, String editorId) {
		// try {
		// EntityEditorInput eei = new EntityEditorInput(nodeId);
		// PeopleUiPlugin.getDefault().getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage()
		// .openEditor(eei, editorId);
		// } catch (PartInitException pie) {
		// throw new PeopleException(
		// "Unexpected PartInitException while opening entity editor",
		// pie);
		// }
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (obj instanceof Node) {
			try {
				Node curNode = (Node) obj;
				Session session = curNode.getSession();
				// Mapping for jobs
				// if (curNode.isNodeType(RssTypes.SOURCE)) {
				// // if (PeopleTypes.PEOPLE_PERSON.equals(parentNodeType)) {
				// // Node linkedOrg = peopleService.getEntityById(session,
				// // curNode.getProperty(PeopleNames.PEOPLE_REF_UID)
				// // .getString());
				// // openNodeEditor(linkedOrg.getIdentifier(), OrgEditor.ID);
				// }
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to open editor for node", re);
			}
		}
	}
}