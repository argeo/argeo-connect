package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.dialogs.PickUpRelatedDialog;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleEditor;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/** Default People activity editor */
public class ActivityEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(ActivityEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".activityEditor";

	// Context
	private Node activity;

	// UI objects
	private AbstractFormPart headerPart;
	private int firstColWHint = 85;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		activity = getNode();
	}

	@Override
	protected Boolean deleteParentOnRemove() {
		return new Boolean(false);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(new GridLayout(6, false));

		// 1st line (NOTE: it defines the grid data layout of this part)
		// Work around to be able to kind of also align bold labels of the body
		Label label = PeopleRapUtils.createBoldLabel(toolkit, parent, "Type");
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = firstColWHint;
		label.setLayoutData(gd);
		final Label typeLbl = toolkit.createLabel(parent, "");
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		typeLbl.setLayoutData(gd);

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Reported by");
		final Label managerLbl = toolkit.createLabel(parent, "");
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		managerLbl.setLayoutData(gd);

		// ACTIVITY DATE
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Date");
		final DateTime activityDateDt = new DateTime(parent, SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		activityDateDt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));

		// 2nd line - RELATED ENTITIES
		label = PeopleRapUtils.createBoldLabel(toolkit, parent, "Related to");
		gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.verticalIndent = 2;
		label.setLayoutData(gd);

		final Composite relatedCmp = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		relatedCmp.setLayoutData(PeopleUiUtils.horizontalFillData(5));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		relatedCmp.setLayout(rl);

		headerPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();

					ActivityService activityService = getPeopleService()
							.getActivityService();

					typeLbl.setText(activityService.getActivityLabel(activity));

					String manager = activityService
							.getActivityManagerDisplayName(activity);
					if (CommonsJcrUtils.checkNotEmptyString(manager))
						managerLbl.setText(manager);

					PeopleRapUtils.refreshFormDateTimeWidget(activityDateDt,
							activity, PeopleNames.PEOPLE_ACTIVITY_DATE);

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
							// toolkit.createLabel(relatedCmp, labelStr,
							// SWT.BOTTOM);

							Link relatedLk = new Link(relatedCmp, SWT.CENTER);
							toolkit.adapt(relatedLk, false, false);
							relatedLk.setText("<a>" + labelStr + "</a>");
							relatedLk
									.addSelectionListener(new MyOpenEditorAdapter(
											valueStr));
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
																activity,
																PeopleNames.PEOPLE_RELATED_TO,
																valueStr);
												headerPart.refresh();
												headerPart.markDirty();
											}
										});
							}
						}
					}
					// The add link
					if (isCO) {
						Link addRelatedLk = new Link(relatedCmp, SWT.CENTER);
						toolkit.adapt(addRelatedLk, false, false);
						addRelatedLk.setText("<a>Add</a>");
						addRelatedLk
								.addSelectionListener(getAddRelatedSelList(relatedCmp
										.getShell()));
					}
					relatedCmp.layout(false);
					relatedCmp.getParent().getParent().layout();
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

		PeopleRapUtils.addSelectionListener(activityDateDt, activity,
				PeopleNames.PEOPLE_ACTIVITY_DATE, headerPart);
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

	private SelectionListener getAddRelatedSelList(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpRelatedDialog diag = new PickUpRelatedDialog(shell,
							"Choose an entity", activity.getSession(),
							getPeopleWorkbenchService(), activity);
					int result = diag.open();
					if (Window.OK == result) {
						Node node = diag.getSelected();
						String errMsg = CommonsJcrUtils
								.addRefToMultiValuedProp(activity,
										PeopleNames.PEOPLE_RELATED_TO, node);
						if (errMsg != null)
							MessageDialog.openError(shell, "Dupplicates",
									errMsg);
						else {
							headerPart.refresh();
							headerPart.markDirty();
						}
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
		parent.setLayout(new GridLayout(2, false));
		// 3rd line: title
		Label label = PeopleRapUtils.createBoldLabel(toolkit, parent, "Title");
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = firstColWHint;
		label.setLayoutData(gd);
		final Text titleTxt = toolkit.createText(parent, "", SWT.BORDER
				| SWT.SINGLE);
		titleTxt.setLayoutData(PeopleUiUtils.horizontalFillData());

		// Bottom part: description
		label = PeopleRapUtils.createBoldLabel(toolkit, parent, "Description");
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = firstColWHint;
		gd.verticalIndent = 2;
		label.setLayoutData(gd);
		final Text descTxt = toolkit.createText(parent, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		descTxt.setLayoutData(PeopleUiUtils.fillGridData());

		final AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleRapUtils.refreshFormTextWidget(titleTxt, activity,
						Property.JCR_TITLE);
				PeopleRapUtils.refreshFormTextWidget(descTxt, activity,
						Property.JCR_DESCRIPTION);
			}
		};

		PeopleRapUtils.addModifyListener(titleTxt, activity,
				Property.JCR_TITLE, formPart);
		PeopleRapUtils.addModifyListener(descTxt, activity,
				Property.JCR_DESCRIPTION, formPart);

		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}
}