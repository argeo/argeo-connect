package org.argeo.connect;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.RemoteJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;

/** Minimal interface that a Connect AppService must implement */
public interface AppService {
	final static Log log = LogFactory.getLog(AppService.class);

	/** Returns the current App name */
	public String getAppBaseName();

	default public String getBaseRelPath(String nodeType) {
		return getAppBaseName();
	}

	default public Node publishEntity(Node parent, String nodeType, Node srcNode) throws RepositoryException {
		return publishEntity(parent, nodeType, srcNode, true);
	}

	default public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		Node createdNode = null;
		if (isKnownType(nodeType)) {
			String relPath = getDefaultRelPath(srcNode);

			if (parent == null) {
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				StringBuilder builder = new StringBuilder();
				builder.append("Trying to publish a node with no parent, "
						+ "this approach will soon be forbidden. Calling stack:\n");
				for (StackTraceElement el : stack) {
					builder.append(el.toString() + "\n");
				}
				log.error(builder.toString());
				parent = srcNode.getSession().getNode("/" + getBaseRelPath(nodeType));
			}

			// TODO check duplicate
			String parRelPath = ConnectJcrUtils.parentRelPath(relPath);

			Node tmpParent = null;
			if (EclipseUiUtils.isEmpty(parRelPath))
				tmpParent = parent;
			else
				tmpParent = JcrUtils.mkdirs(parent, parRelPath);

			createdNode = tmpParent.addNode(ConnectJcrUtils.lastRelPathElement(relPath));

			RemoteJcrUtils.copy(srcNode, createdNode, true);
			createdNode.addMixin(nodeType);
			JcrUtils.updateLastModified(createdNode);
			if (removeSrcNode)
				srcNode.remove();
		}
		return createdNode;
	}

	/**
	 * Try to save and optionally publish a business object after applying
	 * context specific rules and special behaviours (typically cache updates).
	 * 
	 * @return the entity that has been saved (and optionally published): note
	 *         that in some cases (typically, the first save of a draft node in
	 *         the business sub tree) the returned node is not the same as the
	 *         one that has been passed
	 * @param entity
	 * @param publish
	 *            also publishes the corresponding node
	 * @throws PeopleException
	 *             If one of the rule defined for this type is not respected.
	 *             Use getMessage to display to the user if needed
	 */
	default public Node saveEntity(Node entity, boolean publish) {
		try {
			Session session = entity.getSession();
			if (session.hasPendingChanges()) {
				JcrUtils.updateLastModified(entity);
				session.save();
			}
			if (entity.isNodeType(NodeType.MIX_VERSIONABLE))
				// TODO check if some changes happened since last checkpoint
				session.getWorkspace().getVersionManager().checkpoint(entity.getPath());
			return entity;
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot save " + entity, e);
		}
	}

	/**
	 * Computes the App specific relative path for a known type based on
	 * properties of the passed node
	 */
	public String getDefaultRelPath(Node entity) throws RepositoryException;

	/**
	 * Computes the App specific relative path for this known node type based on
	 * the passed id
	 * 
	 * @param session
	 *            TODO
	 */
	public String getDefaultRelPath(Session session, String nodeType, String id);

	/**
	 * Returns a display name that is app specific and that depends on one or
	 * more of the entity properties. The user can always set a flag to force
	 * the value to something else.
	 * 
	 * The Display name is usually stored in the JCR_TITLE property.
	 */
	default public String getDisplayName(Node entity) {
		String defaultDisplayName = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
		if (defaultDisplayName == null || "".equals(defaultDisplayName.trim()))
			return ConnectJcrUtils.getName(entity);
		else
			return defaultDisplayName;
	}

	/**
	 * Returns (after creation if necessary) the base parent for draft nodes of
	 * this application
	 */
	default public Node getDraftParent(Session session) throws RepositoryException {
		Node home = NodeUtils.getUserHome(session);
		String draftRelPath = ConnectConstants.HOME_APP_SYS_RELPARPATH + "/" + getAppBaseName();
		return JcrUtils.mkdirs(home, draftRelPath);
	}

	/**
	 * Convenience method to create a Node with given mixin under the current
	 * logged in user home. Creates a UUID and set the connect:uid properties.
	 * The session is not saved.
	 */
	default public Node createDraftEntity(Session session, String mainMixin) throws RepositoryException {
		Node parent = getDraftParent(session);
		String connectUid = UUID.randomUUID().toString();
		Node draftNode = parent.addNode(connectUid);
		draftNode.addMixin(mainMixin);
		draftNode.setProperty(ConnectNames.CONNECT_UID, connectUid);
		return draftNode;
	}

	/**
	 * Returns the App specific main type of a node, that can be its primary
	 * type or one of its mixin, typically for the People App.
	 */
	default public String getMainNodeType(Node node) {
		return null;
	}

	/**
	 * Simply checks if the passed entity has a primary or mixin type that is
	 * known and thus can be managed by the this App
	 */
	public boolean isKnownType(Node entity);

	/**
	 * Simply checks if the passed type is known and thus can be managed by the
	 * this App. It might be a primary or mixin type
	 */
	public boolean isKnownType(String nodeType);

	/**
	 * Draft implementation of an i18n mechanism to retrieve labels given a key
	 */
	default public String getLabel(String key, String... innerNames) {
		return key;
	}

	/**
	 * Searches the workspace corresponding to the passed session. It returns
	 * the corresponding entity or null if none has been found. This UID is
	 * implementation specific and is not a JCR Identifier.
	 * 
	 * It will throw a PeopleException if more than one item with this ID has
	 * been found
	 * 
	 * @param session
	 * @param parentPath
	 *            can be null or empty
	 * @param uid
	 *            the implementation specific UID of the searched entity
	 */
	default public Node getEntityByUid(Session session, String parentPath, String uid) {
		if (isEmpty(uid))
			throw new ConnectException("uid cannot be null or empty");
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(parentPath));
			builder.append("//element(*, ").append(ConnectTypes.CONNECT_ENTITY).append(")");
			builder.append("[").append(XPathUtils.getPropertyEquals(ConnectNames.CONNECT_UID, uid)).append("]");
			Query xpathQuery = XPathUtils.createQuery(session, builder.toString());
			NodeIterator ni = xpathQuery.execute().getNodes();
			long niSize = ni.getSize();
			if (niSize == 0)
				return null;
			else if (niSize > 1) {
				// TODO rather include the calling stack in the thrown
				// ConnectException
				log.error("Found " + niSize + " entities with connect:uid [" + uid + "] - calling stack:\n "
						+ Thread.currentThread().getStackTrace().toString());
				Node first = ni.nextNode();
				throw new ConnectException(
						"Found " + niSize + " entities for People UID [" + uid + "], First occurence info:\npath: "
								+ first.getPath() + ", node type: " + first.getPrimaryNodeType().getName() + "");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to retrieve entity with connect:uid  [" + uid + "]", e);
		}
	}
}
