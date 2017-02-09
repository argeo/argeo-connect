package org.argeo.connect.people.workbench.rap;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.commands.EditJob;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.workbench.rap.commands.RemoveEntityReference;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;

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
				Node currContact = PeopleJcrUtils.getPrimaryContact(person, PeopleTypes.PEOPLE_ADDRESS);
				if (!(currContact == null || !currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))) {
					org = peopleService.getEntityByUid(ConnectJcrUtils.getSession(currContact),
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
				String snippet = getOpenEditorSnippet(openCmdId, org, title);
				builder.append(snippet).append("<br/>");
			}
			builder.append("</b>");
			if (person != null) {
				String title = peopleService.getDisplayName(person);
				String snippet = getOpenEditorSnippet(openCmdId, person, title);
				builder.append(snippet).append("<br/>");
			}

			String pam = PeopleUiSnippets.getEntityPhoneAndMailFormatted(entity);
			if (notEmpty(pam))
				builder.append(pam);
			return ConnectUiUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create contact snippet for node " + entity, re);
		}
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>OpenEditor</code> command from a cell of a HTML list
	 */
	public static String getOpenEditorSnippet(String commandId, Node relevantNode, String value) {
		String toEditJcrId = ConnectJcrUtils.getIdentifier(relevantNode);
		String href = commandId + "/" + OpenEntityEditor.PARAM_JCR_ID + "=" + toEditJcrId;
		return ConnectUiSnippets.getRWTLink(href, value);
	}

	/**
	 * a snippet to display clickable tags that are linked to the current entity
	 */
	public static String getTags(PeopleService peopleService, PeopleWorkbenchService peopleWorkbenchService,
			Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
				for (Value value : entity.getProperty((PeopleNames.PEOPLE_TAGS)).getValues())
					tags.append("#").append(getTagLink(ConnectJcrUtils.getSession(entity), peopleService,
							peopleWorkbenchService, PeopleConstants.RESOURCE_TAG, value.getString())).append("  ");
			}
			return ConnectUiUtils.replaceAmpersand(tags.toString());
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for entity", e);
		}
	}

	/**
	 * Generate a href link that will call the openEntityEditor Command for this
	 * tag if it is already registered. The corresponding Label / List must have
	 * a HtmlRWTAdapter to catch when the user click on the link
	 */
	public static String getTagLink(Session session, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, String tagId, String value) {
		String commandId = peopleWorkbenchService.getOpenEntityEditorCmdId();
		Node tag = peopleService.getResourceService().getRegisteredTag(session, tagId, value);
		if (tag == null)
			return value;
		String tagJcrId = ConnectJcrUtils.getIdentifier(tag);
		String href = commandId + ConnectUiConstants.HREF_SEPARATOR;
		href += OpenEntityEditor.PARAM_JCR_ID + "=" + tagJcrId;
		return ConnectUiSnippets.getRWTLink(href, value);
	}

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddressForList(PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node contactNode, Node referencedEntity) {
		StringBuilder builder = new StringBuilder();
		// the referenced org
		if (referencedEntity != null) {
			String label = PeopleRapSnippets.getOpenEditorSnippet(peopleWorkbenchService.getOpenEntityEditorCmdId(),
					referencedEntity, ConnectJcrUtils.get(referencedEntity, Property.JCR_TITLE));
			builder.append(label);
		}
		// current contact meta data
		builder.append(PeopleUiSnippets.getContactMetaData(contactNode));
		// Referenced org primary address
		if (referencedEntity != null) {
			Node primaryAddress = PeopleJcrUtils.getPrimaryContact(referencedEntity, PeopleTypes.PEOPLE_ADDRESS);
			if (primaryAddress != null) {
				builder.append("<br />");
				builder.append(PeopleUiSnippets.getAddressDisplayValue(peopleService, primaryAddress));
			}
		}
		return ConnectUiUtils.replaceAmpersand(builder.toString());
	}
	
	private PeopleRapSnippets() {
	}
}
