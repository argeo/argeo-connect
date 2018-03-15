package org.argeo.connect.util;

public class ConnectUtils {

	/** Checks whether a string is neither null nor empty */
	public static boolean notEmpty(String stringToTest) {
		return !isEmpty(stringToTest);
	}

	/** Checks whether a string is null or empty */
	public static boolean isEmpty(String stringToTest) {
		return stringToTest == null || "".equals(stringToTest.trim());
	}

	/** Singleton constructor */
	private ConnectUtils() {

	}

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;");
		return value;
	}
}
