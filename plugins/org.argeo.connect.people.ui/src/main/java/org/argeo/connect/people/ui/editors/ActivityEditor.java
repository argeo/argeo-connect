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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;

/**
 * Default connect activity editor
 */
public class ActivityEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(ActivityEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".activityEditor";

	// Main business Objects
	private Node activity;

	private DateFormat dateFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

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

	@Override
	protected void createBodyPart(Composite parent) {
		populateActivityMainCmp(parent);
	}

	private void populateActivityMainCmp(Composite parent) {
		GridLayout layout = new GridLayout(3, false);
		// PeopleUiUtils.gridLayoutNoBorder(3);
		// layout.horizontalSpacing = layout.verticalSpacing = 5;
		parent.setLayout(layout);

		// 1st line
		final Text typeTxt = createLT(parent, "Type:");
		final Text managerTxt = createLT(parent, "Manager:");
		final Text dateTxt = createLT(parent, "Date:");

		// 2nd line
		Composite secLineCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		gd.horizontalSpan = 3;
		secLineCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));

		toolkit.createLabel(secLineCmp, "Related entities:");

		// Parent composite with related entities and add link
		final Composite relatedCmp = new Composite(parent, SWT.NO_FOCUS);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		relatedCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		relatedCmp.setLayout(rl);

		// The add button
		final Link addRelatedLk = new Link(parent, SWT.BOTTOM);
		toolkit.adapt(addRelatedLk, false, false);
		addRelatedLk.setText("<a>Add...</a>");

		addRelatedLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpRelatedDialog diag = new PickUpRelatedDialog(
							addRelatedLk.getShell(), "Choose an entity",
							activity.getSession(), activity);
					diag.open();
					Node node = diag.getSelected();
					String errMsg = CommonsJcrUtils.addRefToMultiValuedProp(
							activity, PeopleNames.PEOPLE_RELATED_TO, node);
					if (errMsg != null)
						MessageDialog.openError(PeopleUiPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Dupplicates", errMsg);

				} catch (RepositoryException e) {
					throw new PeopleException(
							"Unable to link chosen node to current activity "
									+ activity, e);
				}

			}
		});

		// 3rd line: title
		Group titleGrp = new Group(parent, 0);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
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
				try {
					super.refresh();

					typeTxt.setText(ActivityJcrUtils
							.getActivityTypeLbl(activity));
					typeTxt.setEnabled(false);

					String manager = ActivityJcrUtils
							.getActivityManagerDisplayName(activity);
					if (CommonsJcrUtils.checkNotEmptyString(manager))
						managerTxt.setText(manager);
					managerTxt.setEnabled(false);

					Calendar dateToDisplay = activity.getProperty(
							Property.JCR_CREATED).getDate();
					dateTxt.setText(dateFormat.format(dateToDisplay.getTime()));
					dateTxt.setEnabled(false);

					boolean isCO = CommonsJcrUtils
							.isNodeCheckedOutByMe(activity);
					// show add button only in edit mode
					addRelatedLk.setVisible(isCO);

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
											for (IFormPart part : getManagedForm()
													.getParts()) {
												((AbstractFormPart) part)
														.markStale();
												part.refresh();
											}
										}
									});
							deleteBtn.setVisible(isCO);
						}
						relatedCmp.layout(false);
						relatedCmp.getParent().getParent().layout();
					}

					PeopleUiUtils.refreshFormTextWidget(titleTxt, activity,
							Property.JCR_TITLE);
					PeopleUiUtils.refreshFormTextWidget(descTxt, activity,
							Property.JCR_DESCRIPTION);
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to refresh form part for activity "
									+ activity, re);
				}
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

	private Text createLT(Composite parent, String label) {
		Composite cmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginWidth = gl.verticalSpacing = gl.marginHeight = 0;
		gl.horizontalSpacing = 5;
		cmp.setLayout(gl);
		toolkit.createLabel(cmp, label);
		Text txt = toolkit.createText(cmp, "", SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return txt;
	}
}