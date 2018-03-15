package org.argeo.people.workbench.rap.util;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.workbench.PeopleWorkbenchService;
import org.argeo.people.workbench.rap.commands.EditJob;
import org.argeo.people.workbench.rap.commands.RemoveEntityReference;

/** Some helper methods to generate HTML snippet */
public class PeopleRapSnippets {

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</code> from a cell of a HTML list.
	 */
	public static String getRemoveReferenceSnippetForLists(Node linkNode) {
		String toRemoveJcrId = ConnectJcrUtils.getIdentifier(linkNode);
		String href = RemoveEntityReference.ID + "/" + RemoveEntityReference.PARAM_TOREMOVE_JCR_ID + "="
				+ toRemoveJcrId;
		return ConnectUiSnippets.getRWTLink(href, ConnectUiConstants.CRUD_DELETE);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditJob</code> command from a cell of a HTML list
	 */
	public static String getEditJobSnippetForLists(Node relevantNode, boolean isBackward) {
		String toEditJcrId = ConnectJcrUtils.getIdentifier(relevantNode);
		String href = EditJob.ID + "/" + EditJob.PARAM_RELEVANT_NODE_JCR_ID + "=" + toEditJcrId + "/"
				+ EditJob.PARAM_IS_BACKWARD + "=" + isBackward;
		return ConnectUiSnippets.getRWTLink(href, ConnectUiConstants.CRUD_EDIT);
	}

	public static String getClickableEntityContact(PeopleService peopleService, Node entity, String label,
			String openCmdId) {
		try {
			// local cache
			Node person = null, org = null;

			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				person = entity;
				Node currContact = PeopleJcrUtils.getPrimaryContact(person, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
				if (!(currContact == null || !currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))) {
					org = peopleService.getEntityByUid(ConnectJcrUtils.getSession(currContact), null,
							ConnectJcrUtils.get(currContact, PeopleNames.PEOPLE_REF_UID));
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				org = entity;

			StringBuilder builder = new StringBuilder();

			builder.append("<b>");
			if (notEmpty(label))
				builder.append(label);
			if (org != null) {
				String title = ConnectJcrUtils.get(org, Property.JCR_TITLE);
				String snippet = ConnectWorkbenchUtils.getOpenEditorSnippet(openCmdId, org, title);
				builder.append(snippet).append("<br/>");
			}
			builder.append("</b>");
			if (person != null) {
				String title = peopleService.getDisplayName(person);
				String snippet = ConnectWorkbenchUtils.getOpenEditorSnippet(openCmdId, person, title);
				builder.append(snippet).append("<br/>");
			}

			String pam = PeopleUiSnippets.getEntityPhoneAndMailFormatted(entity);
			if (notEmpty(pam))
				builder.append(pam);
			return ConnectUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create contact snippet for node " + entity, re);
		}
	}

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddressForList(ResourcesService resourceService,
			PeopleWorkbenchService peopleWorkbenchService, Node contactNode, Node referencedEntity) {
		StringBuilder builder = new StringBuilder();
		// the referenced org
		if (referencedEntity != null) {
			String label = ConnectWorkbenchUtils.getOpenEditorSnippet(peopleWorkbenchService.getOpenEntityEditorCmdId(),
					referencedEntity, ConnectJcrUtils.get(referencedEntity, Property.JCR_TITLE));
			builder.append(label);
		}
		// current contact meta data
		builder.append(PeopleUiSnippets.getContactMetaData(contactNode));
		// Referenced org primary address
		if (referencedEntity != null) {
			Node primaryAddress = PeopleJcrUtils.getPrimaryContact(referencedEntity, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
			if (primaryAddress != null) {
				builder.append("<br />");
				builder.append(PeopleUiSnippets.getAddressDisplayValue(resourceService, primaryAddress));
			}
		}
		return ConnectUtils.replaceAmpersand(builder.toString());
	}

	private PeopleRapSnippets() {
	}
}
