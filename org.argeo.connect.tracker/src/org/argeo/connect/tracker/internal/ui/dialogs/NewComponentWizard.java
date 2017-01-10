package org.argeo.connect.tracker.internal.ui.dialogs;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.core.TrackerUtils;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a new component */
public class NewComponentWizard extends Wizard implements ModifyListener {
	private static final long serialVersionUID = -8365425809976445458L;

	// Context
	final private TrackerService issueService;
	final private Node project;

	// UI controls
	// private Text idTxt;
	private Text titleTxt;
	private Text descTxt;

	public NewComponentWizard(TrackerService issueService, Node project) {
		this.project = project;
		this.issueService = issueService;
	}

	@Override
	public void addPages() {
		setWindowTitle("Create a new component");
		ConfigureVersionPage page = new ConfigureVersionPage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// TODO rather use error messages than an error popup
		if (EclipseUiUtils.isEmpty(getId())) {
			MessageDialog.openError(getShell(), "Compulsory ID", "Please define the component ID");
			return false;
		} else if (TrackerUtils.getComponentById(project, getId()) != null) {
			MessageDialog.openError(getShell(), "Already existing component",
					"A component with ID " + getId() + " already exists, cannot create");
			return false;
		}
		try {
			issueService.createComponent(project, getId(), getTitle(), getDescription());
		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create component with ID " + getId() + " on " + project, e1);
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(getId()))
			return false;
		else
			return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	protected class ConfigureVersionPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		public ConfigureVersionPage(String pageName) {
			super(pageName);
			setTitle("Create a new component");
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// createLabel(parent, "ID", SWT.CENTER);
			// idTxt = new Text(parent, SWT.BORDER);
			// idTxt.setMessage("Only alphanumeric characters");
			// GridData gd = EclipseUiUtils.fillWidth();
			// idTxt.setLayoutData(gd);
			// idTxt.addModifyListener(NewComponentWizard.this);

			createLabel(parent, "Name", SWT.CENTER);
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A short name for this component, that is also used as ID within this project");
			titleTxt.setLayoutData(EclipseUiUtils.fillWidth());
			titleTxt.addModifyListener(NewComponentWizard.this);

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setLayoutData(EclipseUiUtils.fillAll());
			descTxt.setMessage("An optional description for this component");

			setControl(titleTxt);
		}
	}

	public String getId() {
		return titleTxt.getText();
	}

	public String getTitle() {
		return titleTxt.getText();
	}

	public String getDescription() {
		return descTxt.getText();
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
