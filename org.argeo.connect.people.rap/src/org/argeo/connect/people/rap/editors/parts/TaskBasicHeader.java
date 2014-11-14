package org.argeo.connect.people.rap.editors.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.dialogs.PickUpGroupDialog;
import org.argeo.connect.people.rap.utils.AbstractPanelFormPart;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Provides basic information about a task in a form context */
public class TaskBasicHeader extends Composite implements PeopleNames {
	private static final long serialVersionUID = -5813631462166187272L;

	// Context
	private final Session session;
	// private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final ResourceService resourceService;
	private final ActivityService activityService;
	private final Node task;
	private final String taskTypeId;

	// local cache
	private Node assignedToNode;
	private boolean isBeingEdited;

	// UI Context
	private final MyFormPart myFormPart;
	private final FormToolkit toolkit;

	// COMPOSITES
	// Edit
	private Combo statusCmb;
	private DateTextPart dueDateCmp;
	private DateTextPart wakeUpDateCmp;
	private Link changeAssignationLk;
	// Read Only
	private Label statusROLbl;
	private Label assignedToROLbl;

	// Both edit and RO
	private LinkListPart relatedCmp;
	private Text titleTxt;
	private Text descTxt;

	private DateFormat dtFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);
	private DateFormat dateFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_FORMAT);

	private boolean isEditing() {
		isBeingEdited = CommonsJcrUtils.isNodeCheckedOutByMe(task);
		return isBeingEdited;
	}

	public TaskBasicHeader(FormToolkit toolkit, IManagedForm form,
			Composite parent, int style, PeopleService peopleService,
			PeopleWorkbenchService msmWorkbenchService, String taskTypeId,
			Node task) {
		super(parent, style);
		this.toolkit = toolkit;
		// this.peopleService = peopleService;
		this.peopleWorkbenchService = msmWorkbenchService;
		this.taskTypeId = taskTypeId;
		this.task = task;

		// Caches a few context object to ease implementation
		resourceService = peopleService.getResourceService();
		activityService = peopleService.getActivityService();
		session = CommonsJcrUtils.getSession(task);

		// Initialise the form
		myFormPart = new MyFormPart(this);
		myFormPart.initialize(form);
		form.addPart(myFormPart);
	}

	private class MyFormPart extends AbstractPanelFormPart {

		public MyFormPart(Composite parent) {
			super(parent, task);
		}

		protected void reCreateChildComposite(Composite panel, Node node) {
			if (isEditing())
				createEditComposite(panel);
			else
				createROComposite(panel);
			refreshContent(panel, node);
		}

		protected void refreshContent(Composite parent, Node node) {
			if (TaskBasicHeader.this.isDisposed())
				return;
			try {
				if (isEditing()) {
					PeopleRapUtils.refreshFormCombo(statusCmb, task,
							PeopleNames.PEOPLE_TASK_STATUS);
					dueDateCmp.refresh();
					wakeUpDateCmp.refresh();
					// update current assigned to group cache here
					String manager = activityService
							.getAssignedToDisplayName(task);
					if (task.hasProperty(PeopleNames.PEOPLE_ASSIGNED_TO))
						assignedToNode = task.getProperty(
								PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
					manager += " ~ <a>Change</a>";
					changeAssignationLk.setText(manager);
					changeAssignationLk.getParent().layout();
				} else {
					statusROLbl.setText(getStatusText());
					assignedToROLbl.setText(activityService
							.getAssignedToDisplayName(task));
				}

				PeopleRapUtils.refreshFormTextWidget(titleTxt, task,
						Property.JCR_TITLE);
				PeopleRapUtils.refreshFormTextWidget(descTxt, task,
						Property.JCR_DESCRIPTION);
				relatedCmp.refresh();
				// Refresh the parent because the whole header must be
				// re-layouted if some added relations triggers the creation
				// of a new line of the row data
				parent.getParent().layout();
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to refresh header form "
						+ "part composite for task " + task, re);
			}
		}
	}

	/* READ ONLY LAYOUT */
	private void createROComposite(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Status");
		statusROLbl = new Label(parent, SWT.NO_FOCUS | SWT.WRAP);
		statusROLbl.setLayoutData(PeopleUiUtils.horizontalFillData());

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Assigned to");
		assignedToROLbl = new Label(parent, SWT.NO_FOCUS | SWT.WRAP);
		assignedToROLbl.setLayoutData(PeopleUiUtils.horizontalFillData());

		// RELATED ENTITIES
		// Label label =
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Related entities");
		relatedCmp = new LinkListPart(toolkit, myFormPart, parent,
				SWT.NO_FOCUS, peopleWorkbenchService, task, PEOPLE_RELATED_TO);
		relatedCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		// Title
		PeopleRapUtils.createBoldLabel(parent, "Title");
		titleTxt = toolkit.createText(parent, "", SWT.BORDER);
		titleTxt.setLayoutData(PeopleUiUtils.horizontalFillData());

		// Description
		PeopleRapUtils.createBoldLabel(parent, "Description");
		descTxt = toolkit.createText(parent, "", SWT.BORDER);
		descTxt.setLayoutData(PeopleUiUtils.horizontalFillData());
	}

	/* EDIT LAYOUT */
	private void createEditComposite(Composite parent) {
		parent.setLayout(new GridLayout(4, false));

		// 1st line (NOTE: it defines the grid data layout of this part)
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Status");
		statusCmb = new Combo(parent, SWT.READ_ONLY);
		List<String> values = resourceService.getTemplateCatalogue(session,
				taskTypeId, PeopleNames.PEOPLE_TASK_STATUS, null);
		statusCmb.setItems(values.toArray(new String[values.size()]));
		statusCmb
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// DUE DATE
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Due date");
		dueDateCmp = new DateTextPart(parent, SWT.NO_FOCUS, toolkit,
				myFormPart, task, PeopleNames.PEOPLE_DUE_DATE);
		dueDateCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		// ASSIGNED TO
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Assigned to");
		changeAssignationLk = new Link(parent, SWT.NONE);
		changeAssignationLk.setLayoutData(PeopleUiUtils.horizontalFillData());

		// WAKE UP DATE
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Wake up date");
		wakeUpDateCmp = new DateTextPart(parent, SWT.NO_FOCUS, toolkit,
				myFormPart, task, PeopleNames.PEOPLE_WAKE_UP_DATE);
		wakeUpDateCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		// RELATED ENTITIES
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Related entities");
		relatedCmp = new LinkListPart(toolkit, myFormPart, parent,
				SWT.NO_FOCUS, peopleWorkbenchService, task, PEOPLE_RELATED_TO);
		relatedCmp.setLayoutData(PeopleUiUtils.horizontalFillData(3));
		relatedCmp.layout();

		// Title
		PeopleRapUtils.createBoldLabel(parent, "Title");
		titleTxt = toolkit.createText(parent, "", SWT.BORDER);
		titleTxt.setLayoutData(PeopleUiUtils.horizontalFillData(3));

		// Description
		PeopleRapUtils.createBoldLabel(parent, "Description");
		descTxt = toolkit.createText(parent, "", SWT.BORDER);
		descTxt.setLayoutData(PeopleUiUtils.horizontalFillData(3));

		// Add listeners
		dueDateCmp.setFormPart(myFormPart);
		wakeUpDateCmp.setFormPart(myFormPart);
		addStatusCmbSelListener(myFormPart, statusCmb, task,
				PEOPLE_TASK_STATUS, PropertyType.STRING);
		PeopleRapUtils.addModifyListener(titleTxt, task, Property.JCR_TITLE,
				myFormPart);
		PeopleRapUtils.addModifyListener(descTxt, task,
				Property.JCR_DESCRIPTION, myFormPart);
		addChangeAssignListener();
	}

	private String getStatusText() {
		try {
			StringBuilder builder = new StringBuilder();
			String status = CommonsJcrUtils.get(task, PEOPLE_TASK_STATUS);

			String dueDateStr = null;
			if (task.hasProperty(PEOPLE_DUE_DATE)) {
				Calendar dueDate = task.getProperty(PEOPLE_DUE_DATE).getDate();
				dueDateStr = dateFormat.format(dueDate.getTime());
			}
			builder.append(status);

			if (activityService.isTaskDone(task)) {
				String closeBy = CommonsJcrUtils.get(task, PEOPLE_CLOSED_BY);
				Calendar closedDate = task.getProperty(PEOPLE_CLOSE_DATE)
						.getDate();
				builder.append(" - Marked as closed by ").append(closeBy);
				builder.append(" on ")
						.append(dtFormat.format(closedDate.getTime()))
						.append(".");
				if (CommonsJcrUtils.checkNotEmptyString(dueDateStr))
					builder.append(" Due date was ").append(dueDateStr);
			} else if (activityService.isTaskSleeping(task)) {
				Calendar wakeUpDate = task.getProperty(PEOPLE_WAKE_UP_DATE)
						.getDate();
				builder.append(" - Sleeping until  ");
				builder.append(dateFormat.format(wakeUpDate.getTime()));
				if (CommonsJcrUtils.checkNotEmptyString(dueDateStr))
					builder.append(".  Due date is ").append(dueDateStr);
			} else if (CommonsJcrUtils.checkNotEmptyString(dueDateStr))
				builder.append(" - Due date is ").append(dueDateStr);

			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get status text for task "
					+ task, e);
		}
	}

	// HELPERS
	private void addStatusCmbSelListener(final AbstractFormPart part,
			final Combo combo, final Node entity, final String propName,
			final int propType) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String selectedCategory = combo.getItem(index);
					if (activityService.updateStatus(taskTypeId, task,
							selectedCategory))
						part.markDirty();
				}

			}
		});
	}

	private void addChangeAssignListener() {
		changeAssignationLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpGroupDialog diag = new PickUpGroupDialog(
							changeAssignationLk.getShell(), "Choose a group",
							session, null);
					int result = diag.open();
					if (Window.OK == result) {
						Node newNode = diag.getSelected();
						if (assignedToNode != null
								&& newNode.getPath().equals(
										assignedToNode.getPath()))
							return; // nothing has changed
						else {
							// Update value
							task.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO,
									newNode);
							// update cache and display.
							assignedToNode = newNode;
							changeAssignationLk.setText(CommonsJcrUtils.get(
									assignedToNode, Property.JCR_TITLE)
									+ "  ~ <a>Change</a>");
							myFormPart.markDirty();
						}
					}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to change assignation for node " + task, re);
				}
			}
		});
	}
}