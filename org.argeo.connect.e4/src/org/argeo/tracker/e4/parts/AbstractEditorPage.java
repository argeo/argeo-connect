package org.argeo.tracker.e4.parts;

import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

abstract class AbstractEditorPage {

	private AbstractTrackerEditor editor;
	private String pageId;
	private String label;

	private IManagedForm managedForm;

	public AbstractEditorPage(AbstractTrackerEditor editor, String pageId, String label) {
		super();
		this.editor = editor;
		this.pageId = pageId;
		this.label = label;
	}

	@Deprecated
	IManagedForm getPageManagedForm() {
		return managedForm;
		// return editor.getManagedForm();
	}

	void createUi(IManagedForm mf) {
		managedForm = mf;
		createFormContent(mf);
	}

	// void createUi(Composite parent) {
	// managedForm = editor.getManagedForm();
	// }

	void createFormContent(final IManagedForm mf) {
		ScrolledComposite form = mf.getForm();
		Composite body = new Composite(form, SWT.NONE);
		form.setContent(body);
		GridLayout layout = new GridLayout();
		body.setLayout(layout);
		body.setLayoutData(new GridData(SWT.FILL,SWT.FILL));
		createFormContent(body);
	}

	void createFormContent(Composite body) {

	}

	// protected void createFormContent(final IManagedForm mf) {
	//
	// }

	public void setActive(boolean active) {
		// TODO implement
	}

	public String getPageId() {
		return pageId;
	}

	public String getLabel() {
		return label;
	}

}
