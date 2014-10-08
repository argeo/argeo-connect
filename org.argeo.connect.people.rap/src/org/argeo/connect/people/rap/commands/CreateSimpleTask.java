package org.argeo.connect.people.rap.commands;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleUiPlugin;
import org.argeo.connect.people.rap.wizards.NewSimpleTaskWizard;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens the {@link NewSimpleTaskWizard}. No parameter is needed
 */
public class CreateSimpleTask extends AbstractHandler {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".createSimpleTask";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ActivityService activityService;
	private String openEntityEditorCmdId = OpenEntityEditor.ID;

	/**
	 * Overwrite to provide a plugin specific open editor command and thus be
	 * able to open plugin specific editors
	 */
	protected String getOpenEntityEditorCmdId() {
		return OpenEntityEditor.ID;
	}

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;
		String uuid = null;
		try {
			session = repository.login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session,
					activityService);
			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), wizard);
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				// CommonsJcrUtils.saveAndCheckin(wizard.getCreatedTask());
				uuid = wizard.getCreatedTask().getIdentifier();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		if (uuid != null)
			CommandUtils.callCommand(getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_ENTITY_UID, uuid);
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.activityService = peopleService.getActivityService();
	}

	public void setOpenEntityEditorCmdId(String openEntityEditorCmdId) {
		this.openEntityEditorCmdId = openEntityEditorCmdId;
	}

}
