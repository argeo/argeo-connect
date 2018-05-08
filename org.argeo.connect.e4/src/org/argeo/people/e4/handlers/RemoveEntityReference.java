package org.argeo.people.e4.handlers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.e4.parts.AbstractConnectEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Remove the reference to a given entity. Jcr Identifier of the reference node
 * to remove and the one of its nearest versionable ancestor must be passed as
 * parameters.
 */
public class RemoveEntityReference {
	private final static Log log = LogFactory.getLog(RemoveEntityReference.class);

	public final static String ID = "org.argeo.suite.e4.command.removeEntityReference";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_TOREMOVE_JCR_ID = "toRemoveJcrId";

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart mPart,
			@Named(PARAM_TOREMOVE_JCR_ID) String toRemoveJcrId) {

		String msg = "Your are about to definitively remove this reference.\n" + "Are you sure you want to proceed ?";

		boolean result = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm Deletion", msg);

		if (!result)
			return;

		// String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);

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
		((AbstractConnectEditor) mPart.getObject()).forceRefresh();
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
