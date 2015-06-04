package org.argeo.connect.people.rap.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapPlugin;
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

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".deleteEntity";
	public final static String DEFAULT_LABEL = "Delete";
	public final static String PARAM_TOREMOVE_JCR_ID = "param.toRemoveJcrId";
	// In many cases, the edited node is a versionable child of a more generic
	// node. Use this parameter to also remove it
	public final static String PARAM_REMOVE_ALSO_PARENT = "param.removeParent";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		String toRemoveJcrId = event.getParameter(PARAM_TOREMOVE_JCR_ID);
		String removeParentStr = event.getParameter(PARAM_REMOVE_ALSO_PARENT);

		Session session = null;
		Node toRemoveNode = null;
		Node parentVersionableNode = null;
		Shell activeShell = HandlerUtil.getActiveWorkbenchWindow(event)
				.getShell();
		try {
			session = repository.login();
			toRemoveNode = session.getNodeByIdentifier(toRemoveJcrId);

			if (toRemoveNode == null)
				return null;

			// Sanity check
			if (!canDelete(toRemoveNode, activeShell))
				return null;

			String msg = "You are about to definitively remove this entity.\n"
					+ "Are you sure you want to proceed ?";
			boolean result = MessageDialog.openConfirm(activeShell,
					"Confirm Deletion", msg);

			if (!result)
				return null;

			IWorkbenchPage iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage();
			IEditorPart iep = iwp.getActiveEditor();

			boolean removeParent = false;
			if (CommonsJcrUtils.checkNotEmptyString(removeParentStr))
				removeParent = new Boolean(removeParentStr);

			if (removeParent)
				toRemoveNode = toRemoveNode.getParent();

			if (!CommonsJcrUtils.checkCOStatusBeforeUpdate(toRemoveNode))
				log.warn("To remove node " + toRemoveNode
						+ " was checked in when we wanted to remove it");
			
			parentVersionableNode = CommonsJcrUtils.getParentVersionableNode(toRemoveNode);
			if (parentVersionableNode != null) {
				if (!CommonsJcrUtils.checkCOStatusBeforeUpdate(parentVersionableNode))
					log.warn("Parent versionable node " + parentVersionableNode
							+ " was checked in when we wanted to remove it");
			}

			JcrUtils.discardUnderlyingSessionQuietly(toRemoveNode);
			toRemoveNode.remove();
			if (parentVersionableNode != null)
				CommonsJcrUtils.checkPoint(parentVersionableNode);
			else
				session.save();

			if (iep != null
					&& iep.getEditorInput().getName().equals(toRemoveJcrId))
				iwp.closeEditor(iep, false);

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

	private boolean canDelete(Node node, Shell activeShell)
			throws RepositoryException {
		List<Node> related = new ArrayList<Node>();

		List<Node> tmp = peopleService.getRelatedEntities(node,
				PeopleTypes.PEOPLE_POSITION, PeopleTypes.PEOPLE_BASE);
		if (tmp != null)
			related.addAll(tmp);

		// tmp = peopleService.getRelatedEntities(node, PeopleTypes.PEOPLE_JOB,
		// PeopleTypes.PEOPLE_PERSON);
		// if (tmp != null)
		// related.addAll(tmp);

		PropertyIterator pit = node.getReferences(null);
		while (pit.hasNext()) {
			related.add(pit.nextProperty().getParent());
		}

		if (related.isEmpty())
			return true;
		else {
			String msg = "Current object is referenced by following entities: "
					+ getRelatedAsString(related) + "\n";
			msg += "Please remove these links before trying again to delete.";
			MessageDialog.openError(activeShell, "Deletion forbidden", msg);
			return false;
		}
	}

	private String getRelatedAsString(List<Node> related)
			throws RepositoryException {
		if (related.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		for (Node node : related) {
			if (node.isNodeType(PeopleTypes.PEOPLE_POSITION)) {
				if (node.hasProperty(PeopleNames.PEOPLE_ROLE))
					builder.append(node.getProperty(PeopleNames.PEOPLE_ROLE)
							.getString());
				Node parent = node.getParent().getParent();
				if (parent.hasProperty(Property.JCR_TITLE))
					builder.append(" (")
							.append(parent.getProperty(Property.JCR_TITLE)
									.getString()).append(")");
			} else if (node.hasProperty(Property.JCR_TITLE))
				builder.append(node.getProperty(Property.JCR_TITLE).getString());
			else
				builder.append(node.getName());
			builder.append("; ");
		}
		String result = builder.toString();

		return result.substring(0, result.length() - 2) + ".";
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

}
