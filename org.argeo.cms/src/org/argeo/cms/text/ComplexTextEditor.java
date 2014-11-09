package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

public class ComplexTextEditor extends AbstractStructuredTextViewer {
	private static final long serialVersionUID = 6049661610883342325L;

	public ComplexTextEditor(Composite parent, Node textNode,
			CmsEditable cmsEditable) throws RepositoryException {
		super(parent, textNode, cmsEditable);

		if (!textNode.hasProperty(Property.JCR_TITLE)) {
			textNode.setProperty(Property.JCR_TITLE, textNode.getName());
			refresh();
		}
	}

}
