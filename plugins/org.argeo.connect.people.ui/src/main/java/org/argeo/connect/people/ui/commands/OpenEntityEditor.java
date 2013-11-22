package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

/**
 * Open the corresponding editor given a node. Centralize here mapping between a node type and an editor
 */
public class OpenEntityEditor extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".openEntityEditor";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	// public final static String PARAM_ENTITY_TYPE = "param.entityType";
	public final static String PARAM_ENTITY_UID = "param.entityUid";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// String entityType = event.getParameter(PARAM_ENTITY_TYPE);
		String entityUid = event.getParameter(PARAM_ENTITY_UID);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			Node entity = peopleService.getEntityByUid(session, entityUid);
			EntityEditorInput eei = new EntityEditorInput(
					entity.getIdentifier());
			PeopleUiPlugin
					.getDefault()
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.openEditor(eei,
							peopleUiService.getEditorIdFromNode(entity));
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

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}