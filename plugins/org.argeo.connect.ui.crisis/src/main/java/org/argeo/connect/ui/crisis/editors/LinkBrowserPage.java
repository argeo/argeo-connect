/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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
