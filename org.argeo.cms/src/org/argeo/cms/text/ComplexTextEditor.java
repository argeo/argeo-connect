package org.argeo.cms.text;

import javax.jcr.Node;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

public class ComplexTextEditor extends AbstractStructuredTextViewer {
	private static final long serialVersionUID = 6049661610883342325L;

	public ComplexTextEditor(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		super(parent, textNode, cmsEditable);
	}

}
