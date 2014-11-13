package org.argeo.cms.viewers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.widgets.EditablePart;

/** An editable part related to a section */
public interface SectionPart extends EditablePart {
	public String getNodeId();

	public Node getNode() throws RepositoryException;

	public Section getSection();
}
