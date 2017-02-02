package org.argeo.connect.people.workbench.rap.wizards;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
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
public class NewMailingListWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	private Node mailingList;

	// This page widgets
	protected Text nameTxt;

	public NewMailingListWizard(Node mailingList) {
		this.mailingList = mailingList;
	}

	@Override
	public void addPages() {
		try {
			MainInfoPage page = new MainInfoPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
		setWindowTitle("New Mailing List");
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		String name = nameTxt.getText();
		if (EclipseUiUtils.isEmpty(name)) {
			MessageDialog.openError(getShell(), "Non-valid information", "Please enter a name that is not empty.");
			return false;
		} else {
			// TODO implement this
			return true;
		}
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		String name = nameTxt.getText();
		if (isEmpty(name)) {
			return false;
		} else
			return true;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Create a mailing list");
			setMessage("Please enter a name for the list to create");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// LastName
			PeopleRapUtils.createBoldLabel(parent, "Mailing list name");
			nameTxt = new Text(parent, SWT.BORDER);
			nameTxt.setMessage("a label");
			nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			ModifyListener ml = new ModifyListener() {
				private static final long serialVersionUID = -1628130380128946886L;

				@Override
				public void modifyText(ModifyEvent event) {
					getContainer().updateButtons();
				}
			};

			nameTxt.addModifyListener(ml);

			// Don't forget this.
			setControl(nameTxt);
			nameTxt.setFocus();
		}
	}
}
