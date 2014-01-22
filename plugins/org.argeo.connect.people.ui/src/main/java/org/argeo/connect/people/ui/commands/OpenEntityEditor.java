package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.ActivityEditor;
import org.argeo.connect.people.ui.editors.GroupEditor;
import org.argeo.connect.people.ui.editors.OrgEditor;
import org.argeo.connect.people.ui.editors.PersonEditor;
import org.argeo.connect.people.ui.editors.TaskEditor;
import org.argeo.connect.people.ui.editors.UserGroupEditor;
import org.argeo.connect.people.ui.editors.utils.EntityEditorInput;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

/**
 * Open the corresponding editor given a node. Centralize here mapping between a
 * node type and an editor Corresponding node can be retrieved either using the
 * JCR ID or a business defined UID
 */
public class OpenEntityEditor extends AbstractHandler {
	private final static Log log = LogFactory.getLog(OpenEntityEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".openEntityEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;

	public final static String PARAM_ENTITY_UID = "param.entityUid";
	public final static String PARAM_JCR_ID = "param.jcrId";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		EntityEditorInput eei = null;
		Node entity = null;
		Session session = null;

		String jcrId = event.getParameter(PARAM_JCR_ID);
		try {
			session = repository.login();
			if (jcrId != null) {
				entity = session.getNodeByIdentifier(jcrId);
				eei = new EntityEditorInput(jcrId);
			} else {
				String entityUid = event.getParameter(PARAM_ENTITY_UID);
				if (CommonsJcrUtils.isEmptyString(entityUid)) {
					if (log.isTraceEnabled())
						log.warn("Cannot open an editor with no UID");
					return null;
				}
				entity = peopleService.getEntityByUid(session, entityUid);
				if (entity == null) {
					if (log.isTraceEnabled())
						log.warn("No entity found for entity UID " + entityUid);
					return null;
				}
				eei = new EntityEditorInput(entity.getIdentifier());
			}

			PeopleUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.openEditor(eei, getEditorIdFromNode(entity));
		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unexpected JCR error while opening editor", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/**
	 * 
	 * Overwrite to open application specific editors depending on a given node
	 * type.
	 * 
	 * @param curNode
	 * @return
	 */
	protected String getEditorIdFromNode(Node curNode) {
		try {
			if (curNode.isNodeType(PeopleTypes.PEOPLE_USER_GROUP))
				return UserGroupEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_TASK))
				return TaskEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY))
				return ActivityEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return PersonEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)) {
				return OrgEditor.ID;
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				return GroupEditor.ID;
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_GROUP)) {
				return GroupEditor.ID;
			} else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}