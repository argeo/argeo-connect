package org.argeo.connect.people.workbench.rap.wizards;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.people.workbench.rap.composites.DateText;
import org.argeo.connect.people.workbench.rap.dialogs.PickUpGroupDialog;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
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
	private Session currSession;
	private PeopleService peopleService;
	private ActivityService activityService;
	private UserAdminService userAdminService;

	// Business objects
	private List<Node> relatedTo;
	private String assignedToGroupId;
	private Node createdTask;

	public void setRelatedTo(List<Node> relatedTo) {
		this.relatedTo = relatedTo;
	}

	public Node getCreatedTask() {
		return createdTask;
	}

	// This page widgets
	protected Text titleTxt;
	protected Text descTxt;

	private DateText dueDateCmp;
	private DateText wakeUpDateCmp;

	protected TableViewer itemsViewer;

	public NewSimpleTaskWizard(Session session, PeopleService peopleService) {
		this.peopleService = peopleService;
		activityService = peopleService.getActivityService();
		userAdminService = peopleService.getUserAdminService();
		this.currSession = session;
	}

	// Exposes to extending classes
	protected Session getSession() {
		return currSession;
	}

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
		if (isEmpty(assignedToGroupId))
			msg = "Please assign this task to a group.";
		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}
		createdTask = activityService.createTask(currSession, null, titleTxt.getText(), descTxt.getText(),
				assignedToGroupId, relatedTo, dueDateCmp.getCalendar(), wakeUpDateCmp.getCalendar());

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
			PeopleRapUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A title for the new task");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// ASSIGNED TO
			PeopleRapUtils.createBoldLabel(parent, "Assigned to");
			Composite assignedToCmp = new Composite(parent, SWT.NO_FOCUS);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			assignedToCmp.setLayoutData(gd);
			GridLayout gl = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			gl.horizontalSpacing = 5;
			assignedToCmp.setLayout(gl);
			final Text assignedToTxt = new Text(assignedToCmp, SWT.BORDER | SWT.NO_FOCUS);
			assignedToTxt.setMessage("Assign a group to manage this task");
			CmsUtils.style(assignedToTxt, ConnectUiStyles.FORCE_BORDER);
			assignedToTxt.setLayoutData(EclipseUiUtils.fillWidth());
			assignedToTxt.setEnabled(false);

			Link assignedToLk = new Link(assignedToCmp, SWT.NONE);
			assignedToLk.setText("<a>Pick up</a>");
			assignedToLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					PickUpGroupDialog diag = new PickUpGroupDialog(assignedToTxt.getShell(), "Choose a group",
							peopleService);
					if (diag.open() == Window.OK) {
						assignedToGroupId = diag.getSelected().getName();
						if (EclipseUiUtils.notEmpty(assignedToGroupId))
							assignedToTxt.setText(userAdminService.getUserDisplayName(assignedToGroupId));
					}
				}
			});

			// DUE DATE
			PeopleRapUtils.createBoldLabel(parent, "Due date");
			dueDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// WAKE UP DATE
			Label lbl = PeopleRapUtils.createBoldLabel(parent, "Wake up date");
			gd = new GridData();
			gd.horizontalIndent = 15;
			lbl.setLayoutData(gd);
			wakeUpDateCmp = new DateText(parent, SWT.NO_FOCUS);

			// DESCRIPTION
			Label label = new Label(parent, SWT.RIGHT | SWT.TOP);
			label.setText("Description");
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
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