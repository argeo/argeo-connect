package org.argeo.connect.people.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.jcr.JcrUtils;

/** Some static utils methods that might be factorized in a near future */
public class CommonsJcrUtils {

	/**
	 * Call {@link Repository#login()} without exceptions (useful in super
	 * constructors).
	 */
	public static Session login(Repository repository) {
		try {
			return repository.login();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to login", re);
		}
	}

	/**
	 * Convert a {@link rowIterator} to a list of {@link Node} given a selector
	 * name. It relies on the <code>Row.getNode(String selectorName)</code>
	 * method.
	 */
	public static List<Node> rowIteratorToList(RowIterator rowIterator,
			String selectorName) throws RepositoryException {
		List<Node> nodes = new ArrayList<Node>();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.nextRow();
			if (row.getNode(selectorName) != null)
				nodes.add(row.getNode(selectorName));
		}
		return nodes;
	}

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
	 * It also checked if the current entity has to be moved or not. TODO : add
	 * management of check out by others.
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
	 * Wraps the versionMananger.checkedIn(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static void cancelAndCheckin(Node node) {
		try {
			String path = node.getPath();
			Session session = node.getSession();
			JcrUtils.discardUnderlyingSessionQuietly(node);
			// if the node has never been saved, it does not exist anymore.
			if (session.nodeExists(path))
				session.getWorkspace().getVersionManager().checkin(path);
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
	 * Concisely get the string value of a property. Contrary to
	 * <code>CommonsJcrUtils.getStringValue()</code>, returns an empty String
	 * rather than null if this node doesn't have this property or if the
	 * corresponding property is an empty string. Useful in the read only label
	 * providers.
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

	/**
	 * Concisely get the identifier of a node in Ui listener for instance
	 * */
	public static String getIdentifierQuietly(Node node) {
		try {
			return node.getIdentifier();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get identifier for node " + node,
					e);
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

	/**
	 * Concisely get the node for the translation of a property given a language
	 * and the rel path to corresponding alternative properties.
	 */
	public static Node getAltPropertyNode(Node parent, String relPath,
			String lang) {
		try {
			if (!parent.hasNode(relPath))
				return null;
			else {
				parent = parent.getNode(relPath);
				if (parent.hasNode(lang))
					return parent.getNode(lang);
				else
					return null;
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get alt property for " + lang, e);
		}
	}

	/**
	 * Returns the corresponding sub node for the given language. Create such a
	 * Node if it does not yet exist. By default it is a nt:unstructured node
	 * with mix:title mixin
	 * */
	public static Node getOrCreateAltLanguageNode(Node node, String lang) {
		try {
			Node child = JcrUtils.mkdirs(node.getSession(), node.getPath()
					+ "/" + PeopleNames.PEOPLE_ALT_LANGS + "/" + lang,
					NodeType.NT_UNSTRUCTURED, NodeType.NT_UNSTRUCTURED, false);
			child.addMixin(NodeType.MIX_TITLE);
			child.addMixin(NodeType.MIX_LANGUAGE);
			child.setProperty(Property.JCR_LANGUAGE, lang);
			return child;
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot create child for language "
					+ lang, e);
		}
	}

	private CommonsJcrUtils() {
	}

}
