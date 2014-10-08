package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Remove the reference to a given entity. Jcr Identifier of the reference node
 * to remove and the one of its nearest versionable ancestor must be passed as
 * parameters.
 */
public class RemoveEntityReference extends AbstractHandler {
	// private final static Log log = LogFactory
	// .getLog(RemoveEntityReference.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".removeEntityReference";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_VERSIONABLE_PARENT_JCR_ID = "param.versionableParentJcrId";
	public final static String PARAM_TOREMOVE_JCR_ID = "param.toRemoveJcrId";

	/* DEPENDENCY INJECTION */
	// private PeopleService peopleService;
	private Repository repository;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String msg = "Your are about to definitively remove this reference.\n"
				+ "Are you sure you want to proceed ?";

		boolean result = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event), "Confirm Deletion", msg);

		if (!result)
			return null;

		String versParentJcrId = event
				.getParameter(PARAM_VERSIONABLE_PARENT_JCR_ID);
		String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);

		Session session = null;
		try {
			session = repository.login();
			Node versionableParent = session
					.getNodeByIdentifier(versParentJcrId);
			Node toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);

			boolean wasCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(versionableParent);
			if (!wasCheckedOut)
				CommonsJcrUtils.checkout(versionableParent);
			toRemoveNode.remove();
			if (wasCheckedOut)
				versionableParent.getSession().save();
			else
				CommonsJcrUtils.saveAndCheckin(versionableParent);

			IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActiveEditor();
			if (iep != null && iep instanceof AbstractEntityCTabEditor)
				((AbstractEntityCTabEditor) iep).forceRefresh();

		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening "
					+ "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}