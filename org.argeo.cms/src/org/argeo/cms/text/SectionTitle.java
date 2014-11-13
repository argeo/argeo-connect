package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.widgets.EditablePart;
import org.argeo.cms.widgets.EditableText;
import org.eclipse.swt.widgets.Composite;

/** The title of a section. */
public class SectionTitle extends EditableText implements EditablePart {
	private static final long serialVersionUID = -1787983154946583171L;

	private final TextSection section;

	public SectionTitle(Composite parent, int swtStyle) {
		super(parent, swtStyle);
		section = (TextSection) TextSection.findSection(this);
	}

	public TextSection getSection() {
		return section;
	}

	public Property getProperty() throws RepositoryException {
		return getSection().getNode().getProperty(Property.JCR_TITLE);
	}
}
