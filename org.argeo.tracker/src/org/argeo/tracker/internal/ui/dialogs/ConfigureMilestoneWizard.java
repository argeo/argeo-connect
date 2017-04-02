package org.argeo.tracker.internal.ui.dialogs;

import static javax.jcr.Property.JCR_DESCRIPTION;
import static javax.jcr.Property.JCR_TITLE;
import static org.argeo.connect.util.ConnectJcrUtils.get;
import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
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
		String msg = null;
		String title = titleTxt.getText();
		if (chosenProject == null)
			msg = "Please pick up a project";
		else if (isEmpty(title))
			msg = "Please give at least a title";

		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}

		try {
			trackerService.configureMilestone(milestone, chosenProject, null, title, descTxt.getText(),
					managerDD.getText(), defaultAssigneeDD.getText(), targetDateCmp.getCalendar());
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

		private Text projectTxt;

		public MainPage(String pageName) {
			super(pageName);
			setMessage("Please complete following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));
			FocusListener fl = getFocusListener();

			// Project
			ConnectWorkbenchUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose relevant project");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			projectTxt.setLayoutData(gd);

			if (project == null) {
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
					}
				});
			} else
				projectTxt.setEditable(false);

			createLabel(parent, "Milestone Name", SWT.CENTER);
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A clear name");
			titleTxt.setLayoutData(EclipseUiUtils.fillWidth(3));
			titleTxt.addFocusListener(fl);

			createLabel(parent, "Manager", SWT.CENTER);
			Text managerTxt = new Text(parent, SWT.BORDER);
			managerTxt.setMessage("Choose a group");
			managerTxt.setLayoutData(EclipseUiUtils.fillWidth());
			managerDD = new ExistingGroupsDropDown(managerTxt, userAdminService, true, false);

			createLabel(parent, "Default Assignee", SWT.CENTER);
			Text defaultAssigneeTxt = new Text(parent, SWT.BORDER);
			defaultAssigneeTxt.setMessage("Choose a group");
			defaultAssigneeTxt.setLayoutData(EclipseUiUtils.fillWidth());
			defaultAssigneeDD = new ExistingGroupsDropDown(defaultAssigneeTxt, userAdminService, true, false);

			createLabel(parent, "Target Date", SWT.CENTER);
			targetDateCmp = new DateText(parent, SWT.NO_FOCUS);
			targetDateCmp.setLayoutData(EclipseUiUtils.fillWidth(3));
			targetDateCmp.setToolTipText("An optional future due date for this milestone");

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			gd = EclipseUiUtils.fillWidth(3);
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this milestone");

			// Initialise values
			try {
				titleTxt.setText(get(milestone, JCR_TITLE));
				descTxt.setText(get(milestone, JCR_DESCRIPTION));
				targetDateCmp.setText(ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_TARGET_DATE));
				if (milestone.hasProperty(TrackerNames.TRACKER_MANAGER))
					managerDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_MANAGER).getString());
				if (milestone.hasProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE))
					defaultAssigneeDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE).getString());
				if (project != null)
					projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));
			} catch (RepositoryException e) {
				throw new TrackerException("Cannot initialise widgets with existing data for " + milestone, e);
			}
			if (project == null) {
				setControl(projectTxt);
				projectTxt.setFocus();
			} else {
				setControl(titleTxt);
				titleTxt.setFocus();
			}
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
