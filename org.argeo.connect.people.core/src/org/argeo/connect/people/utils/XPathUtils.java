package org.argeo.connect.people.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.RepositoryException;

public class XPathUtils {

	public static String descendantFrom(String parentPath) {
		if (CommonsJcrUtils.checkNotEmptyString(parentPath)) {
			if ("/".equals(parentPath))
				parentPath = "";
			return "/jcr:root" + parentPath;
		} else
			return "";
	}

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

	public static String getPropertyContains(String propertyName, String filter)
			throws RepositoryException {
		if (CommonsJcrUtils.checkNotEmptyString(filter))
			return "jcr:contains(@" + propertyName + ",'*" + filter + "*')";
		return "";
	}

	private final static DateFormat jcrRefFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ss.SSS'+02:00'");

	public static String getPropertyDateComparaison(String propertyName,
			Calendar cal, String lowerOrGreater) throws RepositoryException {
		if (cal != null) {
			String jcrDateStr = jcrRefFormatter.format(cal.getTime());

			// jcrDateStr = "2015-08-03T05:00:03:000Z";
			String result = "@" + propertyName + " " + lowerOrGreater
					+ " xs:dateTime('" + jcrDateStr + "')";
			return result;
		}
		return "";
	}

	public static String getPropertyEquals(String propertyName, String value)
			throws RepositoryException {
		if (CommonsJcrUtils.checkNotEmptyString(value))
			return "@" + propertyName + "='" + value + "'";
		return "";
	}

	public static void andAppend(StringBuilder builder, String condition) {
		if (CommonsJcrUtils.checkNotEmptyString(condition)) {
			builder.append(condition);
			builder.append(" and ");
		}
	}

	public static void appendAndPropStringCondition(StringBuilder builder,
			String propertyName, String filter) throws RepositoryException {
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			andAppend(builder, getPropertyContains(propertyName, filter));
		}
	}

	public static void appendAndNotPropStringCondition(StringBuilder builder,
			String propertyName, String filter) throws RepositoryException {
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			String cond = getPropertyContains(propertyName, filter);
			builder.append(xPathNot(cond));
			builder.append(" and ");
		}
	}

}
