package org.argeo.people.workbench.rap.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.parts.AbstractConnectCTabEditor;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
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
	private final static Log log = LogFactory.getLog(RemoveEntityReference.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".removeEntityReference";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_TOREMOVE_JCR_ID = "param.toRemoveJcrId";

	/* DEPENDENCY INJECTION */
	private Repository repository;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String msg = "Your are about to definitively remove this reference.\n" + "Are you sure you want to proceed ?";

		boolean result = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Confirm Deletion", msg);

		if (!result)
			return null;

		String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);

		Session session = null;
		Node versionableParent = null;
		Node toRemoveNode = null;
		try {
			session = repository.login();
			toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);
			versionableParent = ConnectJcrUtils.getVersionableAncestor(toRemoveNode);

			if (versionableParent == null) {
				log.warn("Found no versionnable node in ancestors of " + toRemoveNode + "\n Simply removing.");
				toRemoveNode.remove();
				session.save();
			} else {
				// boolean wasCO =
				ConnectJcrUtils.checkCOStatusBeforeUpdate(versionableParent);
				toRemoveNode.remove();
				// FIXME should we save ? commit ? do nothing
				ConnectJcrUtils.saveAndPublish(versionableParent, true);
				// ConnectJcrUtils.checkCOStatusAfterUpdate(versionableParent,
				// wasCO);
			}
		} catch (RepositoryException e) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Unable to remove ");
			if (toRemoveNode != null)
				errMsg.append(toRemoveNode).append(" - ");
			errMsg.append("JcrID: " + toRemoveJcrId).append(" on parent versionnable node ");
			if (versionableParent != null)
				errMsg.append(versionableParent);
			throw new PeopleException(errMsg.toString(), e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();
		if (iep != null && iep instanceof AbstractConnectCTabEditor)
			((AbstractConnectCTabEditor) iep).forceRefresh();
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
