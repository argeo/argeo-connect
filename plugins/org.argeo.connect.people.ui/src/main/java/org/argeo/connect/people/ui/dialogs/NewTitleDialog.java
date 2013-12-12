/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.ui.dialogs;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Enable creation of a film title
 */
public class NewTitleDialog extends TrayDialog {
	private static final long serialVersionUID = 3766899676609659573L;
	// misc
	private String title;

	// this page widget
	private Button isOriginal;
	private Button isPrimary;
	private Text langTxt;
	private String langIso;
	private Text titleTxt;
	private Text articleTxt;
	private Text latinTxt;

	// Business objects
	private Node filmNode;

	public NewTitleDialog(Shell parentShell, String title, Node filmNode) {
		super(parentShell);
		this.filmNode = filmNode;
		this.title = title;
	}

	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogArea.setLayout(new GridLayout(3, false));

		// Check boxes
		isOriginal = new Button(dialogArea, SWT.CHECK);
		isOriginal.setText("Original Title");
		isOriginal.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false,
				3, 1));
		isPrimary = new Button(dialogArea, SWT.CHECK);
		isPrimary.setText("Primary Title");
		isPrimary.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false,
				3, 1));

		langTxt = createLT(dialogArea, "Language", 1);
		langTxt.setEditable(false);
		// Choose a given language
		final Link chooseLangLk = new Link(dialogArea, SWT.BOTTOM);
		chooseLangLk.setText("<a>Pick up a language</a>");
		chooseLangLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpLangDialog diag = new PickUpLangDialog(chooseLangLk
							.getShell(), "Choose a language", filmNode
							.getSession());
					diag.open();
					langIso = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(langIso)){
						langTxt.setText(ResourcesJcrUtils.getLangEnLabelFromIso(filmNode.getSession(), langIso));
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		});

		// Values
		titleTxt = createLT(dialogArea, "Title", 2);
		articleTxt = createLT(dialogArea, "Title article", 2);
		latinTxt = createLT(dialogArea, "Latin pronunciation", 2);
		latinTxt.setToolTipText("Fill a latin pronunciation for a "
				+ "given language title if necessary");

		parent.pack();
		return dialogArea;
	}

	/** Creates label and text. text control has an horizontal span of 2 */
	protected Text createLT(Composite parent, String label, int horizontalSpan) {
		new Label(parent, SWT.NONE).setText(label);
		Text text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				horizontalSpan, 1));
		return text;
	}

	@Override
	protected void okPressed() {
		String title = titleTxt.getText(), article = articleTxt.getText(), latin = latinTxt.getText();
		boolean orig = isOriginal.getSelection(), prim = isPrimary
				.getSelection();

		String errMmsg = null;
		if (CommonsJcrUtils.isEmptyString(title))
			errMmsg = "Please enter a non empty title";
		else if (CommonsJcrUtils.isEmptyString(langIso))
			errMmsg = "Please choose a language";

		if (errMmsg != null) {
			MessageDialog.openError(getShell(), "Invalid data", errMmsg);
			return;
		}

		// real addition
		FilmJcrUtils
				.addTitle(filmNode, title, article, latin, langIso, orig, prim);
		// will throw an error if the corresponding film is not checked out,
		// that is what we expect.
		super.okPressed();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
}