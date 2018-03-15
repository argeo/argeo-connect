package org.argeo.tracker.internal.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.AssignedToDropDown;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.tracker.internal.ui.controls.ProjectDropDown;
import org.argeo.tracker.internal.ui.controls.RelatedToList;
import org.eclipse.jface.dialogs.MessageDialog;
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
 * Generic wizard to configure an tracker:task
 * 
 * Warning: the passed session is not saved to enable roll-back: all changes are
 * only transient until the caller saves the session
 */

public class ConfigureTaskWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final ActivitiesService activitiesService;
	private final AppWorkbenchService appWorkbenchService;
	private final Node project;
	private final Node task;

	// Business objects
	private Node chosenProject;

	// This page widgets
	private ProjectDropDown projectDD;
	private MilestoneDropDown milestoneDD;
	private Text titleTxt;
	private AssignedToDropDown assignedToDD;
	private DateText dueDateCmp;
	// TODO add wake up date management
	// private DateText wakeUpDateCmp;
	private Combo importanceCmb;
	private Combo priorityCmb;
	private RelatedToList relatedToCmp;
	private Text descTxt;

	public ConfigureTaskWizard(UserAdminService userAdminService, ActivitiesService activitiesService,
			TrackerService trackerService, AppWorkbenchService appWorkbenchService, Node draftEntity) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.activitiesService = activitiesService;
		this.appWorkbenchService = appWorkbenchService;
		this.task = draftEntity;
		project = TrackerUtils.getRelatedProject(trackerService, task);
		chosenProject = project;
	}

	@Override
	public void addPages() {
		setWindowTitle("Task configuration");
		addPage(new ConfigureTaskPage("Main page"));
	}

	@Override
	public boolean performFinish() {
		String msg = null;
		String title = titleTxt.getText();
		if (chosenProject == null && ConnectJcrUtils.isNodeType(task, TrackerTypes.TRACKER_TASK))
			msg = "Please pick up a project";
		else if (isEmpty(title))
			msg = "Please give at least a title";

		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}

		try {
			// TODO implement a cleaner overwritting of default
			// ActivitiesService task creation
			if (ConnectJcrUtils.isNodeType(task, ActivitiesTypes.ACTIVITIES_TASK)
					&& !ConnectJcrUtils.isNodeType(task, TrackerTypes.TRACKER_TASK)) {
				if (chosenProject == null)
					// simple task
					activitiesService.configureTask(task, ActivitiesTypes.ACTIVITIES_TASK, title, descTxt.getText(),
							assignedToDD.getText());

				else {
					task.addMixin(TrackerTypes.TRACKER_TASK);
					trackerService.configureTask(task, chosenProject, milestoneDD.getChosenMilestone(), title,
							descTxt.getText(), assignedToDD.getText()); // priority,
				}
			} else
				trackerService.configureTask(task, chosenProject, milestoneDD.getChosenMilestone(), title,
						descTxt.getText(), assignedToDD.getText()); // priority,

			// Additionnal values
			String importanceStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_IMPORTANCES,
					importanceCmb.getText());
			int importance = new Integer(importanceStr).intValue();
			task.setProperty(TrackerNames.TRACKER_IMPORTANCE, importance);

			String priorityStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_PRIORITIES, priorityCmb.getText());
			int priority = new Integer(priorityStr).intValue();
			task.setProperty(TrackerNames.TRACKER_PRIORITY, priority);

			Calendar dueDate = dueDateCmp.getCalendar();
			if (dueDate != null)
				task.setProperty(ActivitiesNames.ACTIVITIES_DUE_DATE, dueDate);

			// Calendar wakeUpDate = wakeUpDateCmp.getCalendar();
			// if (wakeUpDate != null) {
			// task.setProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE,
			// wakeUpDate);
			// }
			if (relatedToCmp != null) {
				List<String> relatedTos = relatedToCmp.getChosenValues();
				if (relatedTos != null && !relatedTos.isEmpty()) {
					List<Node> nodes = new ArrayList<>();
					for (String uid : relatedTos) {
						nodes.add(ConnectJcrUtils.getNodeByIdentifier(task, uid));
					}
					ConnectJcrUtils.setMultipleReferences(task, ActivitiesNames.ACTIVITIES_RELATED_TO, nodes);
				}
			}
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

	protected class ConfigureTaskPage extends WizardPage {
		private static final long serialVersionUID = -4341854690472102086L;

		private Text projectTxt;

		public ConfigureTaskPage(String pageName) {
			super(pageName);
			setMessage("Please complete below information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// Project
			ConnectUiUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose a relevant project");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			projectTxt.setLayoutData(gd);

			if (project == null) {
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(task), projectTxt, false);

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
			} else
				projectTxt.setEditable(false);

			// Target milestone
			ConnectUiUtils.createBoldLabel(parent, "Milestone");
			Text milestoneTxt = new Text(parent, SWT.BORDER);
			milestoneTxt.setMessage("Choose a milestone");
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			milestoneTxt.setLayoutData(gd);
			milestoneDD = new MilestoneDropDown(ConnectJcrUtils.getSession(task), milestoneTxt, false);
			if (project != null)
				milestoneDD.setProject(project);

			// Title
			ConnectUiUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A precise and concise description of the task");
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// Assigned to
			Text assignedToTxt = createBoldLT(parent, "Assigned to", "Choose a group or a person",
					"Pick up the group or person that will be responsible for doing this task", 1);
			assignedToDD = new AssignedToDropDown(assignedToTxt, userAdminService, true, false);

			// DUE DATE
			ConnectUiUtils.createBoldLabel(parent, "Due date");
			dueDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// // WAKE UP DATE
			// Label lbl = ConnectWorkbenchUtils.createBoldLabel(parent, "Wake
			// up date");
			// gd = new GridData();
			// gd.horizontalIndent = 15;
			// lbl.setLayoutData(gd);
			// wakeUpDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// Importance
			ConnectUiUtils.createBoldLabel(parent, "Importance");
			importanceCmb = new Combo(parent, SWT.READ_ONLY);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			importanceCmb.setLayoutData(gd);
			importanceCmb.setItems(TrackerUtils.MAPS_ISSUE_IMPORTANCES.values().toArray(new String[0]));
			importanceCmb.select(0);

			// Priority
			ConnectUiUtils.createBoldLabel(parent, "Priority");
			priorityCmb = new Combo(parent, SWT.READ_ONLY);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			priorityCmb.setLayoutData(gd);
			priorityCmb.setItems(TrackerUtils.MAPS_ISSUE_PRIORITIES.values().toArray(new String[0]));
			priorityCmb.select(0);

			// Related to list
			// TODO we do not yet have access to the AppWorkbenchService when
			// creating a task from the coolbar via Eclipse command mechanism
			if (appWorkbenchService != null) {
				ConnectUiUtils.createBoldLabel(parent, "Related to");
				relatedToCmp = new RelatedToList(parent, SWT.NO_FOCUS, task, ActivitiesNames.ACTIVITIES_RELATED_TO,
						appWorkbenchService);
				relatedToCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			// Description
			Label label = new Label(parent, SWT.RIGHT | SWT.TOP);
			label.setText("Description");
			label.setFont(EclipseUiUtils.getBoldFont(parent));
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			label.setLayoutData(gd);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A longer description");
			gd = EclipseUiUtils.fillAll();
			gd.horizontalSpan = 3;
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);

			Node milestone = null;
			// Initialise
			try {
				if (task.hasProperty(Property.JCR_TITLE))
					titleTxt.setText(task.getProperty(Property.JCR_TITLE).getString());
				if (task.hasProperty(Property.JCR_DESCRIPTION))
					descTxt.setText(task.getProperty(Property.JCR_DESCRIPTION).getString());
				if (project != null)
					projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));

				// if
				// (task.hasProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE))
				// wakeUpDateCmp.setText(task.getProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE).getDate());
				String muid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_MILESTONE_UID);
				if (EclipseUiUtils.notEmpty(muid)) {
					milestone = trackerService.getEntityByUid(ConnectJcrUtils.getSession(task), null, muid);
					milestoneDD.resetMilestone(milestone);
				}

				if (task.hasProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO))
					assignedToDD.resetDN(task.getProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO).getString());
				else if (milestone != null && milestone.hasProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE))
					assignedToDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE).getString());

				if (task.hasProperty(ActivitiesNames.ACTIVITIES_DUE_DATE))
					dueDateCmp.setText(task.getProperty(ActivitiesNames.ACTIVITIES_DUE_DATE).getDate());
				else if (milestone != null && milestone.hasProperty(TrackerNames.TRACKER_TARGET_DATE))
					dueDateCmp.setText(milestone.getProperty(TrackerNames.TRACKER_TARGET_DATE).getDate());

				Long importance = ConnectJcrUtils.getLongValue(task, TrackerNames.TRACKER_IMPORTANCE);
				if (importance != null) {
					String iv = TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importance.toString());
					importanceCmb.setText(iv);
				}
				Long priority = ConnectJcrUtils.getLongValue(task, TrackerNames.TRACKER_PRIORITY);
				if (priority != null) {
					String iv = TrackerUtils.MAPS_ISSUE_PRIORITIES.get(priority.toString());
					priorityCmb.setText(iv);
				}

			} catch (RepositoryException e) {
				throw new TrackerException("Cannot initialise widgets with existing data on " + task, e);
			}

			milestoneTxt.addFocusListener(new FocusAdapter() {
				private static final long serialVersionUID = 1719432159240562984L;

				@Override
				public void focusLost(FocusEvent event) {
					Node milestone = milestoneDD.getChosenMilestone();
					if (milestone != null) {
						try {
							if (EclipseUiUtils.isEmpty(assignedToDD.getText())
									&& milestone.hasProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE))
								assignedToDD.resetDN(
										milestone.getProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE).getString());
							if (dueDateCmp.getCalendar() == null
									&& milestone.hasProperty(TrackerNames.TRACKER_TARGET_DATE))
								dueDateCmp.setText(milestone.getProperty(TrackerNames.TRACKER_TARGET_DATE).getDate());
						} catch (RepositoryException e) {
							throw new TrackerException("Cannot set default values for milestone " + milestone, e);
						}
					}
				}
			});

			// Don't forget this.
			if (project == null) {
				setControl(projectTxt);
				projectTxt.setFocus();
			} else if (milestone == null) {
				setControl(milestoneTxt);
				milestoneTxt.setFocus();
			} else {
				setControl(titleTxt);
				titleTxt.setFocus();
			}
		}
	}

	private Text createBoldLT(Composite parent, String title, String message, String tooltip, int colspan) {
		ConnectUiUtils.createBoldLabel(parent, title);
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(EclipseUiUtils.fillAll(colspan, 1));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}
}
