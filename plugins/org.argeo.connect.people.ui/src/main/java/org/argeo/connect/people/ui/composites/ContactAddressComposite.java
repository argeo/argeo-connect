package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.dialogs.PickUpOrgDialog;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
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
public class ContactAddressComposite extends Composite {

	private static final long serialVersionUID = 4475049051062923873L;

	private final PeopleService peopleService;
	private final Node contactNode;
	private final Node parentVersionableNode;
	private final AbstractFormPart formPart;
	private final FormToolkit toolkit;
	private final boolean isCheckedOut;

	public ContactAddressComposite(Composite parent, int style,
			FormToolkit toolkit, AbstractFormPart formPart,
			PeopleService peopleService, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.peopleService = peopleService;
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
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		// BUTTONS
		Composite buttCmp = new ContactButtonsComposite(parent, SWT.NONE,
				toolkit, formPart, contactNode, parentVersionableNode);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// DATA
		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		if (!isCheckedOut) // READ ONLY
			populateReadOnlyPanel(dataCmp);
		else
			populateEditPanel(dataCmp);
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		String addressHtml = "";
		if (CommonsJcrUtils.isNodeType(contactNode,
				PeopleTypes.PEOPLE_CONTACT_REF)) {
			Node referencedEntity = peopleService.getEntityByUid(
					CommonsJcrUtils.getSession(contactNode), CommonsJcrUtils
							.get(contactNode, PeopleNames.PEOPLE_REF_UID));
			addressHtml = PeopleHtmlUtils.getWorkAddressDisplaySnippet(
					contactNode, referencedEntity);
		} else
			addressHtml = PeopleHtmlUtils.getContactDisplaySnippet(contactNode,
					parentVersionableNode);
		readOnlyInfoLbl.setText(addressHtml);
	}

	protected void populateEditPanel(Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		parent.setLayout(rl);
		if (CommonsJcrUtils.isNodeType(contactNode,
				PeopleTypes.PEOPLE_CONTACT_REF))
			populateWorkAdresseCmp(parent, contactNode);
		else
			populateAdresseCmp(parent, contactNode);
	}

	private void populateWorkAdresseCmp(Composite parent, final Node contactNode) {
		try {
			// The widgets
			final Combo catCmb = new Combo(parent, SWT.NONE);
			final Text labelTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"", "", 120);

			final Link chooseOrgLk = new Link(parent, SWT.NONE);
			toolkit.adapt(chooseOrgLk, false, false);
			chooseOrgLk.setText("<a>Pick up an Org.</a>");
			final PickUpOrgDialog diag = new PickUpOrgDialog(
					chooseOrgLk.getShell(), "Choose an organisation",
					contactNode.getSession(), contactNode.getParent()
							.getParent());

			final Text valueTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"Chosen org.", "", 150);
			valueTxt.setEnabled(false);

			// REFRESH VALUES
			PeopleUiUtils.refreshFormTextWidget(labelTxt, contactNode,
					PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
			String nature = CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_CONTACT_NATURE);
			catCmb.setItems(ContactValueCatalogs.getCategoryList(
					parentVersionableNode.getPrimaryNodeType().getName(),
					contactNode.getPrimaryNodeType().getName(), nature));
			catCmb.select(0);
			PeopleUiUtils.refreshFormComboValue(catCmb, contactNode,
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
			PeopleUiUtils.addTxtModifyListener(formPart, valueTxt, contactNode,
					PeopleNames.PEOPLE_CONTACT_VALUE, PropertyType.STRING);
			PeopleUiUtils.addTxtModifyListener(formPart, labelTxt, contactNode,
					PeopleNames.PEOPLE_CONTACT_LABEL, PropertyType.STRING);
			if (catCmb != null)
				PeopleUiUtils.addComboSelectionListener(formPart, catCmb,
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
						if (JcrUiUtils.setJcrProperty(contactNode,
								PeopleNames.PEOPLE_REF_UID,
								PropertyType.STRING, uid))
							formPart.markDirty();
					}
				}
			});

			parent.pack(true);
			// parent.layout();
		} catch (RepositoryException e1) {
			throw new PeopleException(
					"unable to refresh edit work address panel ", e1);
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
			final Text streetTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"Street", "Street", 0);
			final Text street2Txt = PeopleUiUtils.createRDText(toolkit, parent,
					"Street Complement", "", 0);
			final Text zipTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"Zip code", "", 0);
			final Text cityTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"City", "", 0);
			final Text stateTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"State", "", 0);
			final Text countryTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"Country", "", 0);
			final Text geoPointTxt = PeopleUiUtils.createRDText(toolkit,
					parent, "Geopoint", "", 0);

			// Refresh
			PeopleUiUtils.refreshFormTextWidget(streetTxt, contactNode,
					PeopleNames.PEOPLE_STREET, "Street");
			PeopleUiUtils.refreshFormTextWidget(street2Txt, contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT, "Street complement");
			PeopleUiUtils.refreshFormTextWidget(zipTxt, contactNode,
					PeopleNames.PEOPLE_ZIP_CODE, "Zip code");
			PeopleUiUtils.refreshFormTextWidget(cityTxt, contactNode,
					PeopleNames.PEOPLE_CITY, "City");
			PeopleUiUtils.refreshFormTextWidget(stateTxt, contactNode,
					PeopleNames.PEOPLE_STATE, "State");
			PeopleUiUtils.refreshFormTextWidget(countryTxt, contactNode,
					PeopleNames.PEOPLE_COUNTRY, "Country");
			PeopleUiUtils.refreshFormTextWidget(geoPointTxt, contactNode,
					PeopleNames.PEOPLE_GEOPOINT, "Geo point");

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
			addAddressTxtModifyListener(formPart, countryTxt, contactNode,
					PeopleNames.PEOPLE_COUNTRY, PropertyType.STRING);
			PeopleUiUtils.addTxtModifyListener(formPart, geoPointTxt,
					contactNode, PeopleNames.PEOPLE_GEOPOINT,
					PropertyType.STRING);
		}
		// parent.pack(true);
		// parent.layout();
		// parent = parent.getParent(); // the switching panel
		// parent = parent.getParent(); // One line of contacts
		// parent = parent.getParent(); // body for scrollable composite
		// parent = parent.getParent(); // the scollable composite
		// parent = parent.getParent(); // the fullTab
		// parent.pack(true);
		// parent.layout();
	}

	private void addAddressTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
						text.getText())) {
					part.markDirty();
					PeopleJcrUtils.updateDisplayAddress(entity);
				}
			}
		});
	}
}