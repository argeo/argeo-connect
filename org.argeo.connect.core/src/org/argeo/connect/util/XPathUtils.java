package org.argeo.connect.util;

import static org.argeo.connect.util.JcrUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.util.ISO9075;

/** Ease XPath generation for JCR requests */
public class XPathUtils {
	private final static Log log = LogFactory.getLog(XPathUtils.class);

	public static String descendantFrom(String parentPath) {
		if (notEmpty(parentPath)) {
			if ("/".equals(parentPath))
				parentPath = "";
			// Hardcoded dependency to Jackrabbit. Remove
			String result = "/jcr:root" + ISO9075.encodePath(parentPath);
			if (log.isTraceEnabled()) {
				String result2 = "/jcr:root" + parentPath;
				if (!result2.equals(result))
					log.warn("Encoded Path " + result2 + " --> " + result);
			}
			return result;
		} else
			return "";
	}

	public static String localAnd(String... conditions) {
		StringBuilder builder = new StringBuilder();
		for (String condition : conditions) {
			if (notEmpty(condition)) {
				builder.append(" ").append(condition).append(" and ");
			}
		}
		if (builder.length() > 3)
			return builder.substring(0, builder.length() - 4);
		else
			return "";
	}

	public static String xPathNot(String condition) {
		if (notEmpty(condition))
			return "not(" + condition + ")";
		else
			return "";
	}

	public static String getFreeTextConstraint(String filter) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		if (notEmpty(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				builder.append("jcr:contains(.,'*" + token + "*') and ");
			}
			return builder.substring(0, builder.length() - 4);
		}
		return "";
	}

	public static String getPropertyContains(String propertyName, String filter) throws RepositoryException {
		if (notEmpty(filter))
			return "jcr:contains(@" + propertyName + ",'*" + filter + "*')";
		return "";
	}

	private final static DateFormat jcrRefFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'+02:00'");

	public static String getPropertyDateComparaison(String propertyName, Calendar cal, String lowerOrGreater)
			throws RepositoryException {
		if (cal != null) {
			String jcrDateStr = jcrRefFormatter.format(cal.getTime());

			// jcrDateStr = "2015-08-03T05:00:03:000Z";
			String result = "@" + propertyName + " " + lowerOrGreater + " xs:dateTime('" + jcrDateStr + "')";
			return result;
		}
		return "";
	}

	public static String getPropertyEquals(String propertyName, String value) {
		if (notEmpty(value))
			return "@" + propertyName + "='" + encodeXPathStringValue(value) + "'";
		return "";
	}

	private static String encodeXPathStringValue(String propertyValue) {
		// TODO implement safer mechanism to escape invalid characters
		// Also check why we have used this regex in ResourceSerrviceImpl l 474
		// String cleanedKey = key.replaceAll("(?:')", "''");
		String result = propertyValue.replaceAll("'", "''");
		return result;
	}

	public static void andAppend(StringBuilder builder, String condition) {
		if (notEmpty(condition)) {
			builder.append(condition);
			builder.append(" and ");
		}
	}

	public static void appendOrderByProperties(StringBuilder builder, boolean ascending, String... propertyNames) {
		if (propertyNames.length > 0) {
			builder.append(" order by ");
			for (String propName : propertyNames)
				builder.append("@").append(propName).append(", ");
			builder = builder.delete(builder.length() - 2, builder.length());
			if (ascending)
				builder.append(" ascending ");
			else
				builder.append(" descending ");
		}
	}

	public static void appendAndPropStringCondition(StringBuilder builder, String propertyName, String filter)
			throws RepositoryException {
		if (notEmpty(filter)) {
			andAppend(builder, getPropertyContains(propertyName, filter));
		}
	}

	public static void appendAndNotPropStringCondition(StringBuilder builder, String propertyName, String filter)
			throws RepositoryException {
		if (notEmpty(filter)) {
			String cond = getPropertyContains(propertyName, filter);
			builder.append(xPathNot(cond));
			builder.append(" and ");
		}
	}
}
