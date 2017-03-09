package org.argeo.people.workbench.rap.commands;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.jcr.JcrMonitor;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleTypes;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
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

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".forceTagCacheRefresh";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ResourcesService resourcesService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String msg = "You are about to update the tag and mailing list "
				+ "caches of the repository with all found values.\n" + "Are you sure you want to proceed ?";
		Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();

		if (!MessageDialog.openConfirm(activeShell, "Confirm Deletion", msg))
			return null;

		new UpdateTagAndInstancesJob(repository, resourcesService).schedule();
		return null;
	}

	/** Privileged job that performs the update asynchronously */
	private class UpdateTagAndInstancesJob extends PrivilegedJob {

		private Repository repository;
		private ResourcesService resourceService;

		/**
		 * @param repository
		 * @param peopleService
		 */
		public UpdateTagAndInstancesJob(Repository repository, ResourcesService resourceService) {
			super("Updating the tag and mailing list repository");
			this.repository = repository;
			this.resourceService = resourceService;
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();

					resourceService.refreshKnownTags(session, ConnectConstants.RESOURCE_TAG);

					resourceService.refreshKnownTags(session, PeopleTypes.PEOPLE_MAILING_LIST);
					monitor.worked(1);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to refresh tag and ML cache on " + repository, e);
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

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}
}
