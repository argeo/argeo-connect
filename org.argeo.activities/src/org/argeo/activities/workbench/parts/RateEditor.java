package org.argeo.activities.workbench.parts;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.core.ActivityUtils;
import org.argeo.activities.ui.LinkListPart;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.ConnectWorkbenchUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.connect.workbench.util.EntityEditorInput;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Default Activities rate editor: display a rate and an optional comment. Only
 * the manager can edit an existing rate, the business admin can yet delete it
 */
public class RateEditor extends AbstractConnectEditor {
	final static Log log = LogFactory.getLog(RateEditor.class);

	public final static String ID = ActivitiesUiPlugin.PLUGIN_ID + ".rateEditor";

	// Context
	private ActivitiesService activitiesService;
	private Node rate;

	// Workaround to align first column of header and body.
	private int firstColWHint = 85;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setTitleImage(ConnectImages.RATE);
		rate = getNode();
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		String pollName = ActivityUtils.getPollName(getNode());
		String manager = ConnectJcrUtils.get(getNode(), ActivitiesNames.ACTIVITIES_REPORTED_BY);
		sei.setTooltipText(manager + "'s rate for " + pollName);
	}

	@Override
	protected void updatePartName() {
		String name = getActivitiesService().getActivityLabel(getNode());
		if (EclipseUiUtils.notEmpty(name))
			setPartName(name);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new RateHeader(getFormToolkit(), getManagedForm(), parent, SWT.NO_FOCUS, rate);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
	}

	protected List<String> getPossibleRates() {
		List<String> rates = new ArrayList<String>();
		for (int i = 0; i <= 20; i++)
			rates.add("" + i);
		return rates;
	}

	@Override
	protected void populateBody(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		FormToolkit toolkit = getFormToolkit();

		// Rate
		ConnectUiUtils.createBoldLabel(toolkit, parent, "Rate");
		final Combo rateCmb = new Combo(parent, SWT.READ_ONLY);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		rateCmb.setLayoutData(gd);
		rateCmb.setItems(getPossibleRates().toArray(new String[0]));

		// Bottom part: optional comment
		Label label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Comment");
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = firstColWHint;
		gd.verticalIndent = 2;
		label.setLayoutData(gd);
		final Text descTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.MULTI | SWT.WRAP);
		descTxt.setLayoutData(EclipseUiUtils.fillAll());

		final AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				boolean canEdit = RateEditor.this.isEditing() && getSession().getUserID()
						.equals(ConnectJcrUtils.get(getNode(), ActivitiesNames.ACTIVITIES_REPORTED_BY));
				ConnectWorkbenchUtils.refreshFormTextWidget(RateEditor.this, descTxt, rate, Property.JCR_DESCRIPTION);
				descTxt.setEnabled(canEdit);
				rateCmb.select(rateCmb.indexOf(ConnectJcrUtils.get(rate, ActivitiesNames.ACTIVITIES_RATE)));
				rateCmb.setEnabled(canEdit);
			}
		};

		ConnectWorkbenchUtils.addModifyListener(descTxt, rate, Property.JCR_DESCRIPTION, formPart);
		ConnectWorkbenchUtils.addComboSelectionListener(formPart, rateCmb, rate, ActivitiesNames.ACTIVITIES_RATE,
				PropertyType.LONG);

		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}

	private class RateHeader extends Composite {
		private static final long serialVersionUID = 3489094552204283164L;

		private final Node activity;

		// UI Context
		private final MyFormPart myFormPart;
		private final FormToolkit toolkit;

		// UI Objects
		private Label pollNameLbl;
		private Label managerLbl;
		private Label dateLbl;
		// private DateTextPart dateComposite;
		private LinkListPart relatedCmp;

		public RateHeader(FormToolkit toolkit, IManagedForm form, Composite parent, int style, Node activity) {
			super(parent, style);
			this.toolkit = toolkit;
			this.activity = activity;

			// Initialise the form
			myFormPart = new MyFormPart();
			myFormPart.initialize(form);
			form.addPart(myFormPart);
		}

		private class MyFormPart extends AbstractFormPart {

			DateFormat dateFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

			@Override
			public void refresh() {
				if (RateHeader.this.isDisposed())
					return;
				if (RateHeader.this.getChildren().length == 0)
					populate(RateHeader.this);

				pollNameLbl.setText(ActivityUtils.getPollName(activity));
				String manager = ConnectJcrUtils.get(activity, ActivitiesNames.ACTIVITIES_REPORTED_BY);
				if (notEmpty(manager)) {
					String dName = getUserAdminService().getUserDisplayName(manager);
					managerLbl.setText(notEmpty(dName) ? dName : manager);
				}
				try {
					if (activity.hasProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE)) {
						Calendar cal = activity.getProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE).getDate();
						dateLbl.setText(dateFormat.format(cal.getTime()));
					}
				} catch (RepositoryException e) {
					throw new ActivitiesException("unable to refresh " + "rating date for " + activity, e);
				}

				// We force full refresh with this workaround at each refresh to
				// insure the editable state will change even if no related
				// object has been added. Might be later cleaned
				if (relatedCmp != null && !relatedCmp.isDisposed()) {
					EclipseUiUtils.clear(relatedCmp);
					relatedCmp.refresh();
				}
				relatedCmp.getParent().getParent().layout(true, true);
			}

			private void populate(Composite parent) {
				parent.setLayout(new GridLayout(6, false));

				// 1st line (NOTE: it defines the grid data layout of this part)
				// Work around to be able to kind of also align bold labels of
				// the body
				Label label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Category");
				GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.widthHint = firstColWHint;
				label.setLayoutData(gd);
				pollNameLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 2;
				pollNameLbl.setLayoutData(gd);

				ConnectUiUtils.createBoldLabel(toolkit, parent, "Rated by");
				managerLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 2;
				managerLbl.setLayoutData(gd);

				// ACTIVITY DATE
				ConnectUiUtils.createBoldLabel(toolkit, parent, "On");
				dateLbl = toolkit.createLabel(parent, "");

				// dateComposite = new DateTextPart(RateEditor.this, parent,
				// SWT.NO_FOCUS, myFormPart, activity,
				// PeopleNames.PEOPLE_ACTIVITY_DATE);
				// dateComposite.setLayoutData(EclipseUiUtils.fillWidth());

				// 2nd line - RELATED ENTITIES
				label = ConnectUiUtils.createBoldLabel(toolkit, parent, "Related to");
				gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.verticalIndent = 2;
				label.setLayoutData(gd);
				relatedCmp = new LinkListPart(RateEditor.this, myFormPart, parent, SWT.NO_FOCUS,
						getSystemWorkbenchService(), activity, ActivitiesNames.ACTIVITIES_RELATED_TO);
				relatedCmp.setLayoutData(EclipseUiUtils.fillWidth(5));
			}
		}
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	/* DEPENDENCY INNJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
