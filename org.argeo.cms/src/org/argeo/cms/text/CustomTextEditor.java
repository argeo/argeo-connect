package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.viewers.Section;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages named hardcoded sections as a flat hierarchy under the main section,
 * which contains no text but can manage a title.
 */
public class CustomTextEditor extends AbstractTextViewer {
	private static final long serialVersionUID = 5277789504209413500L;

	public CustomTextEditor(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		super(parent, textNode, cmsEditable);
	}

	@Override
	protected void refresh(TextSection section) throws RepositoryException {
		if (section == mainSection)
			for (Section s : section.getSubSections().values()) {
				if (s instanceof TextSection)
					super.refresh((TextSection) s);
			}
		else
			super.refresh(section);
	}

}
