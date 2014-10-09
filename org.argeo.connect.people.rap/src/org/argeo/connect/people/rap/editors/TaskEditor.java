package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.dialogs.PickUpGroupDialog;
import org.argeo.connect.people.rap.dialogs.PickUpRelatedDialog;
import org.argeo.connect.people.rap.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.rap.toolkits.ActivityToolkit;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.ActivityJcrUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;

/**
 * Default connect task editor
 */
public class TaskEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	// local constants
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".taskEditor";
	// private DateFormat dateFormat = new SimpleDateFormat(
	// PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// Main business Objects
	private Node assignedToNode;
	private Node task;

	// Usefull toolkits
	private ActivityToolkit activityTK;

	// Form parts must be explicitly disposed
	private AbstractFormPart headerPart;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		task = getNode();
	}

	@Override
	protected void createToolkits() {
		activityTK = new ActivityToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleWorkbenchService());
	}

	@Override
	protected Boolean deleteParentOnRemove() {
		return new Boolean(false);
	}

	protected void populateHeader(Composite parent) {
		GridLayout layout;
		GridData gd;

		layout = new GridLayout(4, false);
		parent.setLayout(layout);

		// 1st line (NOTE: it defines the grid data layout of this part)
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Status");
		final Combo statusCmb = new Combo(parent, SWT.NONE);
		statusCmb.setItems(getPeopleService().getActivityService()
				.getStatusList(task));
		statusCmb.select(0);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gd.widthHint = 100;
		statusCmb.setLayoutData(gd);

		// DUE DATE
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Due date");
		final DateTime dueDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		dueDateDt
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// ASSIGNED TO
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Assigned to");
		final Link changeAssignationLk = new Link(parent, SWT.NONE);
		changeAssignationLk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));

		// WAKE UP DATE
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Wake up date");
		final DateTime wakeUpDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		wakeUpDateDt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		// RELATED ENTITIES
		// Label label =
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Related entities");
		// gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		// gd.verticalIndent = 10;
		// label.setLayoutData(gd);

		Composite relEntitiesCmp = toolkit
				.createComposite(parent, SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		relEntitiesCmp.setLayoutData(gd);
		relEntitiesCmp.setLayout(PeopleUiUtils.noSpaceGridLayout(2));

		// Parent composite with related entities and add link
		final Composite relatedCmp = toolkit.createComposite(relEntitiesCmp,
				SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		relatedCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		relatedCmp.setLayout(rl);

		// Title
		PeopleRapUtils.createBoldLabel(parent, "Title");
		final Text titleTxt = toolkit.createText(parent, "", SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		titleTxt.setLayoutData(gd);

		// Description
		PeopleRapUtils.createBoldLabel(parent, "Description");
		final Text descTxt = toolkit.createText(parent, "", SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, true, 3, 1);
		descTxt.setLayoutData(gd);

		headerPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();

					PeopleRapUtils.refreshFormCombo(statusCmb, task,
							PeopleNames.PEOPLE_TASK_STATUS);
					PeopleRapUtils.refreshFormDateTimeWidget(dueDateDt, task,
							PeopleNames.PEOPLE_DUE_DATE);
					PeopleRapUtils.refreshFormDateTimeWidget(wakeUpDateDt,
							task, PeopleNames.PEOPLE_WAKE_UP_DATE);

					PeopleRapUtils.refreshFormTextWidget(titleTxt, task,
							Property.JCR_TITLE);
					PeopleRapUtils.refreshFormTextWidget(descTxt, task,
							Property.JCR_DESCRIPTION);

					boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(task);

					// TODO clean this.
					// update current assigned to group cache here
					String manager = ActivityJcrUtils
							.getAssignedToDisplayName(task);
					if (task.hasProperty(PeopleNames.PEOPLE_ASSIGNED_TO))
						assignedToNode = task.getProperty(
								PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
					if (isCO)
						manager += " ~ <a>Change</a>";
					changeAssignationLk.setText(manager);
					changeAssignationLk.getParent().layout();

					// We redraw the full related to composite at each refresh,
					// might be a more efficient way to do
					Control[] oldChildren = relatedCmp.getChildren();
					for (Control child : oldChildren)
						child.dispose();

					if (task.hasProperty(PeopleNames.PEOPLE_RELATED_TO)) {
						Value[] values = task.getProperty(
								PeopleNames.PEOPLE_RELATED_TO).getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							Node relatedNode = getSession()
									.getNodeByIdentifier(valueStr);
							String labelStr = CommonsJcrUtils.get(relatedNode,
									Property.JCR_TITLE);

							Link relatedLk = new Link(relatedCmp, SWT.CENTER);
							toolkit.adapt(relatedLk, false, false);
							relatedLk.setText("<a>" + labelStr + "</a>");
							relatedLk
									.addSelectionListener(new MyOpenEditorAdapter(
											valueStr));

							// Display delete button only in edit mode.
							if (isCO) {
								Button deleteBtn = new Button(relatedCmp,
										SWT.FLAT);
								deleteBtn
										.setData(
												RWT.CUSTOM_VARIANT,
												PeopleRapConstants.PEOPLE_CLASS_FLAT_BTN);
								deleteBtn.setImage(PeopleRapImages.DELETE_BTN);
								RowData rd = new RowData();
								rd.height = 16;
								rd.width = 16;
								deleteBtn.setLayoutData(rd);

								deleteBtn
										.addSelectionListener(new SelectionAdapter() {
											private static final long serialVersionUID = 1L;

											@Override
											public void widgetSelected(
													final SelectionEvent event) {
												CommonsJcrUtils
														.removeRefFromMultiValuedProp(
																task,
																PeopleNames.PEOPLE_RELATED_TO,
																valueStr);
												headerPart.refresh();
												headerPart.markDirty();
											}
										});
							}
						}
					}
					// The add button
					if (isCO) {
						final Link addRelatedLk = new Link(relatedCmp,
								SWT.CENTER);
						toolkit.adapt(addRelatedLk, false, false);
						// addRelatedLk.setLayoutData(new RowData(SWT.CENTER,
						// SWT.TOP, false, false));

						addRelatedLk.setText("<a>Add</a>");
						addRelatedLk
								.addSelectionListener(getAddRelatedSelList(relatedCmp
										.getShell()));
					}

					// relatedCmp.pack();
					relatedCmp.layout(false);
					relatedCmp.getParent().getParent().layout();

				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to refresh form part for activity " + task,
							re);
				}
			}
		};
		parent.layout();
		headerPart.initialize(getManagedForm());
		getManagedForm().addPart(headerPart);

		PeopleRapUtils.addSelectionListener(dueDateDt, task,
				PeopleNames.PEOPLE_DUE_DATE, headerPart);
		PeopleRapUtils.addSelectionListener(wakeUpDateDt, task,
				PeopleNames.PEOPLE_WAKE_UP_DATE, headerPart);
		PeopleRapUtils.addComboSelectionListener(headerPart, statusCmb, task,
				PeopleNames.PEOPLE_TASK_STATUS, PropertyType.STRING);
		PeopleRapUtils.addModifyListener(titleTxt, task, Property.JCR_TITLE,
				headerPart);
		PeopleRapUtils.addModifyListener(descTxt, task,
				Property.JCR_DESCRIPTION, headerPart);

		changeAssignationLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpGroupDialog diag = new PickUpGroupDialog(
							changeAssignationLk.getShell(), "Choose a group",
							getSession(), null);
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
							headerPart.markDirty();
						}
					}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to change assignation for node " + task, re);
				}
			}
		});

	}

	private class MyOpenEditorAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;

		private final String jcrId;

		public MyOpenEditorAdapter(String jcrId) {
			this.jcrId = jcrId;
		}

		@Override
		public void widgetSelected(final SelectionEvent event) {
			CommandUtils.callCommand(getPeopleWorkbenchService()
					.getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
					jcrId);
		}
	}

	// Configure the action launched when the user click the add link in the
	// Related to composite
	private SelectionListener getAddRelatedSelList(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpRelatedDialog diag = new PickUpRelatedDialog(shell,
							"Choose an entity", task.getSession(), task);

					int result = diag.open();
					if (Window.OK == result) {

						Node node = diag.getSelected();
						String errMsg = CommonsJcrUtils
								.addRefToMultiValuedProp(task,
										PeopleNames.PEOPLE_RELATED_TO, node);
						if (errMsg != null)
							MessageDialog.openError(shell, "Dupplicates",
									errMsg);
						else {
							headerPart.refresh();
							headerPart.markDirty();

							getManagedForm().dirtyStateChanged();
							for (IFormPart part : getManagedForm().getParts()) {
								((AbstractFormPart) part).markStale();
								part.refresh();
							}

						}
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to link chosen node "
							+ "to current activity " + task, e);
				}
			}
		};
	}

	@Override
	protected void populateTabFolder(CTabFolder tabFolder) {
		// TODO Auto-generated method stub
		// Activities and tasks
		String tooltip = "Activities and tasks related to "
				+ JcrUtils.get(task, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(tabFolder, CTAB_COMP_STYLE,
				"Activity log", PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);
		activityTK.populateActivityLogPanel(innerPannel, task);
	}

}