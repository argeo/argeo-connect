package org.argeo.connect.people.ui.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.editors.utils.EntityEditorInput;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;

/**
 * Centralizes the management of double click on a node
 */
public class NodeListDoubleClickListener implements IDoubleClickListener {

	private PeopleService peopleService;
	private PeopleUiService peopleUiService;
	private String parentNodeType = null;

	/**
	 * Set people service and table id to enable opening of the correct editor
	 * when displaying list of references
	 */
	public NodeListDoubleClickListener(PeopleService peopleService,
			PeopleUiService peopleUiService) {
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
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

				String cmdID = peopleUiService.getOpenEntityEditorCmdId();
				// Mapping for jobs
				if (curNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
					if (PeopleTypes.PEOPLE_PERSON.equals(parentNodeType)) {
						Node linkedOrg = peopleService.getEntityByUid(session,
								curNode.getProperty(PeopleNames.PEOPLE_REF_UID)
										.getString());
						CommandUtils.callCommand(cmdID,
								OpenEntityEditor.PARAM_ENTITY_UID,
								linkedOrg.getIdentifier());
					} else if (PeopleTypes.PEOPLE_ORGANIZATION
							.equals(parentNodeType))
						CommandUtils.callCommand(cmdID,
								OpenEntityEditor.PARAM_ENTITY_UID, curNode
										.getParent().getParent()
										.getIdentifier());
				} else {
					CommandUtils.callCommand(cmdID,
							OpenEntityEditor.PARAM_ENTITY_UID,
							curNode.getIdentifier());
				}
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to open editor for node "
						+ obj, re);
			}
		}
	}
}