package org.argeo.activities.workbench.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.ui.workbench.useradmin.PickUpUserDialog;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.LinkListPart;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.connect.workbench.parts.AbstractPanelFormPart;
import org.argeo.connect.workbench.parts.DateTextPart;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.service.useradmin.User;

/** Provides basic information about a task in a form context */
public class TaskBasicHeader extends Composite {
	private static final long serialVersionUID = -5813631462166187272L;

	// Context
	private final Session session;

	private final UserAdminService userAdminService;
	private final ResourcesService resourceService;
	private final ActivitiesService activityService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node task;
	private final String taskTypeId;

	// local cache
	private String assignedToGroupId;
	// private boolean isBeingEdited;
	private List<String> hiddenItemIds;
	private List<String> modifiedPaths = new ArrayList<String>();

	// UI Context
	private final AbstractConnectEditor editor;
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

	private DateFormat dtFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);
	private DateFormat dateFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_FORMAT);

	public TaskBasicHeader(AbstractConnectEditor editor, Composite parent, int style, UserAdminService uas,
			ResourcesService resourceService, ActivitiesService activityService,
			SystemWorkbenchService systemWorkbenchService, String taskTypeId, Node task) {
		this(editor, parent, style, uas, resourceService, activityService, systemWorkbenchService, taskTypeId, task,
				null);
	}

	public TaskBasicHeader(AbstractConnectEditor editor, Composite parent, int style, UserAdminService uas,
			ResourcesService resourceService, ActivitiesService activityService,
			SystemWorkbenchService systemWorkbenchService, String taskTypeId, Node task, List<String> hiddenItemIds) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.resourceService = resourceService;
		this.activityService = activityService;
		this.userAdminService = uas;
		this.systemWorkbenchService = systemWorkbenchService;
		this.taskTypeId = taskTypeId;
		this.task = task;

		// Caches a few context object to ease implementation
		session = ConnectJcrUtils.getSession(task);

		this.hiddenItemIds = hiddenItemIds;

		// Initialise the form
		myFormPart = new MyFormPart(this);
		myFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(myFormPart);
	}

	public void refresh() {
		myFormPart.refresh();
	}

	private class MyFormPart extends AbstractPanelFormPart {

		public MyFormPart(Composite parent) {
			super(parent, task);
		}

		@Override
		public void commit(boolean onSave) {
			super.commit(onSave);
			if (onSave) {
				ConnectJcrUtils.saveAndPublish(task, false);
				String path = ConnectJcrUtils.getPath(task);
				if (!modifiedPaths.contains(path))
					modifiedPaths.add(path);
				ConnectJcrUtils.checkPoint(ConnectJcrUtils.getSession(task), modifiedPaths, true);
				modifiedPaths.clear();
			}
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
			if (isEditing()) {
				refreshStatusCombo(statusCmb, node);

				dueDateCmp.refresh();
				wakeUpDateCmp.refresh();
				// update current assigned to group cache here
				String manager = activityService.getAssignedToDisplayName(node);
				manager += " ~ <a>Change</a>";
				changeAssignationLk.setText(manager);
				changeAssignationLk.getParent().layout();
			} else {
				statusROLbl.setText(getStatusText());
				assignedToROLbl.setText(activityService.getAssignedToDisplayName(task));
			}

			ConnectWorkbenchUtils.refreshFormTextWidget(editor, titleTxt, task, Property.JCR_TITLE);
			ConnectWorkbenchUtils.refreshFormTextWidget(editor, descTxt, task, Property.JCR_DESCRIPTION);
			relatedCmp.refresh();
			// Refresh the parent because the whole header must be
			// re-layouted if some added relations triggers the creation
			// of a new line of the row data
			parent.getParent().layout(true, true);
		}
	}

	/* READ ONLY LAYOUT */
	private void createROComposite(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		ConnectUiUtils.createBoldLabel(toolkit, parent, "Status");
		statusROLbl = new Label(parent, SWT.NO_FOCUS | SWT.WRAP);
		statusROLbl.setLayoutData(EclipseUiUtils.fillWidth());

		ConnectUiUtils.createBoldLabel(toolkit, parent, "Assigned to");
		assignedToROLbl = new Label(parent, SWT.NO_FOCUS | SWT.WRAP);
		assignedToROLbl.setLayoutData(EclipseUiUtils.fillWidth());

		// RELATED ENTITIES
		// Label label =
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Related to");
		relatedCmp = new LinkListPart(editor, myFormPart, parent, SWT.NO_FOCUS, systemWorkbenchService, task,
				ActivitiesNames.ACTIVITIES_RELATED_TO, hiddenItemIds);
		relatedCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// Title
		ConnectUiUtils.createBoldLabel(parent, "Title");
		titleTxt = toolkit.createText(parent, "", SWT.BORDER);
		titleTxt.setLayoutData(EclipseUiUtils.fillWidth());

		// Description
		ConnectUiUtils.createBoldLabel(parent, "Description");
		descTxt = toolkit.createText(parent, "", SWT.BORDER);
		descTxt.setLayoutData(EclipseUiUtils.fillWidth());
	}

	/* EDIT LAYOUT */
	private void createEditComposite(Composite parent) {
		parent.setLayout(new GridLayout(4, false));

		// 1st line (NOTE: it defines the grid data layout of this part)
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Status");
		statusCmb = new Combo(parent, SWT.READ_ONLY);
		statusCmb.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// DUE DATE
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Due date");
		dueDateCmp = new DateTextPart(editor, parent, SWT.NO_FOCUS, myFormPart, task,
				ActivitiesNames.ACTIVITIES_DUE_DATE);
		dueDateCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// ASSIGNED TO
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Assigned to");
		changeAssignationLk = new Link(parent, SWT.NONE);
		changeAssignationLk.setLayoutData(EclipseUiUtils.fillWidth());

		// WAKE UP DATE
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Wake up date");
		wakeUpDateCmp = new DateTextPart(editor, parent, SWT.NO_FOCUS, myFormPart, task,
				ActivitiesNames.ACTIVITIES_WAKE_UP_DATE);
		wakeUpDateCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// RELATED ENTITIES
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Related to");
		relatedCmp = new LinkListPart(editor, myFormPart, parent, SWT.NO_FOCUS, systemWorkbenchService, task,
				ActivitiesNames.ACTIVITIES_RELATED_TO, hiddenItemIds);
		relatedCmp.setLayoutData(EclipseUiUtils.fillWidth(3));
		relatedCmp.layout();

		// Title
		ConnectUiUtils.createBoldLabel(parent, "Title");
		titleTxt = toolkit.createText(parent, "", SWT.BORDER);
		titleTxt.setLayoutData(EclipseUiUtils.fillWidth(3));

		// Description
		ConnectUiUtils.createBoldLabel(parent, "Description");
		descTxt = toolkit.createText(parent, "", SWT.BORDER);
		descTxt.setLayoutData(EclipseUiUtils.fillWidth(3));

		// Add listeners
		dueDateCmp.setFormPart(myFormPart);
		wakeUpDateCmp.setFormPart(myFormPart);
		addStatusCmbSelListener(myFormPart, statusCmb, task, ActivitiesNames.ACTIVITIES_TASK_STATUS,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addModifyListener(titleTxt, task, Property.JCR_TITLE, myFormPart);
		ConnectWorkbenchUtils.addModifyListener(descTxt, task, Property.JCR_DESCRIPTION, myFormPart);
		addChangeAssignListener();
	}

	private String getStatusText() {
		try {
			StringBuilder builder = new StringBuilder();
			String status = ConnectJcrUtils.get(task, ActivitiesNames.ACTIVITIES_TASK_STATUS);

			String dueDateStr = null;
			if (task.hasProperty(ActivitiesNames.ACTIVITIES_DUE_DATE)) {
				Calendar dueDate = task.getProperty(ActivitiesNames.ACTIVITIES_DUE_DATE).getDate();
				dueDateStr = dateFormat.format(dueDate.getTime());
			}
			builder.append(status);

			if (activityService.isTaskDone(task)) {
				String closeBy = ConnectJcrUtils.get(task, ActivitiesNames.ACTIVITIES_CLOSED_BY);
				Calendar closedDate = task.getProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE).getDate();
				builder.append(" - Marked as closed by ").append(userAdminService.getUserDisplayName(closeBy));
				builder.append(" on ").append(dtFormat.format(closedDate.getTime())).append(".");
				if (EclipseUiUtils.notEmpty(dueDateStr))
					builder.append(" Due date was ").append(dueDateStr);
			} else if (activityService.isTaskSleeping(task)) {
				Calendar wakeUpDate = task.getProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE).getDate();
				builder.append(" - Sleeping until  ");
				builder.append(dateFormat.format(wakeUpDate.getTime()));
				if (EclipseUiUtils.notEmpty(dueDateStr))
					builder.append(".  Due date is ").append(dueDateStr);
			} else if (EclipseUiUtils.notEmpty(dueDateStr))
				builder.append(" - Due date is ").append(dueDateStr);

			return builder.toString();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get status text for task " + task, e);
		}
	}

	// HELPERS
	/** Override this to add specific behaviour on status change */
	protected void addStatusCmbSelListener(final AbstractFormPart part, final Combo combo, final Node entity,
			final String propName, final int propType) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String newStatus = combo.getItem(index);
					try {
						if (activityService.updateStatus(taskTypeId, task, newStatus, modifiedPaths))
							part.markDirty();
					} catch (RepositoryException e1) {
						throw new ActivitiesException("Cannot update status to " + newStatus + " for " + task, e1);
					}
				}

			}
		});
	}

	/** Override this to add specific rights for status change */
	protected void refreshStatusCombo(Combo combo, Node currTask) {
		List<String> values = resourceService.getTemplateCatalogue(session, taskTypeId,
				ActivitiesNames.ACTIVITIES_TASK_STATUS, null);
		combo.setItems(values.toArray(new String[values.size()]));
		ConnectWorkbenchUtils.refreshFormCombo(editor, combo, currTask, ActivitiesNames.ACTIVITIES_TASK_STATUS);
		combo.setEnabled(editor.isEditing());
	}

	private void addChangeAssignListener() {
		changeAssignationLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpUserDialog diag = new PickUpUserDialog(changeAssignationLk.getShell(), "Choose a group",
							userAdminService.getUserAdmin());
					int result = diag.open();
					if (Window.OK == result) {
						User newGroup = diag.getSelected();
						if (newGroup == null)
							return;

						String newGroupName = newGroup.getName();
						if (newGroupName.equals(assignedToGroupId))
							return; // nothing has changed
						else {
							// Update value
							task.setProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, newGroupName);
							// update cache and display.
							assignedToGroupId = newGroupName;
							changeAssignationLk
									.setText(userAdminService.getUserDisplayName(newGroupName) + "  ~ <a>Change</a>");
							myFormPart.markDirty();
						}
					}

					// Node newNode = diag.getSelected();
					// if (assignedToNode != null
					// && newNode.getPath().equals(
					// assignedToNode.getPath()))
					// return; // nothing has changed
					// else {
					// // Update value
					// String groupId = newNode.getProperty(
					// PeopleNames.PEOPLE_GROUP_ID).getString();
					// task.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO,
					// groupId);
					// // update cache and display.
					// assignedToNode = newNode;
					// changeAssignationLk.setText(ConnectJcrUtils.get(
					// assignedToNode, Property.JCR_TITLE)
					// + " ~ <a>Change</a>");
					// myFormPart.markDirty();
					// }
					// }
				} catch (RepositoryException re) {
					throw new ActivitiesException("Unable to change assignation for node " + task, re);
				}
			}
		});
	}
}
