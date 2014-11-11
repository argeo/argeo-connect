package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface NodeTextPart extends EditableTextPart {
	public Node getNode() throws RepositoryException;
}
