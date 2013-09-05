package org.argeo.connect.people.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.argeo.connect.people.ui.editors.FilmEditor;
import org.argeo.connect.people.ui.editors.OrgEditor;
import org.argeo.connect.people.ui.editors.PersonEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;

/**
 * Centralizes the management of double click on a node
 */
public class NodeListDoubleClickListener implements IDoubleClickListener {

	private PeopleService peopleService;
	private String parentNodeType= null;
	private String tableId = null;

	/**
	 * Set people service and table id to enable opening of the correct editor
	 * when displaying list of references
	 */
	public NodeListDoubleClickListener(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
	
	public NodeListDoubleClickListener(PeopleService peopleService,
			String parentNodeType) {
		this.peopleService = peopleService;
		this.parentNodeType = parentNodeType;
	}

	public NodeListDoubleClickListener(PeopleService peopleService,
			String parentNodeType, String tableId) {
		this.peopleService = peopleService;
		this.parentNodeType = parentNodeType;
		this.tableId = tableId;

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
		if (obj instanceof Node) {
			try {
				Node curNode = (Node) obj;
				Session session = curNode.getSession();
				// Mapping for jobs
				if (curNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
					if (PeopleTypes.PEOPLE_PERSON.equals(parentNodeType)) {
						Node linkedOrg = peopleService.getEntityById(session,
								curNode.getProperty(PeopleNames.PEOPLE_REF_UID)
										.getString());
						openNodeEditor(linkedOrg.getIdentifier(), OrgEditor.ID);
					} else if (PeopleTypes.PEOPLE_ORGANIZATION
							.equals(parentNodeType))
						openNodeEditor(curNode.getParent().getParent()
								.getIdentifier(), PersonEditor.ID);
				} else if (curNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					openNodeEditor(curNode.getIdentifier(), PersonEditor.ID);
				} else if (curNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)) {
					openNodeEditor(curNode.getIdentifier(), OrgEditor.ID);
				} else if (curNode.isNodeType(FilmTypes.FILM)) {
					openNodeEditor(curNode.getIdentifier(), FilmEditor.ID);
				}
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to open editor for node", re);
			}
		}
	}
}