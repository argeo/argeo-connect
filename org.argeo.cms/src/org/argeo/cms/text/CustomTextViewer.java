package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsEditable;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages named hardcoded sections as a flat hierarchy under the main section,
 * which contains no text but can manage a title.
 */
public class CustomTextViewer extends AbstractTextViewer {
	private static final long serialVersionUID = 5277789504209413500L;

	public CustomTextViewer(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		super(parent, textNode, cmsEditable);
	}

	@Override
	protected void refresh(Section section) throws RepositoryException {
		if (section == mainSection)
			for (Section s : section.getSubSections().values())
				super.refresh(s);
		else
			super.refresh(section);
	}

}
