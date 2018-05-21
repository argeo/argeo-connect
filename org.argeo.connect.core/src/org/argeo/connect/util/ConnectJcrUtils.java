package org.argeo.connect.util;

import java.math.BigDecimal;
import java.security.AccessControlException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/**
 * Utility methods to ease interaction with a JCR repository while implementing.
 * This might move to commons in a near future
 */
public class ConnectJcrUtils {
	private final static Log log = LogFactory.getLog(ConnectJcrUtils.class);

	private final static String NS_JCR = "http://www.jcp.org/jcr/1.0";
	private final static String NS_NT = "http://www.jcp.org/jcr/nt/1.0";

	/**
	 * Replace the generic namespace with the local "jcr:" value. It is a workaround
	 * that must be later cleaned
	 * 
	 * @param name
	 *            the property name which prefix has to be cleaned
	 * @return the short property name
	 */
	public static String getLocalJcrItemName(String name) {
		String jcr = "{" + NS_JCR + "}";
		String nt = "{" + NS_NT + "}";
		if (name.startsWith(jcr))
			return "jcr:" + name.substring(jcr.length());
		else if (name.startsWith(nt))
			return "nt:" + name.substring(nt.length());
		else
			throw new ConnectException("Unknown prefix for " + name);
	}

	public static String checkAndLocalizeNamespaces(String name) {
		String jcr = "{" + NS_JCR + "}";
		String nt = "{" + NS_NT + "}";
		if (name.startsWith(jcr))
			return "jcr:" + name.substring(jcr.length());
		else if (name.startsWith(nt))
			return "nt:" + name.substring(nt.length());
		else
			return name;
	}

	public static String cleanNodeName(String name) {
		String cleanName = name.trim().replaceAll("[^a-zA-Z0-9-. _]", "");
		return cleanName;
	}

	/**
	 * Add '?' to the list of forbidden characters. See
	 * JcrUtils.replaceInvalidChars(String name)
	 */
	public final static char[] INVALID_NAME_CHARACTERS = { '/', ':', '[', ']', '|', '*', '?', /*
																								 * invalid XML chars :
																								 */
			'<', '>', '&' };

	/**
	 * Replaces characters which are invalid in a JCR name by '_'. Currently not
	 * exhaustive.
	 * 
	 * @see JcrUtils#INVALID_NAME_CHARACTERS
	 */
	public static String replaceInvalidChars(String name) {
		return replaceInvalidChars(name, '_');
	}

	/**
	 * Replaces characters which are invalid in a JCR name. Currently not
	 * exhaustive.
	 * 
	 * @see JcrUtils#INVALID_NAME_CHARACTERS
	 */
	public static String replaceInvalidChars(String name, char replacement) {
		boolean modified = false;
		char[] arr = name.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			char c = arr[i];
			invalid: for (char invalid : INVALID_NAME_CHARACTERS) {
				if (c == invalid) {
					arr[i] = replacement;
					modified = true;
					break invalid;
				}
			}
		}
		if (modified)
			return new String(arr);
		else
			// do not create new object if unnecessary
			return name;
	}

	// PATH MANAGEMENT
	/** Simply retrieves the parent rel path of the provided relative path */
	public static String parentRelPath(String relPath) {
		// Remove trailing slash
		if (relPath.charAt(relPath.length() - 1) == '/')
			relPath = relPath.substring(0, relPath.length() - 2);

		int index = relPath.lastIndexOf('/');
		if (index < 0)
			return "";
		else
			return relPath.substring(0, index);
	}

	/** The last element of a relative path. */
	public static String lastRelPathElement(String relPath) {
		// Remove trailing slash
		if (relPath.charAt(relPath.length() - 1) == '/')
			relPath = relPath.substring(0, relPath.length() - 2);

		int index = relPath.lastIndexOf('/');
		if (index < 0)
			return relPath;
		return relPath.substring(index + 1);
	}

	public static boolean canEdit(Node entity) {
		boolean canEdit = false;
		try {
			entity.getSession().checkPermission(entity.getPath(), "add_node");
			canEdit = true;
		} catch (AccessControlException ace) {
			// silent
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to check permission on " + entity, e);
		}
		return canEdit;
	}

	/**
	 * Helper for label provider: returns the Node if element is a Node or retrieves
	 * the Node if the object is a row. Expects a single Node in the row if no
	 * selector name is provided Call {@link Row#getNode()} catching
	 * {@link RepositoryException}
	 */
	public static Node getNodeFromElement(Object element, String selectorName) {
		Node currNode;
		if (element instanceof Row) {
			Row currRow = (Row) element;
			try {
				if (selectorName != null)
					currNode = currRow.getNode(selectorName);
				else
					currNode = currRow.getNode();
			} catch (RepositoryException re) {
				throw new ConnectException(
						"Unable to retrieve Node with selector name " + selectorName + " on " + currRow, re);
			}
		} else if (element instanceof Node)
			currNode = (Node) element;
		else
			throw new ConnectException("unsupported element type: " + element.getClass().getName());
		return currNode;
	}

	/**
	 * Returns the versionable node in the parent path, this if it is versionable or
	 * null if none is versionnable including root node.
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
			throw new ConnectException("Unable to get check out status for node " + node, re);
		}
	}

	/**
	 * Works around missing method to test if a node has been removed from existing
	 * session
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
			throw new ConnectException("Error while testing node existence", re);
		}
		return true;
	}

	/**
	 * Wraps the versionMananger.isCheckedOut(path) method to adapt it to the
	 * current check in / check out policy.
	 * 
	 * TODO : add management of check out by others.
	 */
	public static boolean isNodeCheckedOut(Node node) {
		try {
			if (!node.isNodeType(NodeType.MIX_VERSIONABLE))
				return true;
			else
				return node.getSession().getWorkspace().getVersionManager().isCheckedOut(node.getPath());
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to get check out status for node " + node, re);
		}
	}

	/**
	 * Shortcut to get a node iterator on all nodes of a given type under a given
	 * subpath.
	 */
	public static NodeIterator getNodesOfType(Session session, String parentPath, String nodeType) {
		try {
			if (parentPath == null)
				parentPath = "/";
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(parentPath));
			builder.append("//element(*, ").append(nodeType).append(")");
			Query query = session.getWorkspace().getQueryManager().createQuery(builder.toString(),
					ConnectConstants.QUERY_XPATH);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve node of type " + nodeType + " under " + parentPath, re);
		}
	}

	public static Node getByPropertyValue(Session session, String parentPath, String nodeType, String propName,
			String propValue) {
		NodeIterator nit = getByPropertyValue(session, parentPath, nodeType, propName, propValue, false);
		if (nit != null && nit.hasNext())
			return nit.nextNode();
		else
			return null;
	}

	public static NodeIterator getByPropertyValue(Session session, String parentPath, String nodeType, String propName,
			String propValue, boolean acceptMultipleResult) {
		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			String xpathQueryStr = XPathUtils.descendantFrom(parentPath) + "//element(*, "
					+ checkAndLocalizeNamespaces(nodeType) + ")";

			String attrQuery = XPathUtils.getPropertyEquals(checkAndLocalizeNamespaces(propName), propValue);

			xpathQueryStr += "[" + attrQuery + "]";
			// String cleanStr = cleanStatement(xpathQueryStr);

			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			QueryResult result = xpathQuery.execute();
			NodeIterator ni = result.getNodes();

			long niSize = ni.getSize();
			if (niSize == 0)
				return null;
			else if (niSize > 1) {
				if (acceptMultipleResult)
					return ni;
				else
					throw new ConnectException("Found " + niSize + " entities of type " + nodeType + "with " + propName
							+ " =[" + propValue + "] under " + parentPath);
			} else
				return ni;
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to retrieve entities of type " + nodeType + " with " + propName + " = ["
					+ propValue + "] under " + parentPath, e);
		}
	}

	/** Retrieves the human readable label of a property */
	public static String getPropertyTypeAsString(Property prop) {
		try {
			return PropertyType.nameFromValue(prop.getType());
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot check type for " + prop, e);
		}
	}

	/**
	 * If this node is has the {@link NodeType#MIX_LAST_MODIFIED} mixin, it updates
	 * the {@link Property#JCR_LAST_MODIFIED} property with the current time and the
	 * {@link Property#JCR_LAST_MODIFIED_BY} property with the passed user id. In
	 * Jackrabbit 2.x,
	 * <a href="https://issues.apache.org/jira/browse/JCR-2233">these properties are
	 * not automatically updated</a>, hence the need for manual update. The session
	 * is not saved.
	 */
	public static void updateLastModified(Node node, String userId) {
		try {
			if (!node.isNodeType(NodeType.MIX_LAST_MODIFIED))
				node.addMixin(NodeType.MIX_LAST_MODIFIED);
			node.setProperty(Property.JCR_LAST_MODIFIED, new GregorianCalendar());
			node.setProperty(Property.JCR_LAST_MODIFIED_BY, userId);
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot update last modified on " + node, e);
		}
	}

	/* VERSIONING MANAGEMENT */
	/**
	 * For the time being, same as isNodeCheckedOut(Node node). TODO : add
	 * management of check out by others.
	 */
	public static boolean isNodeCheckedOutByMe(Node node) {
		return isNodeCheckedOut(node);
	}

	/**
	 * Make a version snapshot of the current state of the given versionable node.
	 * It wraps a JCR save and checkPoint methods
	 */
	public static boolean saveAndPublish(Node node, boolean publish) {
		try {
			boolean changed = false;
			Session session = node.getSession();
			if (session.hasPendingChanges()) {
				JcrUtils.updateLastModified(node);
				session.save();
				changed = true;
			}
			if (isVersionable(node) && publish) {
				VersionManager vm = session.getWorkspace().getVersionManager();
				String path = node.getPath();
				vm.checkpoint(path);
			} else if (publish && !isVersionable(node)) {
				log.warn("Cannot publish unversionnable node at " + node.getPath());
			}
			return changed;
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to perform check point on " + node, re);
		}
	}

	/**
	 * Shortcut to save the underlying session if it has pending changes without
	 * exception
	 */
	public static boolean saveIfNecessary(Node node) {
		boolean changed = false;
		try {
			Session session = node.getSession();
			if (session.hasPendingChanges()) {
				session.save();
				changed = true;
			}
			return changed;
		} catch (RepositoryException re) {
			throw new ConnectException("Cannot save with node " + node, re);
		}
	}

	/**
	 * Wraps a best effort to versionMananger.checkedPoint(path) a list of path. We
	 * check if the node still exists because the list might be out-dated
	 * 
	 * We assume the session has been saved.
	 *
	 * Not that are not versionable won't be touched TODO : add management of check
	 * out by others.
	 */
	public static void checkPoint(Session session, List<String> pathes, boolean updateLastModified) {
		try {
			VersionManager vm = session.getWorkspace().getVersionManager();
			loop: for (String currPath : pathes) {
				if (!session.nodeExists(currPath))
					continue loop;
				try {
					Node currNode = session.getNode(currPath);
					if (!currNode.isNodeType(NodeType.MIX_VERSIONABLE))
						continue loop;

					if (updateLastModified) {
						JcrUtils.updateLastModified(currNode);
						session.save();
					}
					vm.checkpoint(currPath);
				} catch (RepositoryException re) {
					throw new ConnectException("Unable to perform check point on " + currPath, re);
				}
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unexpected error when performing batch check point ", re);
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
	 * Simply retrieves the first versionable node in the current node ancestor tree
	 * (might be the ndoe itself) or null if none of them is versionable
	 */
	public static Node getParentVersionableNode(Node node) throws RepositoryException {
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

	// ENCAPSULATE COMMONS JCR CALLS
	// with the try/catch block to simplify simple UI code
	/**
	 * Call {@link Repository#login()} without exceptions (useful in super
	 * constructors).
	 */
	public static Session login(Repository repository) {
		try {
			return repository.login();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to login", re);
		}
	}

	/** Centralises exception management to call {@link Node#getSession()} */
	public static Session getSession(Node node) {
		try {
			return node.getSession();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve session for node " + node, re);
		}
	}

	/**
	 * Centralises exception management to call
	 * {@link Node#getSession()#getRepository()}
	 */
	public static Repository getRepository(Node node) {
		try {
			return node.getSession().getRepository();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve repository for node " + node, re);
		}
	}

	/** Centralises exception management to call {@link Node#getIdentifier()} */
	public static String getIdentifier(Node node) {
		try {
			return node.getIdentifier();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve identifier for node " + node, re);
		}
	}

	/** Centralises exception management to call {@link Node#getName()} */
	public static String getName(Node node) {
		try {
			return node.getName();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve name for node " + node, re);
		}

	}

	/** Centralises exception management to call {@link Node#getPath()} */
	public static String getPath(Node node) {
		try {
			return node.getPath();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve path for node " + node, re);
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

	/** Simply calls {@link Session#itemExists(String)} with no try/catch */
	public static boolean itemExists(Session session, String absPath) {
		try {
			return session.itemExists(absPath);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to check existence of item at " + absPath, re);
		}
	}

	/**
	 * Centralizes exception management to call
	 * {@link Session#getNodeByIdentifier(String)}
	 */
	public static Node getNodeByIdentifier(Session session, String identifier) {
		try {
			return session.getNodeByIdentifier(identifier);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve node by identifier with " + identifier, re);
		}
	}

	/** Centralizes exception management to call {@link Node#getParent()} */
	public static Node getParent(Node child) {
		try {
			return child.getParent();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve parent node for " + child, re);
		}
	}

	/**
	 * Centralizes exception management to call
	 * {@link Session#getNodeByIdentifier(String)}. The session is retrieved using
	 * the passed node
	 */
	public static Node getNodeByIdentifier(Node sessionNode, String identifier) {
		return getNodeByIdentifier(getSession(sessionNode), identifier);
	}

	/**
	 * Call {@link Session#getNode()} catching {@link RepositoryException}
	 */
	public static Node getNode(Session session, String absPath) {
		try {
			return session.getNode(absPath);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve Node at path " + absPath, re);
		}
	}

	/**
	 * If session.absParentPath exists and is visible, build a relative path with
	 * the passed relative subpath and returns the node at corresponding relative
	 * path if it exists
	 * 
	 * Call {@link Session#getNode()} catching {@link RepositoryException}
	 */
	public static Node getNode(Session session, String absParentPath, String... childRelPath) {
		try {
			if (EclipseUiUtils.isEmpty(absParentPath))
				absParentPath = "/";
			if (!session.itemExists(absParentPath))
				throw new ConnectException("Node at " + absParentPath + " does not exist or is not visible");
			StringBuilder builder = new StringBuilder();
			for (String rp : childRelPath)
				builder.append(rp).append("/");
			String relPath = builder.substring(0, builder.length() - 1);
			Node parent = session.getNode(absParentPath);
			if (parent.hasNode(relPath))
				return parent.getNode(relPath);
			else
				return null;
		} catch (RepositoryException re) {
			throw new ConnectException(
					"Unable to retrieve Node at path " + absParentPath + " with child rel path " + childRelPath, re);
		}
	}

	/**
	 * Call {@link Row#getNode()} catching {@link RepositoryException}
	 */
	public static Node getNode(Row row, String selectorName) {
		try {
			if (selectorName == null)
				return row.getNode();
			else
				return row.getNode(selectorName);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve Node with selector name " + selectorName + " on " + row, re);
		}
	}

	/** Calls {@link Node#isNodetype(String)} without exceptions */
	public static boolean isNodeType(Node node, String nodeTypeName) {
		try {
			return node.isNodeType(nodeTypeName);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to test NodeType " + nodeTypeName + " for " + node, re);
		}
	}

	/** Simply retrieves primary node type name */
	public static String getPrimaryNodeType(Node node) {
		try {
			return node.getPrimaryNodeType().getName();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to retrieve node type name for " + node, re);
		}
	}

	/** Concisely check out a node. */
	private static void checkout(Node node) {
		try {
			node.getSession().getWorkspace().getVersionManager().checkout(node.getPath());
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to check out node  " + node, re);
		}
	}

	/** Simply check if a node is versionable */
	public static boolean isVersionable(Node node) {
		try {
			return node.isNodeType(NodeType.MIX_VERSIONABLE);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to test versionability  of " + node, re);
		}
	}

	/* HELPERS FOR SINGLE VALUES */
	/**
	 * Concisely gets the String value of a property. Returns an empty String rather
	 * than null if this node doesn't have this property or if the corresponding
	 * property is an empty string.
	 */
	public static String get(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return "";
			else
				return node.getProperty(propertyName).getString();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get property " + propertyName + " of " + node, e);
		}
	}

	/**
	 * Concisely gets the value of a long property or null if this node doesn't have
	 * this property
	 */
	public static Long getLongValue(Node node, String propRelPath) {
		try {
			if (!node.hasProperty(propRelPath))
				return null;
			else
				return node.getProperty(propRelPath).getLong();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get long property " + propRelPath + " of " + node, e);
		}
	}

	/**
	 * Concisely gets the value of a double property or null if this node doesn't
	 * have this property
	 */
	public static Double getDoubleValue(Node node, String propRelPath) {
		try {
			if (!node.hasProperty(propRelPath))
				return null;
			else
				return node.getProperty(propRelPath).getDouble();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get double property " + propRelPath + " of " + node, e);
		}
	}

	/**
	 * Concisely gets the value of a date property or null if this node doesn't have
	 * this property
	 */
	public static Calendar getDateValue(Node node, String propRelPath) {
		try {
			if (!node.hasProperty(propRelPath))
				return null;
			else
				return node.getProperty(propRelPath).getDate();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get date property " + propRelPath + " of " + node, e);
		}
	}

	/**
	 * Concisely gets the value of a boolean property or null if this node doesn't
	 * have this property
	 */
	public static Boolean getBooleanValue(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return null;
			else
				return node.getProperty(propertyName).getBoolean();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get boolean property " + propertyName + " of " + node, e);
		}
	}

	/**
	 * Concisely gets the value of a date property formatted as String or an empty
	 * String this node doesn't have this property
	 */
	public static String getDateFormattedAsString(Node node, String propertyName, String dateFormatPattern) {
		try {
			if (!node.hasProperty(propertyName))
				return null;
			else {
				Calendar cal = node.getProperty(propertyName).getDate();
				return new SimpleDateFormat(dateFormatPattern).format(cal.getTime());
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get date property " + propertyName + " on " + node, e);
		}
	}

	/**
	 * Concisely gets a referenced node or null if the given node doesn't have this
	 * property or if the property is of the wrong type
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
			throw new ConnectException("Unable to get reference " + propName + " for node " + node, re);
		}
	}

	/**
	 * Centralises management of updating property value. Among other to avoid
	 * infinite loop when the new value is the same as the one that is already
	 * stored in JCR (typically in UI Text controls that have a listener).
	 * 
	 * @return true if the value as changed
	 */
	public static boolean setJcrProperty(Node node, String propName, int propertyType, Object value) {
		try {
			// Manage null value
			if (value == null)
				if (node.hasProperty(propName)) {
					node.getProperty(propName).remove();
					return true;
				} else
					return false;

			switch (propertyType) {
			case PropertyType.STRING:
				String strValue = (String) value;
				if (node.hasProperty(propName) && node.getProperty(propName).getString().equals(strValue))
					return false;
				else {
					node.setProperty(propName, strValue);
					return true;
				}
			case PropertyType.REFERENCE:
			case PropertyType.WEAKREFERENCE:
				Node nodeValue = (Node) value;
				if (node.hasProperty(propName)
						&& nodeValue.getIdentifier().equals(node.getProperty(propName).getNode().getIdentifier()))
					return false;
				else {
					node.setProperty(propName, nodeValue);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (node.hasProperty(propName) && node.getProperty(propName).getBoolean() == (Boolean) value)
					return false;
				else {
					node.setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (node.hasProperty(propName) && node.getProperty(propName).getDate().equals((Calendar) value))
					return false;
				else {
					node.setProperty(propName, (Calendar) value);
					return true;
				}
			case PropertyType.LONG:
				Long lgValue = (Long) value;
				if (node.hasProperty(propName) && node.getProperty(propName).getLong() == lgValue)
					return false;
				else {
					node.setProperty(propName, lgValue);
					return true;
				}
			case PropertyType.DOUBLE:
				Double dbValue;
				if (value instanceof Double)
					dbValue = (Double) value;
				else {
					try {
						dbValue = Double.parseDouble(value.toString());
					} catch (NumberFormatException e) {
						return false;
					}
				}
				if (node.hasProperty(propName) && node.getProperty(propName).getDouble() == dbValue)
					return false;
				else {
					node.setProperty(propName, dbValue);
					return true;
				}
			case PropertyType.DECIMAL:
				BigDecimal bdValue = (BigDecimal) value;
				if (node.hasProperty(propName) && node.getProperty(propName).getDecimal() == bdValue)
					return false;
				else {
					node.setProperty(propName, bdValue);
					return true;
				}
			default:
				throw new ConnectException("Update unimplemented for property type " + propertyType
						+ ". Unable to update property " + propName + " on " + node);
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unexpected error while setting property " + propName + " on " + node, re);
		}
	}

	/* MULTIPLE VALUES MANAGEMENT */
	/**
	 * Removes a given String from a multi value property of a node. If the String
	 * is not found, it does nothing
	 */
	public static void removeMultiPropertyValue(Node node, String propName, String stringToRemove) {
		try {
			boolean foundValue = false;

			List<String> strings = new ArrayList<String>();
			Value[] values = node.getProperty(propName).getValues();
			for (int i = 0; i < values.length; i++) {
				String curr = values[i].getString();
				if (stringToRemove.equals(curr))
					foundValue = true;
				else
					strings.add(curr);
			}
			if (foundValue)
				node.setProperty(propName, strings.toArray(new String[0]));
		} catch (RepositoryException e) {
			throw new ConnectException(
					"Unable to remove value " + stringToRemove + " for property " + propName + " of " + node, e);
		}
	}

	/**
	 * Adds a string value on a multivalued property. If this value is already part
	 * of the list, it returns an error message. We use case insensitive comparison
	 */
	public static String addMultiPropertyValue(Node node, String propName, String value) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check duplicate
				for (Value currVal : values) {
					String curTagUpperCase = currVal.getString().toUpperCase().trim();
					if (value.toUpperCase().trim().equals(curTagUpperCase)) {
						errMsg = value + " is already in the list and thus could not be added.";
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
			throw new ConnectException("Unable to set tags", re);
		}
	}

	/**
	 * Add a string value on a multivalued property. WARNING if values is not an
	 * empty String, it overrides any existing value, and delete old ones.
	 */
	public static void setMultiValueStringPropFromString(Node node, String propName, String values, String separator) {
		try {
			if (notEmpty(values)) {
				String[] valArray = values.split(separator);
				// Remove any empty value
				List<String> newValList = new ArrayList<String>();
				for (String currValue : valArray) {
					if (notEmpty(currValue))
						newValList.add(currValue);
				}
				node.setProperty(propName, newValList.toArray(new String[0]));
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to set multi value property " + propName + " of node " + node
					+ " with values [" + values + "]", re);
		}
	}

	/**
	 * Concisely gets a string that concatenates values of a multi-valued String
	 * property. It returns an empty String rather than null if this node doesn't
	 * have this property or if the corresponding property is an empty string.
	 * 
	 * Useful in the read only label providers. Caller might define a concatenation
	 * string, otherwise a semi-colon and a space are used.
	 */
	public static String getMultiAsString(Node node, String propertyName, String separator) {
		try {
			if (separator == null)
				separator = "; ";
			if (!node.hasProperty(propertyName))
				return "";
			else {
				Value[] values = node.getProperty(propertyName).getValues();
				StringBuilder builder = new StringBuilder();
				for (Value val : values) {
					String currStr = val.getString();
					if (notEmpty(currStr))
						builder.append(currStr).append(separator);
				}
				if (builder.lastIndexOf(separator) >= 0)
					return builder.substring(0, builder.length() - separator.length());
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get multi valued property " + propertyName + " of " + node, e);
		}
	}

	/**
	 * Concisely gets a list with the values of a multi-valued String property.
	 * Returns an empty list if the property does not exist.
	 */
	public static List<String> getMultiAsList(Node node, String propertyName) {
		List<String> results = new ArrayList<String>();
		try {
			if (propertyName == null)
				return results;
			if (!node.hasProperty(propertyName))
				return results;
			else {
				Value[] values = node.getProperty(propertyName).getValues();
				for (Value val : values) {
					results.add(val.getString());
				}
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get multi valued property " + propertyName + " of " + node, e);
		}
		return results;
	}

	/**
	 * Sets a property of type REFERENCE that is multiple. Overrides any already
	 * defined value of this property
	 */
	public static void setMultipleReferences(Node node, String propertyName, List<Node> nodes)
			throws RepositoryException {
		ValueFactory vFactory = node.getSession().getValueFactory();
		int size = nodes.size();
		Value[] values = new Value[size];
		int i = 0;
		for (Node currNode : nodes) {
			Value val = vFactory.createValue(currNode.getIdentifier(), PropertyType.REFERENCE);
			values[i++] = val;
		}
		node.setProperty(propertyName, values);
	}

	/** Remove a Reference from a multi valued property */
	public static void removeRefFromMultiValuedProp(Node node, String propName, String identifier) {
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
			throw new ConnectException("Unable to remove reference from property " + propName + " of Node " + node, e);
		}
	}

	/**
	 * Adds a reference to a JCR Node to the multi valued REFERENCE property of a
	 * Node. An error message is returned if the Node is already referenced. The new
	 * reference is always added after all already existing references
	 * 
	 * TODO rather use exception when trying to add an already referenced node TODO
	 * Enable definition of a primary item by adding the new property as first
	 * element in the list
	 */
	public static String addRefToMultiValuedProp(Node node, String propName, Node nodeToReference) {
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
						errMsg = ConnectJcrUtils.get(nodeToReference, Property.JCR_TITLE)
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
			throw new ConnectException("Unable to add reference ", re);
		}
	}

	/**
	 * Insert a reference to a given node in a multi value reference property just
	 * before the reference that is passed as target parameter. Usefull among other
	 * in the UI drag and drop mechanisms. If the target reference is not found, the
	 * new reference is added at the end of the list. This mechanism also check if
	 * another occurence of the source reference is present and remove it
	 */
	public static void orderReferenceBefore(Node node, String propName, Node sourceNode, Node targetNode) {
		try {
			Session session = node.getSession();
			String sourceId = sourceNode.getIdentifier();
			String targetId = null;
			if (targetNode != null)
				targetId = targetNode.getIdentifier();

			Value[] values;
			List<Node> nodes = new ArrayList<Node>();
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();
				// Check duplicate
				for (Value currValue : values) {
					String jcrId = currValue.getString();
					if (sourceId.equals(jcrId)) {
						// does not add
					} else if (jcrId.equals(targetId)) {
						nodes.add(session.getNodeByIdentifier(sourceId));
						nodes.add(session.getNodeByIdentifier(targetId));
					} else
						nodes.add(session.getNodeByIdentifier(jcrId));
				}
				if (targetId == null)
					nodes.add(session.getNodeByIdentifier(sourceId));
			}
			setMultipleReferences(node, propName, nodes);
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to update node " + node + " to order " + sourceNode + " before "
					+ targetNode + " in multi value reference property " + propName, re);
		}
	}

	/**
	 * Simply checks a multi valued STRING property of a Node and returns true if
	 * the given property has already such a value. comparison is case insensitive
	 * and trimmed.
	 */
	public static boolean valueExists(Node node, String propName, String value) {
		try {
			value = value.trim().toLowerCase();
			if (node.hasProperty(propName)) {
				Value[] values = node.getProperty(propName).getValues();
				for (Value currVal : values) {
					String currStr = currVal.getString().trim().toLowerCase();
					if (value.equals(currStr))
						return true;
				}
			}
			return false;
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to set tags", re);
		}
	}

	/**
	 * Adds a String to the multi valued STRING property of a Node. An error message
	 * is returned if the String is already in the list. The new String is always
	 * added after all already existing Strings.
	 * 
	 * TODO rather use exception when trying to add an already existing String TODO
	 * Enable definition of a primary item by adding the new property as first
	 * element in the list
	 */
	public static String addStringToMultiValuedProp(Node node, String propName, String value) {
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
						errMsg = value + " is already in the list and thus " + "could not be added.";
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
			throw new ConnectException("Unable to set tags", re);
		}
	}

	/** Remove a String from a multi valued property */
	public static void removeStringFromMultiValuedProp(Node node, String propName, String value) {
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
			throw new ConnectException("Unable to remove reference from property " + propName + " of Node " + node, e);
		}
	}

	/* MISCELLANEOUS */

	/* WIDELY USED PATTERNS */
	/** Browses a {@code NodeIterator} to build the corresponding Node array. */
	public static Node[] nodeIteratorToArray(NodeIterator nit) {
		Node[] nodes = new Node[(int) nit.getSize()];
		int i = 0;
		while (nit.hasNext()) {
			nodes[i++] = nit.nextNode();
		}
		return nodes;
	}

	/** Browses a {@code RowIterator} to build the corresponding row array. */
	public static Row[] rowIteratorToArray(RowIterator rit) {
		List<Row> rows = new ArrayList<Row>();
		while (rit.hasNext()) {
			rows.add(rit.nextRow());
		}
		return rows.toArray(new Row[rows.size()]);
	}

	/**
	 * Browses a {@code RowIterator} to build the corresponding row array. Performs
	 * a kind of "select distinct" based on the JcrUID of the nodes designed by the
	 * selector name
	 */
	public static Row[] rowIteratorToDistinctArray(RowIterator rit, String distinctSelectorName)
			throws RepositoryException {
		List<Row> rows = new ArrayList<Row>();
		List<String> jcrIds = new ArrayList<String>();
		while (rit.hasNext()) {
			Row curr = rit.nextRow();
			String currId = curr.getNode(distinctSelectorName).getIdentifier();
			if (jcrIds.contains(currId))
				; // skip it
			else {
				jcrIds.add(currId);
				rows.add(curr);
			}
		}
		return rows.toArray(new Row[rows.size()]);
	}

	/**
	 * Convert a {@link RowIterator} to a list of {@link Node} given a selector
	 * name. It relies on the &lt;code&gt;Row.getNode(String
	 * selectorName)&lt;/code&gt; method.
	 */
	public static List<Node> rowIteratorToNodeList(RowIterator rowIterator, String selectorName)
			throws RepositoryException {
		List<Node> nodes = new ArrayList<Node>();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.nextRow();
			if (row.getNode(selectorName) != null)
				nodes.add(row.getNode(selectorName));
		}
		return nodes;
	}

	/** Parses and trims a String of values */
	public static String[] parseAndClean(String string, String regExp, boolean clean) {
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

	/** Concatenates 2 strings with given separator if they are not empty */
	public static String concatIfNotEmpty(String str1, String str2, String separator) {
		StringBuilder builder = new StringBuilder();
		if (notEmpty(str1))
			builder.append(str1);

		if (notEmpty(str1) && notEmpty(str2))
			builder.append(separator);

		if (notEmpty(str2))
			builder.append(str2);
		return builder.toString();
	}

	/* QOM HELPERS */
	/**
	 * Returns and(constraintA, constraintB) if constraintA != null, or constraintB
	 * otherwise (that cannot be null)
	 */
	public static Constraint localAnd(QueryObjectModelFactory factory, Constraint defaultC, Constraint newC)
			throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	/** Widely used pattern in various UI Parts */
	public static Constraint getFreeTextConstraint(Session session, QueryObjectModelFactory factory, Selector source,
			String filter) throws RepositoryException {
		Constraint defaultC = null;
		if (notEmpty(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory().createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(source.getSelectorName(), null, so);
				defaultC = localAnd(factory, defaultC, currC);
			}
		}
		return defaultC;
	}

	/* HELPERS */
	static boolean notEmpty(String stringToTest) {
		return !(stringToTest == null || "".equals(stringToTest.trim()));
	}

	/* PREVENTS INSTANTIATION */
	private ConnectJcrUtils() {
	}
}
