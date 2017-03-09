package org.argeo.people.workbench.rap.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.argeo.people.workbench.rap.dialogs.EditJobDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens a dialog to create or edit a position of a person in an organisation,
 * e.g. a job Might be called from a person or an organisation editor.
 * 
 * 
 * Positions are currently stored as children of an existing person. So when
 * trying to add or edit a person to a given organisation, the "isBackward" flag
 * must be true. When creating a new job, this flag is optional and not used: it
 * can be deduced from the passed node type.
 * 
 */
public class EditJob extends AbstractHandler {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".editJob";
	public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleRapPlugin.getImageDescriptor("icons/add.png");
	public final static String PARAM_RELEVANT_NODE_JCR_ID = "param.relevantNodeJcrId";
	public final static String PARAM_IS_BACKWARD = "param.isBackward";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ResourcesService resourcesService;
	private PeopleService peopleService;
	private SystemWorkbenchService systemWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String relevantNodeJcrId = event.getParameter(PARAM_RELEVANT_NODE_JCR_ID);

		Session session = null;
		try {
			session = repository.login();

			Node relevantNode = session.getNodeByIdentifier(relevantNodeJcrId);

			Dialog diag;
			boolean isBackward;
			if (relevantNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				// Edit an existing job
				isBackward = new Boolean(event.getParameter(PARAM_IS_BACKWARD));
				diag = new EditJobDialog(HandlerUtil.getActiveShell(event), "Edit employee information",
						resourcesService, peopleService, systemWorkbenchService, relevantNode, null, isBackward);
			} else {
				// Create a new job
				isBackward = relevantNode.isNodeType(PeopleTypes.PEOPLE_ORG);
				diag = new EditJobDialog(HandlerUtil.getActiveShell(event), "Edit position", resourcesService,
						peopleService, systemWorkbenchService, null, relevantNode, isBackward);
			}

			int result = diag.open();
			if (result == Window.OK) {
				IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
				if (iep != null && iep instanceof AbstractConnectEditor)
					((AbstractConnectEditor) iep).forceRefresh();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening " + "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}
