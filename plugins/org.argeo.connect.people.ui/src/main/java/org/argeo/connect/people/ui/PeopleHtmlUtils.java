package org.argeo.connect.people.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.commands.DeleteEntity;
import org.argeo.connect.people.ui.commands.EditEntityReference;
import org.argeo.connect.people.ui.commands.EditEntityReferenceWithPosition;
import org.argeo.connect.people.ui.commands.RemoveEntityReference;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;

/** Some helper methods to generate html snippets */
public class PeopleHtmlUtils {
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy 'at' HH:mm");

	/** shortcut to set form data while dealing with switching panel */
	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("<i>").append("Last updated on ");
				builder.append(df.format(entity
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");
				builder.append(entity
						.getProperty(Property.JCR_LAST_MODIFIED_BY).getString());
				builder.append(". </i>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	/** creates a full monty name snippet for overview panels */
	public static String getFullMontyName(Node node) {
		String salutation = getHtml(node, PeopleNames.PEOPLE_SALUTATION);
		String firstName = getHtml(node, PeopleNames.PEOPLE_FIRST_NAME);
		String lastName = getHtml(node, PeopleNames.PEOPLE_LAST_NAME);
		String title = getHtml(node, PeopleNames.PEOPLE_PERSON_TITLE);
		String suffix = getHtml(node, PeopleNames.PEOPLE_NAME_SUFFIX);
		String nickName = getHtml(node, PeopleNames.PEOPLE_NICKNAME);
		String maidenName = getHtml(node, PeopleNames.PEOPLE_MAIDEN_NAME);
		String middleName = getHtml(node, PeopleNames.PEOPLE_MIDDLE_NAME);

		if (CommonsJcrUtils.checkNotEmptyString(salutation)
				|| CommonsJcrUtils.checkNotEmptyString(title)
				|| CommonsJcrUtils.checkNotEmptyString(suffix)
				|| CommonsJcrUtils.checkNotEmptyString(nickName)
				|| CommonsJcrUtils.checkNotEmptyString(maidenName)
				|| CommonsJcrUtils.checkNotEmptyString(middleName)) {
			StringBuilder builder = new StringBuilder();
			// builder.append("<i>");
			builder.append(salutation);
			if (CommonsJcrUtils.checkNotEmptyString(title))
				builder.append(" ").append(title);
			builder.append(" ").append(firstName);
			if (CommonsJcrUtils.checkNotEmptyString(middleName))
				builder.append(" \"").append(middleName).append("\" ");
			builder.append(" ").append(lastName);
			builder.append(" ").append(suffix);

			if (CommonsJcrUtils.checkNotEmptyString(maidenName)
					|| CommonsJcrUtils.checkNotEmptyString(nickName)) {
				builder.append(" <i>(");
				if (CommonsJcrUtils.checkNotEmptyString(maidenName))
					builder.append("Born: ").append(maidenName);

				if (CommonsJcrUtils.checkNotEmptyString(maidenName)
						&& CommonsJcrUtils.checkNotEmptyString(nickName))
					builder.append(", ");
				if (CommonsJcrUtils.checkNotEmptyString(nickName))
					builder.append("aka: ").append(nickName);
				builder.append(")</i>");
			}
			// builder.append("</i>");
			return builder.toString();
		} // useless otherwise
		else
			return "";
	}

	/** creates the display ReadOnly HTML snippet for various contacts */
	public static String getContactDisplaySnippet(Node node, Node entity) {
		try {
			StringBuilder builder = new StringBuilder();

			String nature = null;
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				nature = cleanHtmlString(CommonsJcrUtils.get(node,
						PeopleNames.PEOPLE_CONTACT_NATURE));
			String category = cleanHtmlString(CommonsJcrUtils.get(node,
					PeopleNames.PEOPLE_CONTACT_CATEGORY));
			String label = cleanHtmlString(CommonsJcrUtils.get(node,
					PeopleNames.PEOPLE_CONTACT_LABEL));

			if (node.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
				builder.append(cleanHtmlString(getAddressDisplayValue(node)));
			} else {
				String value = cleanHtmlString(CommonsJcrUtils.get(node,
						PeopleNames.PEOPLE_CONTACT_VALUE));
				if (node.isNodeType(PeopleTypes.PEOPLE_URL)
						|| node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)) {
					builder.append("<a ")
							.append(PeopleUiConstants.PEOPLE_CSS_URL_STYLE)
							.append(" href=\"http://");
					builder.append(value + "\" target=\"_blank\" >" + value
							+ "</a>");
				} else if (node.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
					builder.append("<a "
							+ PeopleUiConstants.PEOPLE_CSS_URL_STYLE);
					builder.append(" href=\"mailto:" + value + "\">" + value
							+ " </a>");
				} else if (node.isNodeType(PeopleTypes.PEOPLE_IMPP)
						|| node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)
						|| node.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
					builder.append(value);
				}
			}

			if (CommonsJcrUtils.checkNotEmptyString(nature)
					|| CommonsJcrUtils.checkNotEmptyString(category)
					|| CommonsJcrUtils.checkNotEmptyString(label)) {
				builder.append("&#160;&#160;[");

				if (CommonsJcrUtils.checkNotEmptyString(nature)) {
					builder.append(nature).append(" ");
				}
				if (CommonsJcrUtils.checkNotEmptyString(category)) {
					builder.append(category);
				}

				if (CommonsJcrUtils.checkNotEmptyString(label)) {
					if (CommonsJcrUtils.checkNotEmptyString(nature)
							|| CommonsJcrUtils.checkNotEmptyString(category))
						builder.append(", ");
					builder.append(label);
				}
				builder.append("]");
			}

			return builder.toString();

		} catch (RepositoryException re) {
			throw new PeopleException("Error while generating contact "
					+ "display HTML snippet ", re);
		}
	}

	/** creates an address Display value */
	public static String getAddressDisplayValue(Node node) {

		String street = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STREET);
		String street2 = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_STREET_COMPLEMENT);
		String zip = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_ZIP_CODE);
		String city = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_CITY);
		String state = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STATE);
		String country = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_COUNTRY);

		StringBuilder builder = new StringBuilder();

		if (CommonsJcrUtils.checkNotEmptyString(street))
			builder.append(street);

		if (CommonsJcrUtils.checkNotEmptyString(street2)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(street2);
		}

		if (CommonsJcrUtils.checkNotEmptyString(zip)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(zip);
		}

		if (CommonsJcrUtils.checkNotEmptyString(city)) {
			if (builder.length() > 0)
				if (CommonsJcrUtils.checkNotEmptyString(zip))
					builder.append(" ");
				else
					builder.append(", ");
			builder.append(city);
		}

		if (CommonsJcrUtils.checkNotEmptyString(state)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(state);
		}

		if (CommonsJcrUtils.checkNotEmptyString(country)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(country.toUpperCase());
		}
		return builder.toString();
	}

	/** creates the localisation snippet */
	public static String getLocalisationInfo(Node entity) {
		String town = PeopleJcrUtils.getTownFromItem(entity);
		String country = PeopleJcrUtils.getCountryFromItem(entity);
		if (CommonsJcrUtils.checkNotEmptyString(town)
				|| CommonsJcrUtils.checkNotEmptyString(country)) {
			StringBuilder builder = new StringBuilder();
			if (CommonsJcrUtils.checkNotEmptyString(town)) {
				builder.append(town);
				if (!CommonsJcrUtils.isEmptyString(country))
					builder.append(", ");
			}
			if (!CommonsJcrUtils.isEmptyString(country))
				builder.append(country);
			return builder.toString();
		} else
			return "";
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTags(Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
				tags.append("<i>");
				for (Value value : entity
						.getProperty((PeopleNames.PEOPLE_TAGS)).getValues())
					tags.append("#").append(cleanHtmlString(value.getString()))
							.append(" ");
				tags.append("</i>");
			}
			return tags.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for entity", e);
		}
	}

	/** a snippet to display primary contact information for this entity */
	public static String getPrimaryContacts(Node entity, boolean smallList) {
		StringBuilder builder = new StringBuilder();
		// Small is too small for small lists
		if (!smallList)
			builder.append("<small>");

		String tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_PHONE);
		if (tmpStr != null) {
			builder.append(cleanHtmlString(tmpStr));
			builder.append("&#160;&#160;&#160;");
		}

		tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_EMAIL);
		if (tmpStr != null) {
			builder.append("<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE
					+ " href=\"mailto:");
			builder.append(tmpStr).append("\">");
			builder.append(tmpStr);
			builder.append("</a>&#160;&#160;&#160;");
		}
		tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_URL);
		if (tmpStr != null) {
			builder.append("<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE
					+ " href=\"http://");
			builder.append(cleanHtmlString(tmpStr)).append("\"")
					.append(" target=\"_blank\" ").append(">");
			builder.append(tmpStr);
			builder.append("</a>&#160;&#160;&#160;");
		}
		if (!smallList)
			builder.append("</small>");
		return builder.toString();
	}

	/**
	 * Calls <code>CommonsJcrUtils.get(Node node, String propName)</code> method
	 * and replace any '&' by its html encoding '&amp;' to avoid
	 * <code>IllegalArgumentException</code> while rendering html read only
	 * snippets
	 */
	public static String getHtml(Node node, String propName) {
		String value = CommonsJcrUtils.get(node, propName);
		value = cleanHtmlString(value);
		return value;
	}

	/**
	 * Cleans a String by replacing any '&' by its html encoding '&#38;' to
	 * avoid <code>IllegalArgumentException</code> while rendering html
	 * read-only snippets
	 */
	public static String cleanHtmlString(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;");
		return value;
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveReferenceSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		try {
			String toRemoveJcrId = currNode.getIdentifier();
			String versionableParJcrId = parentVersionableNode.getIdentifier();

			String uri = RemoveEntityReference.ID + "/"
					+ RemoveEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID
					+ "=" + versionableParJcrId + "/"
					+ RemoveEntityReference.PARAM_TOREMOVE_JCR_ID + "="
					+ toRemoveJcrId;

			return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\""
					+ uri + "\" target=\"_rwt\">Delete</a>";
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Error getting remove snippet for list item", re);
		}
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveSnippetForLists(Node currNode,
			boolean removeParent) {
		try {
			String toRemoveJcrId = currNode.getIdentifier();

			String uri = DeleteEntity.ID + "/"
					+ DeleteEntity.PARAM_TOREMOVE_JCR_ID + "=" + toRemoveJcrId
					+ "/" + DeleteEntity.PARAM_REMOVE_ALSO_PARENT + "="
					+ removeParent;

			return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\""
					+ uri + "\" target=\"_rwt\">Delete</a>";
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Error getting remove snippet for list item", re);
		}
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReference</command> from a cell of a HTML list
	 */
	public static String getEditSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		try {
			String toEditJcrId = currNode.getIdentifier();
			String versionableParJcrId = parentVersionableNode.getIdentifier();

			String uri = EditEntityReference.ID + "/"
					+ EditEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID + "="
					+ versionableParJcrId + "/"
					+ EditEntityReference.PARAM_TOEDIT_JCR_ID + "="
					+ toEditJcrId;

			return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\""
					+ uri + "\" target=\"_rwt\">Edit</a>";
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Error getting remove snippet for list item", re);
		}
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReferenceWithPosition</command> from a cell of a HTML list
	 */
	public static String getEditWithPosSnippetForLists(Node linkNode,
			boolean isBackward, String toSearchNodeType) {
		try {
			String toEditJcrId = linkNode.getIdentifier();

			String uri = EditEntityReferenceWithPosition.ID + "/"
					+ EditEntityReferenceWithPosition.PARAM_OLD_LINK_JCR_ID
					+ "=" + toEditJcrId + "/"
					+ EditEntityReferenceWithPosition.PARAM_IS_BACKWARD + "="
					+ isBackward + "/"
					+ EditEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE
					+ "=" + toSearchNodeType;
			return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\""
					+ uri + "\" target=\"_rwt\">Edit</a>";
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Error getting remove snippet for list item", re);
		}
	}
}