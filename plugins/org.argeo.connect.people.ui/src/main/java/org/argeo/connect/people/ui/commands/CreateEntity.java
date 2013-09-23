package org.argeo.connect.people.ui.commands;

import java.util.Calendar;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Create a new entity under draft path of the current repository and opens the
 * corresponding editor. The Node type of the relevant entity must be passed as
 * parameter.
 */
public class CreateEntity extends AbstractHandler {
	private final static Log log = LogFactory.getLog(CreateEntity.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".createEntity";
	public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleUiPlugin
			.getImageDescriptor("icons/add.png");
	public final static String DEFAULT_LABEL = "Create";
	public final static String PARAM_TARGET_NODE_TYPE = "param.targetNodeType";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {

		String nodeType = event.getParameter(PARAM_TARGET_NODE_TYPE);

		Session session = null;
		try {
			session = peopleService.getRepository().login();
			String draftPath = getDraftBasePath(nodeType);
			String datePath = JcrUtils.dateAsPath(Calendar.getInstance(), true);

			Node parent = JcrUtils.mkdirs(session, draftPath + "/" + datePath);
			Node newNode = parent.addNode(nodeType, nodeType);
			newNode.setProperty(PeopleNames.PEOPLE_UID, UUID.randomUUID()
					.toString());
			newNode.setProperty(Property.JCR_TITLE, "<new>");
			session.save();
			String editorId = peopleUiService.getEditorIdFromNode(newNode);

			log.debug("draftPath - " + draftPath);
			log.debug("datePath - " + datePath);

			EntityEditorInput eei = new EntityEditorInput(
					newNode.getIdentifier());
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(eei, editorId);
		} catch (PartInitException pie) {
			throw new PeopleException("Error while opening editor for the "
					+ "draft entity (NodeType: " + nodeType + ")", pie);
		} catch (RepositoryException e) {
			throw new PeopleException("unexpected JCR error while opening "
					+ "editor for newly created programm", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/**
	 * Overwrite to provide the relevant draft base path for the current
	 * application
	 */
	protected String getDraftBasePath(String nodeType)
			throws RepositoryException {
		// TODO implement this in a People only context
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}
