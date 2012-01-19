package org.argeo.connect.ui.gps.commands;

import javax.jcr.Session;

import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.ui.gps.wizards.CreateLocalRepoWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Open the new repository wizard . */
public class OpenNewRepoWizard extends AbstractHandler {
	public final static String ID = "org.argeo.connect.ui.gps.openNewRepoWizard";
	public final static String DEFAULT_ICON_REL_PATH = "icons/repo.gif";
	public final static String DEFAULT_LABEL = "Create a new local repository";

	/* DEPENDENCY INJECTION */
	private TrackDao trackDao;
	private Session jcrSession;

	public Object execute(ExecutionEvent event) throws ExecutionException {

		CreateLocalRepoWizard wizard = new CreateLocalRepoWizard(jcrSession,
				trackDao);
		WizardDialog dialog = new WizardDialog(
				HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}

}
