package org.argeo.connect.streams.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.editors.ChannelEditor;
import org.argeo.connect.streams.ui.editors.ChannelEditorNoCTab;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;

/**
 * Centralizes the management of double click on a node
 */
public class NodeListDoubleClickListener implements IDoubleClickListener {

	private PeopleService peopleService;
	private String parentNodeType = null;
	private String tableId = null;

	/**
	 * Set Rss service and table id to enable opening of the correct editor when
	 * displaying list of references
	 */
	public NodeListDoubleClickListener(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public NodeListDoubleClickListener(PeopleService peopleService,
			String parentNodeType) {
		this.peopleService = peopleService;
		this.parentNodeType = parentNodeType;
	}

	public NodeListDoubleClickListener(PeopleService rssService,
			String parentNodeType, String tableId) {
		this.peopleService = rssService;
		this.parentNodeType = parentNodeType;
		this.tableId = tableId;

	}

	protected void openNodeEditor(String editorId, String nodeId) {
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
		if (obj instanceof Node) {
			try {
				Node curNode = (Node) obj;
				if (curNode.isNodeType(RssTypes.RSS_CHANNEL_INFO)) {
					openNodeEditor(ChannelEditorNoCTab.ID, curNode.getIdentifier());
					//openNodeEditor(ChannelEditor.ID, curNode.getIdentifier());
					
					
					
					// if (PeopleTypes.PEOPLE_PERSON.equals(parentNodeType)) {
					// Node linkedOrg = peopleService.getEntityById(session,
					// curNode.getProperty(PeopleNames.PEOPLE_REF_UID)
					// .getString());
					// openNodeEditor(linkedOrg.getIdentifier(), OrgEditor.ID);
				}
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to open editor for node", re);
			}
		}
	}
}