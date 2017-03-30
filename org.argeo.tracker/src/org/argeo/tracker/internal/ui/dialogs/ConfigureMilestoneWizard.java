package org.argeo.tracker.internal.ui.dialogs;

import static javax.jcr.Property.JCR_DESCRIPTION;
import static javax.jcr.Property.JCR_TITLE;
import static org.argeo.connect.util.ConnectJcrUtils.get;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.ExistingGroupsDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
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
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a milestone */
public class ConfigureMilestoneWizard extends Wizard {

	// Context
	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final Node project;
	private final Node milestone;

	// Ease implementation
	private Node chosenProject;

	// UI objects
	private ProjectDropDown projectDD;
	private Text titleTxt;
	private ExistingGroupsDropDown managerDD;
	private ExistingGroupsDropDown defaultAssigneeDD;
	private DateText targetDateCmp;
	private Text descTxt;

	public ConfigureMilestoneWizard(UserAdminService userAdminService, TrackerService trackerService, Node milestone) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.milestone = milestone;
		try {
			if (milestone.hasProperty(TrackerNames.TRACKER_PROJECT_UID)) {
				project = trackerService.getEntityByUid(milestone.getSession(), null,
						milestone.getProperty(TrackerNames.TRACKER_PROJECT_UID).getString());
				chosenProject = project;
			} else
				project = null;
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve project for " + milestone, e);
		}
	}

	@Override
	public void addPages() {
		setWindowTitle("Milestone configuration");
		MainPage page = new MainPage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// TODO rather use error messages than an error popup
		// Calendar now = new GregorianCalendar();
		String title = titleTxt.getText();
		if (EclipseUiUtils.isEmpty(title)) {
			MessageDialog.openError(getShell(), "Compulsory ID", "Please define the version ID");
			return false;
		} else if (TrackerUtils.getVersionById(project, title) != null) {
			MessageDialog.openError(getShell(), "Already existing version",
					"A version with ID " + title + " already exists, cannot create");
			return false;
		}

		try {
			trackerService.configureMilestone(milestone, project, null, title, descTxt.getText(), managerDD.getText(),
					defaultAssigneeDD.getText(), targetDateCmp.getCalendar());
			if (milestone.getSession().hasPendingChanges())
				JcrUtils.updateLastModified(milestone);
			return true;

		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create version with ID " + title + " on " + project, e1);
		}
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(titleTxt.getText()) || chosenProject == null)
			return false;
		else
			return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	private class MainPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		public MainPage(String pageName) {
			super(pageName);
			setMessage("Please complete following information.");
		}

		public void createControl(Composite bodyCmp) {
			bodyCmp.setLayout(new GridLayout(4, false));
			FocusListener fl = getFocusListener();

			// Project
			if (project == null) {
				ConnectWorkbenchUtils.createBoldLabel(bodyCmp, "Project");
				Text projectTxt = new Text(bodyCmp, SWT.BORDER);
				projectTxt.setMessage("Choose relevant project");
				GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gd.horizontalSpan = 3;
				projectTxt.setLayoutData(gd);
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(milestone), projectTxt, false);
				projectTxt.addFocusListener(new FocusAdapter() {
					private static final long serialVersionUID = 1719432159240562984L;

					@Override
					public void focusLost(FocusEvent event) {
						Node project = projectDD.getChosenProject();
						if (project == null)
							setErrorMessage("Choose a valid project");
						else {
							setErrorMessage(null);
							chosenProject = project;
						}
						getContainer().updateButtons();
					}
				});
				projectTxt.addFocusListener(fl);
			}

			createLabel(bodyCmp, "Title", SWT.CENTER);
			titleTxt = new Text(bodyCmp, SWT.BORDER);
			titleTxt.setMessage("A clear name");
			titleTxt.setLayoutData(EclipseUiUtils.fillWidth(3));
			titleTxt.addFocusListener(fl);

			createLabel(bodyCmp, "Manager", SWT.CENTER);
			Text managerTxt = new Text(bodyCmp, SWT.BORDER);
			managerTxt.setMessage("Choose a group");
			managerTxt.setLayoutData(EclipseUiUtils.fillWidth());
			managerDD = new ExistingGroupsDropDown(managerTxt, userAdminService, true, false);

			createLabel(bodyCmp, "Default Assignee", SWT.CENTER);
			Text defaultAssigneeTxt = new Text(bodyCmp, SWT.BORDER);
			defaultAssigneeTxt.setMessage("Choose a group");
			defaultAssigneeTxt.setLayoutData(EclipseUiUtils.fillWidth());
			defaultAssigneeDD = new ExistingGroupsDropDown(defaultAssigneeTxt, userAdminService, true, false);

			createLabel(bodyCmp, "Target Date", SWT.CENTER);
			targetDateCmp = new DateText(bodyCmp, SWT.NO_FOCUS);
			targetDateCmp.setLayoutData(EclipseUiUtils.fillWidth(3));
			targetDateCmp.setToolTipText("An optional future due date for this milestone");

			createLabel(bodyCmp, "Description", SWT.TOP);
			descTxt = new Text(bodyCmp, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			GridData gd = EclipseUiUtils.fillWidth(3);
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this milestone");

			// Initialise values
			titleTxt.setText(get(milestone, JCR_TITLE));
			descTxt.setText(get(milestone, JCR_DESCRIPTION));
			targetDateCmp.setText(ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_TARGET_DATE));

			// Don't forget this.
			setControl(titleTxt);
		}
	}

	private FocusListener getFocusListener() {
		return new FocusAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent event) {
				getContainer().updateButtons();
			}
		};
	}

	private Label createLabel(Composite parent, String label, int verticalAlign) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, verticalAlign, false, false));
		return lbl;
	}
}
