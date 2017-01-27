package org.argeo.connect.people.workbench.rap.editors;

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
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.util.ActivityUtils;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.people.workbench.rap.editors.parts.LinkListPart;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractPeopleEditor;
import org.argeo.connect.people.workbench.rap.editors.util.EntityEditorInput;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.util.JcrUiUtils;
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
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Default People rate editor: display a rate and an optional comment. Only the
 * manager can edit an existing rate, the business admin can yet delete it
 */
public class RateEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(RateEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".rateEditor";

	// Context
	private Node rate;

	// Workaround to align first column of header and body.
	private int firstColWHint = 85;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		rate = getNode();
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		String pollName = ActivityUtils.getPollName(getNode());
		String manager = JcrUiUtils.get(getNode(),
				PeopleNames.PEOPLE_REPORTED_BY);
		sei.setTooltipText(manager + "'s rate for " + pollName);
	}

	@Override
	protected void updatePartName() {
		ActivityService as = getPeopleService().getActivityService();
		String name = as.getActivityLabel(getNode());
		if (EclipseUiUtils.notEmpty(name))
			setPartName(name);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new RateHeader(getFormToolkit(),
				getManagedForm(), parent, SWT.NO_FOCUS, getPeopleService(),
				rate);
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
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Rate");
		final Combo rateCmb = new Combo(parent, SWT.READ_ONLY);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		rateCmb.setLayoutData(gd);
		rateCmb.setItems(getPossibleRates().toArray(new String[0]));

		// Bottom part: optional comment
		Label label = PeopleRapUtils
				.createBoldLabel(toolkit, parent, "Comment");
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = firstColWHint;
		gd.verticalIndent = 2;
		label.setLayoutData(gd);
		final Text descTxt = toolkit.createText(parent, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		descTxt.setLayoutData(EclipseUiUtils.fillAll());

		final AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				boolean canEdit = RateEditor.this.isEditing()
						&& getSession().getUserID().equals(
								JcrUiUtils.get(getNode(),
										PeopleNames.PEOPLE_REPORTED_BY));
				PeopleRapUtils.refreshFormTextWidget(RateEditor.this, descTxt,
						rate, Property.JCR_DESCRIPTION);
				descTxt.setEnabled(canEdit);
				rateCmb.select(rateCmb.indexOf(JcrUiUtils.get(rate,
						PeopleNames.PEOPLE_RATE)));
				rateCmb.setEnabled(canEdit);
			}
		};

		PeopleRapUtils.addModifyListener(descTxt, rate,
				Property.JCR_DESCRIPTION, formPart);
		PeopleRapUtils.addComboSelectionListener(formPart, rateCmb, rate,
				PeopleNames.PEOPLE_RATE, PropertyType.LONG);

		formPart.initialize(getManagedForm());
		getManagedForm().addPart(formPart);
	}

	private class RateHeader extends Composite {
		private static final long serialVersionUID = 6434106955847719839L;

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

		public RateHeader(FormToolkit toolkit, IManagedForm form,
				Composite parent, int style, PeopleService peopleService,
				Node activity) {
			super(parent, style);
			this.toolkit = toolkit;
			this.activity = activity;

			// Initialise the form
			myFormPart = new MyFormPart();
			myFormPart.initialize(form);
			form.addPart(myFormPart);
		}

		private class MyFormPart extends AbstractFormPart {

			DateFormat dateFormat = new SimpleDateFormat(
					ConnectUiConstants.DEFAULT_DATE_TIME_FORMAT);

			@Override
			public void refresh() {
				if (RateHeader.this.isDisposed())
					return;
				if (RateHeader.this.getChildren().length == 0)
					populate(RateHeader.this);

				pollNameLbl.setText(ActivityUtils.getPollName(activity));
				String manager = JcrUiUtils.get(activity,
						PeopleNames.PEOPLE_REPORTED_BY);
				if (notEmpty(manager)) {
					String dName = getPeopleService().getUserAdminService()
							.getUserDisplayName(manager);
					managerLbl.setText(notEmpty(dName) ? dName : manager);
				}
				try {
					if (activity.hasProperty(PeopleNames.PEOPLE_ACTIVITY_DATE)) {
						Calendar cal = activity.getProperty(
								PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
						dateLbl.setText(dateFormat.format(cal.getTime()));
					}
				} catch (RepositoryException e) {
					throw new PeopleException("unable to refresh "
							+ "rating date for " + activity, e);
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
				Label label = PeopleRapUtils.createBoldLabel(toolkit, parent,
						"Category");
				GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.widthHint = firstColWHint;
				label.setLayoutData(gd);
				pollNameLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 2;
				pollNameLbl.setLayoutData(gd);

				PeopleRapUtils.createBoldLabel(toolkit, parent, "Rated by");
				managerLbl = toolkit.createLabel(parent, "");

				gd = EclipseUiUtils.fillWidth();
				gd.verticalIndent = 2;
				managerLbl.setLayoutData(gd);

				// ACTIVITY DATE
				PeopleRapUtils.createBoldLabel(toolkit, parent, "On");
				dateLbl = toolkit.createLabel(parent, "");

				// dateComposite = new DateTextPart(RateEditor.this, parent,
				// SWT.NO_FOCUS, myFormPart, activity,
				// PeopleNames.PEOPLE_ACTIVITY_DATE);
				// dateComposite.setLayoutData(EclipseUiUtils.fillWidth());

				// 2nd line - RELATED ENTITIES
				label = PeopleRapUtils.createBoldLabel(toolkit, parent,
						"Related to");
				gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				gd.verticalIndent = 2;
				label.setLayoutData(gd);
				relatedCmp = new LinkListPart(RateEditor.this, myFormPart,
						parent, SWT.NO_FOCUS, getPeopleWorkbenchService(),
						activity, PeopleNames.PEOPLE_RELATED_TO);
				relatedCmp.setLayoutData(EclipseUiUtils.fillWidth(5));
			}
		}
	}
}