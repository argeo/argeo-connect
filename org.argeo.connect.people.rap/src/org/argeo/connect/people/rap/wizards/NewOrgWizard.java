package org.argeo.connect.people.rap.wizards;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to manually create a new organisation in the system. The node
 * must have been created and is only updated with user filled info. The session
 * is *NOT* saved.
 */

public class NewOrgWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(NewOrgWizard.class);

	// Context
	// private PeopleService peopleService;
	private Node org;

	// This page widgets
	private Text legalNameTxt;
	private Button useDistinctDisplayNameBtn;
	private Text displayNameTxt;
	private Text legalFormTxt;

	public NewOrgWizard(PeopleService peopleService, Node org) {
		// this.peopleService = peopleService;
		this.org = org;
	}

	@Override
	public void addPages() {
		// Configure the wizard
		// setDefaultPageImageDescriptor(PeopleImages.);
		try {
			MainInfoPage page = new MainInfoPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String legalName = legalNameTxt.getText();
		String displayName = displayNameTxt.getText();

		boolean useDistinctDisplayName = useDistinctDisplayNameBtn
				.getSelection();
		String legalForm = legalFormTxt.getText();

		if (EclipseUiUtils.isEmpty(legalName)
				&& EclipseUiUtils.isEmpty(displayName)) {
			MessageDialog
					.openError(getShell(), "Non-valid information",
							"Please enter at least a legal or a display name that is not empty.");
			return false;
		}

		JcrUiUtils.setJcrProperty(org, PEOPLE_LEGAL_NAME, PropertyType.STRING,
				legalName);
		if (useDistinctDisplayName) {
			JcrUiUtils.setJcrProperty(org, PEOPLE_USE_DISTINCT_DISPLAY_NAME,
					PropertyType.BOOLEAN, useDistinctDisplayName);
			JcrUiUtils.setJcrProperty(org, Property.JCR_TITLE,
					PropertyType.STRING, displayName);
		}
		if (EclipseUiUtils.notEmpty(legalForm))
			JcrUiUtils.setJcrProperty(org, PEOPLE_LEGAL_FORM,
					PropertyType.STRING, legalForm);
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Create an organisation");
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// Legal Name
			PeopleRapUtils.createBoldLabel(parent, "Legal Name");
			legalNameTxt = new Text(parent, SWT.BORDER);
			legalNameTxt.setMessage("the legal name");
			legalNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			// Legal Form
			PeopleRapUtils.createBoldLabel(parent, "Legal Form");
			legalFormTxt = new Text(parent, SWT.BORDER);
			legalFormTxt.setMessage("the legal name (Ltd, Org, GmbH...) ");
			legalFormTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			// Display Name
			useDistinctDisplayNameBtn = new Button(parent, SWT.CHECK);
			useDistinctDisplayNameBtn
					.setText("Define a display name that is not the legal name");
			useDistinctDisplayNameBtn.setLayoutData(new GridData(SWT.FILL,
					SWT.CENTER, true, false, 2, 1));

			PeopleRapUtils.createBoldLabel(parent, "Display Name");
			displayNameTxt = new Text(parent, SWT.BORDER);
			displayNameTxt.setMessage("an optional display name");
			displayNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					true, false));
			displayNameTxt.setEnabled(false);

			useDistinctDisplayNameBtn
					.addSelectionListener(new SelectionAdapter() {
						private static final long serialVersionUID = 1L;

						@Override
						public void widgetSelected(SelectionEvent e) {
							displayNameTxt.setEnabled(useDistinctDisplayNameBtn
									.getSelection());
						}
					});

			// Don't forget this.
			setControl(legalNameTxt);
			legalNameTxt.setFocus();
		}
	}
}