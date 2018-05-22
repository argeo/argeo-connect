package org.argeo.people.ui.dialogs;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.Selected;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleMsg;
import org.argeo.people.util.PeopleJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Ask legal name and form, and an optional display name. Update the node that
 * has been created and passed on finish. The session is *NOT* saved.
 */

public class NewOrgWizard extends Wizard implements PeopleNames {

	// Context
	private Node draft;

	private PeopleService peopleService;
	private ResourcesService resourcesService;

	// This page widgets
	private Text legalNameTxt;
	// private Button useDistinctDisplayNameBtn;
	// private Text displayNameTxt;
	private Text legalFormTxt;

	Button createUser;
	Text userFirstName, userLastName, userEmail;

	public NewOrgWizard(Node org, PeopleService peopleService, ResourcesService resourcesService) {
		this.draft = org;
		this.peopleService = peopleService;
		this.resourcesService = resourcesService;
	}

	@Override
	public void addPages() {
		try {
			MainInfoPage page = new MainInfoPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
		setWindowTitle(PeopleMsg.orgWizardWindowTitle.lead());
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String legalName = legalNameTxt.getText();
		// boolean useDistinctDisplayName = useDistinctDisplayNameBtn.getSelection();
		String legalForm = legalFormTxt.getText();
		// String displayName = displayNameTxt.getText();

		if (EclipseUiUtils.isEmpty(legalName)) {
			MessageDialog.openError(getShell(), "Non-valid information",
					"Please enter at least a legal or a display name that is not empty.");
			return false;
		}

		ConnectJcrUtils.setJcrProperty(draft, PEOPLE_LEGAL_NAME, PropertyType.STRING, legalName);
		// if (useDistinctDisplayName)
		// ConnectJcrUtils.setJcrProperty(org, PEOPLE_DISPLAY_NAME, PropertyType.STRING,
		// displayName);
		if (EclipseUiUtils.notEmpty(legalForm))
			ConnectJcrUtils.setJcrProperty(draft, PEOPLE_LEGAL_FORM, PropertyType.STRING, legalForm);

		if (createUser.getSelection()) {
			try {
				Node person = draft.addNode(PeopleNames.PEOPLE_ROLE);
				person.addMixin(PeopleTypes.PEOPLE_PERSON);
				String personUid = UUID.randomUUID().toString();
				// TODO centralize with new person page
				ConnectJcrUtils.setJcrProperty(person, ConnectNames.CONNECT_UID, PropertyType.STRING, personUid);
				ConnectJcrUtils.setJcrProperty(person, PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
						userFirstName.getText());
				ConnectJcrUtils.setJcrProperty(person, PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING,
						userLastName.getText());
				ConnectJcrUtils.setJcrProperty(person, PeopleNames.PEOPLE_DISPLAY_NAME, PropertyType.STRING,
						userFirstName.getText() + " " + userLastName.getText());
				String email = userEmail.getText();
				ConnectJcrUtils.setJcrProperty(person, PeopleNames.PEOPLE_PRIMARY_EMAIL, PropertyType.STRING, email);
				PeopleJcrUtils.createEmail(resourcesService, peopleService, person, email, true, null, null);
			} catch (RepositoryException e) {
				throw new ConnectException("Cannot add person", e);
			}
		}
		return true;
	}

	protected Node getDraft() {
		return draft;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected ResourcesService getResourcesService() {
		return resourcesService;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle(PeopleMsg.orgWizardPageTitle.lead());
			// setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// Legal Name
			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.legalName);
			legalNameTxt = new Text(parent, SWT.BORDER);
			// legalNameTxt.setMessage("the legal name");
			legalNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Legal Form
			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.legalForm);
			legalFormTxt = new Text(parent, SWT.BORDER);
			// legalFormTxt.setMessage("the legal name (Ltd, Org, GmbH...) ");
			legalFormTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Display Name
			// useDistinctDisplayNameBtn = new Button(parent, SWT.CHECK);
			// useDistinctDisplayNameBtn.setText("Define a display name that is not the
			// legal name");
			// useDistinctDisplayNameBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
			// true, false, 2, 1));
			//
			// ConnectWorkbenchUtils.createBoldLabel(parent, "Display Name");
			// displayNameTxt = new Text(parent, SWT.BORDER);
			// displayNameTxt.setMessage("an optional display name");
			// displayNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
			// false));
			// displayNameTxt.setEnabled(false);
			//
			// useDistinctDisplayNameBtn.addSelectionListener(new SelectionAdapter() {
			// private static final long serialVersionUID = 1L;
			//
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// displayNameTxt.setEnabled(useDistinctDisplayNameBtn.getSelection());
			// }
			// });
			createUser = new Button(parent, SWT.CHECK);
			createUser.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.personWizardPageTitle);

			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.firstName);
			userFirstName = new Text(parent, SWT.BORDER);
			userFirstName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.lastName);
			userLastName = new Text(parent, SWT.BORDER);
			userLastName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ConnectUiUtils.createBoldLabel(parent, PeopleMsg.email);
			userEmail = new Text(parent, SWT.BORDER);
			userEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			createUser.addSelectionListener((Selected) (e) -> {
				updateUserForm();
			});
			createUser.setSelection(true);
			updateUserForm();

			// Don't forget this.
			setControl(legalNameTxt);
			legalNameTxt.setFocus();
		}

		private void updateUserForm() {
			userFirstName.setEnabled(createUser.getSelection());
			userLastName.setEnabled(createUser.getSelection());
			userEmail.setEnabled(createUser.getSelection());
		}
	}
}
