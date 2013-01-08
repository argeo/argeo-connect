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
package org.argeo.connect.ui.gps.wizards;

import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Display a form to collect useful information to create a new repository
 */
public class DefineRepositoryModel extends WizardPage implements ModifyListener {

	// This page widget
	private Text techName;
	private Text displayName;

	// Define acceptable chars for the technical name
	private static Pattern p = Pattern.compile("^[A-Za-z0-9]+$");

	public DefineRepositoryModel() {
		super("Main");
		setTitle("Create a new local repository");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label lbl = new Label(composite, SWT.LEAD);
		lbl.setText("Technical name (no blank, no special chars)");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		techName = new Text(composite, SWT.LEAD | SWT.BORDER);
		techName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (techName != null)
			techName.addModifyListener(this);

		lbl = new Label(composite, SWT.LEAD);
		lbl.setText("Display name : ");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		displayName = new Text(composite, SWT.LEAD | SWT.BORDER);
		displayName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		if (displayName != null)
			displayName.addModifyListener(this);

		// Compulsory
		setControl(composite);

	}

	protected String getTechName() {
		return techName.getText();
	}

	protected String getDisplayName() {
		return displayName.getText();
	}

	private static boolean match(String s) {
		return p.matcher(s).matches();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		String message = checkComplete();
		if (message != null)
			setMessage(message, WizardPage.ERROR);
		else {
			setMessage("Complete", WizardPage.INFORMATION);
			setPageComplete(true);
		}
	}

	/** @return error message or null if complete */
	protected String checkComplete() {

		String techStr = techName.getText();
		if (techStr == null || "".equals(techStr))
			return "Please enter a short technical name for the new repository.";
		else if (!match(techStr))
			return "Please use only alphanumerical chars for the short technical name.";

		String displayStr = displayName.getText();
		if (displayStr == null || "".equals(displayStr))
			return "Please enter a displayable name for the new repository.";
		return null;
	}
}
