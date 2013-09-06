package org.argeo.connect.streams.ui.providers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.streams.RssNames;

/** Some helper methods to generate html snippets */

public class RssHtmlProvider implements RssNames {
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy");
	private final static int SHORT_TITLE_LENGHT = 40;
	private final static int SHORT_DESCRIPTION_LENGHT = 140;

	public static String getChannelShort(Node node) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.getStringValue(node, Property.JCR_TITLE));
		builder.append("</b>");
		builder.append("<br/>");
		builder.append(RssHtmlProvider.getTags(node));
		builder.append("<br/>");

		// String desc = CommonsJcrUtils.getStringValue(node,
		// Property.JCR_DESCRIPTION);
		// if (desc != null) {
		// builder.append(desc);
		// }
		return builder.toString();
	}
	
	public static String getChannelMedium(Node node) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.getStringValue(node, Property.JCR_TITLE));
		builder.append("</b>");
		builder.append("<br/>");
		builder.append(RssHtmlProvider.getTags(node));
		builder.append("<br/>");

		String desc = CommonsJcrUtils.getStringValue(node,
				Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(desc);
		}
		return builder.toString();
	}

	public static String getItemShort(Node node) {
		StringBuilder builder = new StringBuilder();
		String link = CommonsJcrUtils.getStringValue(node, RSS_LINK);
		builder.append("<span> <big> <a "
				+ PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\"");
		builder.append(link).append("\"").append(" target=\"_blank\" ")
				.append(">");
		builder.append(CommonsJcrUtils.getStringValue(node, Property.JCR_TITLE));
		builder.append("</a></big>");
		builder.append("<br/>");
		builder.append(RssHtmlProvider.getPubDate(node));
		builder.append("<br/>");
		String desc = CommonsJcrUtils.getStringValue(node,
				Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(desc);
		}
		builder.append("</span>");
		return builder.toString();
	}

	public static String getItemMedium(Node node) {
		StringBuilder builder = new StringBuilder();
		String link = PeopleJcrUtils.getDefaultContactValue(node, RSS_LINK);
		// TODO add image management
		builder.append("<span> <big> <a "
				+ PeopleUiConstants.PEOPLE_CSS_URL_STYLE + " href=\"");
		builder.append(link).append("\"").append(" target=\"_blank\" ")
				.append(">");
		builder.append(CommonsJcrUtils.getStringValue(node, Property.JCR_TITLE));
		builder.append("</a></big> </span>");
		builder.append("<br/>");

		// Add tags
		builder.append(" Published on ");
		builder.append(RssHtmlProvider.getPubDate(node));
		builder.append("<br/>");

		String desc = CommonsJcrUtils.getStringValue(node,
				Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(shortenString(desc, SHORT_DESCRIPTION_LENGHT));
		}
		return builder.toString();
	}

	/** shortcut to set form data while dealing with switching panel */
	public static String getPubDate(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.hasProperty(RSS_PUB_DATE)) {
				builder.append("<small><i>");
				builder.append(df.format(entity.getProperty(RSS_PUB_DATE)
						.getDate().getTime()));
				builder.append("</i></small>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot create organizations content", re);
		}
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTags(Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty("prop")) {
				tags.append("<i>");
				for (Value value : entity.getProperty(("proo")).getValues())
					tags.append("#").append(value.getString()).append(" ");
				tags.append("</i>");
			}
			return tags.toString();
		} catch (RepositoryException e) {
			throw new ArgeoException("Error while getting tags for entity", e);
		}
	}

	private static String shortenString(String string, int lenght) {
		if (string.length() > SHORT_TITLE_LENGHT)
			return string.substring(0, lenght) + "...";
		else
			return string;
	}

}
