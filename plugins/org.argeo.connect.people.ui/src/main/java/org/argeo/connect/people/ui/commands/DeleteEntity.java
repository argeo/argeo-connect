package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Remove the reference to a given entity. Jcr Identifier of the reference node
 * to remove and  a flag indicating if we should also remove its parent must be passed as
 * parameters.
 */
public class DeleteEntity extends AbstractHandler {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".deleteEntity";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_TOREMOVE_JCR_ID = "param.toRemoveJcrId";
	/**
	 * in many cases, edited node is the versionable child of a more generic
	 * node. Use this parameter to also remove it
	 */
	public final static String PARAM_REMOVE_ALSO_PARENT = "param.removeParent";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String msg = "You are about to definitively remove this entity.\n"
				+ "Are you sure you want to proceed ?";

		boolean result = MessageDialog.openConfirm(PeopleUiPlugin.getDefault()
				.getWorkbench().getDisplay().getActiveShell(),
				"Confirm Deletion", msg);

		if (!result)
			return null;

		String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);
		String tmpStr = event.getParameter(PARAM_REMOVE_ALSO_PARENT);
		boolean removeParent = false;
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			removeParent = new Boolean(tmpStr);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			Node toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);
			boolean wasCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(toRemoveNode);
			if (!wasCheckedOut)
				CommonsJcrUtils.checkout(toRemoveNode);

			JcrUtils.discardUnderlyingSessionQuietly(toRemoveNode);

			IWorkbenchPage iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage();
			IEditorPart iep = iwp.getActiveEditor();
			if (iep != null
					&& iep.getEditorInput().getName().equals(toRemoveJcrId))
				iwp.closeEditor(iep, false);

			if (removeParent)
				toRemoveNode.getParent().remove();
			else
				toRemoveNode.remove();
			session.save();
		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening "
					+ "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
