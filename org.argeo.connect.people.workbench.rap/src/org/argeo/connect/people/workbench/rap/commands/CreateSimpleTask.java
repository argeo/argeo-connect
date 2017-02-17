package org.argeo.connect.people.workbench.rap.commands;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.activities.ActivitiesService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.wizards.NewSimpleTaskWizard;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Opens the {@link NewSimpleTaskWizard}. No parameter is needed */
public class CreateSimpleTask extends AbstractHandler {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".createSimpleTask";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private UserAdminService userAdminService;
	private ActivitiesService activityService;
	private AppWorkbenchService appWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;
		String jcrId = null;
		try {
			session = repository.login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session, userAdminService, activityService);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				// ConnectJcrUtils.saveAndCheckin(wizard.getCreatedTask());
				session.save();
				jcrId = wizard.getCreatedTask().getIdentifier();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		if (jcrId != null)
			ConnectWorkbenchUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
					jcrId, OpenEntityEditor.PARAM_OPEN_FOR_EDIT, "true");
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setActivityService(ActivitiesService activityService) {
		this.activityService = activityService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
