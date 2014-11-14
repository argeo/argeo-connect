package org.argeo.cms.viewers;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

/** An editable part related to a JCR Property */
public interface PropertyPart extends ItemPart<Property> {
	public Property getProperty() throws RepositoryException;
}
