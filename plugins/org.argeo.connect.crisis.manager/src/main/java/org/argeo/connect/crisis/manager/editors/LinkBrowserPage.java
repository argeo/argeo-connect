package org.argeo.connect.crisis.manager.editors;

import org.argeo.connect.ConnectNames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class LinkBrowserPage extends FormPage implements ConnectNames {
	private LinkEditorInput editorInput;

	public LinkBrowserPage(FormEditor editor,String id, String title, LinkEditorInput editorInput) {
		super(editor,id, title);
		this.editorInput = editorInput;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());

		FormToolkit tk = managedForm.getToolkit();

		// upper left
//		 Composite upperLeft = tk.createComposite(parent);
//		 upperLeft.setLayout(new GridLayout(1, true));

		Browser browser = new Browser(parent, SWT.NONE);
		browser.setUrl(editorInput.getUrl());
		tk.adapt(browser);
	}

}
