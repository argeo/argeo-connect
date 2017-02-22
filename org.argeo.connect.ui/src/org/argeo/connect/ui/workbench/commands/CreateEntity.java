package org.argeo.connect.ui.workbench.commands;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectUiPlugin;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
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
	private AppService appService;
	private AppWorkbenchService appWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String nodeType = event.getParameter(PARAM_TARGET_NODE_TYPE);

		Session session = null;
		Node newNode = null;
		try {
			session = repository.login();
			Node parent = appService.getDraftParent(session);
			String uuid = UUID.randomUUID().toString();
			newNode = parent.addNode(uuid);
			newNode.addMixin(nodeType);
			newNode.setProperty(ConnectNames.CONNECT_UID, uuid);

			Wizard wizard = appWorkbenchService.getCreationWizard(newNode);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.setTitle("New...");
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				// Save the newly created entity without creating a base version
				newNode = appService.saveEntity(newNode, false);
				// Open the corresponding editor
				String jcrId = newNode.getIdentifier();
				ConnectWorkbenchUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, jcrId, OpenEntityEditor.PARAM_OPEN_FOR_EDIT, "true");

				return newNode.getPath();
			} else {
				// This will try to remove the newly created temporary Node if
				// the process fails before first save
				JcrUtils.discardQuietly(session);
			}
		} catch (RepositoryException e) {
			throw new ConnectException("unexpected JCR error while opening " + "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	// expose to children classes
	protected Repository getRepository() {
		return repository;
	}

	protected AppService getAppService() {
		return appService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return appWorkbenchService;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
