package org.argeo.connect.streams.web.providers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.web.StreamsWebStyles;
import org.argeo.connect.streams.web.utils.StreamsJcrUtils;

/**
 * Some helper methods to generate HTML snippets and provide styled labels that
 * are supported by RAP
 */
public class RssHtmlProvider implements RssNames {

	private final static Log log = LogFactory.getLog(RssHtmlProvider.class);
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy");
	private final static int SHORT_TITLE_LENGHT = 40;
	@SuppressWarnings("unused")
	private final static int SHORT_DESCRIPTION_LENGHT = 140;

	public static String getChannelShort(Node node) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(StreamsJcrUtils.get(node, Property.JCR_TITLE));
		builder.append("</b>");
		builder.append("<br/>");
		builder.append(getLastUpdateSnippet(node));
		builder.append("<br/>");
		builder.append(RssHtmlProvider.getTags(node));
		builder.append("<br/>");

		// String desc = StreamsJcrUtils.get(node,
		// Property.JCR_DESCRIPTION);
		// if (desc != null) {
		// builder.append(desc);
		// }
		return builder.toString();
	}

	public static String getChannelTitle(Node node) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		builder.append("<b><big>");
		builder.append(StreamsJcrUtils.get(node, Property.JCR_TITLE));
		builder.append("</big></b><br/>");
		String desc = StreamsJcrUtils.get(node, Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(desc);
		}
		return builder.toString();
	}

	public static String getChannelMedium(Node node) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		builder.append("<b><big>");
		builder.append(StreamsJcrUtils.get(node, Property.JCR_TITLE));
		builder.append("</big></b><br/>");
		builder.append(RssHtmlProvider.getTags(node));
		builder.append("<br/>");
		String desc = StreamsJcrUtils.get(node, Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(desc);
		}
		return builder.toString();
	}

	public static String getItemShort(Node node) {
		StringBuilder builder = new StringBuilder();
		String link = StreamsJcrUtils.get(node, RSS_LINK);

		builder.append("<span style=\"float:left;padding:0px;white-space:pre-wrap;\"> ");
		builder.append("<a " + StreamsWebStyles.CSS_URL_STYLE + " ");
		builder.append("href=\"");
		builder.append(cleanLink(link));
		builder.append("\"").append(" target=\"_blank\" ").append(">");
		String title = StreamsJcrUtils.get(node, Property.JCR_TITLE);
		builder.append(title);
		builder.append("</a>");

		builder.append("<br/>");
		String desc = StreamsJcrUtils.get(node, Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append(cleanDescription(desc));
		}
		builder.append("</span>");
		String result = builder.toString();
		if (log.isTraceEnabled())
			log.debug("Cleaned snippet: " + result);
		return result;
	}

	public static String getItemMedium(Node node) {
		StringBuilder builder = new StringBuilder();
		String link = StreamsJcrUtils.get(node, RSS_LINK);
		// TODO add image management
		builder.append("<span> <big> <a ")
				.append(StreamsWebStyles.CSS_URL_STYLE).append(" ");
		builder.append("href=\"");
		builder.append(cleanLink(link));
		builder.append("\"").append(" target=\"_blank\" ").append(">");

		String title = StreamsJcrUtils.get(node, Property.JCR_TITLE);
		builder.append(title);
		builder.append("</a>");
		builder.append("</big> </span>");

		// Add tags
		builder.append(" Published on ");
		builder.append(RssHtmlProvider.getPubDate(node));
		builder.append("<br/>");
		builder.append(RssHtmlProvider.getTags(node));

		String desc = StreamsJcrUtils.get(node, Property.JCR_DESCRIPTION);
		if (desc != null) {
			builder.append("<span style=\"float:left;padding:0px;white-space:pre-wrap;\" >");
			int index = desc.indexOf("<");
			if (index > 0)
				desc = desc.substring(0, desc.indexOf("<"));

			// builder.append(shortenString(desc, SHORT_DESCRIPTION_LENGHT));
			builder.append(desc);
			builder.append("</span>");

		}
		return builder.toString();
	}

	/** shortcut to set form data while dealing with switching panel */
	public static String getPubDate(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.hasProperty(RSS_PUB_DATE)) {
				builder.append(df.format(entity.getProperty(RSS_PUB_DATE)
						.getDate().getTime()));
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot create organizations content", re);
		}
	}

	/** shortcut to set form data while dealing with switching panel */
	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.isNodeType(NodeType.MIX_VERSIONABLE)) {
				builder.append("<small><i>").append("Last updated on ");
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
			throw new ArgeoException("Cannot create organizations content", re);
		}
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTags(Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(RSS_CATEGORY)) {
				for (Value value : entity.getProperty(RSS_CATEGORY).getValues()) {
					tags.append("<i>");
					String currVal = value.getString().replaceAll("&", "&amp;");
					tags.append("#").append(currVal).append(" ");
					tags.append("</i>");
				}

			}
			return tags.toString();
		} catch (RepositoryException e) {
			throw new ArgeoException("Error while getting tags for entity", e);
		}
	}

	// TODO clean this
	@SuppressWarnings("unused")
	private final static String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>";
	@SuppressWarnings("unused")
	private final static String XML_SUFFIX = "</xml>";

	public static String cleanLink(String string) {
		string = StringEscapeUtils.escapeXml(string.trim());
		return string;
	}

	public static String cleanDescription(String string) {
		// InputStream is = null;
		try {
			// int index = string.indexOf("<");
			// if (index > 0)
			// string = string.substring(0, index);

			// string = string.replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;");
			// string = string.replaceAll("&nbsp;", "&#160;");
			// string = XML_PREFIX + string + XML_SUFFIX;
			// SAXBuilder builder = new SAXBuilder(false);
			// is = new ByteArrayInputStream(string.getBytes("utf-8"));
			// Document doc = (Document) builder.build(new InputSource(is));
			// Element rootNode = doc.getRootElement();
			// String result = rootNode.getText();
			// result = StringEscapeUtils.escapeXml(result.trim());
			// return result;

			// string = StringEscapeUtils.escapeXml(string.trim());
			return string;

		} catch (Exception ex) {
			throw new ArgeoException("Unable to parse and clean description",
					ex);
		}
	}

	@SuppressWarnings("unused")
	private static String shortenString(String string, int lenght) {
		if (string.length() > SHORT_TITLE_LENGHT)
			return string.substring(0, lenght) + "...";
		else
			return string;
	}

}