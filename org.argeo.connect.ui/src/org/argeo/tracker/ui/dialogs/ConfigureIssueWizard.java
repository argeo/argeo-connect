package org.argeo.tracker.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.activities.ActivitiesNames;
import org.argeo.cms.CmsUserManager;
import org.argeo.connect.core.OfficeRole;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.GroupDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.controls.MilestoneDropDown;
import org.argeo.tracker.ui.controls.ProjectDropDown;
import org.argeo.tracker.ui.controls.TagListWithDropDownComposite;
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
 * Generic wizard to configure a tracker:issue.
 * 
 * Warning: the passed session is not saved to enable roll-back: all changes are
 * only transient until the caller saves the session .
 */

public class ConfigureIssueWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private final CmsUserManager userAdminService;
	private final TrackerService trackerService;
	private final Node project;
	private final Node issue;

	// Local business objects
	private Node chosenProject;
	private List<String> versionIds;
	private List<String> componentIds;

	// This page widgets
	private Text projectTxt;
	private ProjectDropDown projectDD;
	private MilestoneDropDown milestoneDD;
	private Text titleTxt;
	private GroupDropDown assignedToDD;
	private DateText dueDateCmp;

	private Combo importanceCmb;
	private Combo priorityCmb;
	private TagListWithDropDownComposite versionsCmp;
	private TagListWithDropDownComposite componentsCmp;
	private Text descTxt;

	public ConfigureIssueWizard(CmsUserManager userAdminService, TrackerService trackerService, Node issue) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.issue = issue;
		project = TrackerUtils.getRelatedProject(trackerService, issue);
		chosenProject = project;

		try {
			if (issue.hasProperty(TrackerNames.TRACKER_VERSION_IDS)) {
				Value[] values = issue.getProperty(TrackerNames.TRACKER_VERSION_IDS).getValues();
				versionIds = new ArrayList<>();
				for (Value value : values)
					versionIds.add(value.getString());
			}
			if (issue.hasProperty(TrackerNames.TRACKER_COMPONENT_IDS)) {
				Value[] values = issue.getProperty(TrackerNames.TRACKER_COMPONENT_IDS).getValues();
				componentIds = new ArrayList<>();
				for (Value value : values)
					componentIds.add(value.getString());
			}
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve info on " + issue, e);
		}
	}

	@Override
	public void addPages() {
		setWindowTitle("Issue configuration");
		addPage(new ConfigureIssuePage("Main page"));
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
			String importanceStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_IMPORTANCES,
					importanceCmb.getText());
			int importance = new Integer(importanceStr).intValue();
			String priorityStr = TrackerUtils.getKeyByValue(TrackerUtils.MAPS_ISSUE_PRIORITIES, priorityCmb.getText());
			int priority = new Integer(priorityStr).intValue();

			trackerService.configureIssue(issue, chosenProject, milestoneDD.getChosenMilestone(), title,
					descTxt.getText(), versionsCmp.getChosenValues(), componentsCmp.getChosenValues(), priority,
					importance, assignedToDD.getText());

			Calendar dueDate = dueDateCmp.getCalendar();
			if (dueDate != null)
				issue.setProperty(ActivitiesNames.ACTIVITIES_DUE_DATE, dueDate);

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
		private static final long serialVersionUID = 4546838571499571513L;

		public ConfigureIssuePage(String pageName) {
			super(pageName);
			setMessage("Please complete below information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// Project
			ConnectUiUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose relevant project");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			projectTxt.setLayoutData(gd);

			if (project == null) {
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
							milestoneDD.setProject(chosenProject);
						}
					}
				});
			} else
				projectTxt.setEditable(false);

			// Target milestone
			ConnectUiUtils.createBoldLabel(parent, "Milestone");
			Text milestoneTxt = new Text(parent, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			milestoneTxt.setLayoutData(gd);
			milestoneDD = new MilestoneDropDown(ConnectJcrUtils.getSession(issue), milestoneTxt, false);
			if (project != null)
				milestoneDD.setProject(project);

			// Title
			ConnectUiUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("To be shown in the various lists");
			titleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			// Assigned to
			Text assignedToTxt = createBoldLT(parent, "Assigned to", "",
					"Choose a group or person to manage this issue", 1);
			assignedToDD = new GroupDropDown(assignedToTxt, userAdminService, OfficeRole.coworker.dn());

			// DUE DATE
			ConnectUiUtils.createBoldLabel(parent, "Due date");
			dueDateCmp = new DateText(parent, SWT.NO_FOCUS);

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

			// Versions
			ConnectUiUtils.createBoldLabel(parent, "Impacted Version");
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
			ConnectUiUtils.createBoldLabel(parent, "Components");
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

			// Description
			Label label = new Label(parent, SWT.LEAD | SWT.TOP);
			label.setText("Description");
			label.setFont(EclipseUiUtils.getBoldFont(parent));
			gd = new GridData(SWT.LEAD, SWT.TOP, false, false);
			label.setLayoutData(gd);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A longer description");
			gd = EclipseUiUtils.fillAll();
			gd.horizontalSpan = 3;
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);

			// Initialise
			Node milestone = null;
			try {
				if (issue.hasProperty(Property.JCR_TITLE))
					titleTxt.setText(issue.getProperty(Property.JCR_TITLE).getString());
				if (issue.hasProperty(Property.JCR_DESCRIPTION))
					descTxt.setText(issue.getProperty(Property.JCR_DESCRIPTION).getString());
				if (project != null)
					projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));

				String muid = ConnectJcrUtils.get(issue, TrackerNames.TRACKER_MILESTONE_UID);
				if (EclipseUiUtils.notEmpty(muid)) {
					milestone = trackerService.getEntityByUid(ConnectJcrUtils.getSession(issue), null, muid);
					milestoneDD.resetMilestone(milestone);
				}

				if (issue.hasProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO))
					assignedToDD.resetDN(issue.getProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO).getString());
				else if (milestone != null && milestone.hasProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE))
					assignedToDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE).getString());

				if (issue.hasProperty(ActivitiesNames.ACTIVITIES_DUE_DATE))
					dueDateCmp.setText(issue.getProperty(ActivitiesNames.ACTIVITIES_DUE_DATE).getDate());
				else if (milestone != null && milestone.hasProperty(TrackerNames.TRACKER_TARGET_DATE))
					dueDateCmp.setText(milestone.getProperty(TrackerNames.TRACKER_TARGET_DATE).getDate());

				// if
				// (issue.hasProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE))
				// wakeUpDateCmp.setText(issue.getProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE).getDate());

				Long importance = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_IMPORTANCE);
				if (importance != null) {
					String iv = TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importance.toString());
					importanceCmb.setText(iv);
				}
				Long priority = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_PRIORITY);
				if (priority != null) {
					String iv = TrackerUtils.MAPS_ISSUE_PRIORITIES.get(priority.toString());
					priorityCmb.setText(iv);
				}

			} catch (RepositoryException e) {
				throw new TrackerException("Cannot initialise widgets with existing data on " + issue, e);
			}

			milestoneTxt.addFocusListener(new FocusAdapter() {
				private static final long serialVersionUID = -5599617250726559371L;

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
