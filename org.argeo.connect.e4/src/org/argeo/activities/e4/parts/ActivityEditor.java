package org.argeo.activities.e4.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ui.LinkListPart;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.e4.parts.AbstractConnectEditor;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.ConnectWorkbenchUtils;
import org.argeo.connect.ui.parts.DateTextPart;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Default People activity editor */
public class ActivityEditor extends AbstractConnectEditor {
	final static Log log = LogFactory.getLog(ActivityEditor.class);

//	 public final static String ID = ActivitiesUiPlugin.PLUGIN_ID +
//	 ".activityEditor";

	// Context
	private Node activity;
	
	@Inject
	private ActivitiesService activitiesService;

	// Workaround to align first column of header and body.
	private int firstColWHint = 85;

	@PostConstruct
	public void init() {
		super.init();
		activity = getNode();
	}

	@Override
	protected void updatePartName() {
		String name = activitiesService.getActivityLabel(getNode());
		if (EclipseUiUtils.notEmpty(name))
			setPartName(name);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new ActivityHeader(getFormToolkit(), getManagedForm(), parent, SWT.NO_FOCUS,
				getUserAdminService(), activitiesService, activity);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
	}

	@Override
	protected void populateBody(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		FormToolkit toolkit = getFormToolkit();
		// 3rd line: title
		Label label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Title");
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = firstColWHint;
		label.setLayoutData(gd);
		final Text titleTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE);
		titleTxt.setLayoutData(EclipseUiUtils.fillWidth());

		// Bottom part: description
		label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Description");
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = firstColWHint;
		gd.verticalIndent = 2;
		label.setLayoutData(gd);
		final Text descTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.MULTI | SWT.WRAP);
		descTxt.setLayoutData(EclipseUiUtils.fillAll());

		final AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				ConnectWorkbenchUtils.refreshFormTextWidget(ActivityEditor.this, titleTxt, activity,
						Property.JCR_TITLE);
				ConnectWorkbenchUtils.refreshFormTextWidget(ActivityEditor.this, descTxt, activity,
						Property.JCR_DESCRIPTION);
			}
		};

		ConnectWorkbenchUtils.addModifyListener(titleTxt, activity, Property.JCR_TITLE, formPart);
		ConnectWorkbenchUtils.addModifyListener(descTxt, activity, Property.JCR_DESCRIPTION, formPart);

		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}

	private class ActivityHeader extends Composite {
		private static final long serialVersionUID = 6434106955847719839L;

		private final ActivitiesService activityService;
		private final UserAdminService userAdminService;
		private final Node activity;

		// UI Context
		private final MyFormPart myFormPart;
		private final FormToolkit toolkit;

		// UI Objects
		private Label typeLbl;
		private Label managerLbl;
		private DateTextPart dateComposite;
		private LinkListPart relatedCmp;

		public ActivityHeader(FormToolkit toolkit, IManagedForm form, Composite parent, int style,
				UserAdminService userAdminService, ActivitiesService activityService, Node activity) {
			super(parent, style);
			this.userAdminService = userAdminService;
			this.activityService = activityService;
			this.toolkit = toolkit;
			this.activity = activity;

			// Initialise the form
			myFormPart = new MyFormPart();
			myFormPart.initialize(form);
			form.addPart(myFormPart);
		}

		private class MyFormPart extends AbstractFormPart {

			@Override
			public void refresh() {
				if (ActivityHeader.this.isDisposed())
					return;

				if (ActivityHeader.this.getChildren().length == 0)
					populate(ActivityHeader.this);

				typeLbl.setText(activityService.getActivityLabel(activity));
				String manager = ConnectJcrUtils.get(activity, ActivitiesNames.ACTIVITIES_REPORTED_BY);
				if (EclipseUiUtils.notEmpty(manager))
					managerLbl.setText(userAdminService.getUserDisplayName(manager));

				dateComposite.refresh();

				// We force full refresh with this workaround at each refresh to
				// insure the editable state will change even if no related
				// object has been added. Might be later cleaned
				if (relatedCmp != null && !relatedCmp.isDisposed()) {
					EclipseUiUtils.clear(relatedCmp);
					relatedCmp.refresh();
				}
				ActivityHeader.this.layout(true, true);
			}

			private void populate(Composite parent) {
				parent.setLayout(new GridLayout(6, false));

				// 1st line (NOTE: it defines the grid data layout of this part)
				// Work around to be able to kind of also align bold labels of
				// the body
				Label label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Type");
				GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.widthHint = firstColWHint;
				label.setLayoutData(gd);
				typeLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 3;
				typeLbl.setLayoutData(gd);

				ConnectUiUtils.createBoldLabel(toolkit, parent, "Reported by");
				managerLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 3;
				managerLbl.setLayoutData(gd);

				// ACTIVITY DATE
				ConnectUiUtils.createBoldLabel(toolkit, parent, "Date");
				dateComposite = new DateTextPart(ActivityEditor.this, parent, SWT.NO_FOCUS, myFormPart, activity,
						ActivitiesNames.ACTIVITIES_ACTIVITY_DATE);
				dateComposite.setLayoutData(EclipseUiUtils.fillWidth());

				// 2nd line - RELATED ENTITIES
				label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Related to");
				gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.verticalIndent = 2;
				label.setLayoutData(gd);
				relatedCmp = new LinkListPart(ActivityEditor.this, myFormPart, parent, SWT.NO_FOCUS,
						getSystemWorkbenchService(), activity, ActivitiesNames.ACTIVITIES_RELATED_TO);
				relatedCmp.setLayoutData(EclipseUiUtils.fillWidth(5));
			}
		}
	}

	/* DEPENDENCY INJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
