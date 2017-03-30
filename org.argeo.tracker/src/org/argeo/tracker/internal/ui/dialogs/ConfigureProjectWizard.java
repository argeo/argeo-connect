package org.argeo.tracker.internal.ui.dialogs;

import static javax.jcr.Property.JCR_DESCRIPTION;
import static javax.jcr.Property.JCR_TITLE;
import static org.argeo.connect.util.ConnectJcrUtils.get;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.widgets.ExistingGroupsDropDown;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a project */
public class ConfigureProjectWizard extends Wizard implements ModifyListener {
	private static final long serialVersionUID = -8365425809976445458L;

	// Context
	final private UserAdminService userAdminService;
	final private TrackerService trackerService;
	final private Node project;

	// UI controls
	private Text titleTxt;
	private ExistingGroupsDropDown managerDD;
	private Text descTxt;

	public ConfigureProjectWizard(UserAdminService userAdminService, TrackerService trackerService, Node project) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.project = project;
	}

	@Override
	public void addPages() {
		setWindowTitle("Project configuration");
		addPage(new MainPage("Main page"));
	}

	@Override
	public boolean performFinish() {
		// TODO rather use error messages than an error popup
		String title = titleTxt.getText();
		if (EclipseUiUtils.isEmpty(title)) {
			MessageDialog.openError(getShell(), "Compulsory title", "Please define this project title");
			return false;
		}
		try {
			trackerService.configureProject(project, title, descTxt.getText(), managerDD.getText());
		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create project with title " + title, e1);
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(titleTxt.getText()))
			return false;
		else
			return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	protected class MainPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		public MainPage(String pageName) {
			super(pageName);
			// setTitle("Project overview");
			setMessage("Please complete following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			createLabel(parent, "Name", SWT.CENTER);
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A short name for this project");
			titleTxt.setLayoutData(EclipseUiUtils.fillWidth());
			titleTxt.addModifyListener(ConfigureProjectWizard.this);

			createLabel(parent, "Manager", SWT.CENTER);
			Text managerTxt = new Text(parent, SWT.BORDER);
			managerTxt.setMessage("Choose a group");
			managerTxt.setLayoutData(EclipseUiUtils.fillWidth());
			managerDD = new ExistingGroupsDropDown(managerTxt, userAdminService, true, false);

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			GridData gd = EclipseUiUtils.fillWidth();
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this component");

			titleTxt.setText(get(project, JCR_TITLE));
			descTxt.setText(get(project, JCR_DESCRIPTION));

			setControl(titleTxt);
		}
	}

	private Label createLabel(Composite parent, String label, int verticalAlign) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, verticalAlign, false, false));
		return lbl;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		getContainer().updateButtons();
	}
}
