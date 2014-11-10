package org.argeo.cms.text;

import javax.jcr.Node;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

/**
 * Minimalistic editor which manages only a list of paragraphs, but no section
 * and no title.
 */
public class FlatTextEditor extends AbstractTextViewer {
	private static final long serialVersionUID = 2557391868864854943L;

	public FlatTextEditor(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		super(parent, textNode, cmsEditable);
	}

}
