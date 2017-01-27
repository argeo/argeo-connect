package org.argeo.connect.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 * Some helper methods to generate html snippets TODO refactor this once the
 * internationalization has been implemented.
 */
public class ConnectUiSnippets {

	/**
	 * Simply formats a couple href / label to display a link in a markup
	 * enabled tree / table / label that will trigger a corresponding RWT
	 * specific listener. Such a listener must be able to understand the
	 * specific format of the value of this href attribute
	 */
	public static String getRWTLink(String href, String value) {
		return "<a href=\"" + href + "\" target=\"_rwt\">" + value + "</a>";
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able phone number
	 */
	public static String getPhoneLink(String value) {
		return getPhoneLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able phone number
	 * 
	 * @param value
	 * @param label
	 *            a potentially distinct label
	 * @return
	 */
	public static String getPhoneLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		builder.append("<a href=\"tel:");
		builder.append(value).append("\" target=\"_blank\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able mail
	 */
	public static String getMailLink(String value) {
		return getMailLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able mail
	 * 
	 * @param value
	 * @param label
	 *            a potentially distinct label
	 * @return
	 */
	public static String getMailLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = ConnectUiUtils.replaceAmpersand(value);
		builder.append("<a href=\"mailto:");
		builder.append(value).append("\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLink(String value) {
		return getUrlLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = ConnectUiUtils.replaceAmpersand(value);
		label = ConnectUiUtils.replaceAmpersand(label);
		if (!(value.startsWith("http://") || value.startsWith("https://")))
			value = "http://" + value;
		builder.append("<a href=\"");
		builder.append(value + "\" target=\"_blank\" >" + label + "</a>");
		return builder.toString();
	}

	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();

		/** shortcut to set form data while dealing with switching panel */
		// private final static
		DateFormat df = new SimpleDateFormat(ConnectUiConstants.DEFAULT_DATE_TIME_FORMAT);

		try {
			if (entity.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("<i>").append("Last updated on ");
				builder.append(df.format(entity.getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime()));
				builder.append(", by ");
				builder.append(entity.getProperty(Property.JCR_LAST_MODIFIED_BY).getString());
				builder.append(". </i>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new RuntimeException("Cannot create organizations content", re);
		}
	}
}
