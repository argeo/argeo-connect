package org.argeo.connect.people.rap.commands;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Asynchronously browse all taggable items of the injected repository and
 * updates the tag cache if needed.
 */
public class ForceTagCacheRefresh extends AbstractHandler {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".forceTagCacheRefresh";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String msg = "You are about to update the tag and mailing list "
				+ "caches of the repository with all found values.\n"
				+ "Are you sure you want to proceed ?";
		Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event)
				.getShell();

		if (!MessageDialog.openConfirm(activeShell, "Confirm Deletion", msg))
			return null;

		new UpdateTagAndInstancesJob(repository, peopleService).schedule();
		return null;
	}

	/** Privileged job that performs the update asynchronously */
	private class UpdateTagAndInstancesJob extends PrivilegedJob {

		private Repository repository;
		private PeopleService peopleService;

		/**
		 * @param repository
		 * @param peopleService
		 */
		public UpdateTagAndInstancesJob(Repository repository,
				PeopleService peopleService) {
			super("Updating the tag and mailing list repository");
			// Must be called *before* the job is scheduled so that a progress
			// window appears.
			// setUser(true);
			this.repository = repository;
			this.peopleService = peopleService;
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();

					peopleService.getResourceService().refreshKnownTags(
							session, PeopleConstants.RESOURCE_TAG);

					peopleService.getResourceService().refreshKnownTags(
							session, PeopleTypes.PEOPLE_MAILING_LIST);
					monitor.worked(1);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to refresh tag and ML cache on " + repository,
						e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
