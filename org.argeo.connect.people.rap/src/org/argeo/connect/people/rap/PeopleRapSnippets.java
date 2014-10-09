package org.argeo.connect.people.rap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.commands.DeleteEntity;
import org.argeo.connect.people.rap.commands.EditEntityReference;
import org.argeo.connect.people.rap.commands.EditEntityReferenceWithPosition;
import org.argeo.connect.people.rap.commands.EditJob;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.commands.RemoveEntityReference;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;

/** Some helper methods to generate html snippets */
public class PeopleRapSnippets {

	/**
	 * Simply formats a couple href / label to display a link in a markup
	 * enabled tree / table / label that will trigger a corresponding RWT
	 * specific listener. Such a listener must be able to understand the
	 * specific format of the value of this href attribute
	 */
	public static String getFormattedLinkSnippet(String href, String value) {
		// TODO remove the hard coded fixed CSS style
		return "<a " + PeopleRapConstants.PEOPLE_STYLE_LINK + " href=\"" + href
				+ "\" target=\"_rwt\">" + value + "</a>";
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveReferenceSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		String toRemoveJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String versionableParJcrId = CommonsJcrUtils
				.getIdentifier(parentVersionableNode);
		String href = RemoveEntityReference.ID + "/"
				+ RemoveEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID + "="
				+ versionableParJcrId + "/"
				+ RemoveEntityReference.PARAM_TOREMOVE_JCR_ID + "="
				+ toRemoveJcrId;
		return getFormattedLinkSnippet(href, PeopleUiConstants.CRUD_DELETE);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveSnippetForLists(Node currNode,
			boolean removeParent) {
		String toRemoveJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String href = DeleteEntity.ID + "/"
				+ DeleteEntity.PARAM_TOREMOVE_JCR_ID + "=" + toRemoveJcrId
				+ "/" + DeleteEntity.PARAM_REMOVE_ALSO_PARENT + "="
				+ removeParent;
		return getFormattedLinkSnippet(href, PeopleUiConstants.CRUD_DELETE);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReference</command> from a cell of a HTML list
	 */
	public static String getEditSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String versionableParJcrId = CommonsJcrUtils
				.getIdentifier(parentVersionableNode);
		String href = EditEntityReference.ID + "/"
				+ EditEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID + "="
				+ versionableParJcrId + "/"
				+ EditEntityReference.PARAM_TOEDIT_JCR_ID + "=" + toEditJcrId;
		return getFormattedLinkSnippet(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditJob</code> command from a cell of a HTML list
	 */
	public static String getEditJobSnippetForLists(Node relevantNode,
			boolean isBackward) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(relevantNode);
		String href = EditJob.ID + "/" + EditJob.PUBLIC_RELEVANT_NODE_JCR_ID
				+ "=" + toEditJcrId + "/" + EditJob.PARAM_IS_BACKWARD + "="
				+ isBackward;
		return getFormattedLinkSnippet(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReferenceWithPosition</command> from a cell of a HTML list
	 */
	public static String getEditWithPosSnippetForLists(Node linkNode,
			boolean isBackward, String toSearchNodeType) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(linkNode);

		String href = EditEntityReferenceWithPosition.ID + "/"
				+ EditEntityReferenceWithPosition.PARAM_OLD_LINK_JCR_ID + "="
				+ toEditJcrId + "/"
				+ EditEntityReferenceWithPosition.PARAM_IS_BACKWARD + "="
				+ isBackward + "/"
				+ EditEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE
				+ "=" + toSearchNodeType;
		return getFormattedLinkSnippet(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>OpenEditor</code> command from a cell of a HTML list
	 */
	public static String getOpenEditorSnippet(String commandId,
			Node relevantNode, String value) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(relevantNode);
		String href = commandId + "/" + OpenEntityEditor.PARAM_JCR_ID + "="
				+ toEditJcrId;
		return getFormattedLinkSnippet(href, value);
	}

	// TODO finalize removal of the below methods that have been moved to
	// PeopleUiSnippets class.

	/**
	 * 
	 * @param entity
	 * @param label
	 *            an optional label to be displayed first
	 * @return
	 */
	public static String getEntityContactSnippet(PeopleService peopleService,
			Node entity, String label) {
		return PeopleUiSnippets.getEntityContact(peopleService, entity, label);

		// try {
		// // local cache
		// Node person = null, org = null;
		//
		// if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
		// person = entity;
		// Node currContact = PeopleJcrUtils.getPrimaryContact(person,
		// PeopleTypes.PEOPLE_ADDRESS);
		// if (!(currContact == null || !currContact
		// .isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))) {
		// org = peopleService.getEntityByUid(CommonsJcrUtils
		// .getSession(currContact), CommonsJcrUtils.get(
		// currContact, PeopleNames.PEOPLE_REF_UID));
		// }
		// } else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
		// org = entity;
		//
		// StringBuilder builder = new StringBuilder();
		// // builder.append("<span>");
		//
		// builder.append("<b>");
		// if (CommonsJcrUtils.checkNotEmptyString(label))
		// builder.append(label);
		// if (org != null)
		// builder.append(CommonsJcrUtils.get(org, Property.JCR_TITLE))
		// .append("<br/>");
		// builder.append("</b>");
		// if (person != null)
		// builder.append(peopleService.getDisplayName(person)).append(
		// "<br/>");
		//
		// // phone
		// String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
		// PeopleTypes.PEOPLE_PHONE);
		// if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
		// builder.append(tmpStr);
		// builder.append("<br/>");
		// }
		//
		// // mail
		// tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
		// PeopleTypes.PEOPLE_EMAIL);
		// if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
		// builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK
		// + " href=\"mailto:");
		// builder.append(tmpStr).append("\">");
		// builder.append(tmpStr).append("</a><br/>");
		// }
		// // builder.append("</span>");
		//
		// return PeopleUiUtils.replaceAmpersand(builder.toString());
		//
		// } catch (RepositoryException re) {
		// throw new PeopleException(
		// "Unable to create contact snippet for node " + entity, re);
		// }
	}

	/** creates the display ReadOnly HTML snippet for various contacts */
	public static String getContactDisplaySnippet(PeopleService peopleService,
			Node node) {
		return PeopleUiSnippets.getContactDisplaySnippet(peopleService, node);
		//
		// try {
		// StringBuilder builder = new StringBuilder();
		//
		// if (node.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
		// builder.append(PeopleUiUtils
		// .replaceAmpersand(getAddressDisplayValue(peopleService,
		// node)));
		// } else {
		// String value = CommonsJcrUtils.get(node,
		// PeopleNames.PEOPLE_CONTACT_VALUE);
		// if (node.isNodeType(PeopleTypes.PEOPLE_URL)
		// || node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)) {
		// builder.append(getUrlLink(value));
		// } else if (node.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
		// builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK);
		// builder.append(" href=\"mailto:" + value + "\">" + value
		// + " </a>");
		// } else if (node.isNodeType(PeopleTypes.PEOPLE_IMPP)
		// || node.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
		// builder.append(value);
		// }
		// }
		// builder.append(getContactMetaData(node));
		// return builder.toString();
		//
		// } catch (RepositoryException re) {
		// throw new PeopleException("Error while generating contact "
		// + "display HTML snippet ", re);
		// }
	}

	/**
	 * creates the ReadOnly HTML snippet to display in a label with styling
	 * enabled in order to provide a clickable link
	 */
	public static String getUrlLink(String value) {
		return getUrlLink(value, value);
	}

	/**
	 * creates the ReadOnly HTML snippet to display in a label with styling
	 * enabled in order to provide a clickable link
	 */
	public static String getUrlLink(String value, String label) {
		StringBuilder builder = new StringBuilder();

		value = PeopleUiUtils.replaceAmpersand(value);
		if (!(value.startsWith("http://") || value.startsWith("https://")))
			value = "http://" + value;
		builder.append("<a ").append(PeopleRapConstants.PEOPLE_STYLE_LINK)
				.append(" href=\"");
		builder.append(value + "\" target=\"_blank\" >" + label + "</a>");
		return builder.toString();
	}

	// /**
	// * creates the ReadOnly HTML snippet to display in a label with styling
	// * enabled in order to provide a clickable link
	// */
	// public static String getWebsiteSnippet(String address) {
	// StringBuilder builder = new StringBuilder();
	//
	// String value = PeopleUiUtils.replaceAmpersand(address);
	// builder.append("<a ").append(PeopleRapConstants.PEOPLE_STYLE_LINK)
	// .append(" href=\"http://");
	// builder.append(value + "\" target=\"_blank\" >" + value + "</a>");
	// return builder.toString();
	// }

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddressDisplaySnippet(
			PeopleService peopleService, Node contactNode, Node referencedEntity) {
		return PeopleUiSnippets.getWorkAddress(peopleService, contactNode,
				referencedEntity);

		// StringBuilder builder = new StringBuilder();
		// // the referenced org
		// if (referencedEntity != null)
		// builder.append(PeopleUiUtils.replaceAmpersand(CommonsJcrUtils.get(
		// referencedEntity, Property.JCR_TITLE)));
		// // current contact meta data
		// builder.append(getContactMetaData(contactNode));
		// // Referenced org primary address
		// if (referencedEntity != null) {
		// Node primaryAddress = PeopleJcrUtils.getPrimaryContact(
		// referencedEntity, PeopleTypes.PEOPLE_ADDRESS);
		// if (primaryAddress != null) {
		// builder.append("<br />");
		// builder.append(PeopleUiUtils
		// .replaceAmpersand(getAddressDisplayValue(peopleService,
		// primaryAddress)));
		// }
		// }
		// return builder.toString();
	}

	/** creates the display ReadOnly HTML snippet for various contact metadata */
	public static String getContactMetaData(Node node) {
		return PeopleUiSnippets.getContactMetaData(node);
		//
		// StringBuilder builder = new StringBuilder();
		//
		// String nature = PeopleUiUtils.replaceAmpersand(CommonsJcrUtils.get(
		// node, PeopleNames.PEOPLE_CONTACT_NATURE));
		// String category = PeopleUiUtils.replaceAmpersand(CommonsJcrUtils.get(
		// node, PeopleNames.PEOPLE_CONTACT_CATEGORY));
		// String label =
		// PeopleUiUtils.replaceAmpersand(CommonsJcrUtils.get(node,
		// PeopleNames.PEOPLE_CONTACT_LABEL));
		//
		// if (CommonsJcrUtils.checkNotEmptyString(nature)
		// || CommonsJcrUtils.checkNotEmptyString(category)
		// || CommonsJcrUtils.checkNotEmptyString(label)) {
		// builder.append("&#160;&#160;[");
		//
		// if (CommonsJcrUtils.checkNotEmptyString(nature)) {
		// builder.append(nature).append(
		// CommonsJcrUtils.checkNotEmptyString(category) ? " "
		// : "");
		// }
		// if (CommonsJcrUtils.checkNotEmptyString(category)) {
		// builder.append(category);
		// }
		//
		// if (CommonsJcrUtils.checkNotEmptyString(label)) {
		// if (CommonsJcrUtils.checkNotEmptyString(nature)
		// || CommonsJcrUtils.checkNotEmptyString(category))
		// builder.append(", ");
		// builder.append(label);
		// }
		// builder.append("]");
		// }
		// return builder.toString();
	}

	/** creates an address Display value */
	public static String getAddressDisplayValue(PeopleService peopleService,
			Node node) {
		return PeopleUiSnippets.getAddressDisplayValue(peopleService, node);

		// String street = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STREET);
		// String street2 = CommonsJcrUtils.get(node,
		// PeopleNames.PEOPLE_STREET_COMPLEMENT);
		// String zip = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_ZIP_CODE);
		// String city = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_CITY);
		// String state = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STATE);
		// String country = CommonsJcrUtils.get(node,
		// PeopleNames.PEOPLE_COUNTRY);
		//
		// StringBuilder builder = new StringBuilder();
		//
		// if (CommonsJcrUtils.checkNotEmptyString(street))
		// builder.append(street);
		//
		// if (CommonsJcrUtils.checkNotEmptyString(street2)) {
		// if (builder.length() > 0)
		// builder.append(", ");
		// builder.append(street2);
		// }
		//
		// if (CommonsJcrUtils.checkNotEmptyString(zip)) {
		// if (builder.length() > 0)
		// builder.append(", ");
		// builder.append(zip);
		// }
		//
		// if (CommonsJcrUtils.checkNotEmptyString(city)) {
		// if (builder.length() > 0)
		// if (CommonsJcrUtils.checkNotEmptyString(zip))
		// builder.append(" ");
		// else
		// builder.append(", ");
		// builder.append(city);
		// }
		//
		// if (CommonsJcrUtils.checkNotEmptyString(state)) {
		// if (builder.length() > 0)
		// builder.append(", ");
		// builder.append(state);
		// }
		//
		// if (CommonsJcrUtils.checkNotEmptyString(country)) {
		// country = ResourcesJcrUtils.getCountryEnLabelFromIso(peopleService,
		// CommonsJcrUtils.getSession(node), country);
		//
		// if (builder.length() > 0)
		// builder.append(", ");
		// builder.append(country.toUpperCase());
		// }
		// return builder.toString();
	}

	/** creates the localisation snippet */
	public static String getLocalisationInfo(PeopleService peopleService,
			Node entity) {
		String town = PeopleJcrUtils.getTownFromItem(entity);
		String country = PeopleJcrUtils.getCountryFromItem(entity);
		if (CommonsJcrUtils.checkNotEmptyString(town)
				|| CommonsJcrUtils.checkNotEmptyString(country)) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			if (CommonsJcrUtils.checkNotEmptyString(town)) {
				builder.append(town);
				if (!CommonsJcrUtils.isEmptyString(country))
					builder.append(", ");
			}
			if (!CommonsJcrUtils.isEmptyString(country)) {
				country = ResourcesJcrUtils.getCountryEnLabelFromIso(
						peopleService, CommonsJcrUtils.getSession(entity),
						country);
				builder.append(country);
			}
			builder.append("]");
			return builder.toString();
		} else
			return "";
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTags(Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
				for (Value value : entity
						.getProperty((PeopleNames.PEOPLE_TAGS)).getValues())
					tags.append("#")
							.append(PeopleUiUtils.replaceAmpersand(value
									.getString())).append(" ");
			}
			return PeopleUiUtils.replaceAmpersand(tags.toString());
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for entity", e);
		}
	}

	// /** a snippet to display primary contact information for this entity */
	// public static String getPrimaryContacts(Node entity) {
	// StringBuilder builder = new StringBuilder();
	//
	// String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
	// PeopleTypes.PEOPLE_PHONE);
	// if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
	// builder.append(PeopleUiUtils.replaceAmpersand(tmpStr));
	// builder.append("&#160;&#160;&#160;");
	// }
	//
	// tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
	// PeopleTypes.PEOPLE_EMAIL);
	// if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
	// builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK
	// + " href=\"mailto:");
	// builder.append(tmpStr).append("\">");
	// builder.append(tmpStr);
	// builder.append("</a>&#160;&#160;&#160;");
	// }
	// tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
	// PeopleTypes.PEOPLE_URL);
	// if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
	// builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK
	// + " href=\"http://");
	// builder.append(PeopleUiUtils.replaceAmpersand(tmpStr)).append("\"")
	// .append(" target=\"_blank\" ").append(">");
	// builder.append(tmpStr);
	// builder.append("</a>&#160;&#160;&#160;");
	// }
	// return builder.toString();
	// }

	/**
	 * Calls <code>CommonsJcrUtils.get(Node node, String propName)</code> method
	 * and replace any '&' by its html encoding '&amp;' to avoid
	 * <code>IllegalArgumentException</code> while rendering html read only
	 * snippets
	 */
	public static String getHtml(Node node, String propName) {
		String value = CommonsJcrUtils.get(node, propName);
		value = PeopleUiUtils.replaceAmpersand(value);
		return value;
	}

	// /**
	// * Cleans a String by replacing any '&' by its html encoding '&#38;' to
	// * avoid <code>IllegalArgumentException</code> while rendering html
	// * read-only snippets
	// */
	// public static String PeopleUiUtils.replaceAmpersand(String value) {
	// value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;");
	// return value;
	// }

}