package org.argeo.connect.people.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.PropertyDiff;

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
	 * Call {@link Node#getSession()} without exceptions
	 */
	public static Session getSession(Node node) {
		try {
			return node.getSession();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve session for node "
					+ node, re);
		}
	}

	/**
	 * Call {@link Node#isNodetype(String nodeTypeName)} without exceptions
	 */
	public static boolean isNodeType(Node node, String nodeTypeName) {
		try {
			return node.isNodeType(nodeTypeName);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to test NodeType " + nodeTypeName
					+ " for node " + node, re);
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
	 * works around missing method to test if a node has been removed from
	 * existing session
	 * 
	 * @param node
	 * @return
	 */
	public static boolean nodeStillExists(Node node) {
		try {
			node.getPath();
		} catch (InvalidItemStateException iise) {
			return false;
		} catch (RepositoryException re) {
			throw new PeopleException("Error while testing node existence", re);
		}
		return true;
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

	/* MULTIPLE VALUES MANAGEMENT */

	/**
	 * Concisely get a string that concat values of a multi-valued String
	 * property. returns an empty String rather than null if this node doesn't
	 * have this property or if the corresponding property is an empty string.
	 * Useful in the read only label providers. Caller might define a
	 * concatenation sign, otherwise a semi-colon and a space are used
	 */
	public static String getMultiAsString(Node node, String propertyName,
			String separator) {
		try {

			if (separator == null)
				separator = "; ";
			// original language & lenght
			if (!node.hasProperty(propertyName))
				return "";
			else {
				Value[] langs = node.getProperty(propertyName).getValues();
				StringBuilder builder = new StringBuilder();
				for (Value val : langs) {
					String currStr = val.getString();
					if (CommonsJcrUtils.checkNotEmptyString(currStr))
						builder.append(currStr).append(separator);
				}
				if (builder.lastIndexOf(separator) >= 0)
					return builder.substring(0, builder.length() - 2);
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get multi valued property "
					+ propertyName + " of " + node, e);
		}
	}

	/**
	 * Sets a property of type REFERENCE that is multiple. Overrides any already
	 * defined value of this property
	 */
	public static void setMultipleReferences(Node node, String propertyName,
			List<Node> nodes) throws RepositoryException {
		ValueFactory vFactory = node.getSession().getValueFactory();
		int size = nodes.size();
		Value[] values = new Value[size];
		int i = 0;
		for (Node currNode : nodes) {
			Value val = vFactory.createValue(currNode.getIdentifier(),
					PropertyType.REFERENCE);
			values[i++] = val;
		}
		node.setProperty(propertyName, values);
	}

	/** Remove a Reference from a multi valued property */
	public static void removeRefFromMultiValuedProp(Node node, String propName,
			String identifier) {
		try {
			Session session = node.getSession();
			List<Node> nodes = new ArrayList<Node>();
			Value[] values = node.getProperty(propName).getValues();
			for (int i = 0; i < values.length; i++) {
				String curr = values[i].getString();
				if (!identifier.equals(curr))
					nodes.add(session.getNodeByIdentifier(curr));
			}
			setMultipleReferences(node, propName, nodes);
			node.setProperty(propName, nodes.toArray(new String[0]));
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to remove reference from property " + propName
							+ " of Node " + node, e);
		}
	}

	public static String addRefToMultiValuedProp(Node node, String propName,
			Node nodeToReference) {
		try {
			Session session = node.getSession();
			Value[] values;
			List<Node> nodes = new ArrayList<Node>();
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check dupplicate
				for (Value currValue : values) {
					String jcrId = currValue.getString();
					if (nodeToReference.getIdentifier().equals(jcrId)) {
						errMsg = CommonsJcrUtils.get(nodeToReference,
								Property.JCR_TITLE)
								+ " is already in the list and thus could not be added.";
						return errMsg;
					} else
						nodes.add(session.getNodeByIdentifier(jcrId));
				}
			}
			nodes.add(nodeToReference);
			setMultipleReferences(node, propName, nodes);
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to add reference ", re);
		}
	}

	public static String addStringToMultiValuedProp(Node node, String propName,
			String value) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check dupplicate
				for (Value jcrId : values) {
					String currRef = jcrId.getString();
					if (value.equals(currRef)) {
						errMsg = CommonsJcrUtils
								.get(node.getSession().getNodeByIdentifier(
										value), Property.JCR_TITLE)
								+ " is already in the list and thus could not be added.";
						return errMsg;
					}
				}

				valuesStr = new String[values.length + 1];
				int i;
				for (i = 0; i < values.length; i++) {
					valuesStr[i] = values[i].getString();
				}
				valuesStr[i] = value;
			} else {
				valuesStr = new String[1];
				valuesStr[0] = value;
			}
			node.setProperty(propName, valuesStr);
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set tags", re);
		}
	}

	/**
	 * Concisely get the value of a boolean property or null if this node
	 * doesn't have this property
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

	public static Map<String, PropertyDiff> diffProperties(Node reference,
			Node observed) {
		Map<String, PropertyDiff> diffs = new TreeMap<String, PropertyDiff>();
		diffPropertiesLevel(diffs, null, reference, observed);
		return diffs;
	}

	/**
	 * Compare the properties of two nodes. Extends
	 * <code>JcrUtils.diffPropertiesLevel</code> to also track differences in
	 * multiple value properties and sub graph. No property is skipped (among
	 * other all technical jcr:... properties) to be able to track jcr:title and
	 * description properties, among other. Filtering must be applied afterwards
	 * to only keep relevant properties.
	 */
	static void diffPropertiesLevel(Map<String, PropertyDiff> diffs,
			String baseRelPath, Node reference, Node observed) {
		try {
			// check removed and modified
			PropertyIterator pit = reference.getProperties();
			props: while (pit.hasNext()) {
				Property p = pit.nextProperty();
				String name = p.getName();
				// if (name.startsWith("jcr:"))
				// continue props;

				if (!observed.hasProperty(name)) {
					String relPath = propertyRelPath(baseRelPath, name);
					PropertyDiff pDiff = new PropertyDiff(PropertyDiff.REMOVED,
							relPath, p.getValue(), null);
					diffs.put(relPath, pDiff);
				} else {
					if (p.isMultiple()) {
						int i = 0;

						Value[] refValues = p.getValues();
						Value[] newValues = observed.getProperty(name)
								.getValues();
						String relPath = propertyRelPath(baseRelPath, name);
						refValues: for (Value refValue : refValues) {
							for (Value newValue : newValues) {
								if (refValue.equals(newValue))
									continue refValues;
							}
							PropertyDiff pDiff = new PropertyDiff(
									PropertyDiff.REMOVED, relPath, refValue,
									null);
							diffs.put(relPath + "_" + i++, pDiff);
						}

						newValues: for (Value newValue : newValues) {
							for (Value refValue : refValues) {
								if (refValue.equals(newValue))
									continue newValues;
							}
							PropertyDiff pDiff = new PropertyDiff(
									PropertyDiff.ADDED, relPath, null, newValue);
							diffs.put(relPath + "_" + i++, pDiff);
						}

					} else {
						Value referenceValue = p.getValue();
						Value newValue = observed.getProperty(name).getValue();
						if (!referenceValue.equals(newValue)) {
							String relPath = propertyRelPath(baseRelPath, name);
							PropertyDiff pDiff = new PropertyDiff(
									PropertyDiff.MODIFIED, relPath,
									referenceValue, newValue);
							diffs.put(relPath, pDiff);
						}
					}
				}
			}
			// check added
			pit = observed.getProperties();
			// props:
			while (pit.hasNext()) {
				Property p = pit.nextProperty();
				String name = p.getName();
				// if (name.startsWith("jcr:"))
				// continue props;
				if (!reference.hasProperty(name)) {
					String relPath = propertyRelPath(baseRelPath, name);
					if (p.isMultiple()) {
						Value[] newValues = observed.getProperty(name)
								.getValues();
						int i = 0;
						for (Value newValue : newValues) {
							PropertyDiff pDiff = new PropertyDiff(
									PropertyDiff.ADDED, relPath, null, newValue);
							diffs.put(relPath + "_" + i++, pDiff);
						}
					} else {
						PropertyDiff pDiff = new PropertyDiff(
								PropertyDiff.ADDED, relPath, null, p.getValue());
						diffs.put(relPath, pDiff);
					}
				}
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot diff " + reference + " and "
					+ observed, e);
		}
	}

	/** Builds a property relPath to be used in the diff. */
	private static String propertyRelPath(String baseRelPath,
			String propertyName) {
		if (baseRelPath == null)
			return propertyName;
		else
			return baseRelPath + '/' + propertyName;
	}

}
