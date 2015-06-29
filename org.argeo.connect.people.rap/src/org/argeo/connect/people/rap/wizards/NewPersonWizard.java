package org.argeo.connect.people.rap.wizards;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to manually create a new "Person" entry in the system
 * 
 * The node must have been created. But the wizard will try to save and commit
 * using the PeopleService before returning SWT.OK. The caller might then call
 * the "open entity editor" command if needed.
 */

public class NewPersonWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	// private PeopleService peopleService;
	private Node person;

	// This page widgets
	protected Text lastNameTxt;
	protected Text firstNameTxt;

	public NewPersonWizard(PeopleService peopleService, Node person) {
		// this.peopleService = peopleService;
		this.person = person;
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
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();
		if (CommonsJcrUtils.isEmptyString(lastName)
				&& CommonsJcrUtils.isEmptyString(firstName)) {
			MessageDialog.openError(getShell(), "Non-valid information",
					"Please enter at least a name that is not empty.");
			return false;
		} else {
			CommonsJcrUtils.setJcrProperty(person, PEOPLE_LAST_NAME,
					PropertyType.STRING, lastName);
			CommonsJcrUtils.setJcrProperty(person, PEOPLE_FIRST_NAME,
					PropertyType.STRING, firstName);
			return true;
		}
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();
		if (CommonsJcrUtils.isEmptyString(lastName)
				&& CommonsJcrUtils.isEmptyString(firstName)) {
			return false;
		} else
			return true;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Create a contact");
			setMessage("Please enter a last name and/or a first name.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// LastName
			PeopleRapUtils.createBoldLabel(parent, "Last Name");
			lastNameTxt = new Text(parent, SWT.BORDER);
			lastNameTxt.setMessage("a last name");
			lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			// FirstName
			PeopleRapUtils.createBoldLabel(parent, "First Name");
			firstNameTxt = new Text(parent, SWT.BORDER);
			firstNameTxt.setMessage("a first name");
			firstNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = -1628130380128946886L;

				@Override
				public void modifyText(ModifyEvent event) {
					getContainer().updateButtons();
				}
			};

			firstNameTxt.addModifyListener(ml);
			lastNameTxt.addModifyListener(ml);

			// Don't forget this.
			setControl(lastNameTxt);
			lastNameTxt.setFocus();
		}
	}
}