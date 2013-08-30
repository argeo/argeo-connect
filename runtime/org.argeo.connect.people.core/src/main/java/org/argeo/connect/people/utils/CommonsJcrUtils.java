package org.argeo.connect.people.utils;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;

/** Some static utils methods that might be factorized in a near future */
public class CommonsJcrUtils {

	/**
	 * Check if a string is null or an empty string (a string with only spaces
	 * is considered as empty
	 */
	public static boolean isEmptyString(String stringToTest) {
		return stringToTest == null || "".equals(stringToTest.trim());
	}

	/**
	 * Check if a string is null or an empty string (a string with only spaces
	 * is considered as empty
	 */
	public static boolean checkNotEmptyString(String string) {
		return string != null && !"".equals(string.trim());
	}

	/**
	 * Workaround for bug 158
	 * 
	 * @deprecated
	 * @param nodeType
	 *            the type of the leaf node
	 */
	public static Node mkdirs(Node parentNode, String relativePath,
			String nodeType) {
		return mkdirs(parentNode, relativePath, nodeType, null);
	}

	/**
	 * Workaround for bug 158
	 * 
	 * @deprecated
	 * 
	 * @param nodeType
	 *            the type of the leaf node
	 */
	public static Node mkdirs(Node parentNode, String relativePath,
			String nodeType, String intermediaryNodeType) {
		List<String> tokens = org.argeo.jcr.JcrUtils.tokenize(relativePath);
		Node currParent = parentNode;
		try {
			for (int i = 0; i < tokens.size(); i++) {
				String name = tokens.get(i);
				if (currParent.hasNode(name)) {
					currParent = currParent.getNode(name);
				} else {
					if (i != (tokens.size() - 1)) {// intermediary
						currParent = currParent.addNode(name,
								intermediaryNodeType);
					} else {// leaf
						currParent = currParent.addNode(name, nodeType);
					}
				}
			}
			return currParent;
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot mkdirs relative path "
					+ relativePath + " from " + parentNode, e);
		}
	}

	/**
	 * Parse and trim a String of values
	 */
	public static String[] parseAndClean(String string, String regExp,
			boolean clean) {
		String[] temp = string.split(regExp);
		if (clean) {
			String[] cleanRes = new String[temp.length];
			int i = 0;
			for (String tag : temp) {
				cleanRes[i] = tag.trim();
				i++;
			}
			return cleanRes;
		}
		return temp;
	}

	/**
	 * Clean a String to remove or replace chars that are known to be
	 * problematic
	 */
	public static String cleanString(String string) {
		String cleanStr = string;
		cleanStr.replaceAll("&", "&amp;");
		return cleanStr;
	}

	/**
	 * Concisely get the string value of a property or null if this node doesn't
	 * have this property or if the corresponding property is an empty string
	 */
	public static String getStringValue(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName)
					|| "".equals(node.getProperty(propertyName).getString()
							.trim()))
				return null;
			else
				return node.getProperty(propertyName).getString();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get property " + propertyName
					+ " of " + node, e);
		}
	}

	/**
	 * Concisely get the value of a property or null if this node doesn't have
	 * this property
	 */
	public static Boolean getBooleanValue(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return null;
			else
				return node.getProperty(propertyName).getBoolean();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get boolean property "
					+ propertyName + " of " + node, e);
		}
	}

}
