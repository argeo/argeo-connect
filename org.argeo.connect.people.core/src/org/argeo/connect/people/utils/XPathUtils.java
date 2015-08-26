package org.argeo.connect.people.utils;

import javax.jcr.RepositoryException;

public class XPathUtils {

	public static String localAnd(String... conditions) {
		StringBuilder builder = new StringBuilder();
		for (String condition : conditions) {
			if (CommonsJcrUtils.checkNotEmptyString(condition)) {
				builder.append(" ").append(condition).append(" and ");
			}
		}
		if (builder.length() > 3)
			return builder.substring(0, builder.length() - 4);
		else
			return "";
	}

	public static String xPathNot(String condition) {
		if (CommonsJcrUtils.checkNotEmptyString(condition))
			return "not(" + condition + ")";
		else
			return "";
	}

	public static String getFreeTextConstraint(String filter)
			throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				builder.append("jcr:contains(.,'*" + token + "*') and ");
			}
			return builder.substring(0, builder.length() - 4);
		}
		return "";
	}

	public static String getPropertyConstraint(String propertyName,
			String filter) throws RepositoryException {
		return "jcr:contains(@" + propertyName + ",'*" + filter + "*')";
	}
}
