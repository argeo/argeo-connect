package org.argeo.connect.people.workbench.rap.wizards;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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

/** Ask first & last name. Update the passed node on finish */
public class NewPersonWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	private Node person;

	// This page widgets
	protected Text lastNameTxt;
	protected Text firstNameTxt;

	public NewPersonWizard(Node person) {
		this.person = person;
	}

	@Override
	public void addPages() {
		try {
			MainInfoPage page = new MainInfoPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
		setWindowTitle("New person");
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String lastName = lastNameTxt.getText();
		String firstName = firstNameTxt.getText();
		if (EclipseUiUtils.isEmpty(lastName)
				&& EclipseUiUtils.isEmpty(firstName)) {
			MessageDialog.openError(getShell(), "Non-valid information",
					"Please enter at least a name that is not empty.");
			return false;
		} else {
			ConnectJcrUtils.setJcrProperty(person, PEOPLE_LAST_NAME,
					PropertyType.STRING, lastName);
			ConnectJcrUtils.setJcrProperty(person, PEOPLE_FIRST_NAME,
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
		if (isEmpty(lastName) && isEmpty(firstName)) {
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
			ConnectWorkbenchUtils.createBoldLabel(parent, "Last Name");
			lastNameTxt = new Text(parent, SWT.BORDER);
			lastNameTxt.setMessage("a last name");
			lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			// FirstName
			ConnectWorkbenchUtils.createBoldLabel(parent, "First Name");
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