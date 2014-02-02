package org.argeo.connect.people.ui.editors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Default connect activity editor
 */
public class ActivityEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(ActivityEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".activityEditor";
	private DateFormat dateFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// Main business Objects
	private Node activity;

	// Form parts must be explicitly disposed
	private AbstractFormPart headerPart;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		activity = getNode();
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

		layout = new GridLayout(6, false);
		parent.setLayout(layout);

		// 1st line (NOTE: it defines the grid data layout of this part)
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Type");
		final Label typeLbl = toolkit.createLabel(parent, "");
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		typeLbl.setLayoutData(gd);

		PeopleUiUtils.createBoldLabel(toolkit, parent, "Manager");
		final Label managerLbl = toolkit.createLabel(parent, "");
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		managerLbl.setLayoutData(gd);

		// DATE
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Date");
		final Label dateLbl = toolkit.createLabel(parent, "");
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		dateLbl.setLayoutData(gd);

		// 2nd line - RELATED ENTITIES
		Label label = PeopleUiUtils.createBoldLabel(toolkit, parent,
				"Related entities");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Composite relatedCmp = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		relatedCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		relatedCmp.setLayout(rl);

		// Composite secLineCmp = new Composite(parent, SWT.NO_FOCUS);
		// gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		// gd.horizontalSpan = 3;
		// secLineCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));
		//
		// toolkit.createLabel(secLineCmp, "Related entities:");
		//
		// // Parent composite with related entities and add link
		// final Composite relatedCmp = new Composite(parent, SWT.NO_FOCUS);
		// gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		// relatedCmp.setLayoutData(gd);
		// RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		// rl.wrap = true;
		// rl.marginLeft = 5;
		// rl.marginRight = 0;
		// relatedCmp.setLayout(rl);
		//
		// // The add button
		// final Link addRelatedLk = new Link(parent, SWT.BOTTOM);
		// toolkit.adapt(addRelatedLk, false, false);
		// addRelatedLk.setText("<a>Add...</a>");

		headerPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();

					typeLbl.setText(ActivityJcrUtils
							.getActivityTypeLbl(activity));

					// TODO display correct display name instead of ID
					String manager = ActivityJcrUtils
							.getActivityManagerDisplayName(activity);
					// peopleService.getUserManagementService()
					// .getUserDisplayName(lst.get(i).getUserId())

					if (CommonsJcrUtils.checkNotEmptyString(manager))
						managerLbl.setText(manager);

					Calendar dateToDisplay = activity.getProperty(
							Property.JCR_CREATED).getDate();
					dateLbl.setText(dateFormat.format(dateToDisplay.getTime()));

					boolean isCO = CommonsJcrUtils
							.isNodeCheckedOutByMe(activity);
					// We redraw the full control at each refresh, might be a
					// more efficient way to do
					Control[] oldChildren = relatedCmp.getChildren();
					for (Control child : oldChildren)
						child.dispose();

					if (activity.hasProperty(PeopleNames.PEOPLE_RELATED_TO)) {
						Value[] values = activity.getProperty(
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
															activity,
															PeopleNames.PEOPLE_RELATED_TO,
															valueStr);
											headerPart.refresh();
											headerPart.markDirty();
										}
									});
							deleteBtn.setVisible(isCO);
						}
						relatedCmp.layout(false);
						relatedCmp.getParent().getParent().layout();

					}
					// The add link
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
							"Unable to refresh form part for activity "
									+ activity, re);
				}
			}
		};
		parent.layout();
		headerPart.initialize(getManagedForm());
		getManagedForm().addPart(headerPart);
	}

	private SelectionListener getAddRelatedSelList(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpRelatedDialog diag = new PickUpRelatedDialog(shell,
							"Choose an entity", activity.getSession(), activity);
					diag.open();
					Node node = diag.getSelected();
					String errMsg = CommonsJcrUtils.addRefToMultiValuedProp(
							activity, PeopleNames.PEOPLE_RELATED_TO, node);
					if (errMsg != null)
						MessageDialog.openError(PeopleUiPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Dupplicates", errMsg);
					else {
						headerPart.refresh();
						headerPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Unable to link chosen node to current activity "
									+ activity, e);
				}

			}
		};
	}

	@Override
	protected void populateBody(Composite parent) {
		// 3rd line: title
		Group titleGrp = new Group(parent, 0);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 60;
		gd.horizontalSpan = 3;
		titleGrp.setLayoutData(gd);
		titleGrp.setText("Title");
		titleGrp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Text titleTxt = toolkit.createText(titleGrp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		titleTxt.setLayoutData(gd);

		// Bottom part: description
		Group descGrp = new Group(parent, 0);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
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
				PeopleUiUtils.refreshFormTextWidget(titleTxt, activity,
						Property.JCR_TITLE);
				PeopleUiUtils.refreshFormTextWidget(descTxt, activity,
						Property.JCR_DESCRIPTION);
			}
		};

		PeopleUiUtils.addModifyListener(titleTxt, activity, Property.JCR_TITLE,
				formPart);
		PeopleUiUtils.addModifyListener(descTxt, activity,
				Property.JCR_DESCRIPTION, formPart);

		parent.layout();
		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}

}