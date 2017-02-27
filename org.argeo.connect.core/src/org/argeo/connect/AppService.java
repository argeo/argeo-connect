package org.argeo.connect;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;

/** Minimal interface that an Argeo App must implement */
public interface AppService {

	/** Returns the current App name */
	public String getAppBaseName();

	default public String getDefaultBasePath() {
		return "/" + getAppBaseName();
	}

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
	 * Computes the App specific relative path for a known type based on
	 * properties of the passed node
	 */
	public String getDefaultRelPath(Node entity) throws RepositoryException;

	/**
	 * Computes the App specific relative path for this known node type based on
	 * the passed id
	 */
	public String getDefaultRelPath(String nodeType, String id);

	/**
	 * Try to save and optionally publish a business object after applying
	 * context specific rules and special behaviours (typically cache updates).
	 * 
	 * @return the entity that has been saved (and optionally published): note
	 *         that is some cases (typically, the first save of a draft node in
	 *         the business sub tree) the returned node is not the same as the
	 *         one that has been passed
	 * @param entity
	 * @param publish
	 *            also publish the corresponding node
	 * @throws PeopleException
	 *             If one a the rule defined for this type is not respected. Use
	 *             getMessage to display to the user if needed
	 */
	default public Node saveEntity(Node entity, boolean publish) {
		try {
			if (entity.getSession().hasPendingChanges())
				entity.getSession().save();
			if (entity.isNodeType(NodeType.MIX_VERSIONABLE))
				// TODO check if some changes happened since last checkpoint
				entity.getSession().getWorkspace().getVersionManager().checkpoint(entity.getPath());
			return entity;
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot save " + entity, e);
		}
	}

	default public Node getDraftParent(Session session) throws RepositoryException {
		Node home = NodeUtils.getUserHome(session);
		String draftRelPath = ConnectConstants.HOME_APP_SYS_RELPARPATH + "/" + getAppBaseName();
		return JcrUtils.mkdirs(home, draftRelPath);
	}

	/**
	 * Returns the App specific main type of a node, that can be its primary
	 * type or one of its mixin, typically for People App.
	 */
	default public String getMainNodeType(Node node) {
		return ConnectJcrUtils.get(node, Property.JCR_PRIMARY_TYPE);
	}

	public boolean isKnownType(Node entity);

	public boolean isKnownType(String nodeType);

	default public String getLabel(String key, String... innerNames) {
		return key;
	}
}
