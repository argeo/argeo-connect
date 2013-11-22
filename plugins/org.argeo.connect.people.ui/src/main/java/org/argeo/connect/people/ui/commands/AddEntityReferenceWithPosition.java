package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.AbstractEntityEditor;
import org.argeo.connect.people.ui.wizards.AddMLMembersDialog;
import org.argeo.connect.people.ui.wizards.AddMLMembershipDialog;
import org.argeo.connect.people.ui.wizards.CreateEntityRefWithPositionDialog;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens a dialog to add a reference with position between two entities.
 */
@Deprecated
public class AddEntityReferenceWithPosition extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".addEntityReferenceWithPosition";
	public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleUiPlugin
			.getImageDescriptor("icons/add.png");
	public final static String DEFAULT_LABEL = "Add...";
	public final static String PARAM_REFERENCING_JCR_ID = "param.referencingJcrId";
	public final static String PARAM_REFERENCED_JCR_ID = "param.referencedJcrId";
	public final static String PARAM_TO_SEARCH_NODE_TYPE = "param.toSearchNodeType";
	public final static String PARAM_DIALOG_ID = "param.dialogId";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String referencingJcrId = event.getParameter(PARAM_REFERENCING_JCR_ID);
		String referencedJcrId = event.getParameter(PARAM_REFERENCED_JCR_ID);
		String toSearchNodeType = event.getParameter(PARAM_TO_SEARCH_NODE_TYPE);
		String dialogId = event.getParameter(PARAM_DIALOG_ID);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			Node referencing = null;
			if (referencingJcrId != null)
				referencing = session.getNodeByIdentifier(referencingJcrId);

			Node referenced = null;
			if (referencedJcrId != null)
				referenced = session.getNodeByIdentifier(referencedJcrId);

			Dialog diag = null;

			if (PeopleUiConstants.DIALOG_ADD_ML_MEMBERS.equals(dialogId))
				diag = new AddMLMembersDialog(
						HandlerUtil.getActiveShell(event),
						"Add Mailing List Members...", peopleService,
						referencing, new String[] { toSearchNodeType });
			else if (PeopleUiConstants.DIALOG_ADD_ML_MEMBERSHIP
					.equals(dialogId))
				diag = new AddMLMembershipDialog(
						HandlerUtil.getActiveShell(event),
						"Add Mailing List membership", peopleService,
						referenced, new String[] { toSearchNodeType });
			else
				diag = new CreateEntityRefWithPositionDialog(
						HandlerUtil.getActiveShell(event), "Create position",
						peopleService, referencing, referenced,
						toSearchNodeType);

			int result = diag.open();

			if (result == Dialog.OK) {
				IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event)
						.getActivePage().getActiveEditor();
				if (iep != null && iep instanceof AbstractEntityEditor)
					((AbstractEntityEditor) iep).forceRefresh();
			}
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
