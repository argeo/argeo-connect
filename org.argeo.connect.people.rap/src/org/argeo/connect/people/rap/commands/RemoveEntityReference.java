package org.argeo.connect.people.rap.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.editors.utils.AbstractEntityCTabEditor;
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

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
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
		Node versionableParent = null;
		Node toRemoveNode = null;
		try {
			session = repository.login();
			versionableParent = session.getNodeByIdentifier(versParentJcrId);
			toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);

			boolean wasCO = CommonsJcrUtils
					.checkCOStatusBeforeUpdate(versionableParent);
			toRemoveNode.remove();
			CommonsJcrUtils.checkCOStatusAfterUpdate(versionableParent, wasCO);

		} catch (RepositoryException e) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Unable to remove ");
			if (toRemoveNode != null)
				errMsg.append(toRemoveNode).append(" - ");
			errMsg.append("JcrID: " + toRemoveJcrId).append(
					" on parent versionnable node ");
			if (versionableParent != null)
				errMsg.append(versionableParent).append(" - ");
			errMsg.append("JcrID: " + versParentJcrId);
			throw new PeopleException(errMsg.toString(), e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getActiveEditor();
		if (iep != null && iep instanceof AbstractEntityCTabEditor)
			((AbstractEntityCTabEditor) iep).forceRefresh();
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}