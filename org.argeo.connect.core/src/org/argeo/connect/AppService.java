package org.argeo.connect;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/** Minimal interface that an Argeo App must implement */
public interface AppService {

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
}
