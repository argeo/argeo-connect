package org.argeo.connect.people.ui.wizards;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.ActivitiesImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.dialogs.PickUpGroupDialog;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to created a new task. Caller might use setRelatedTo method to
 * add some related entities after creation and before opening the wizard
 * dialog.
 * 
 * The newly created task might be retrieved after termination with Result
 * SWT.OK Corresponding session is not saved.
 */

public class NewSimpleTaskWizard extends Wizard {
	// private final static Log log = LogFactory
	// .getLog(CreateSimpleTaskWizard.class);

	// Set upon instantiation
	private Session currSession;
	private ActivityService activityService;

	// Business objects
	private List<Node> relatedTo;
	private Node assignedToGroupNode;
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
	private DateTime dueDateDt;
	private DateTime wakeUpDateDt;

	protected TableViewer itemsViewer;

	public NewSimpleTaskWizard(Session session, ActivityService activityService) {
		this.activityService = activityService;
		this.currSession = session;
	}

	// Exposes to children
	protected Session getSession() {
		return currSession;
	}

	@Override
	public void addPages() {
		// Configure the wizard
		setDefaultPageImageDescriptor(ActivitiesImages.TODO_IMGDESC);
		try {
			SelectChildrenPage page = new SelectChildrenPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		// Sanity check
		String msg = null;
		if (assignedToGroupNode == null)
			msg = "Please assign this task to a group.";
		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}

		Calendar dueDate = GregorianCalendar.getInstance();
		dueDate.set(dueDateDt.getYear(), dueDateDt.getMonth(),
				dueDateDt.getDay());

		Calendar wakeUpDate = GregorianCalendar.getInstance();
		wakeUpDate.set(wakeUpDateDt.getYear(), wakeUpDateDt.getMonth(),
				wakeUpDateDt.getDay());

		createdTask = activityService.createTask(currSession, null,
				titleTxt.getText(), descTxt.getText(), assignedToGroupNode,
				relatedTo, dueDate, wakeUpDate);
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

	@Override
	public void dispose() {
		super.dispose();
	}

	protected class SelectChildrenPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public SelectChildrenPage(String pageName) {
			super(pageName);
			setTitle("New task...");
			setMessage("Create a new task with basic information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			// TITLE
			PeopleUiUtils.createBoldLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A title for the new task");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			titleTxt.setLayoutData(gd);

			// ASSIGNED TO
			PeopleUiUtils.createBoldLabel(parent, "Assigned to");
			final Text assignedToTxt = new Text(parent, SWT.BORDER
					| SWT.NO_FOCUS);
			assignedToTxt.setMessage("Assign a group to manage this task");
			assignedToTxt.setData(PeopleUiConstants.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CLASS_FORCE_BORDER);

			gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			assignedToTxt.setLayoutData(gd);
			assignedToTxt.setEnabled(false);

			Link assignedToLk = new Link(parent, SWT.NONE);
			assignedToLk.setText("<a>Pick up</a>");
			assignedToLk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false));

			assignedToLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					try {
						// PickUpByNodeTypeDialog diag = new
						// PickUpByNodeTypeDialog(
						// assignedToTxt.getShell(), "Choose a group",
						// currSession, PeopleTypes.PEOPLE_USER_GROUP);
						//
						PickUpGroupDialog diag = new PickUpGroupDialog(
								assignedToTxt.getShell(), "Choose a group",
								currSession, null);
						int result = diag.open();
						if (Window.OK == result) {
							// update display
							assignedToGroupNode = diag.getSelected();
							// TODO use correct group name
							assignedToTxt.setText(assignedToGroupNode.getName());
						}
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to pick up a group node to assign to",
								e);
					}
				}
			});

			// DUE DATE
			PeopleUiUtils.createBoldLabel(parent, "Due date");
			dueDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE | SWT.MEDIUM
					| SWT.DROP_DOWN);
			// dueDateDt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
			// false));

			// WAKE UP DATE
			PeopleUiUtils.createBoldLabel(parent, "Wake up date");
			wakeUpDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE
					| SWT.MEDIUM | SWT.DROP_DOWN);

			// DESCRIPTION
			Label label = new Label(parent, SWT.RIGHT | SWT.TOP);
			label.setText("Description");
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			label.setLayoutData(gd);

			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A description");
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalSpan = 3;
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);

			// Don't forget this.
			setControl(titleTxt);
			titleTxt.setFocus();
		}

	}

	// private void createLabel(Composite parent, String text) {
	// Label label = new Label(parent, SWT.RIGHT);
	// label.setText(text);
	// GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
	// label.setLayoutData(gd);
	// }
}