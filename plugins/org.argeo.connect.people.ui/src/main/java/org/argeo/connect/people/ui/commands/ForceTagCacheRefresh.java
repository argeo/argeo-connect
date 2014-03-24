package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Browse all taggable items of the injected repository and updates the tag
 * cache if needed.
 */
public class ForceTagCacheRefresh extends AbstractHandler {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".forceTagCacheRefresh";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;

	/**
	 * Overwrite to provide a plugin specific path to the taggable node parent
	 */
	protected String getPathToTagsParent() {
		return PeopleConstants.PEOPLE_TAGS_BASE_PATH;
	}

	protected String getPathToBusinessParent() {
		return PeopleConstants.PEOPLE_BASE_PATH;
	}

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Session session = null;
		try {
			session = repository.login();

			String tagParPath = getPathToTagsParent();
			String businessPath = getPathToBusinessParent();
			Node tagParent = null;
			if (!session.nodeExists(tagParPath)) {
				tagParent = JcrUtils.mkdirs(session, tagParPath);
				session.save();
			} else
				tagParent = session.getNode(tagParPath);
			
			Node businessParent = session
					.getNode(businessPath);

			String msg = "You are about to update the tag cache repository with all already defined values.\n"
					+ "Are you sure you want to proceed ?";

			Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event)
					.getShell();
			if (MessageDialog.openConfirm(activeShell, "Confirm Deletion", msg))
				peopleService.refreshKnownTags(tagParent, businessParent);

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node", e);
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
