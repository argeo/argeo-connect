package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.PickUpByNodeTypeDialog;
import org.argeo.connect.people.ui.dialogs.PickUpRelatedDialog;
import org.argeo.connect.people.ui.editors.utils.AbstractPeopleEditor;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.ActivityJcrUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
public class TaskEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".taskEditor";
	// private DateFormat dateFormat = new SimpleDateFormat(
	// PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// Main business Objects
	private Node assignedToNode;
	private Node task;

	// Form parts must be explicitly disposed
	private AbstractFormPart headerPart;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		task = getNode();
	}

	@Override
	protected void createToolkits() {
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
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Status");
		final Combo statusCmb = new Combo(parent, SWT.NONE);
		statusCmb.setItems(getPeopleService().getActivityService()
				.getStatusList(task));
		statusCmb.select(0);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gd.widthHint = 100;
		statusCmb.setLayoutData(gd);

		// DUE DATE
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Due date");
		final DateTime dueDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		dueDateDt
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// ASSIGNED TO
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Assigned to");
		final Link changeAssignationLk = new Link(parent, SWT.NONE);
		changeAssignationLk.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));

		// WAKE UP DATE
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Wake up date");
		final DateTime wakeUpDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		wakeUpDateDt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		// RELATED ENTITIES
		Label label = PeopleUiUtils.createBoldLabel(toolkit, parent,
				"Related entities");

		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.verticalIndent = 4;
		label.setLayoutData(gd);

		Composite relEntitiesCmp = toolkit
				.createComposite(parent, SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		relEntitiesCmp.setLayoutData(gd);
		relEntitiesCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		// Parent composite with related entities and add link
		final Composite relatedCmp = toolkit.createComposite(relEntitiesCmp,
				SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		relatedCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		relatedCmp.setLayout(rl);

		headerPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();

					PeopleUiUtils.refreshFormComboValue(statusCmb, task,
							PeopleNames.PEOPLE_TASK_STATUS);
					PeopleUiUtils.refreshFormDateTimeWidget(dueDateDt, task,
							PeopleNames.PEOPLE_DUE_DATE);
					PeopleUiUtils.refreshFormDateTimeWidget(wakeUpDateDt, task,
							PeopleNames.PEOPLE_WAKE_UP_DATE);

					boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(task);

					// TODO clean this.
					// update current assigned to group cache here
					String manager = ActivityJcrUtils
							.getActivityManagerDisplayName(task);
					if (task.hasProperty(PeopleNames.PEOPLE_ASSIGNED_TO))
						assignedToNode = task.getProperty(
								PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
					if (isCO)
						manager += " ~ <a>Change</a>";
					changeAssignationLk.setText(manager);

					// We redraw the full related to composite at each refresh,
					// might be a
					// more efficient way to do
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
							toolkit.createLabel(relatedCmp, labelStr,
									SWT.BOTTOM);

							Button deleteBtn = new Button(relatedCmp, SWT.FLAT);
							deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN);
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
							deleteBtn.setVisible(isCO);
						}
						// relatedCmp.pack();
						relatedCmp.layout(false);
						relatedCmp.getParent().getParent().layout();
					}
					// The add button
					if (isCO) {
						final Link addRelatedLk = new Link(relatedCmp,
								SWT.CENTER);
						toolkit.adapt(addRelatedLk, false, false);
						addRelatedLk.setText("<a>Add</a>");
						addRelatedLk
								.addSelectionListener(getAddRelatedSelList(relatedCmp
										.getShell()));
					}
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

		PeopleUiUtils.addSelectionListener(dueDateDt, task,
				PeopleNames.PEOPLE_DUE_DATE, headerPart);
		PeopleUiUtils.addSelectionListener(wakeUpDateDt, task,
				PeopleNames.PEOPLE_WAKE_UP_DATE, headerPart);

		PeopleUiUtils.addComboSelectionListener(headerPart, statusCmb, task,
				PeopleNames.PEOPLE_TASK_STATUS, PropertyType.STRING);

		changeAssignationLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpByNodeTypeDialog diag = new PickUpByNodeTypeDialog(
							changeAssignationLk.getShell(), "Choose a group",
							getSession(), PeopleTypes.PEOPLE_USER_GROUP);
					diag.open();
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
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to change assignation for node " + task, re);
				}
			}
		});

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
					diag.open();
					Node node = diag.getSelected();
					String errMsg = CommonsJcrUtils.addRefToMultiValuedProp(
							task, PeopleNames.PEOPLE_RELATED_TO, node);
					if (errMsg != null)
						MessageDialog.openError(PeopleUiPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Dupplicates", errMsg);
					else {
						headerPart.refresh();
						headerPart.markDirty();

						getManagedForm().dirtyStateChanged();
						for (IFormPart part : getManagedForm().getParts()) {
							((AbstractFormPart) part).markStale();
							part.refresh();
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
	protected void populateBody(Composite parent) {
		parent.setLayout(new GridLayout());

		// Title
		Group titleGrp = new Group(parent, 0);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 60;
		titleGrp.setLayoutData(gd);
		titleGrp.setText("Title");
		titleGrp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Text titleTxt = toolkit.createText(titleGrp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		titleTxt.setLayoutData(gd);

		// Description
		Group descGrp = new Group(parent, 0);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		descGrp.setLayoutData(gd);
		descGrp.setText("Description");
		descGrp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Text descTxt = toolkit.createText(descGrp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		descTxt.setLayoutData(gd);

		final AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleUiUtils.refreshFormTextWidget(titleTxt, task,
						Property.JCR_TITLE);
				PeopleUiUtils.refreshFormTextWidget(descTxt, task,
						Property.JCR_DESCRIPTION);
			}
		};

		PeopleUiUtils.addModifyListener(titleTxt, task, Property.JCR_TITLE,
				formPart);
		PeopleUiUtils.addModifyListener(descTxt, task,
				Property.JCR_DESCRIPTION, formPart);

		parent.layout();
		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}

}