package org.argeo.connect.people.rap.commands;

import java.util.Calendar;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleUiPlugin;
import org.argeo.connect.people.rap.PeopleUiService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Creates a new entity under draft path of the current repository and opens the
 * corresponding editor. The Node type of the relevant entity must be passed as
 * parameter.
 */
public class CreateEntity extends AbstractHandler {
	// private final static Log log = LogFactory.getLog(CreateEntity.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".createEntity";
	public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleUiPlugin
			.getImageDescriptor("icons/add.png");
	public final static String DEFAULT_LABEL = "Create";
	public final static String PARAM_TARGET_NODE_TYPE = "param.targetNodeType";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String nodeType = event.getParameter(PARAM_TARGET_NODE_TYPE);

		Session session = null;
		try {
			session = repository.login();
			String draftPath = peopleService.getTmpPath();
			String datePath = JcrUtils.dateAsPath(Calendar.getInstance(), true);

			Node parent = JcrUtils.mkdirs(session, draftPath + "/" + datePath);
			Node newNode = parent.addNode(nodeType, nodeType);
			String uuid = UUID.randomUUID().toString();
			newNode.setProperty(PeopleNames.PEOPLE_UID, uuid);
			// newNode.setProperty(PeopleNames.PEOPLE_IS_DRAFT, true);

			Wizard wizard = peopleUiService.getCreationWizard(peopleService,
					newNode);
			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), wizard);
			dialog.setTitle("New...");
			int result = dialog.open();
			if (result == WizardDialog.OK) {
				String jcrId = newNode.getIdentifier();
				CommandUtils.callCommand(
						peopleUiService.getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, jcrId);
			} else
				// remove tmp Node
				CommonsJcrUtils.cancelAndCheckin(newNode);

		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening "
					+ "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	// expose to children classes
	protected Repository getRepository() {
		return repository;
	}

	protected PeopleUiService getPeopleUiService() {
		return peopleUiService;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}