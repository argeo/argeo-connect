package org.argeo.tracker.e4.parts;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.eclipse.swt.widgets.Composite;

class SectionPart extends AbstractFormPart {
	private Composite section;

	public SectionPart(Composite section) {
		super();
		this.section = section;
	}

	@Deprecated
	public Section getSection() {
		return (Section) section;
	}

	public Composite getComposite() {
		return section;
	}
}
