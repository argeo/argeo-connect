package org.argeo.connect.tracker.internal.ui.dialogs;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Manually register a new prospect in the system. The node must have been
 * created. But the wizard will try to save and commit using the PeopleService
 * before returning SWT.OK. The caller might then call the "open entity editor"
 * command if needed.
 */

public class NewProspectWizard extends Wizard implements PeopleNames {
	// private final static Log log =
	// LogFactory.getLog(NewProspectWizard.class);

	// Context
	private PeopleService peopleService;
	private Node prospect;

	
	// Local cache
	private Node counterparty;
	private Node contact;
	// private User manager;
	
	// This page widgets
	private Text titleTxt;
	private Text descTxt;
	private Text hopeTxt;
	private Text cpTxt;
	private Text contactTxt;
	private Text managerTxt;

	
	
	public NewProspectWizard(PeopleService peopleService, Node prospect) {
		this.peopleService = peopleService;
		this.prospect = prospect;
	}

	@Override
	public void addPages() {
		MainInfoPage page = new MainInfoPage("Main page");
		addPage(page);
		setWindowTitle("Registrer a new prospect");
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. Properties are
	 * updated and the corresponding session is saved.
	 */
	@Override
	public boolean performFinish() {
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
			setTitle("Create a new prospect");
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NO_FOCUS);
			body.setLayout(new GridLayout(3, false));
			// Don't forget this.
			setControl(body);
		}
	}
}