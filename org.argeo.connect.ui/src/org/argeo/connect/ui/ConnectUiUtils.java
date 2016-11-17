package org.argeo.connect.ui;

/** Helper methods for various People UIs */
public class ConnectUiUtils {
	// private final static Log log = LogFactory.getLog(ConnectUiUtils.class);

	private static String AMPERSAND = "&#38;";

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", AMPERSAND);
		return value;
	}
}