package org.argeo.connect.people.rap.commands;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.wizards.NewSimpleTaskWizard;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Opens the {@link NewSimpleTaskWizard}. No parameter is needed */
public class CreateSimpleTask extends AbstractHandler {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".createSimpleTask";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;
		String jcrId = null;
		try {
			session = repository.login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session,
					peopleService);
			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), wizard);
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				// JcrUiUtils.saveAndCheckin(wizard.getCreatedTask());
				session.save();
				jcrId = wizard.getCreatedTask().getIdentifier();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		if (jcrId != null)
			PeopleRapUtils.callCommand(
					peopleWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID, jcrId,
					OpenEntityEditor.PARAM_OPEN_FOR_EDIT, "true");
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}