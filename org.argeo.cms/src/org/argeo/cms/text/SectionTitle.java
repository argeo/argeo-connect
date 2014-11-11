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

	public Section getSection() {
		return section;
	}
}
