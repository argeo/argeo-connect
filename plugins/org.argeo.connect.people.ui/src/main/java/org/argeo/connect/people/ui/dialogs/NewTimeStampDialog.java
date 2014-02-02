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

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Enable creation of a time stamp: an award or a simple time stamp
 */
public class NewTimeStampDialog extends TrayDialog {
	private static final long serialVersionUID = 3766899676609659573L;
	// misc
	private String title;

	// this page widget
	private DateTime dateTimeCtl;
	private Button isAward;
	private Text countryTxt;
	private String countryIso;
	private Text titleTxt;
	private Text descTxt;

	// Business objects
	private Node filmNode;
	private Session session;

	public NewTimeStampDialog(Shell parentShell, String title, Node filmNode) {
		super(parentShell);
		this.filmNode = filmNode;
		try {
			this.session = filmNode.getSession();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get session", e);
		}
		this.title = title;
	}

	protected Point getInitialSize() {
		return new Point(400, 200);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogArea.setLayout(new GridLayout(3, false));

		// DATE
		// new Label(dialogArea, SWT.NONE).setText("Date");
		PeopleUiUtils.createBoldLabel(dialogArea, "Date");
		dateTimeCtl = new DateTime(dialogArea, SWT.DATE | SWT.MEDIUM
				| SWT.DROP_DOWN | SWT.BORDER);
		dateTimeCtl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				2, 1));

		// AWARD
		isAward = new Button(dialogArea, SWT.CHECK | SWT.RIGHT);
		isAward.setText("Is Award");
		isAward.setFont(EclipseUiUtils.getBoldFont(dialogArea));
		isAward.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		// Countries for awards
		countryTxt = new Text(dialogArea, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		countryTxt.setMessage("Country");
		countryTxt.setEnabled(false);
		countryTxt.setVisible(false);
		countryTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		countryTxt.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_ALWAYS_SHOW_BORDER);

		final Link chooseCountryLk = new Link(dialogArea, SWT.BOTTOM);
		chooseCountryLk.setText("<a>Choose a country</a>");
		chooseCountryLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 7876174121605550407L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpCountryDialog diag = new PickUpCountryDialog(
							chooseCountryLk.getShell(), "Choose a country",
							filmNode.getSession());
					diag.open();
					countryIso = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(countryIso)) {
						String country = ResourcesJcrUtils
								.getCountryEnLabelFromIso(session, countryIso);
						countryTxt.setText(country);
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add a country", e);
				}
			}
		});
		chooseCountryLk.setVisible(false);

		isAward.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 2623945938776438301L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean flag = isAward.getSelection();
				countryTxt.setVisible(flag);
				chooseCountryLk.setVisible(flag);
			}
		});

		// Title and description
		titleTxt = createLT(dialogArea, "Title", 2);
		descTxt = createLT(dialogArea, "Description", 2);
		parent.pack();
		return dialogArea;
	}

	/** Creates label and text. text control has an horizontal span of 2 */
	protected Text createLT(Composite parent, String label, int horizontalSpan) {
		PeopleUiUtils.createBoldLabel(parent, label);
		Text text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				horizontalSpan, 1));
		return text;
	}

	@Override
	protected void okPressed() {
		String title = titleTxt.getText(), desc = descTxt.getText();
		boolean award = isAward.getSelection();

		Calendar cal = GregorianCalendar.getInstance();
		cal.set(dateTimeCtl.getYear(), dateTimeCtl.getMonth(),
				dateTimeCtl.getDay(), 12, 0);

		String errMmsg = null;
		if (CommonsJcrUtils.isEmptyString(title))
			errMmsg = "Please enter a non empty title";
		else if (award && countryIso == null)
			errMmsg = "Please choose a country";

		if (errMmsg != null) {
			MessageDialog.openError(getShell(), "Invalid data", errMmsg);
			return;
		}
		if (award)
			FilmJcrUtils.createAward(filmNode, cal, countryIso, title, desc);
		else
			FilmJcrUtils.createTimestamp(filmNode, cal, title, desc);
		super.okPressed();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
}