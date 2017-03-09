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
package org.argeo.connect.workbench.parts;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;

/** Dialog with a calendar to choose a date */
public class PickUpDateDialog extends TrayDialog {
	private static final long serialVersionUID = 3806120909726231133L;

	// this page widgets and UI objects
	private DateTime dateTimeCtl;
	private String title;

	private Calendar selectedDate = GregorianCalendar.getInstance();

	public PickUpDateDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	protected Point getInitialSize() {
		return new Point(240, 240);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(EclipseUiUtils.noSpaceGridLayout());

		dateTimeCtl = new DateTime(dialogArea, SWT.CALENDAR);
		dateTimeCtl.setLayoutData(EclipseUiUtils.fillAll());

		dateTimeCtl.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -8414377364434281112L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedDate.set(dateTimeCtl.getYear(), dateTimeCtl.getMonth(),
						dateTimeCtl.getDay(), 12, 0);
			}
		});

		parent.pack();
		return dialogArea;
	}

	public Calendar getSelected() {
		return selectedDate;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
}
