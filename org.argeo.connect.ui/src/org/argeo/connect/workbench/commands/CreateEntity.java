package org.argeo.connect.workbench.commands;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.ConnectUiPlugin;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Creates a new entity under draft path of the current repository and opens the
 * corresponding editor. The Node type of the relevant entity must be passed as
 * parameter.
 */
public class CreateEntity extends AbstractHandler {
	// private final static Log log = LogFactory.getLog(CreateEntity.class);

	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".createEntity";
	public final static String PARAM_TARGET_NODE_TYPE = "param.targetNodeType";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private SystemAppService systemAppService;
	private SystemWorkbenchService systemWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		String nodeType = event.getParameter(PARAM_TARGET_NODE_TYPE);
		Session draftSession = null;
		Session mainSession = null;
		String jcrId = null;
		try {
			draftSession = repository.login();
			mainSession = repository.login();
			Node tmpParent = systemAppService.getDraftParent(draftSession);
			String uuid = UUID.randomUUID().toString();
			Node tmpNode = tmpParent.addNode(uuid);
			tmpNode.addMixin(nodeType);
			tmpNode.setProperty(ConnectNames.CONNECT_UID, uuid);
			Wizard wizard = systemWorkbenchService.getCreationWizard(tmpNode);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.setTitle("New...");
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				Node parent = mainSession.getNode("/" + systemAppService.getBaseRelPath(nodeType));
				Node newNode = systemAppService.createEntity(parent, nodeType, tmpNode);
				// Save the newly created entity without creating a base version
				newNode = systemAppService.saveEntity(newNode, false);
				jcrId = newNode.getIdentifier();
			} else {
				// This will try to remove the newly created temporary Node if
				// the process fails before first save
				JcrUtils.discardQuietly(draftSession);
			}

		} catch (RepositoryException e) {
			throw new ConnectException("Cannot create "
					+ nodeType+ "entity", e);
		} finally {
			JcrUtils.logoutQuietly(draftSession);
			JcrUtils.logoutQuietly(mainSession);
		}

		if (jcrId != null)
			// Open the corresponding editor
			ConnectWorkbenchUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID, jcrId, OpenEntityEditor.PARAM_OPEN_FOR_EDIT, "true");

		return null;
	}

	// expose to children classes
	protected Repository getRepository() {
		return repository;
	}

	protected AppService getAppService() {
		return systemAppService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return systemWorkbenchService;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setSystemAppService(SystemAppService systemAppService) {
		this.systemAppService = systemAppService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}
