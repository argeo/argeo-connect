package org.argeo.tracker.internal.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.ExistingGroupsDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
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
 * Generic wizard to create a new issue. Caller might use setRelatedTo method to
 * add some related entities after creation and before opening the wizard
 * dialog.
 * 
 * The newly created issue might be retrieved after termination if Dialog.open()
 * return Window.OK.
 * 
 * Warning: the passed session is not saved: the issue stays in a transient mode
 * until the caller save the session to enable roll-back.
 */

public class NewIssueWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final Node draftEntity;

	// Business objects
	private Node project;
	private Node chosenProject;
	private String targetMilestone;
	private List<String> versionIds;
	private List<String> componentIds;

	// private Node createdIssue;

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
	// protected ComponentDropDown componentDD;
	// protected VersionDropDown versionDD;

	protected Text descTxt;

	private DateText targetDateCmp;
	private DateText releaseDateCmp;
	private List<Node> relatedTo;

	protected TableViewer itemsViewer;

	public NewIssueWizard(UserAdminService userAdminService, TrackerService trackerService, Node draftEntity) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.draftEntity = draftEntity;
	}

	public void setKnownProperties(Node project, String targetMilestone, List<String> versionIds,
			List<String> componentIds) {
		this.project = project;
		this.chosenProject = project;
		this.targetMilestone = targetMilestone;
		this.versionIds = versionIds;
		this.componentIds = componentIds;
	}

	@Override
	public void addPages() {
		setWindowTitle("Create an issue");
		ConfigureIssuePage page = new ConfigureIssuePage("Main page");
		addPage(page);
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
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

			trackerService.configureIssue(draftEntity, chosenProject, title, descTxt.getText(), targetDD.getText(),
					versionIds, componentIds, priority, importance, assignedToDD.getText());
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to create issue on project " + project, e);
		}
		return true;
	}

	public void setRelatedTo(List<Node> relatedTo) {
		this.relatedTo = relatedTo;
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
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(draftEntity), projectTxt, false);

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
			targetDD = new MilestoneDropDown(project, milestoneTxt);
			if (EclipseUiUtils.notEmpty(targetMilestone))
				targetDD.reset(targetMilestone);

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

			// DESCRIPTION
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
