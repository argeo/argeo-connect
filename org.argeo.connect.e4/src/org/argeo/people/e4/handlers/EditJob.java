package org.argeo.people.e4.handlers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.e4.parts.AbstractConnectEditor;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.dialogs.EditJobDialog;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

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
public class EditJob {
	// FIXME make command id configurable
	public final static String ID = "org.argeo.suite.e4.command.editJob";
	// public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR =
	// PeopleRapPlugin.getImageDescriptor("icons/add.png");
	public final static String PARAM_RELEVANT_NODE_JCR_ID = "relevantNodeJcrId";
	public final static String PARAM_IS_BACKWARD = "isBackward";

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private PeopleService peopleService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	@Execute
	public void execute(MPart mPart, @Named(PARAM_RELEVANT_NODE_JCR_ID) String relevantNodeJcrId) {
		execute(mPart, relevantNodeJcrId, null);
	}

	@Execute
	public void execute(MPart mPart, @Named(PARAM_RELEVANT_NODE_JCR_ID) String relevantNodeJcrId,
			@Named(PARAM_IS_BACKWARD) String isBackwardStr) {
		Session session = null;
		try {
			session = repository.login();

			Node relevantNode = session.getNodeByIdentifier(relevantNodeJcrId);

			Dialog diag;
			// boolean isBackward = false;
			if (relevantNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				// Edit an existing job
				// isBackward = new Boolean(event.getParameter(PARAM_IS_BACKWARD));
				boolean isBackward = isBackwardStr == null ? false : new Boolean(isBackwardStr);
				diag = new EditJobDialog(Display.getCurrent().getActiveShell(), "Edit employee information",
						resourcesService, peopleService, systemWorkbenchService, relevantNode, null, isBackward);
			} else {
				// Create a new job
				boolean isBackward = relevantNode.isNodeType(PeopleTypes.PEOPLE_ORG);
				diag = new EditJobDialog(Display.getCurrent().getActiveShell(), "Edit position", resourcesService,
						peopleService, systemWorkbenchService, null, relevantNode, isBackward);
			}

			int result = diag.open();
			if (result == Window.OK) {
				// IEditorPart iep =
				// HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
				// if (iep != null && iep instanceof AbstractConnectEditor)
				// ((AbstractConnectEditor) iep).forceRefresh();
				((AbstractConnectEditor) mPart.getObject()).forceRefresh();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening " + "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	/* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }
	//
	// public void setResourcesService(ResourcesService resourcesService) {
	// this.resourcesService = resourcesService;
	// }
	//
	// public void setPeopleService(PeopleService peopleService) {
	// this.peopleService = peopleService;
	// }
	//
	// public void setSystemWorkbenchService(SystemWorkbenchService
	// systemWorkbenchService) {
	// this.systemWorkbenchService = systemWorkbenchService;
	// }
}
