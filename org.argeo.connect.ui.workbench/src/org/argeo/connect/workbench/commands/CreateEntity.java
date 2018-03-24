package org.argeo.connect.workbench.commands;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.workbench.ConnectUiPlugin;
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

		// FIXME : Known bug (or limitation?) in Jackrabbit: When a user does
		// not have JCR_READ privileges on the root of the workspace, trying to
		// save a session that have transient issues in distinct subtrees of the
		// workspace will fail

		Session draftSession = null;
		Session mainSession = null;
		String jcrId = null;

		String nodeType = event.getParameter(PARAM_TARGET_NODE_TYPE);
		try {
			draftSession = repository.login();

			Node tmpNode = systemAppService.createDraftEntity(draftSession, nodeType);
			Wizard wizard = systemWorkbenchService.getCreationWizard(tmpNode);
			if (wizard != null) {
				WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
				dialog.setTitle("New...");
				if (dialog.open() != WizardDialog.OK) {
					// This will try to remove the newly created temporary Node if
					// the process fails before first save
					JcrUtils.discardQuietly(draftSession);
					return null;
				}
			}
			mainSession = repository.login();

			// By default, all entities are stored at the same place,
			// depending on their types.
			// We will enhance this in the future, typically to enable
			// (partially) private entities
			Node parent = mainSession.getNode("/" + systemAppService.getBaseRelPath(nodeType));
			Node newNode = systemAppService.publishEntity(parent, nodeType, tmpNode);

			newNode = systemAppService.saveEntity(newNode, false);
			jcrId = newNode.getIdentifier();

		} catch (RepositoryException e) {
			throw new ConnectException("Cannot create " + nodeType + "entity", e);
		} finally {
			JcrUtils.logoutQuietly(draftSession);
			JcrUtils.logoutQuietly(mainSession);
		}

		if (jcrId != null) {
			// Open the corresponding editor
			Map<String, String> params = new HashMap<>();
			params.put(ConnectEditor.PARAM_JCR_ID, jcrId);
			params.put(ConnectEditor.PARAM_OPEN_FOR_EDIT, "true");
			// ConnectWorkbenchUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
			// ConnectEditor.PARAM_JCR_ID, jcrId, ConnectEditor.PARAM_OPEN_FOR_EDIT,
			// "true");
		}
		return null;
	}

	// Expose to children classes
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
