package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;

/** The title of a section. */
public class SectionTitle extends EditableText implements PropertyTextPart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final Section section;

	public SectionTitle(Composite parent, int swtStyle) {
		super(parent, swtStyle);
		section = Section.findSection(this);
	}

	public Section getSection() {
		return section;
	}

	public Property getProperty() throws RepositoryException {
		return getSection().getNode().getProperty(Property.JCR_TITLE);
	}
}
