package org.argeo.tracker.ui.dialogs;

import static javax.jcr.PropertyType.STRING;
import static org.argeo.connect.ConnectNames.CONNECT_UID;
import static org.argeo.connect.util.ConnectJcrUtils.get;
import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.tracker.TrackerNames.TRACKER_PROJECT_UID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.controls.ProjectDropDown;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a component */
public class ConfigureComponentWizard extends Wizard implements ModifyListener {
	private static final long serialVersionUID = -8365425809976445458L;

	// Context
	private final Node project;
	private final Node component;

	private Node chosenProject;

	// UI controls
	private Text idTxt;
	private ProjectDropDown projectDD;
	private Text descTxt;

	public ConfigureComponentWizard(TrackerService trackerService, Node component) {
		// this.trackerService = trackerService;
		this.component = component;
		project = TrackerUtils.getRelatedProject(trackerService, component);
		chosenProject = project;
	}

	@Override
	public void addPages() {
		setWindowTitle("Create a new component");
		addPage(new MainPage("Main page"));
	}

	@Override
	public boolean performFinish() {
		String id = idTxt.getText();
		if (EclipseUiUtils.isEmpty(id)) {
			MessageDialog.openError(getShell(), "Compulsory ID", "Please define the component ID");
			return false;
			// } else if (TrackerUtils.getComponentById(project, getId()) !=
			// null) {
			// MessageDialog.openError(getShell(), "Already existing component",
			// "A component with ID " + getId() + " already exists, cannot
			// create");
			// return false;
		}
		try {
			ConnectJcrUtils.setJcrProperty(component, TRACKER_PROJECT_UID, STRING, get(chosenProject, CONNECT_UID));
			ConnectJcrUtils.setJcrProperty(component, TrackerNames.TRACKER_ID, PropertyType.STRING, id);
			ConnectJcrUtils.setJcrProperty(component, Property.JCR_TITLE, PropertyType.STRING, id);
			ConnectJcrUtils.setJcrProperty(component, Property.JCR_DESCRIPTION, PropertyType.STRING, descTxt.getText());

			if (component.getSession().hasPendingChanges())
				JcrUtils.updateLastModified(component);
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

	protected class MainPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;
		private Text projectTxt;

		public MainPage(String pageName) {
			super(pageName);
			setMessage("Please complete belowinformation.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// Project
			ConnectUiUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose relevant project");
			projectTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			if (project == null) {
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(component), projectTxt, false);

				projectTxt.addFocusListener(new FocusAdapter() {
					private static final long serialVersionUID = 1L;

					@Override
					public void focusLost(FocusEvent event) {
						Node project = projectDD.getChosenProject();
						if (project == null)
							setErrorMessage("Choose a valid project");
						else {
							setErrorMessage(null);
							chosenProject = project;
						}
					}
				});
			} else
				projectTxt.setEditable(false);

			createLabel(parent, "Name", SWT.CENTER);
			idTxt = new Text(parent, SWT.BORDER);
			idTxt.setMessage("A short name for this component, that is also used as ID within this project");
			idTxt.setLayoutData(EclipseUiUtils.fillWidth());
			idTxt.addModifyListener(ConfigureComponentWizard.this);

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			GridData gd = EclipseUiUtils.fillAll();
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this component");

			String id = ConnectJcrUtils.get(component, TrackerNames.TRACKER_ID);
			String desc = ConnectJcrUtils.get(component, Property.JCR_DESCRIPTION);
			if (project != null)
				projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));

			if (EclipseUiUtils.notEmpty(id)) {
				idTxt.setText(id);
				idTxt.setEditable(false);
			}
			if (EclipseUiUtils.notEmpty(desc))
				descTxt.setText(desc);

			if (project == null) {
				setControl(projectTxt);
				projectTxt.setFocus();
			} else if (isEmpty(id)) {
				setControl(idTxt);
				idTxt.setFocus();
			} else
				setControl(idTxt);

		}
	}

	private String getId() {
		return idTxt.getText();
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
