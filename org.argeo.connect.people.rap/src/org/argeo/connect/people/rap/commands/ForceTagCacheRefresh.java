package org.argeo.connect.people.rap.commands;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Browse all tagable items of the injected repository and updates the tag cache
 * if needed.
 */
public class ForceTagCacheRefresh extends AbstractHandler {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".forceTagCacheRefresh";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;

		String msg = "You are about to update the tag and mailing list "
				+ "caches of the repository with all found values.\n"
				+ "Are you sure you want to proceed ?";
		Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event)
				.getShell();

		if (!MessageDialog.openConfirm(activeShell, "Confirm Deletion", msg))
			return null;

		try {
			session = repository.login();
			peopleService.getTagService().refreshKnownTags(
					session,
					NodeType.NT_UNSTRUCTURED,
					peopleService
							.getResourceBasePath(PeopleConstants.RESOURCE_TAG),
					PeopleTypes.PEOPLE_BASE, PeopleNames.PEOPLE_TAGS,
					peopleService.getBasePath(null));

			peopleService
					.getTagService()
					.refreshKnownTags(
							session,
							PeopleTypes.PEOPLE_MAILING_LIST,
							peopleService
									.getResourceBasePath(PeopleTypes.PEOPLE_MAILING_LIST),
							PeopleTypes.PEOPLE_BASE,
							PeopleNames.PEOPLE_MAILING_LISTS,
							peopleService.getBasePath(null));

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to log in the repository", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
