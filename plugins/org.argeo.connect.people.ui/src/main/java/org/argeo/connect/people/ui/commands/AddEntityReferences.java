package org.argeo.connect.people.ui.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.CreateEntityRefWithPositionDialog;
import org.argeo.connect.people.ui.editors.AbstractEntityEditor;
import org.argeo.connect.people.ui.wizards.AddEntityReferenceWizard;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens a wizard to add references to one or more entities to a given Parent
 * Node. The parent node Jcr Identifier must be passed as parameter. The type of
 * object that can then added depend either of the node type referencing node or
 * of the name of its parent.
 */
@Deprecated
public class AddEntityReferences extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(AddEntityReferences.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".addEntityReferences";
	public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleUiPlugin
			.getImageDescriptor("icons/add.png");
	public final static String DEFAULT_LABEL = "Add...";
	public final static String PARAM_REFERENCING_JCR_ID = "param.referencingJcrId";
	public final static String PARAM_REFERENCED_JCR_ID = "param.referencedJcrId";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String referencingJcrId = event.getParameter(PARAM_REFERENCING_JCR_ID);
		String referencedJcrId = event.getParameter(PARAM_REFERENCED_JCR_ID);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			Node referencing = null;
			if (referencingJcrId != null)
				referencing = session.getNodeByIdentifier(referencingJcrId);

			Node referenced = null;
			if (referencedJcrId != null)
				referenced = session.getNodeByIdentifier(referencedJcrId);

			AddEntityReferenceWizard wizard = getCorrespondingWizard(
					referencing, referenced);

			if (wizard == null) {
				Dialog diag = new CreateEntityRefWithPositionDialog(
						HandlerUtil.getActiveShell(event), "Create position",
						peopleService, referencing, referenced,
						PeopleTypes.PEOPLE_ORGANIZATION);
				diag.open();
				return null;
			}

			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), wizard);
			int result = dialog.open();

			if (result == WizardDialog.OK) {
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

	protected AddEntityReferenceWizard getCorrespondingWizard(Node referencing,
			Node referenced) throws RepositoryException {
		if (referencing != null) {
			if (referencing.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return null;
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
