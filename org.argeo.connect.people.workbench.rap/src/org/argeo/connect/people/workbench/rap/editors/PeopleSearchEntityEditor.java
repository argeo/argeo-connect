package org.argeo.connect.people.workbench.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.activities.ActivitiesNames;
import org.argeo.connect.activities.ActivitiesService;
import org.argeo.connect.activities.ActivitiesTypes;
import org.argeo.connect.activities.ui.AssignedToLP;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.exports.CountMemberLP;
import org.argeo.connect.people.ui.exports.NotPrimContactValueLP;
import org.argeo.connect.people.ui.exports.PrimAddressLP;
import org.argeo.connect.people.ui.exports.PrimBankAccountLP;
import org.argeo.connect.people.ui.exports.PrimContactValueLP;
import org.argeo.connect.people.ui.exports.PrimOrgNameLP;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.UserNameLP;
import org.argeo.connect.ui.widgets.TagLikeDropDown;
import org.argeo.connect.ui.workbench.parts.DefaultSearchEntityEditor;
import org.argeo.connect.ui.workbench.util.JcrHtmlLabelProvider;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Search the repository with a given entity type */
public class PeopleSearchEntityEditor extends DefaultSearchEntityEditor implements PeopleNames, IJcrTableViewer {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".peopleSearchEntityEditor";

	// Context
	private PeopleService peopleService;
	private ActivitiesService activitiesService;

	// Default column
	private List<ConnectColumnDefinition> colDefs;
	private TagLikeDropDown tagDD;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Tags", new JcrHtmlLabelProvider(ConnectNames.CONNECT_TAGS), 300));
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
		body.setLayout(new GridLayout(4, false));

		Text tagTxt = createBoldLT(body, "Tag", "",
				"Select from list to find entities that are categorised with this tag");
		tagDD = new TagLikeDropDown(getSession(), getResourceService(), ConnectConstants.RESOURCE_TAG, tagTxt);

		Button goBtn = new Button(body, SWT.PUSH);
		goBtn.setText("Search");
		goBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList();
			}
		});

		Button resetBtn = new Button(body, SWT.PUSH);
		resetBtn.setText("Reset");
		resetBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tagDD.reset(null);
			}
		});
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String attrQuery = XPathUtils.localAnd(XPathUtils.getFreeTextConstraint(getFilterText().getText()),
					XPathUtils.getPropertyEquals(ConnectNames.CONNECT_TAGS, tagDD.getText()));
			if (EclipseUiUtils.notEmpty(attrQuery))
				builder.append("[").append(attrQuery).append("]");
			builder.append(" order by @").append(Property.JCR_TITLE).append(" ascending");
			Query query = XPathUtils.createQuery(getSession(), builder.toString());
			QueryResult result = query.execute();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType() + " entities with static filter ", e);
		}
	}

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {

		String currType = getEntityType();
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();

		// The editor table sends a null ID whereas the JxlExport mechanism
		// always passes an ID
		if (EclipseUiUtils.isEmpty(extractId)) {
			if (ActivitiesTypes.ACTIVITIES_TASK.equals(currType)) {
				columns.add(new ConnectColumnDefinition("Status",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS), 80));
				columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE), 200));
				columns.add(new ConnectColumnDefinition("Assigned To",
						new AssignedToLP(activitiesService, null, Property.JCR_DESCRIPTION), 150));
				columns.add(new ConnectColumnDefinition("Due Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Close Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_CLOSE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Closed by",
						new UserNameLP(getUserAdminService(), null, ActivitiesNames.ACTIVITIES_CLOSED_BY), 120));
				return columns;
			} else if (PeopleTypes.PEOPLE_MAILING_LIST.equals(currType)
					|| ResourcesTypes.RESOURCES_TAG.equals(currType)) {
				columns.add(new ConnectColumnDefinition("Title", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
				columns.add(new ConnectColumnDefinition("Nb of members", new CountMemberLP(getResourceService()), 85));

				columns.add(new ConnectColumnDefinition("Name", new JcrRowLabelProvider(Property.JCR_TITLE), 200));
				columns.add(new ConnectColumnDefinition("Assigned To",
						new AssignedToLP(activitiesService, null, Property.JCR_DESCRIPTION), 150));
				columns.add(new ConnectColumnDefinition("Due Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Close Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_CLOSE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Closed by",
						new UserNameLP(getUserAdminService(), null, ActivitiesNames.ACTIVITIES_CLOSED_BY), 120));
				return columns;
			} else
				return colDefs;
		}

		if (PeopleTypes.PEOPLE_PERSON.equals(currType)) {
			columns.add(new ConnectColumnDefinition("Display Name", new JcrRowLabelProvider(Property.JCR_TITLE)));
			columns.add(new ConnectColumnDefinition("Salutation", new JcrRowLabelProvider(PEOPLE_SALUTATION)));
			columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(PEOPLE_HONORIFIC_TITLE)));
			columns.add(new ConnectColumnDefinition("First name", new JcrRowLabelProvider(PEOPLE_FIRST_NAME)));
			columns.add(new ConnectColumnDefinition("Middle name", new JcrRowLabelProvider(PEOPLE_MIDDLE_NAME)));
			columns.add(new ConnectColumnDefinition("Last name", new JcrRowLabelProvider(PEOPLE_LAST_NAME)));
			columns.add(new ConnectColumnDefinition("Name Suffix", new JcrRowLabelProvider(PEOPLE_NAME_SUFFIX)));
			columns.add(new ConnectColumnDefinition("Organisation", new PrimOrgNameLP(peopleService, null)));
			columns.add(new ConnectColumnDefinition("Primary Street",
					new PrimAddressLP(peopleService, null, PEOPLE_STREET)));
			columns.add(new ConnectColumnDefinition("Primary Street2",
					new PrimAddressLP(peopleService, null, PEOPLE_STREET_COMPLEMENT)));
			columns.add(new ConnectColumnDefinition("Primary Zip",
					new PrimAddressLP(peopleService, null, PEOPLE_ZIP_CODE)));
			columns.add(
					new ConnectColumnDefinition("Primary City", new PrimAddressLP(peopleService, null, PEOPLE_CITY)));
			columns.add(
					new ConnectColumnDefinition("Primary State", new PrimAddressLP(peopleService, null, PEOPLE_STATE)));
			columns.add(new ConnectColumnDefinition("Primary Country ISO",
					new PrimAddressLP(peopleService, null, PEOPLE_COUNTRY)));
			columns.add(new ConnectColumnDefinition("Primary Phone",
					new PrimContactValueLP(null, PeopleTypes.PEOPLE_PHONE)));
			columns.add(new ConnectColumnDefinition("Primary Email",
					new PrimContactValueLP(null, PeopleTypes.PEOPLE_EMAIL)));
			columns.add(new ConnectColumnDefinition("Other Emails",
					new NotPrimContactValueLP(null, PeopleTypes.PEOPLE_EMAIL)));
			columns.add(new ConnectColumnDefinition("Primary Website",
					new PrimContactValueLP(null, PeopleTypes.PEOPLE_URL)));
			columns.add(new ConnectColumnDefinition("Notes", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Tags", new JcrRowLabelProvider(ConnectNames.CONNECT_TAGS)));
			columns.add(new ConnectColumnDefinition("Mailing Lists", new JcrRowLabelProvider(PEOPLE_MAILING_LISTS)));
		} else if (PeopleTypes.PEOPLE_ORG.equals(currType)) {

			// DISPLAY NAME
			columns.add(new ConnectColumnDefinition("Display Name", new JcrRowLabelProvider(Property.JCR_TITLE)));

			// PRIMARY ADDRESS
			columns.add(new ConnectColumnDefinition("Primary Street",
					new PrimAddressLP(peopleService, null, PEOPLE_STREET)));
			columns.add(new ConnectColumnDefinition("Primary Street2",
					new PrimAddressLP(peopleService, null, PEOPLE_STREET_COMPLEMENT)));
			columns.add(
					new ConnectColumnDefinition("Primary City", new PrimAddressLP(peopleService, null, PEOPLE_CITY)));
			columns.add(
					new ConnectColumnDefinition("Primary State", new PrimAddressLP(peopleService, null, PEOPLE_STATE)));
			columns.add(new ConnectColumnDefinition("Primary Zip",
					new PrimAddressLP(peopleService, null, PEOPLE_ZIP_CODE)));
			columns.add(new ConnectColumnDefinition("Primary Country ISO",
					new PrimAddressLP(peopleService, null, PEOPLE_COUNTRY)));

			// PRIMARY CONTACTS
			columns.add(
					new ConnectColumnDefinition("Primary Phone", new PrimContactValueLP("", PeopleTypes.PEOPLE_PHONE)));
			columns.add(
					new ConnectColumnDefinition("Primary Email", new PrimContactValueLP("", PeopleTypes.PEOPLE_EMAIL)));
			columns.add(new ConnectColumnDefinition("Other Emails",
					new NotPrimContactValueLP("", PeopleTypes.PEOPLE_EMAIL)));
			columns.add(new ConnectColumnDefinition("Primary Website",
					new PrimContactValueLP("", PeopleTypes.PEOPLE_URL), 100));

			// LEGAL INFO
			columns.add(new ConnectColumnDefinition("Legal name", new JcrRowLabelProvider(PEOPLE_LEGAL_NAME)));
			columns.add(new ConnectColumnDefinition("Legal form", new JcrRowLabelProvider(PEOPLE_LEGAL_FORM)));
			columns.add(new ConnectColumnDefinition("VAT ID", new JcrRowLabelProvider(PEOPLE_VAT_ID_NB)));

			// PRIMARY PAIEMENT ACCOUNT
			columns.add(new ConnectColumnDefinition("Bank Name", new PrimBankAccountLP("", PEOPLE_BANK_NAME)));
			columns.add(
					new ConnectColumnDefinition("Account Holder", new PrimBankAccountLP("", PEOPLE_ACCOUNT_HOLDER)));
			columns.add(
					new ConnectColumnDefinition("Account Number", new PrimBankAccountLP("", PEOPLE_ACCOUNT_NB), 100));
			columns.add(new ConnectColumnDefinition("Bank Number", new PrimBankAccountLP("", PEOPLE_BANK_NB)));
			columns.add(new ConnectColumnDefinition("BIC", new PrimBankAccountLP("", PEOPLE_BIC)));
			columns.add(new ConnectColumnDefinition("IBAN", new PrimBankAccountLP("", PEOPLE_IBAN)));

			// Tags, notes and mailing list
			columns.add(new ConnectColumnDefinition("Notes", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Tags", new JcrRowLabelProvider(ConnectNames.CONNECT_TAGS)));
			columns.add(new ConnectColumnDefinition("Mailing Lists", new JcrRowLabelProvider(PEOPLE_MAILING_LISTS)));
		} else if (ActivitiesTypes.ACTIVITIES_TASK.equals(currType)) {
			columns.add(new ConnectColumnDefinition("Status",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS)));
			columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE)));
			columns.add(new ConnectColumnDefinition("Description", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Assigned To",
					new AssignedToLP(activitiesService, null, Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Due Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE)));
			columns.add(new ConnectColumnDefinition("Wake-Up Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE)));
			columns.add(new ConnectColumnDefinition("Close Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_CLOSE_DATE)));
			columns.add(new ConnectColumnDefinition("Closed by",
					new UserNameLP(getUserAdminService(), null, ActivitiesNames.ACTIVITIES_CLOSED_BY)));
		} else
			return null;

		return columns;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
