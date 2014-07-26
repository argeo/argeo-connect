package org.argeo.connect.people.ui.commands;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Remove the reference to a given entity. Jcr Identifier of the reference node
 * to remove and a flag indicating if we should also remove its parent must be
 * passed as parameters.
 */
public class DeleteEntity extends AbstractHandler {
	private final static Log log = LogFactory.getLog(DeleteEntity.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".deleteEntity";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_TOREMOVE_JCR_ID = "param.toRemoveJcrId";
	/**
	 * in many cases, edited node is the versionable child of a more generic
	 * node. Use this parameter to also remove it
	 */
	public final static String PARAM_REMOVE_ALSO_PARENT = "param.removeParent";

	/* DEPENDENCY INJECTION */
	private Repository repository;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		String msg = "You are about to definitively remove this entity.\n"
				+ "Are you sure you want to proceed ?";

		Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event)
				.getShell();
		boolean result = MessageDialog.openConfirm(activeShell,
				"Confirm Deletion", msg);

		if (!result)
			return null;

		IWorkbenchPage iwp = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage();
		IEditorPart iep = iwp.getActiveEditor();

		String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);
		String tmpStr = event.getParameter(PARAM_REMOVE_ALSO_PARENT);
		boolean removeParent = false;
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			removeParent = new Boolean(tmpStr);

		Session session = null;
		Node toRemoveNode = null;
		Node parentVersionableNode = null;
		try {
			session = repository.login();
			toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);

			if (removeParent)
				toRemoveNode = toRemoveNode.getParent();

			boolean wasCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(toRemoveNode);
			if (!wasCheckedOut)
				CommonsJcrUtils.checkout(toRemoveNode);

			boolean parentWasCheckout = true;

			parentVersionableNode = getParentVersionableNode(toRemoveNode);
			if (parentVersionableNode != null) {
				parentWasCheckout = CommonsJcrUtils
						.isNodeCheckedOutByMe(parentVersionableNode);
				if (!parentWasCheckout)
					CommonsJcrUtils.checkout(parentVersionableNode);
			}

			JcrUtils.discardUnderlyingSessionQuietly(toRemoveNode);

			toRemoveNode.remove();
			if (!parentWasCheckout)
				CommonsJcrUtils.saveAndCheckin(parentVersionableNode);
			else
				session.save();

			if (iep != null
					&& iep.getEditorInput().getName().equals(toRemoveJcrId))
				iwp.closeEditor(iep, false);

			session.save();
		} catch (ReferentialIntegrityException e) {
			MessageDialog
					.openError(
							activeShell,
							"Delete impossible",
							"Current contact cannot be removed, it is "
									+ "still being referenced in some activities or as participant"
									+ " in a project or organisation. Remove corresponding links andtry again.");
			if (log.isDebugEnabled())
				e.printStackTrace();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to delete node " + toRemoveNode,
					e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	// TODO workaround to retrieve parent versionable node.
	private Node getParentVersionableNode(Node node) throws RepositoryException {
		Node curr = node;
		while (true) {
			try {
				curr = curr.getParent();
			} catch (ItemNotFoundException infe) {
				// root node
				return null;
			}

			if (curr.isNodeType(NodeType.MIX_VERSIONABLE))
				return curr;
		}
	}

}
