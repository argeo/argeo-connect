package org.argeo.people.ui.dialogs;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.PeopleNames;
import org.argeo.people.ui.PeopleMsg;
import org.argeo.people.util.PeopleJcrUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

public class NewPersonPage extends WizardPage {
	private static final long serialVersionUID = -944349994177526468L;
	protected Text lastNameTxt;
	protected Text firstNameTxt;
	protected Text emailTxt;

	protected NewPersonPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		// FirstName
		ConnectUiUtils.createBoldLabel(parent, PeopleMsg.firstName.lead());
		firstNameTxt = new Text(parent, SWT.BORDER);
		firstNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// LastName
		ConnectUiUtils.createBoldLabel(parent, PeopleMsg.lastName.lead());
		lastNameTxt = new Text(parent, SWT.BORDER);
		lastNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ConnectUiUtils.createBoldLabel(parent, PeopleMsg.email.lead());
		emailTxt = new Text(parent, SWT.BORDER);
		emailTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ModifyListener ml = new ModifyListener() {
			private static final long serialVersionUID = -1628130380128946886L;

			@Override
			public void modifyText(ModifyEvent event) {
				getContainer().updateButtons();
			}
		};

		firstNameTxt.addModifyListener(ml);
		lastNameTxt.addModifyListener(ml);
		emailTxt.addModifyListener(ml);

		// Don't forget this.
		setControl(firstNameTxt);
		firstNameTxt.setFocus();

	}

	public void updateNode(Node node) {
		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING, lastNameTxt.getText());
		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
				firstNameTxt.getText());
		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_DISPLAY_NAME, PropertyType.STRING,
				firstNameTxt.getText() + " " + lastNameTxt.getText());
		String email = emailTxt.getText();
		ConnectJcrUtils.setJcrProperty(node, PeopleNames.PEOPLE_PRIMARY_EMAIL, PropertyType.STRING, email);
		//PeopleJcrUtils.createEmail(getResourcesService(), getPeopleService(), node, email, true, null, null);
	}
}
