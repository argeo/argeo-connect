package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.jcr.JcrUtils;

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
	 * Wraps the versionMananger.isCheckedOut(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static boolean isNodeCheckedOut(Node node) {
		try {
			return node.getSession().getWorkspace().getVersionManager()
					.isCheckedOut(node.getPath());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to get check out status for node", re);
		}
	}

	/**
	 * For the time being, same as isNodeCheckedOut(Node node).
	 * 
	 * TODO : add management of check out by others.
	 */
	public static boolean isNodeCheckedOutByMe(Node node) {
		return isNodeCheckedOut(node);
	}

	/**
	 * Wraps the versionMananger.checkedOut(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static void checkout(Node node) {
		try {
			node.getSession().getWorkspace().getVersionManager()
					.checkout(node.getPath());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to check out Node", re);
		}
	}

	/**
	 * Wraps the versionMananger.checkedIn(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static void saveAndCheckin(Node node) {
		try {
			JcrUtils.updateLastModified(node);
			node.getSession().save();
			node.getSession().getWorkspace().getVersionManager()
					.checkin(node.getPath());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to save and chek in node", re);
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
	 * Concisely get the string value of a property. Contrary to <code>CommonsJcrUtils.getStringValue()</code>, returns an empty String rather than null if this node doesn't
	 * have this property or if the corresponding property is an empty string. Useful in the read only label providers.
	 */
	public static String get(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return "";
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

	public static Node getOrCreateDirNode(Node parent, String dirName)
			throws RepositoryException {
		Node dirNode;
		if (parent.hasNode(dirName))
			dirNode = parent.getNode(dirName);
		else
			dirNode = parent.addNode(dirName, NodeType.NT_UNSTRUCTURED);
		return dirNode;
	}

}
