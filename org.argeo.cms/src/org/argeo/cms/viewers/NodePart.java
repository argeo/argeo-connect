package org.argeo.cms.viewers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** An editable part related to a node */
public interface NodePart extends ItemPart<Node> {
	public Node getNode() throws RepositoryException;
}
