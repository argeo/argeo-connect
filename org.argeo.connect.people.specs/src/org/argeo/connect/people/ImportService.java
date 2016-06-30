package org.argeo.connect.people;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** Provides method interfaces to import and merge data in a people repository */
public interface ImportService {

	/**
	 * Best effort to merge 2 nodes given a master and a slave. Session is not
	 * saved.
	 */
	public void mergeNodes(Node masterNode, Node slaveNode)
			throws RepositoryException;
}
