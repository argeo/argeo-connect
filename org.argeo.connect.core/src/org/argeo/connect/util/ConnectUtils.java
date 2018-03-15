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
}
