package org.argeo.activities.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.core.OfficeRole;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.GroupDropDown;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to created a new task. Caller might use setRelatedTo method to
 * add some related entities after creation and before opening the wizard
 * dialog.
 * 
 * The newly created task might be retrieved after termination if Dialog.open()
 * return Window.OK.
 * 
 * Warning: the passed session is not saved: the task stays in a transient mode
 * until the caller save the session to enable rollback.
 */

public class NewSimpleTaskWizard extends Wizard {
	// private final static Log log = LogFactory
	// .getLog(CreateSimpleTaskWizard.class);

	// Set upon instantiation
	private final UserAdminService userAdminService;
	private final ActivitiesService activityService;
	private final Node draftTask;

	// Business objects
	private List<Node> relatedTo;
	// private String assignedToGroupId;

	// This page widgets
	protected Text titleTxt;
	protected Text descTxt;
	private GroupDropDown assignedToDD;

	private DateText dueDateCmp;
	private DateText wakeUpDateCmp;

	protected TableViewer itemsViewer;

	public NewSimpleTaskWizard(UserAdminService userAdminService, ActivitiesService activityService, Node draftTask) {
		this.userAdminService = userAdminService;
		this.activityService = activityService;
		this.draftTask = draftTask;
	}

	public void setRelatedTo(List<Node> relatedTo) {
		this.relatedTo = relatedTo;
	}

	// // Exposes to extending classes
	// protected Session getSession() {
	// return currSession;
	// }

	@Override
	public void addPages() {
		setWindowTitle("Create a task");
		SelectChildrenPage page = new SelectChildrenPage("Main page");
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
		if (isEmpty(title))
			msg = "Please give at least a title";

		// if (isEmpty(assignedToGroupId))
		// msg = "Please assign this task to a group.";
		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}

		try {
			activityService.configureTask(draftTask, ActivitiesTypes.ACTIVITIES_TASK,
					draftTask.getSession().getUserID(), titleTxt.getText(), descTxt.getText(), assignedToDD.getText(),
					relatedTo, GregorianCalendar.getInstance(), dueDateCmp.getCalendar(), wakeUpDateCmp.getCalendar());
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create simple task with title " + titleTxt.getText(), e);
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

	protected class SelectChildrenPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public SelectChildrenPage(String pageName) {
			super(pageName);
			setTitle("Create a new simple task");
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// TITLE
			ConnectUiUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A title for the new task");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// ASSIGNED TO
			ConnectUiUtils.createBoldLabel(parent, "Assigned to");
			Text assignedToTxt = new Text(parent, SWT.BORDER);
			assignedToTxt.setMessage("Pick up a group");
			assignedToTxt.setToolTipText("Choose a group or person to manage this issue");
			assignedToTxt.setLayoutData(EclipseUiUtils.fillWidth(3));
			// assignedToDD = new ExistingGroupsDropDown(assignedToTxt, userAdminService,
			// true, false);
			assignedToDD = new GroupDropDown(assignedToTxt, userAdminService, OfficeRole.coworker.dn());

			// ConnectWorkbenchUtils.createBoldLabel(parent, "Assigned to");
			// Composite assignedToCmp = new Composite(parent, SWT.NO_FOCUS);
			// gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			// assignedToCmp.setLayoutData(gd);
			// GridLayout gl = EclipseUiUtils.noSpaceGridLayout(new
			// GridLayout(2, false));
			// gl.horizontalSpacing = 5;
			// assignedToCmp.setLayout(gl);
			// final Text assignedToTxt = new Text(assignedToCmp, SWT.BORDER |
			// SWT.NO_FOCUS);
			// assignedToTxt.setMessage("Assign a group to manage this task");
			// CmsUtils.style(assignedToTxt, ConnectUiStyles.FORCE_BORDER);
			// assignedToTxt.setLayoutData(EclipseUiUtils.fillWidth());
			// assignedToTxt.setEnabled(false);
			//
			// Link assignedToLk = new Link(assignedToCmp, SWT.NONE);
			// assignedToLk.setText("<a>Pick up</a>");
			// assignedToLk.addSelectionListener(new SelectionAdapter() {
			// private static final long serialVersionUID = 1L;
			//
			// @Override
			// public void widgetSelected(final SelectionEvent event) {
			// PickUpGroupDialog diag = new
			// PickUpGroupDialog(assignedToTxt.getShell(), "Choose a group",
			// userAdminService);
			// if (diag.open() == Window.OK) {
			// assignedToGroupId = diag.getSelected().getName();
			// if (EclipseUiUtils.notEmpty(assignedToGroupId))
			// assignedToTxt.setText(userAdminService.getUserDisplayName(assignedToGroupId));
			// }
			// }
			// });

			// DUE DATE
			ConnectUiUtils.createBoldLabel(parent, "Due date");
			dueDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// WAKE UP DATE
			Label lbl = ConnectUiUtils.createBoldLabel(parent, "Wake up date");
			gd = new GridData();
			gd.horizontalIndent = 15;
			lbl.setLayoutData(gd);
			wakeUpDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// DESCRIPTION
			Label label = new Label(parent, SWT.LEAD | SWT.TOP);
			label.setText("Description");
			label.setFont(EclipseUiUtils.getBoldFont(parent));
			gd = new GridData(SWT.LEAD, SWT.TOP, false, false);
			label.setLayoutData(gd);

			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A description");
			gd = EclipseUiUtils.fillAll();
			gd.horizontalSpan = 3;
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			// Don't forget this.
			setControl(titleTxt);
			titleTxt.setFocus();
		}
	}
}
