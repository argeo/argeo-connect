package org.argeo.cms.text;

import org.eclipse.swt.widgets.Composite;

/** The title of a section. */
public class SectionTitle extends StyledComposite {
	private static final long serialVersionUID = -1787983154946583171L;

	private final Section section;

	public SectionTitle(Composite parent, int swtStyle) {
		super(parent, swtStyle);
		section = Section.findSection(this);
	}

//	public void updateContent() throws RepositoryException {
//		if (section.getRelativeDepth() == 0)
//			setStyle(TextStyles.TEXT_TITLE);
//		else
//			setStyle(TextStyles.TEXT_H + section.getRelativeDepth());
//
//		// retrieve control AFTER setting style, since
//		// it may have been reset
//		setText((Property) getData());
//
//		// if (section.getViewer().getCmsEditable().canEdit())
//		// setMouseListener((MouseListener) section.getViewer());
//	}

	public Section getSection() {
		return section;
	}

}
