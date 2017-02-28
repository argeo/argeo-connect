package org.argeo.activities.workbench.commands;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesService;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.activities.workbench.parts.NewSimpleTaskWizard;
import org.argeo.connect.ConnectException;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Opens the {@link NewSimpleTaskWizard}. No parameter is needed */
public class CreateSimpleTask extends AbstractHandler {

	public final static String ID = ActivitiesUiPlugin.PLUGIN_ID + ".createSimpleTask";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private UserAdminService userAdminService;
	private ActivitiesService activitiesService;
	private AppWorkbenchService appWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;
		String jcrId = null;
		try {
			session = repository.login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session, userAdminService, activitiesService);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				// ConnectJcrUtils.saveAndCheckin(wizard.getCreatedTask());
				session.save();
				jcrId = wizard.getCreatedTask().getIdentifier();
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to create task node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		if (jcrId != null)
			ConnectWorkbenchUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID, jcrId, OpenEntityEditor.PARAM_OPEN_FOR_EDIT, "true");
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
