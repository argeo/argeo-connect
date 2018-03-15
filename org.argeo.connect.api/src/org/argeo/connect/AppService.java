package org.argeo.connect;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/** Minimal interface that a Connect AppService must implement */
public interface AppService {
	// final static Log log = LogFactory.getLog(AppService.class);

	/** Returns the current App name */
	public String getAppBaseName();

	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException;

	/**
	 * Try to save and optionally publish a business object after applying context
	 * specific rules and special behaviours (typically cache updates).
	 * 
	 * @return the entity that has been saved (and optionally published): note that
	 *         in some cases (typically, the first save of a draft node in the
	 *         business sub tree) the returned node is not the same as the one that
	 *         has been passed
	 * @param entity
	 * @param publish
	 *            also publishes the corresponding node
	 * @throws PeopleException
	 *             If one of the rule defined for this type is not respected. Use
	 *             getMessage to display to the user if needed
	 */
	public Node saveEntity(Node entity, boolean publish);

	/**
	 * Computes the App specific relative path for a known type based on properties
	 * of the passed node
	 */
	public String getDefaultRelPath(Node entity) throws RepositoryException;

	/**
	 * Computes the App specific relative path for this known node type based on the
	 * passed id
	 * 
	 * @param session
	 *            TODO
	 */
	public String getDefaultRelPath(Session session, String nodeType, String id);

	/**
	 * Returns a display name that is app specific and that depends on one or more
	 * of the entity properties. The user can always set a flag to force the value
	 * to something else.
	 * 
	 * The Display name is usually stored in the JCR_TITLE property.
	 */
	public String getDisplayName(Node entity);

	/**
	 * Returns (after creation if necessary) the base parent for draft nodes of this
	 * application
	 */
	public Node getDraftParent(Session session) throws RepositoryException;

	/**
	 * Convenience method to create a Node with given mixin under the current logged
	 * in user home. Creates a UUID and set the connect:uid properties. The session
	 * is not saved.
	 */
	public Node createDraftEntity(Session session, String mainMixin) throws RepositoryException;

	/**
	 * Simply checks if the passed entity has a primary or mixin type that is known
	 * and thus can be managed by the this App
	 */
	public boolean isKnownType(Node entity);

	/**
	 * Simply checks if the passed type is known and thus can be managed by the this
	 * App. It might be a primary or mixin type
	 */
	public boolean isKnownType(String nodeType);

	/**
	 * Searches the workspace corresponding to the passed session. It returns the
	 * corresponding entity or null if none has been found. This UID is
	 * implementation specific and is not a JCR Identifier.
	 * 
	 * It will throw a PeopleException if more than one item with this ID has been
	 * found
	 * 
	 * @param session
	 * @param parentPath
	 *            can be null or empty
	 * @param uid
	 *            the implementation specific UID of the searched entity
	 */
	public Node getEntityByUid(Session session, String parentPath, String uid);

	//
	// CANONICAL DEFAULTS
	//

	/**
	 * Returns the App specific main type of a node, that can be its primary type or
	 * one of its mixin, typically for the People App.
	 */
	default public String getMainNodeType(Node node) {
		return null;
	}

	default public String getBaseRelPath(String nodeType) {
		return getAppBaseName();
	}

	default public Node publishEntity(Node parent, String nodeType, Node srcNode) throws RepositoryException {
		return publishEntity(parent, nodeType, srcNode, true);
	}

	/**
	 * Draft implementation of an i18n mechanism to retrieve labels given a key
	 */
	default public String getLabel(String key, String... innerNames) {
		return key;
	}

}
