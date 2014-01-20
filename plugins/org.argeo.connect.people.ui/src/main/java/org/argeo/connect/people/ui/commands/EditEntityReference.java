package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
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
 * Opens the dialog to edit reference to a given entity. Jcr Identifier of the
 * reference node to edit and its nearest versionable ancestor must be passed as
 * parameters.
 */
public class EditEntityReference extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(EditEntityReference.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".editEntityReference";
	public final static String DEFAULT_LABEL = "Edit";
	public final static String PARAM_VERSIONABLE_PARENT_JCR_ID = "param.versionableParentJcrId";
	public final static String PARAM_TOEDIT_JCR_ID = "param.toEditJcrId";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String versParentJcrId = event
				.getParameter(PARAM_VERSIONABLE_PARENT_JCR_ID);
		String toEditJcrId = event.getParameter(PARAM_TOEDIT_JCR_ID);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			Node versionableParent = session
					.getNodeByIdentifier(versParentJcrId);
			Node toEditNode = session.getNodeByIdentifier(toEditJcrId);

			boolean wasCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(versionableParent);
			if (!wasCheckedOut)
				CommonsJcrUtils.checkout(versionableParent);

			MessageDialog.openConfirm(PeopleUiPlugin.getDefault()
					.getWorkbench().getDisplay().getActiveShell(), "Edit",
					"Implement edition");

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
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
