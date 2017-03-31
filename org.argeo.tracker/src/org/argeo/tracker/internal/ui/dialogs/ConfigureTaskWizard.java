package org.argeo.tracker.internal.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.widgets.ExistingGroupsDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.tracker.internal.ui.controls.ProjectDropDown;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to configure an tracker:task
 * 
 * Warning: the passed session is not saved to enable roll-back: all changes are
 * only transient until the caller save the session .
 */

public class ConfigureTaskWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final Node draftTask;

	// Business objects
	private Node project;
	private Node chosenProject;

	// This page widgets
	private ProjectDropDown projectDD;
	private MilestoneDropDown milestoneDD;
	private Text titleTxt;
	private ExistingGroupsDropDown assignedToDD;
	// protected Combo importanceCmb;
	// protected Combo priorityCmb;
	protected Text descTxt;

	public ConfigureTaskWizard(UserAdminService userAdminService, TrackerService trackerService, Node draftEntity) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.draftTask = draftEntity;
		try {
			if (draftTask.hasProperty(TrackerNames.TRACKER_PROJECT_UID)) {
				project = trackerService.getEntityByUid(draftTask.getSession(), null,
						draftTask.getProperty(TrackerNames.TRACKER_PROJECT_UID).getString());
				chosenProject = project;
			} else
				project = null;
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve project for " + draftTask, e);
		}
	}

	@Override
	public void addPages() {
		setWindowTitle("Task configuration");
		ConfigureIssuePage page = new ConfigureIssuePage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// Sanity check
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
			// String importanceStr =
			// TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_IMPORTANCES,
			// importanceCmb.getText());
			// int importance = new Integer(importanceStr).intValue();
			// String priorityStr =
			// TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_PRIORITIES,
			// priorityCmb.getText());
			// int priority = new Integer(priorityStr).intValue();

			trackerService.configureTask(draftTask, chosenProject, milestoneDD.getChosenMilestone(), title,
					descTxt.getText(), assignedToDD.getText()); // priority,
																// importance,
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to create issue on project " + project, e);
		}
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

	protected class ConfigureIssuePage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		private Text projectTxt;

		public ConfigureIssuePage(String pageName) {
			super(pageName);
			setMessage("Please complete below information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// Project
			if (project == null) {
				ConnectWorkbenchUtils.createBoldLabel(parent, "Project");
				projectTxt = new Text(parent, SWT.BORDER);
				projectTxt.setMessage("Choose relevant project");
				GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gd.horizontalSpan = 3;
				projectTxt.setLayoutData(gd);
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(draftTask), projectTxt, false);

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
							milestoneDD.setProject(chosenProject);
						}
					}
				});
			}

			// Title
			ConnectWorkbenchUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("To be shown in the various lists");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// Assigned to
			Text assignedToTxt = createBoldLT(parent, "Assigned to", "",
					"Choose a group or person to manage this issue", 1);
			assignedToDD = new ExistingGroupsDropDown(assignedToTxt, userAdminService, true, false);

			// Target milestone
			ConnectWorkbenchUtils.createBoldLabel(parent, "Milestone");
			Text milestoneTxt = new Text(parent, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			milestoneTxt.setLayoutData(gd);
			milestoneDD = new MilestoneDropDown(project, milestoneTxt, false);

			// // Importance
			// ConnectWorkbenchUtils.createBoldLabel(parent, "Importance");
			// importanceCmb = new Combo(parent, SWT.READ_ONLY);
			// gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			// importanceCmb.setLayoutData(gd);
			// importanceCmb.setItems(TrackerUtils.MAPS_ISSUE_IMPORTANCES.values().toArray(new
			// String[0]));
			// importanceCmb.select(0);
			//
			// // Priority
			// ConnectWorkbenchUtils.createBoldLabel(parent, "Priority");
			// priorityCmb = new Combo(parent, SWT.READ_ONLY);
			// gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			// priorityCmb.setLayoutData(gd);
			// priorityCmb.setItems(TrackerUtils.MAPS_ISSUE_PRIORITIES.values().toArray(new
			// String[0]));
			// priorityCmb.select(0);

			// Description
			Label label = new Label(parent, SWT.RIGHT | SWT.TOP);
			label.setText("Description");
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			label.setLayoutData(gd);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A longer description");
			gd = EclipseUiUtils.fillAll();
			gd.horizontalSpan = 3;
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);

			// Initialise
			// TODO
			// if (EclipseUiUtils.notEmpty(targetMilestone))
			// milestoneDD.reset(targetMilestone);

			// Don't forget this.
			if (projectTxt != null) {
				setControl(projectTxt);
				projectTxt.setFocus();
			} else {
				setControl(titleTxt);
				titleTxt.setFocus();
			}
		}
	}

	private Text createBoldLT(Composite parent, String title, String message, String tooltip, int colspan) {
		ConnectWorkbenchUtils.createBoldLabel(parent, title);
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(EclipseUiUtils.fillAll(colspan, 1));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}
}
