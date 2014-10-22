package org.argeo.connect.people.rap.composites;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.dropdowns.TagLikeDropDown;
import org.argeo.connect.people.rap.dialogs.PickUpOrgDialog;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit a contact of type post mail
 * address information.
 * 
 */
public class ContactAddressComposite extends Composite implements PeopleNames {

	private static final long serialVersionUID = 4475049051062923873L;

	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleUiService;
	private final Node contactNode;
	private final Node parentVersionableNode;
	private final AbstractFormPart formPart;
	private final FormToolkit toolkit;
	private final boolean isCheckedOut;

	public ContactAddressComposite(Composite parent, int style,
			FormToolkit toolkit, AbstractFormPart formPart,
			PeopleService peopleService,
			PeopleWorkbenchService peopleUiService, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.contactNode = contactNode;
		this.toolkit = toolkit;
		this.formPart = formPart;
		this.parentVersionableNode = parentVersionableNode;
		this.isCheckedOut = CommonsJcrUtils
				.isNodeCheckedOutByMe(parentVersionableNode);
		populate();
	}

	private void populate() {
		// Initialization
		final Composite parent = this;
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout(2));

		// BUTTONS
		Composite buttCmp = new ContactButtonsComposite(parent, SWT.NONE,
				toolkit, formPart, contactNode, parentVersionableNode,
				peopleUiService, peopleService);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// DATA
		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		if (!isCheckedOut) // READ ONLY
			populateReadOnlyPanel(dataCmp);
		else
			populateEditPanel(dataCmp);
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		String addressHtml = "";
		String refUid = CommonsJcrUtils.get(contactNode,
				PeopleNames.PEOPLE_REF_UID);
		if (CommonsJcrUtils.isNodeType(contactNode,
				PeopleTypes.PEOPLE_CONTACT_REF)
				&& CommonsJcrUtils.checkNotEmptyString(refUid)) {
			Node referencedEntity = peopleService.getEntityByUid(
					CommonsJcrUtils.getSession(contactNode), refUid);
			addressHtml = PeopleUiSnippets.getWorkAddress(peopleService,
					contactNode, referencedEntity);
		} else
			addressHtml = PeopleUiSnippets.getContactDisplaySnippet(
					peopleService, contactNode);
		readOnlyInfoLbl.setText(addressHtml);
	}

	protected void populateEditPanel(Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		rl.center = true;
		parent.setLayout(rl);

		String refUid = CommonsJcrUtils.get(contactNode,
				PeopleNames.PEOPLE_REF_UID);
		if (CommonsJcrUtils.isNodeType(contactNode,
				PeopleTypes.PEOPLE_CONTACT_REF)
				&& CommonsJcrUtils.checkNotEmptyString(refUid))
			populateWorkAdresseCmp(parent, contactNode);
		else
			populateAdresseCmp(parent, contactNode);
	}

	private void populateWorkAdresseCmp(Composite parent, final Node contactNode) {
		try {
			final Text valueTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"Chosen org.", "", 0);
			valueTxt.setEnabled(false);

			Link chooseOrgLk = new Link(parent, SWT.LEFT | SWT.BOTTOM);
			chooseOrgLk.setText("<a>Change</a>");
			// toolkit.adapt(chooseOrgLk, false, false);

			Text labelTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"A custom label", "A custom label", 120);

			Combo catCmb = new Combo(parent, SWT.BOTTOM | SWT.READ_ONLY);
			catCmb.setItems(peopleService.getContactService()
					.getContactPossibleValues(contactNode,
							PEOPLE_CONTACT_CATEGORY));

			final PickUpOrgDialog diag = new PickUpOrgDialog(
					chooseOrgLk.getShell(), "Choose an organisation",
					contactNode.getSession(), contactNode.getParent()
							.getParent());

			// REFRESH VALUES
			PeopleRapUtils.refreshFormText(labelTxt, contactNode,
					PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
			PeopleRapUtils.refreshFormCombo(catCmb, contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY);

			if (contactNode.hasProperty(PeopleNames.PEOPLE_REF_UID)) {
				Node linkedOrg = PeopleJcrUtils.getEntityByUid(contactNode
						.getSession(),
						contactNode.getProperty(PeopleNames.PEOPLE_REF_UID)
								.getString());
				if (linkedOrg != null)
					valueTxt.setText(CommonsJcrUtils.get(linkedOrg,
							Property.JCR_TITLE));
			}

			// Listeners
			PeopleRapUtils.addTxtModifyListener(formPart, valueTxt,
					contactNode, PeopleNames.PEOPLE_CONTACT_VALUE,
					PropertyType.STRING);
			PeopleRapUtils.addTxtModifyListener(formPart, labelTxt,
					contactNode, PeopleNames.PEOPLE_CONTACT_LABEL,
					PropertyType.STRING);
			PeopleRapUtils.addComboSelectionListener(formPart, catCmb,
					contactNode, PeopleNames.PEOPLE_CONTACT_CATEGORY,
					PropertyType.STRING);

			chooseOrgLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7118320199160680131L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					diag.open();
					Node currNode = diag.getSelected();
					if (currNode != null) {
						valueTxt.setText(CommonsJcrUtils.get(currNode,
								Property.JCR_TITLE));
						String uid = CommonsJcrUtils.get(currNode,
								PeopleNames.PEOPLE_UID);
						if (CommonsJcrUtils.setJcrProperty(contactNode,
								PeopleNames.PEOPLE_REF_UID,
								PropertyType.STRING, uid))
							formPart.markDirty();
					}
				}
			});

			parent.pack(true);
		} catch (RepositoryException e1) {
			throw new PeopleException(
					"Unable to refresh editable panel for work address", e1);
		}
	}

	private void populateAdresseCmp(Composite parent, Node contactNode) {
		boolean isCheckedOut = CommonsJcrUtils
				.isNodeCheckedOutByMe(contactNode);

		for (Control control : parent.getChildren()) {
			control.dispose();
		}

		if (isCheckedOut) {
			// specific for addresses
			final Text streetTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"Street", "Street", 0);
			final Text street2Txt = PeopleRapUtils.createRDText(toolkit,
					parent, "Street Complement", "", 0);
			final Text zipTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"Zip code", "", 0);
			final Text cityTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"City", "", 0);
			final Text stateTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"State", "", 0);
			Text countryTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"Country", "", 110);

			// The country drop down
			Session session = CommonsJcrUtils.getSession(contactNode);
			final TagLikeDropDown countryDD = new TagLikeDropDown(session,
					peopleService.getResourceService(),
					PeopleConstants.RESOURCE_COUNTRY, countryTxt);

			final Text geoPointTxt = PeopleRapUtils.createRDText(toolkit,
					parent, "Geopoint", "", 0);
			final Text labelTxt = PeopleRapUtils.createRDText(toolkit, parent,
					"Label", "", 0);

			Combo catCmb = new Combo(parent, SWT.READ_ONLY);
			catCmb.setItems(peopleService.getContactService()
					.getContactPossibleValues(contactNode,
							PEOPLE_CONTACT_CATEGORY));

			// Refresh
			PeopleRapUtils.refreshFormText(streetTxt, contactNode,
					PeopleNames.PEOPLE_STREET, "Street");
			PeopleRapUtils.refreshFormText(street2Txt, contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT, "Street complement");
			PeopleRapUtils.refreshFormText(zipTxt, contactNode,
					PeopleNames.PEOPLE_ZIP_CODE, "Zip code");
			PeopleRapUtils.refreshFormText(cityTxt, contactNode,
					PeopleNames.PEOPLE_CITY, "City");
			PeopleRapUtils.refreshFormText(stateTxt, contactNode,
					PeopleNames.PEOPLE_STATE, "State");
			PeopleRapUtils.refreshFormText(geoPointTxt, contactNode,
					PeopleNames.PEOPLE_GEOPOINT, "Geo point");
			PeopleRapUtils.refreshFormText(labelTxt, contactNode,
					PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
			PeopleRapUtils.refreshFormCombo(catCmb, contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY);

			// add listeners
			addAddressTxtModifyListener(formPart, streetTxt, contactNode,
					PeopleNames.PEOPLE_STREET, PropertyType.STRING);
			addAddressTxtModifyListener(formPart, street2Txt, contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT, PropertyType.STRING);
			addAddressTxtModifyListener(formPart, zipTxt, contactNode,
					PeopleNames.PEOPLE_ZIP_CODE, PropertyType.STRING);
			addAddressTxtModifyListener(formPart, cityTxt, contactNode,
					PeopleNames.PEOPLE_CITY, PropertyType.STRING);
			addAddressTxtModifyListener(formPart, stateTxt, contactNode,
					PeopleNames.PEOPLE_STATE, PropertyType.STRING);
			PeopleRapUtils.addTxtModifyListener(formPart, geoPointTxt,
					contactNode, PeopleNames.PEOPLE_GEOPOINT,
					PropertyType.STRING);
			PeopleRapUtils.addTxtModifyListener(formPart, labelTxt,
					contactNode, PeopleNames.PEOPLE_CONTACT_LABEL,
					PropertyType.STRING);
			PeopleRapUtils.addComboSelectionListener(formPart, catCmb,
					contactNode, PeopleNames.PEOPLE_CONTACT_CATEGORY,
					PropertyType.STRING);

			// specific for drop downs
			String countryIso = CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_COUNTRY);
			if (CommonsJcrUtils.checkNotEmptyString(countryIso)) {
				String countryVal = peopleService.getResourceService()
						.getEncodedTagValue(session,
								PeopleConstants.RESOURCE_COUNTRY, countryIso);
				countryDD.reset(countryVal);
			}
			addCountryTxtModifyListener(formPart, countryTxt);
		}
	}

	private void addCountryTxtModifyListener(final AbstractFormPart part,
			final Text text) {

		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {

				String label = text.getText();
				if (CommonsJcrUtils.isEmptyString(label))
					return;
				Session session = CommonsJcrUtils.getSession(contactNode);
				String iso = peopleService.getResourceService()
						.getEncodedTagCodeFromValue(session,
								PeopleConstants.RESOURCE_COUNTRY, label);
				if (CommonsJcrUtils.checkNotEmptyString(iso)
						&& CommonsJcrUtils.setJcrProperty(contactNode,
								PeopleNames.PEOPLE_COUNTRY,
								PropertyType.STRING, iso)) {
					part.markDirty();
				}
			}
		});
	}

	private void addAddressTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (CommonsJcrUtils.setJcrProperty(entity, propName, propType,
						text.getText())) {
					part.markDirty();
					PeopleJcrUtils.updateDisplayAddress(entity);
				}
			}
		});
	}
}