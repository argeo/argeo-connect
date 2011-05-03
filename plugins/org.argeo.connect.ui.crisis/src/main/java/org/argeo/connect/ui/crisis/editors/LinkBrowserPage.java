package org.argeo.connect.ui.crisis.editors;

import org.argeo.connect.ConnectNames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class LinkBrowserPage extends FormPage implements ConnectNames {
	private LinkEditorInput editorInput;
	private Browser browser;

	public LinkBrowserPage(FormEditor editor, String id, String title,
			LinkEditorInput editorInput) {
		super(editor, id, title);
		this.editorInput = editorInput;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());

		FormToolkit tk = managedForm.getToolkit();

		// upper left
		// Composite upperLeft = tk.createComposite(parent);
		// upperLeft.setLayout(new GridLayout(1, true));

		browser = new Browser(parent, SWT.NONE);
		browser.setUrl(editorInput.getUrl());

		LinkFormPart linkFormPart = new LinkFormPart();
		getManagedForm().addPart(linkFormPart);
		// URL changes from within the browser are not notified to RAP
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=243743
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196787
		browser.addLocationListener(linkFormPart);

		tk.adapt(browser);
	}

	public String getUrl() {
		return browser.getUrl();
	}

	private static class LinkFormPart extends AbstractFormPart implements
			LocationListener {

		public void changing(LocationEvent event) {
		}

		public void changed(LocationEvent event) {
			markDirty();
		}

	}

}
