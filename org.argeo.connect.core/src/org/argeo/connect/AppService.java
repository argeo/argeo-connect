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

	public String getDefaultRelPath(Node entity) throws RepositoryException;

	public String getDefaultRelPath(String id);

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
	 * type or one of its mixin, typically for people
	 */
	default public String getMainNodeType(Node node) {
		return ConnectJcrUtils.get(node, Property.JCR_PRIMARY_TYPE);
	}

	default public String getLabel(String key, String... innerNames) {
		return key;
	}
}
