package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

public interface PropertyTextPart extends EditableTextPart {
	public Property getProperty() throws RepositoryException;
}
