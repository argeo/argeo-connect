package org.argeo.tracker.internal.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

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
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.tracker.internal.ui.controls.ProjectDropDown;
import org.argeo.tracker.internal.ui.controls.TagListWithDropDownComposite;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to configure an issue.
 * 
 * Warning: the passed session is not saved to enable roll-back: all changes are
 * only transient until the caller save the session .
 */

public class ConfigureIssueWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final Node issue;
	private final Node project;

	// Business objects
	private Node chosenProject;

	// private String targetMilestone;
	private List<String> versionIds;
	private List<String> componentIds;

	// This page widgets
	protected Text projectTxt;
	protected ProjectDropDown projectDD;
	protected Text titleTxt;
	private ExistingGroupsDropDown assignedToDD;
	protected Combo importanceCmb;
	protected Combo priorityCmb;
	protected MilestoneDropDown targetDD;
	protected TagListWithDropDownComposite versionsCmp;
	protected TagListWithDropDownComposite componentsCmp;
	protected Text descTxt;

	protected TableViewer itemsViewer;

	public ConfigureIssueWizard(UserAdminService userAdminService, TrackerService trackerService, Node issue) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.issue = issue;

		try {
			if (issue.hasProperty(TrackerNames.TRACKER_PROJECT_UID)) {
				project = trackerService.getEntityByUid(issue.getSession(), null,
						issue.getProperty(TrackerNames.TRACKER_PROJECT_UID).getString());
				chosenProject = project;
			} else
				project = null;
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve project for " + issue, e);
		}
	}

	@Override
	public void addPages() {
		setWindowTitle("Create an issue");
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
			String importanceStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_IMPORTANCES,
					importanceCmb.getText());
			int importance = new Integer(importanceStr).intValue();
			String priorityStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_PRIORITIES, priorityCmb.getText());
			int priority = new Integer(priorityStr).intValue();

			trackerService.configureIssue(issue, chosenProject, targetDD.getChosenMilestone(), title, descTxt.getText(),
					versionIds, componentIds, priority, importance, assignedToDD.getText());
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

		public ConfigureIssuePage(String pageName) {
			super(pageName);
			setTitle("Create a new issue");
			setMessage("Please fill in following information.");
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
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(issue), projectTxt, false);

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
							targetDD.setProject(chosenProject);
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
			ConnectWorkbenchUtils.createBoldLabel(parent, "Target milestone");
			Text milestoneTxt = new Text(parent, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			milestoneTxt.setLayoutData(gd);
			targetDD = new MilestoneDropDown(project, milestoneTxt, false);

			String muid = ConnectJcrUtils.get(issue, TrackerNames.TRACKER_MILESTONE_UID);
			if (EclipseUiUtils.notEmpty(muid))
				targetDD.resetMilestone(trackerService.getEntityByUid(ConnectJcrUtils.getSession(issue), null, muid));

			// Versions
			ConnectWorkbenchUtils.createBoldLabel(parent, "Impacted Version");
			versionsCmp = new TagListWithDropDownComposite(parent, SWT.NO_FOCUS, versionIds) {
				private static final long serialVersionUID = -3852824835081771001L;

				@Override
				protected List<String> getFilteredValues(String filter) {
					if (chosenProject == null)
						return new ArrayList<>();
					else
						return TrackerUtils.getVersionIds(chosenProject, filter);
				}
			};
			versionsCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			// Components
			ConnectWorkbenchUtils.createBoldLabel(parent, "Components");
			componentsCmp = new TagListWithDropDownComposite(parent, SWT.NO_FOCUS, componentIds) {
				private static final long serialVersionUID = 2356778978317806935L;

				@Override
				protected List<String> getFilteredValues(String filter) {
					if (chosenProject == null)
						return new ArrayList<>();
					else
						return TrackerUtils.getComponentIds(chosenProject, filter);
				}
			};
			componentsCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			// Importance
			ConnectWorkbenchUtils.createBoldLabel(parent, "Importance");
			importanceCmb = new Combo(parent, SWT.READ_ONLY);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			importanceCmb.setLayoutData(gd);
			importanceCmb.setItems(TrackerUtils.MAPS_ISSUE_IMPORTANCES.values().toArray(new String[0]));
			importanceCmb.select(0);

			// Priority
			ConnectWorkbenchUtils.createBoldLabel(parent, "Priority");
			priorityCmb = new Combo(parent, SWT.READ_ONLY);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			priorityCmb.setLayoutData(gd);
			priorityCmb.setItems(TrackerUtils.MAPS_ISSUE_PRIORITIES.values().toArray(new String[0]));
			priorityCmb.select(0);

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
