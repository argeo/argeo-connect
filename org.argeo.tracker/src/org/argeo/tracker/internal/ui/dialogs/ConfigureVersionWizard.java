package org.argeo.tracker.internal.ui.dialogs;

import static javax.jcr.PropertyType.STRING;
import static org.argeo.connect.ConnectNames.CONNECT_UID;
import static org.argeo.connect.util.ConnectJcrUtils.get;
import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.tracker.TrackerNames.TRACKER_PROJECT_UID;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.controls.ProjectDropDown;
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

/** Dialog to simply configure a new milestone or version */
public class ConfigureVersionWizard extends Wizard implements ModifyListener {
	private static final long serialVersionUID = -8365425809976445458L;

	// Context
	private final Node project;
	private Node version;

	// UI objects
	private ProjectDropDown projectDD;
	private Text idTxt;
	private DateText releaseDateCmp;
	private Text descTxt;

	// Ease implementation
	private Node chosenProject;

	public ConfigureVersionWizard(TrackerService trackerService, Node version) {
		this.version = version;
		project = TrackerUtils.getRelatedProject(trackerService, version);
		chosenProject = project;
	}

	@Override
	public void addPages() {
		setWindowTitle("Create a new version");
		ConfigureVersionPage page = new ConfigureVersionPage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		String msg = null;
		Calendar now = new GregorianCalendar();
		now.add(Calendar.DAY_OF_YEAR, 1);

		String id = idTxt.getText();

		if (chosenProject == null)
			msg = "Please pick up a project";
		else if (EclipseUiUtils.isEmpty(id))
			MessageDialog.openError(getShell(), "Compulsory ID", "Please define the version ID");
//		else if (TrackerUtils.getVersionById(project, id) != null)
//			msg = "A version with ID " + id + " already exists, cannot create";
		else if (releaseDateCmp.getCalendar() == null)
			msg = "Release date is compulsory, please define it";
		else if (releaseDateCmp.getCalendar().after(now))
			msg = "A release date can only be defined when the release "
					+ "has been done, and thus must be now or in the past.";

		if (msg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", msg);
			return false;
		}

		try {
			ConnectJcrUtils.setJcrProperty(version, TRACKER_PROJECT_UID, STRING, get(chosenProject, CONNECT_UID));
			ConnectJcrUtils.setJcrProperty(version, TrackerNames.TRACKER_ID, PropertyType.STRING, id);
			ConnectJcrUtils.setJcrProperty(version, Property.JCR_TITLE, PropertyType.STRING, id);
			ConnectJcrUtils.setJcrProperty(version, Property.JCR_DESCRIPTION, PropertyType.STRING, descTxt.getText());
			ConnectJcrUtils.setJcrProperty(version, TrackerNames.TRACKER_RELEASE_DATE, PropertyType.DATE,
					releaseDateCmp.getCalendar());

			if (version.getSession().hasPendingChanges())
				JcrUtils.updateLastModified(version);
		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create version with ID " + id + " on " + project, e1);
		}

		return true;
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(idTxt.getText()))
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

		private Text projectTxt;

		public ConfigureVersionPage(String pageName) {
			super(pageName);
			setMessage("Please complete below information");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(2, false));

			// Project
			ConnectUiUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose relevant project");
			projectTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			if (project == null) {
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(version), projectTxt, false);

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

			createLabel(parent, "ID", SWT.CENTER);
			idTxt = new Text(parent, SWT.BORDER);
			idTxt.setMessage("Major.Minor.Micro: 2.1.37");
			idTxt.setLayoutData(EclipseUiUtils.fillWidth());
			idTxt.addModifyListener(ConfigureVersionWizard.this);

			// Label lbl =
			createLabel(parent, "Release Date ", SWT.CENTER);
			// gd = new GridData();
			// gd.horizontalIndent = 15;
			// lbl.setLayoutData(gd);
			releaseDateCmp = new DateText(parent, SWT.NO_FOCUS);
			releaseDateCmp.setToolTipText("The release date of the version to create,\n "
					+ "typically in the case of reporting a bug for a version that was not yet listed.");

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			GridData gd = EclipseUiUtils.fillAll();
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this version");

			String id = ConnectJcrUtils.get(version, TrackerNames.TRACKER_ID);
			String desc = ConnectJcrUtils.get(version, Property.JCR_DESCRIPTION);
			Calendar releaseDate = ConnectJcrUtils.getDateValue(version, TrackerNames.TRACKER_RELEASE_DATE);
			if (project != null)
				projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));

			if (EclipseUiUtils.notEmpty(id)) {
				idTxt.setText(id);
				idTxt.setEditable(false);
			}
			if (EclipseUiUtils.notEmpty(desc))
				descTxt.setText(desc);
			if (releaseDate != null)
				releaseDateCmp.setText(releaseDate);

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

	// local helpers
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
