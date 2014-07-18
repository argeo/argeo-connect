package org.argeo.connect.people.ui.wizards;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.dialogs.PickUpGroupDialog;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to manually create a new "Person" entry in the system
 * 
 * The node must have been created. But the wizard will try to save and commit
 * using the PeopleServiec before returning SWT.OK. the caller might then call
 * the "open entity editor" command if needed.
 */

public class NewPersonWizard extends Wizard implements PeopleNames {
	private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	private PeopleService peopleService;
	private Node person;

	// This page widgets
	protected Text lastNameTxt;
	protected Text firstNameTxt;

	public NewPersonWizard(PeopleService peopleService, Node person) {
		this.peopleService = peopleService;
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
		// Sanity check
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();

		CommonsJcrUtils.setJcrProperty(person, PEOPLE_LAST_NAME,
				PropertyType.STRING, lastName);
		CommonsJcrUtils.setJcrProperty(person, PEOPLE_FIRST_NAME,
				PropertyType.STRING, lastName);

		try {
			peopleService.saveEntity(person, true);
		} catch (PeopleException e) {
			MessageDialog.openError(getShell(), "Unvalid information",
					e.getMessage());
			log.warn("Unable to save newly created person " + lastName + ", "
					+ firstName);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		// TODO implement Sanity check
//		String lastName = lastNameTxt.getText();
//		String firstName = firstNameTxt.getText();
		return true;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Create a contact");
			setMessage("Please enter a last name and / or a first name.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// LastName
			PeopleUiUtils.createBoldLabel(parent, "Last Name");
			lastNameTxt = new Text(parent, SWT.BORDER);
			lastNameTxt.setMessage("a last name");
			lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// FirstName
			// LastName
			PeopleUiUtils.createBoldLabel(parent, "First Name");
			firstNameTxt = new Text(parent, SWT.BORDER);
			firstNameTxt.setMessage("a first name");
			firstNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Don't forget this.
			setControl(lastNameTxt);
			lastNameTxt.setFocus();
		}

	}
}