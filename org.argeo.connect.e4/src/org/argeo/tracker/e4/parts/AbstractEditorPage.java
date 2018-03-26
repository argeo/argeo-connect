package org.argeo.tracker.e4.parts;

import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.cms.ui.eclipse.forms.ManagedForm;

class AbstractEditorPage {

	private AbstractTrackerEditor editor;
	private String pageId;
	private String label;

	public AbstractEditorPage(AbstractTrackerEditor editor, String pageId, String label) {
		super();
		this.editor = editor;
		this.pageId = pageId;
		this.label = label;
	}

	public ManagedForm getManagedForm() {
		return null;
	}

	protected void createFormContent(final IManagedForm mf) {

	}

	public void setActive(boolean active) {
		// TODO implement
	}
}
