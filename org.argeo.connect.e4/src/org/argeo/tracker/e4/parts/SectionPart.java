package org.argeo.tracker.e4.parts;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;

class SectionPart extends AbstractFormPart {
	private Section section;

	public SectionPart(Section section) {
		super();
		this.section = section;
	}

	public Section getSection() {
		return section;
	}

}
