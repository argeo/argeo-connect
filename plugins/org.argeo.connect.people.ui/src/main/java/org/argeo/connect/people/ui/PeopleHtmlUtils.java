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
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;

/** Some helper methods to generate html snippets */
public class PeopleHtmlUtils {
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy");

	/** shortcut to set form data while dealing with switching panel */
	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.isNodeType(NodeType.MIX_VERSIONABLE)) {
				builder.append("<br/><br/><small><i>").append(
						"Last updated on ");
				builder.append(df.format(entity
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");
				builder.append(entity
						.getProperty(Property.JCR_LAST_MODIFIED_BY).getString());
				builder.append("</i></small>");
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

		if (CommonsJcrUtils.checkNotEmptyString(salutation)
				|| CommonsJcrUtils.checkNotEmptyString(title)
				|| CommonsJcrUtils.checkNotEmptyString(suffix)) {
			StringBuilder builder = new StringBuilder();
			// builder.append("<i>");
			builder.append(salutation);
			if (CommonsJcrUtils.checkNotEmptyString(title))
				builder.append(" ").append(title);
			builder.append(" ").append(firstName);
			builder.append(" ").append(lastName);
			builder.append(" ").append(suffix);
			// builder.append("</i>");
			return builder.toString();
		} // useless otherwise
		else
			return "";
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
	// TODO: primary is not yet implemented
	public static String getPrimaryContacts(Node entity, boolean smallList) {
		StringBuilder builder = new StringBuilder();
		// Small is too small for small lists
		if (!smallList)
			builder.append("<small>");

		String tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_PHONE);
		if (tmpStr != null) {
			builder.append("<span> ");
			builder.append(cleanHtmlString(tmpStr));
			builder.append(" </span> ~ ");
		}

		tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_EMAIL);
		if (tmpStr != null) {
			builder.append("<span> <a "
					+ PeopleUiConstants.PEOPLE_CSS_URL_STYLE
					+ " href=\"mailto:");
			builder.append(tmpStr).append("\">");
			builder.append(tmpStr);
			builder.append("</a> </span> ~ ");
		}
		tmpStr = PeopleJcrUtils.getDefaultContactValue(entity,
				PeopleTypes.PEOPLE_URL);
		if (tmpStr != null) {
			builder.append("<span> <a "
					+ PeopleUiConstants.PEOPLE_CSS_URL_STYLE
					+ " href=\"http://");
			builder.append(cleanHtmlString(tmpStr)).append("\"")
					.append(" target=\"_blank\" ").append(">");
			builder.append(tmpStr);
			builder.append("</a> </span> ~ ");
		}

		if (builder.lastIndexOf("~") > 0)
			builder.deleteCharAt(builder.lastIndexOf("~"));
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
}