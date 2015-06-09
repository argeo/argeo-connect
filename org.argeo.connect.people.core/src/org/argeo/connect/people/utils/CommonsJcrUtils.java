package org.argeo.connect.people.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;
import javax.jcr.version.VersionManager;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.jcr.JcrUtils;

/** Some static utilities methods that might be factorized in a near future */
public class CommonsJcrUtils {
	// private final static Log log = LogFactory.getLog(CommonsJcrUtils.class);

	/*
	 * Encapsulate some commons JCR calls with the try/catch block in order to
	 * simplify the code
	 */

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

	/** Centralizes exception management to call {@link Node#getSession()} */
	public static Session getSession(Node node) {
		try {
			return node.getSession();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve session for node "
					+ node, re);
		}
	}

	/** Centralizes exception management to call {@link Node#getIdentifier()} */
	public static String getIdentifier(Node node) {
		try {
			return node.getIdentifier();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve identifier for node "
					+ node, re);
		}
	}

	/** Centralizes exception management to call {@link Node#getPath()} */
	public static String getPath(Node node) {
		try {
			return node.getPath();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve path for node "
					+ node, re);
		}
	}

	/** Shortcut to manage case where parentPath is "/" (parent is root) */
	public static String getAbsPath(String parPath, String nodeName) {
		String absPath = null;
		if ("/".equals(parPath))
			absPath = "/" + nodeName;
		else
			absPath = parPath + "/" + nodeName;
		return absPath;
	}

	/**
	 * Centralizes exception management to call
	 * {@link Session#getNodeByIdentifier(String)}
	 */
	public static Node getNodeByIdentifier(Session session, String identifier) {
		try {
			return session.getNodeByIdentifier(identifier);
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to retrieve node by identifier with " + identifier,
					re);
		}
	}

	/** Centralizes exception management to call {@link Node#getParent()} */
	public static Node getParent(Node child) {
		try {
			return child.getParent();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to retrieve parent node for "
					+ child, re);
		}
	}

	/**
	 * Centralizes exception management to call
	 * {@link Session#getNodeByIdentifier(String)}. The session is retrieves
	 * using the passed node
	 */
	public static Node getNodeByIdentifier(Node sessionNode, String identifier) {
		return getNodeByIdentifier(getSession(sessionNode), identifier);
	}

	/**
	 * Call {@link Row#getNode()} catching {@link RepositoryException}
	 */
	public static Node getNode(Row row, String selectorName) {
		try {
			return row.getNode(selectorName);
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to retrieve Node with selector name "
							+ selectorName + " on " + row, re);
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
	 * Wraps the versionMananger.isCheckedOut(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static boolean isNodeCheckedOut(Node node) {
		try {
			if (!node.isNodeType(NodeType.MIX_VERSIONABLE)) // "mix:versionable")
				return true;
			else
				return node.getSession().getWorkspace().getVersionManager()
						.isCheckedOut(node.getPath());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to get check out status for node " + node, re);
		}
	}

	/**
	 * Returns the versionable node in the parent path, this if it is
	 * versionable or null if no-one is versionnable including root node.
	 */
	public static Node getVersionableAncestor(Node node) {
		try {
			if (node.isNodeType(NodeType.MIX_VERSIONABLE)) // "mix:versionable"
				return node;
			else if (node.getPath().equals("/"))
				return null;
			else
				return getVersionableAncestor(node.getParent());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to get check out status for node " + node, re);
		}
	}

	/**
	 * Works around missing method to test if a node has been removed from
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
	 * Concisely check out a node.
	 * 
	 * TODO : add management of check out by others.
	 */
	private static void checkout(Node node) {
		try {
			node.getSession().getWorkspace().getVersionManager()
					.checkout(node.getPath());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to check out node  " + node, re);
		}
	}

	/** Simply check if a node is versionable */
	public static boolean isVersionable(Node node) {
		try {
			return node.isNodeType(NodeType.MIX_VERSIONABLE);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to test versionability  of "
					+ node, re);
		}
	}

	/**
	 * Replace the generic namespace with the local "jcr:" value. It is a
	 * workaround that must be later cleaned
	 */
	public static String getLocalJcrItemName(String name) {
		String jcr = "{http://www.jcp.org/jcr/1.0}";
		String nt = "{http://www.jcp.org/jcr/nt/1.0}";
		if (name.startsWith(jcr))
			return "jcr:" + name.substring(jcr.length());
		else if (name.startsWith(nt))
			return "nt:" + name.substring(nt.length());
		else
			throw new PeopleException("Unknown prefix for " + name);
	}

	/* VERSIONING MANAGEMENT */
	/**
	 * For the time being, same as isNodeCheckedOut(Node node).
	 * 
	 * TODO : add management of check out by others.
	 */
	public static boolean isNodeCheckedOutByMe(Node node) {
		return isNodeCheckedOut(node);
	}

	/**
	 * Make a version snapshot of the current state of the given versionable
	 * node. It wraps a JCR save and checkPoint methods
	 */
	public static void checkPoint(Node node) {
		try {
			JcrUtils.updateLastModified(node);
			node.getSession().save();
			VersionManager vm = node.getSession().getWorkspace()
					.getVersionManager();
			String path = node.getPath();
			vm.checkpoint(path);
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to save and chek in node " + node, re);
		}
	}

	/** Concisely save the underlying session if some changes have been done */
	public static boolean save(Node node) {
		return save(node, false);
	}

	/**
	 * Concisely save the underlying session if some changes have been done and
	 * make a version tag if asked and if the node is already versionable
	 */
	public static boolean save(Node node, boolean tagVersion) {
		try {
			Session adminSession = node.getSession();
			if (adminSession.hasPendingChanges()) {
				adminSession.save();
				if (tagVersion && isVersionable(node))
					checkPoint(node);
				return true;
			}
			return false;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to save session  for " + node, e);
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
			if (!node.isCheckedOut())
				// versionable & already published, nothing to do.
				return;
			if (node.isNodeType(NodeType.MIX_VERSIONABLE)) {
				JcrUtils.updateLastModified(node);
				node.getSession().save();
				node.getSession().getWorkspace().getVersionManager()
						.checkin(node.getPath());
			} else {
				// JcrUtils.updateLastModified(node);
				node.getSession().save();
			}
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to save and chek in node " + node, re);
		}
	}

	/**
	 * Simplify the save strategy keeping the check-in status unchanged. Goes
	 * together with <code>checkCOStatusAfterUpdate</code>
	 */
	public static boolean checkCOStatusBeforeUpdate(Node node) {
		boolean wasCheckedOut = isNodeCheckedOutByMe(node);
		if (!wasCheckedOut)
			checkout(node);
		return wasCheckedOut;
	}

	/**
	 * Simplify the save strategy keeping the check-in status unchanged. Goes
	 * together with <code>checkCOStatusBeforeUpdate</code>
	 */
	public static void checkCOStatusAfterUpdate(Node node, boolean wasCheckedOut)
			throws RepositoryException {
		if (!wasCheckedOut)
			saveAndCheckin(node);
		else
			node.getSession().save();
	}

	/**
	 * Simply retrieves the first versionable node in the current node ancestor
	 * tree (might be the ndoe itself) or null if none of them is versionable
	 */
	public static Node getParentVersionableNode(Node node)
			throws RepositoryException {
		Node curr = node;
		while (true) {
			if (curr.isNodeType(NodeType.MIX_VERSIONABLE))
				return curr;
			try {
				curr = curr.getParent();
			} catch (ItemNotFoundException infe) {
				// root node
				return null;
			}
		}
	}

	/* HELPERS FOR SINGLE VALUES */
	/**
	 * Concisely get the string value of a property. Returns an empty String
	 * rather than null if this node doesn't have this property or if the
	 * corresponding property is an empty string.
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
	 * Concisely get the value of a long property or null if this node doesn't
	 * have this property
	 */
	public static Long getLongValue(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return null;
			else
				return node.getProperty(propertyName).getLong();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get long property " + propertyName
					+ " of " + node, e);
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
	 * Concisely get a referenced node or null if the given node doesn't have
	 * this property or if the property is of the wrong type
	 */
	public static Node getReference(Node node, String propName) {
		try {
			Node ref = null;
			if (node.hasProperty(propName)) {
				Property prop = node.getProperty(propName);
				if (prop.getType() == PropertyType.REFERENCE) {
					ref = prop.getNode();
				}
			}
			return ref;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get reference " + propName
					+ " for node " + node, re);
		}
	}

	/**
	 * Centralizes management of updating property value. Among other to avoid
	 * infinite loop when the new value is the same as the one that is already
	 * stored in JCR (typically in UI Text controls that have a listener).
	 * 
	 * @return true if the value as changed
	 */
	public static boolean setJcrProperty(Node node, String propName,
			int propertyType, Object value) {
		try {
			switch (propertyType) {
			case PropertyType.STRING:
				if ("".equals((String) value)
						&& (!node.hasProperty(propName) || node
								.hasProperty(propName)
								&& "".equals(node.getProperty(propName)
										.getString())))
					return false;
				else if (node.hasProperty(propName)
						&& node.getProperty(propName).getString()
								.equals((String) value))
					return false;
				else {
					node.setProperty(propName, (String) value);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getBoolean() == (Boolean) value)
					return false;
				else {
					node.setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getDate()
								.equals((Calendar) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Calendar) value);
					return true;
				}
			case PropertyType.LONG:
				Long lgValue = (Long) value;

				if (lgValue == null)
					lgValue = 0L;

				if (node.hasProperty(propName)
						&& node.getProperty(propName).getLong() == lgValue)
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, lgValue);
					return true;
				}

			default:
				throw new PeopleException(
						"update unimplemented for property type "
								+ propertyType + ". Unable to update property "
								+ propName + " on " + node);
			}
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unexpected error while setting property " + propName
							+ " on " + node, re);
		}
	}

	/* MULTIPLE VALUES MANAGEMENT */
	/**
	 * Remove a given String from a multi value property of a node. If the
	 * String is not found, it fails silently
	 */
	public static void removeMultiPropertyValue(Node node, String propName,
			String stringToRemove) {
		try {
			List<String> strings = new ArrayList<String>();
			Value[] values = node.getProperty(propName).getValues();
			for (int i = 0; i < values.length; i++) {
				String curr = values[i].getString();
				if (!stringToRemove.equals(curr))
					strings.add(curr);
			}
			node.setProperty(propName, strings.toArray(new String[0]));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to remove value "
					+ stringToRemove + " for property " + propName + " of "
					+ node, e);
		}
	}

	/**
	 * Add a string value on a multivalued property. If this value is already
	 * part of the list, it returns an error message. We use case insensitive
	 * comparison
	 */
	public static String addMultiPropertyValue(Node node, String propName,
			String value) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check dupplicate
				for (Value currVal : values) {
					String curTagUpperCase = currVal.getString().toUpperCase()
							.trim();
					if (value.toUpperCase().trim().equals(curTagUpperCase)) {
						errMsg = value
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
	 * Add a string value on a multivalued property. WARNING if values is not an
	 * empty String, it overrides any existing value, and delete old ones.
	 */
	public static void setMultiValueStringPropFromString(Node node,
			String propName, String values, String separator) {
		try {
			if (!CommonsJcrUtils.isEmptyString(values)) {
				String[] valArray = values.split(separator);
				// Remove any empty value
				List<String> newValList = new ArrayList<String>();
				for (String currValue : valArray) {
					if (CommonsJcrUtils.checkNotEmptyString(currValue))
						newValList.add(currValue);
				}
				node.setProperty(propName, newValList.toArray(new String[0]));
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set multi value property "
					+ propName + " of node " + node + " with values [" + values
					+ "]", re);
		}
	}

	/**
	 * Concisely get a string that concatenates values of a multi-valued String
	 * property. It returns an empty String rather than null if this node
	 * doesn't have this property or if the corresponding property is an empty
	 * string.
	 * 
	 * Useful in the read only label providers. Caller might define a
	 * concatenation string, otherwise a semi-colon and a space are used.
	 */
	public static String getMultiAsString(Node node, String propertyName,
			String separator) {
		try {
			if (separator == null)
				separator = "; ";
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
					return builder.substring(0,
							builder.length() - separator.length());
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get multi valued property "
					+ propertyName + " of " + node, e);
		}
	}

	/**
	 * Concisely gets a list with the values of a multi-valued String property.
	 * Returns an empty list if the property does not exist.
	 */
	public static List<String> getMultiAsList(Node node, String propertyName) {
		List<String> results = new ArrayList<String>();
		try {
			if (!node.hasProperty(propertyName))
				return results;
			else {
				Value[] values = node.getProperty(propertyName).getValues();
				for (Value val : values) {
					results.add(val.getString());
				}
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get multi valued property "
					+ propertyName + " of " + node, e);
		}
		return results;
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
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to remove reference from property " + propName
							+ " of Node " + node, e);
		}
	}

	/**
	 * Adds a reference to a JCR Node to the multi valued REFERENCE property of
	 * a Node. An error message is returned if the Node is already referenced.
	 * The new reference is always added after all already existing references
	 * 
	 * TODO rather use exception when trying to add an already referenced node
	 * TODO Enable definition of a primary item by adding the new property as
	 * first element in the list
	 */
	public static String addRefToMultiValuedProp(Node node, String propName,
			Node nodeToReference) {
		try {
			Session session = node.getSession();
			Value[] values;
			List<Node> nodes = new ArrayList<Node>();
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();
				// Check duplicate
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

	/**
	 * Insert a reference to a given node in a multi value reference property
	 * just before the reference that is passed as target parameter. Usefull
	 * among other in the UI drag & drop mechanisms. If the target reference is
	 * not found, the new reference is added at the end of the list. This
	 * mechanism also check if another occurence of the source reference is
	 * present and remove it
	 */
	public static void orderReferenceBefore(Node node, String propName,
			Node sourceNode, Node targetNode) {
		try {
			Session session = node.getSession();
			String sourceId = sourceNode.getIdentifier();
			String targetId = targetNode.getIdentifier();

			Value[] values;
			List<Node> nodes = new ArrayList<Node>();
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();
				// Check duplicate
				for (Value currValue : values) {
					String jcrId = currValue.getString();
					if (sourceId.equals(jcrId)) {
						// does not add
					} else if (targetId.equals(jcrId)) {
						nodes.add(session.getNodeByIdentifier(sourceId));
						nodes.add(session.getNodeByIdentifier(targetId));
					} else
						nodes.add(session.getNodeByIdentifier(jcrId));
				}
			}
			setMultipleReferences(node, propName, nodes);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to update node " + node
					+ " to order " + sourceNode + " before " + targetNode
					+ " in multi value reference property " + propName, re);
		}
	}

	/**
	 * Adds a String to the multi valued STRING property of a Node. An error
	 * message is returned if the String is already in the list. The new String
	 * is always added after all already existing Strings.
	 * 
	 * TODO rather use exception when trying to add an already existing String
	 * TODO Enable definition of a primary item by adding the new property as
	 * first element in the list
	 */
	public static String addStringToMultiValuedProp(Node node, String propName,
			String value) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check duplicate
				for (Value currVal : values) {
					String currStr = currVal.getString();
					if (value.equals(currStr)) {
						errMsg = value + " is already in the list and thus "
								+ "could not be added.";
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

	/** Remove a String from a multi valued property */
	public static void removeStringFromMultiValuedProp(Node node,
			String propName, String value) {
		try {
			if (node.hasProperty(propName)) {
				List<Value> nodes = new ArrayList<Value>();
				Value[] values = node.getProperty(propName).getValues();
				for (int i = 0; i < values.length; i++) {
					String curr = values[i].getString();
					if (!value.equals(curr))
						nodes.add(values[i]);
				}
				Value[] results = nodes.toArray(new Value[0]);
				node.setProperty(propName, results);
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to remove reference from property " + propName
							+ " of Node " + node, e);
		}
	}

	/* INTERNATIONALISATION HELPERS */

	/**
	 * Concisely gets the node for the translation of some main properties of
	 * the current node given a language by default such a name is at
	 * {@link PeopleNames#PEOPLE_ALT_LANGS}/lang
	 */
	public static Node getAltLangNode(Node currentNode, String lang) {
		return getAltLangNode(currentNode, PeopleNames.PEOPLE_ALT_LANGS, lang);
	}

	/**
	 * Concisely gets the node for the translation of some main properties of
	 * the current node given a language and the name of an intermediary node
	 * 
	 * If no rel path is given, we use the default
	 * {@link PeopleNames#PEOPLE_ALT_LANGS}
	 */
	public static Node getAltLangNode(Node currentNode, String relPath,
			String lang) {
		try {
			if (isEmptyString(relPath))
				relPath = PeopleNames.PEOPLE_ALT_LANGS;
			if (!currentNode.hasNode(relPath + "/" + lang))
				return null;
			else {
				return currentNode.getNode(relPath + "/" + lang);
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get alt property for " + lang, e);
		}
	}

	public static Node getOrCreateAltLanguageNode(Node node, String lang,
			List<String> mixins) {
		return getOrCreateAltLanguageNode(node, PeopleNames.PEOPLE_ALT_LANGS,
				lang, mixins);
	}

	public static Node getOrCreateAltLanguageNode(Node node, String relPath,
			String lang, List<String> mixins) {
		try {
			Node child = JcrUtils.mkdirs(node.getSession(), node.getPath()
					+ "/" + relPath + "/" + lang, NodeType.NT_UNSTRUCTURED,
					NodeType.NT_UNSTRUCTURED, false);
			child.addMixin(NodeType.MIX_LANGUAGE);
			if (mixins != null && !mixins.isEmpty())
				for (String mixin : mixins)
					child.addMixin(mixin);

			child.setProperty(Property.JCR_LANGUAGE, lang);
			return child;
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot create child for language "
					+ lang, e);
		}
	}

	/* MISCELLANEOUS */

	/* WIDELY USED PATTERNS */
	/**
	 * Browses a {@code RowIterator} to build the corresponding row array.
	 */
	public static Row[] rowIteratorToArray(RowIterator rit) {
		List<Row> rows = new ArrayList<Row>();
		while (rit.hasNext()) {
			rows.add(rit.nextRow());
		}
		return rows.toArray(new Row[rows.size()]);
	}

	/**
	 * Browses a {@code RowIterator} to build the corresponding row array.
	 * Performs a kind of "select distinct" based on the JcrUID of the nodes
	 * designed by the selector name
	 */
	public static Row[] rowIteratorToDistinctArray(RowIterator rit,
			String distinctSelectorName) throws RepositoryException {
		List<Row> rows = new ArrayList<Row>();
		List<String> filmIds = new ArrayList<String>();
		while (rit.hasNext()) {
			Row curr = rit.nextRow();
			String currId = curr.getNode(distinctSelectorName).getIdentifier();
			if (filmIds.contains(currId))
				; // skip it
			else {
				filmIds.add(currId);
				rows.add(curr);
			}
		}
		return rows.toArray(new Row[rows.size()]);
	}

	/**
	 * Convert a {@link rowIterator} to a list of {@link Node} given a selector
	 * name. It relies on the <code>Row.getNode(String selectorName)</code>
	 * method.
	 */
	public static List<Node> rowIteratorToNodeList(RowIterator rowIterator,
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
	 * concatenate 2 strings with given separator if they are both not empty
	 */
	public static String concatIfNotEmpty(String str1, String str2,
			String separator) {
		StringBuilder builder = new StringBuilder();
		if (checkNotEmptyString(str1))
			builder.append(str1);

		if (checkNotEmptyString(str1) && checkNotEmptyString(str2))
			builder.append(separator);

		if (checkNotEmptyString(str2))
			builder.append(str2);
		return builder.toString();
	}

	/* PREVENT INSTANTIATION */
	private CommonsJcrUtils() {
	}

	/* QOM HELPERS */
	/**
	 * returns and(constraintA, constraintB) if constraintA != null, or
	 * constraintB otherwise (that cannot be null)
	 */
	public static Constraint localAnd(QueryObjectModelFactory factory,
			Constraint defaultC, Constraint newC) throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	/** widely used pattern in various UI Parts */
	public static Constraint getFreeTextConstraint(Session session,
			QueryObjectModelFactory factory, Selector source, String filter)
			throws RepositoryException {
		Constraint defaultC = null;
		if (checkNotEmptyString(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				defaultC = localAnd(factory, defaultC, currC);
			}
		}
		return defaultC;
	}
}