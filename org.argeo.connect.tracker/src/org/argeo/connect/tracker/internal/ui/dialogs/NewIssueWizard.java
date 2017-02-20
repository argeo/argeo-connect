package org.argeo.connect.tracker.internal.ui.dialogs;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.people.workbench.rap.composites.dropdowns.ExistingGroupsDropDown;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.connect.tracker.internal.ui.controls.VersionDropDown;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to created a new issue. Caller might use setRelatedTo method
 * to add some related entities after creation and before opening the wizard
 * dialog.
 * 
 * The newly created task might be retrieved after termination if Dialog.open()
 * return Window.OK.
 * 
 * Warning: the passed session is not saved: the task stays in a transient mode
 * until the caller save the session to enable roll-back.
 */

public class NewIssueWizard extends Wizard {
	private final static Log log = LogFactory.getLog(NewIssueWizard.class);

	private UserAdminService userAdminService;
	private TrackerService trackerService;

	// Business objects
	private Node project;
	private Node createdIssue;
	private List<Node> relatedTo;
	// private String assignedToGroupId;
	private ExistingGroupsDropDown assignedToDD;

	// This page widgets
	protected Text titleTxt;

	protected Combo importanceCmb;
	protected Combo priorityCmb;

	protected VersionDropDown versionDD;
	protected MilestoneDropDown targetDD;

	protected Text descTxt;

	private DateText targetDateCmp;
	private DateText releaseDateCmp;

	protected TableViewer itemsViewer;

	public NewIssueWizard(UserAdminService userAdminService, TrackerService trackerService, Node project) {
		this.project = project;
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
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
		if (isEmpty(assignedToDD.getText()))
			msg = "Please assign this issue to a group.";
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

			createdIssue = trackerService.createIssue(project.getNode(TrackerUtils.issuesRelPath()), titleTxt.getText(),
					descTxt.getText(), versionDD.getText(), targetDD.getText(), priority, importance,
					assignedToDD.getText());
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to create issue on project " + project, e);
		}
		return true;
	}

	public void setRelatedTo(List<Node> relatedTo) {
		this.relatedTo = relatedTo;
	}

	public Node getCreatedIssue() {
		return createdIssue;
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
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// TITLE
			ConnectWorkbenchUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("To be shown in the various lists");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// Versions
			ConnectWorkbenchUtils.createBoldLabel(parent, "Impacted Version");
			Text versionTxt = new Text(parent, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			versionTxt.setLayoutData(gd);
			versionDD = new VersionDropDown(project, versionTxt);

			// Target milestone
			ConnectWorkbenchUtils.createBoldLabel(parent, "Target milestone");
			Text milestoneTxt = new Text(parent, SWT.BORDER);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			milestoneTxt.setLayoutData(gd);
			targetDD = new MilestoneDropDown(project, milestoneTxt);

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

			// Assigned to
			Text assignedToTxt = createBoldLT(parent, "Assigned to", "",
					"Choose a group or person to manage this issue", 3);
			assignedToDD = new ExistingGroupsDropDown(assignedToTxt, userAdminService, true, false);

			// PeopleRapUtils.createBoldLabel(parent, "Assigned to");
			// Composite assignedToCmp = new Composite(parent, SWT.NO_FOCUS);
			// gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			// assignedToCmp.setLayoutData(gd);
			// GridLayout gl = EclipseUiUtils.noSpaceGridLayout(new
			// GridLayout(2,
			// false));
			// gl.horizontalSpacing = 5;
			// assignedToCmp.setLayout(gl);
			// final Text assignedToTxt = new Text(assignedToCmp, SWT.BORDER
			// | SWT.NO_FOCUS);
			// assignedToTxt
			// .setMessage("Assign this issue to a group or an individual");
			// CmsUtils.style(assignedToTxt,
			// PeopleStyles.PEOPLE_CLASS_FORCE_BORDER);
			// assignedToTxt.setLayoutData(EclipseUiUtils.fillWidth());
			// assignedToTxt.setEnabled(false);

			// private ExistingGroupsDropDown assignedToDD;

			//
			// Link assignedToLk = new Link(assignedToCmp, SWT.NONE);
			// assignedToLk.setText("<a>Pick up</a>");
			// assignedToLk.addSelectionListener(new SelectionAdapter() {
			// private static final long serialVersionUID = 1L;
			//
			// @Override
			// public void widgetSelected(final SelectionEvent event) {
			// PickUpGroupDialog diag = new PickUpGroupDialog(
			// assignedToTxt.getShell(), "Choose a group",
			// aoService);
			// if (diag.open() == Window.OK) {
			// assignedToGroupId = diag.getSelected().getName();
			// if (EclipseUiUtils.notEmpty(assignedToGroupId))
			// assignedToTxt.setText(userAdminService
			// .getUserDisplayName(assignedToGroupId));
			// }
			// }
			// });

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
			setControl(titleTxt);
			titleTxt.setFocus();
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
