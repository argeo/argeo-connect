package org.argeo.connect.e4.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.NodeConstants;
import org.argeo.cms.ui.dialogs.CmsWizardDialog;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Creates a new entity under draft path of the current repository and opens the
 * corresponding editor. The Node type of the relevant entity must be passed as
 * parameter.
 */
public class CreateEntity {
	// private final static Log log = LogFactory.getLog(CreateEntity.class);

	// public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".createEntity";
	public final static String PARAM_TARGET_NODE_TYPE = "targetNodeType";

	@Inject
	private Repository repository;
	@Inject
	private SystemAppService systemAppService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private EHandlerService handlerService;
	@Inject
	private ECommandService commandService;

	@Execute
	public void execute(@Named(PARAM_TARGET_NODE_TYPE) String nodeType) {

		// FIXME : Known bug (or limitation?) in Jackrabbit: When a user does
		// not have JCR_READ privileges on the root of the workspace, trying to
		// save a session that have transient issues in distinct subtrees of the
		// workspace will fail

		Session draftSession = null;
		Session mainSession = null;
		String jcrId = null;

		try {
			draftSession = repository.login(NodeConstants.HOME_WORKSPACE);

			Node tmpNode = systemAppService.createDraftEntity(draftSession, nodeType);
			Wizard wizard = systemWorkbenchService.getCreationWizard(tmpNode);
			if (wizard != null) {
				// WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
				// wizard);
				CmsWizardDialog dialog = new CmsWizardDialog(Display.getCurrent().getActiveShell(), wizard);
				dialog.setTitle("New...");
				if (dialog.open() != WizardDialog.OK) {
					// This will try to remove the newly created temporary Node if
					// the process fails before first save
					JcrUtils.discardQuietly(draftSession);
					return;
				}
			}
			// if(true)
			// return null;
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
			throw new ConnectException("Cannot create " + nodeType + " entity", e);
		} finally {
			JcrUtils.logoutQuietly(draftSession);
			JcrUtils.logoutQuietly(mainSession);
		}

		if (jcrId != null) {
			// Open the corresponding editor
			Map<String, String> params = new HashMap<>();
			params.put(ConnectEditor.PARAM_JCR_ID, jcrId);
			params.put(ConnectEditor.PARAM_OPEN_FOR_EDIT, "true");
			String openEntityCmd = systemWorkbenchService.getOpenEntityEditorCmdId();
			systemWorkbenchService.callCommand(openEntityCmd, params);
		}
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
}
