package org.argeo.people.workbench.rap.composites;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.widgets.TagLikeDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.workbench.rap.dialogs.PickUpOrgDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.forms.AbstractFormPart;
//import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit a contact of type post mail
 * address information.
 */
public class ContactAddressComposite extends Composite implements PeopleNames {
	private static final long serialVersionUID = 4475049051062923873L;

	// private final static Log log = LogFactory
	// .getLog(ContactAddressComposite.class);

	private final ResourcesService resourcesService;
	private final PeopleService peopleService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node contactNode;
	private final Node parentVersionableNode;

	private final AbstractConnectEditor editor;
	private final AbstractFormPart formPart;
	private final FormToolkit toolkit;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param editor
	 * @param formPart
	 * @param resourcesService
	 * @param peopleService
	 * @param systemWorkbenchService
	 * @param contactNode
	 * @param parentVersionableNode
	 */
	public ContactAddressComposite(Composite parent, int style, AbstractConnectEditor editor, AbstractFormPart formPart,
			ResourcesService resourcesService, PeopleService peopleService,
			SystemWorkbenchService systemWorkbenchService, Node contactNode, Node parentVersionableNode) {
		super(parent, style);
		this.resourcesService = resourcesService;
		this.peopleService = peopleService;
		this.systemWorkbenchService = systemWorkbenchService;
		this.contactNode = contactNode;

		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.formPart = formPart;
		this.parentVersionableNode = parentVersionableNode;
		populate();
	}

	private void populate() {
		// Initialization
		final Composite parent = this;
		parent.setLayout(ConnectUiUtils.noSpaceGridLayout(2));

		// BUTTONS
		Composite buttCmp = new ContactButtonsComposite(editor, formPart, parent, SWT.NONE, contactNode,
				parentVersionableNode, resourcesService, peopleService, systemWorkbenchService);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// DATA
		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(EclipseUiUtils.fillWidth());

		if (editor.isEditing())
			populateEditPanel(dataCmp);
		else
			populateReadOnlyPanel(dataCmp);
	}

	protected void populateReadOnlyPanel(Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		String refUid = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_REF_UID);
		if (ConnectJcrUtils.isNodeType(contactNode, PeopleTypes.PEOPLE_CONTACT_REF)
				&& EclipseUiUtils.notEmpty(refUid)) {

			final Node referencedEntity = peopleService.getEntityByUid(ConnectJcrUtils.getSession(contactNode), null,
					refUid);

			Link readOnlyInfoLk = new Link(readOnlyPanel, SWT.WRAP);
			// CmsUtils.markup(readOnlyInfoLk);
			StringBuilder builder = new StringBuilder();
			// the referenced org
			if (referencedEntity != null) {
				String label = ConnectJcrUtils.get(referencedEntity, Property.JCR_TITLE);
				builder.append("<a>").append(label).append("</a> ");
			}
			// current contact meta data
			String meta = PeopleUiSnippets.getContactMetaData(contactNode);
			// work around to remove the encoded space. To be cleaned.
			if (meta.startsWith(ConnectUiConstants.NB_DOUBLE_SPACE))
				meta = meta.substring(ConnectUiConstants.NB_DOUBLE_SPACE.length());
			builder.append(meta);

			// Referenced org primary address
			if (referencedEntity != null) {
				Node primaryAddress = PeopleJcrUtils.getPrimaryContact(referencedEntity,
						PeopleTypes.PEOPLE_POSTAL_ADDRESS);
				if (primaryAddress != null) {
					builder.append("\n");
					builder.append(PeopleUiSnippets.getAddressDisplayValue(resourcesService, primaryAddress));
				}
			}
			readOnlyInfoLk.setText(ConnectUiUtils.replaceAmpersandforSWTLink(builder.toString()));
			OrgLinkListener oll = new OrgLinkListener();
			oll.setOrg(referencedEntity);
			readOnlyInfoLk.addSelectionListener(oll);
		} else {
			Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "", SWT.WRAP);
			readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			String addressHtml = PeopleUiSnippets.getContactDisplaySnippet(resourcesService, contactNode);
			readOnlyInfoLbl.setText(addressHtml);
		}
	}

	protected void populateEditPanel(Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		rl.center = true;
		parent.setLayout(rl);

		String refUid = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_REF_UID);
		if (ConnectJcrUtils.isNodeType(contactNode, PeopleTypes.PEOPLE_CONTACT_REF) && EclipseUiUtils.notEmpty(refUid))
			populateWorkAdresseCmp(parent, contactNode);
		else
			populateAdresseCmp(parent, contactNode);
	}

	private void populateWorkAdresseCmp(Composite parent, final Node contactNode) {
		try {
			final Link nameLk = new Link(parent, SWT.LEFT | SWT.BOTTOM);
			final OrgLinkListener nameLkListener = new OrgLinkListener();
			nameLk.addSelectionListener(nameLkListener);

			Link chooseOrgLk = new Link(parent, SWT.LEFT | SWT.BOTTOM);
			chooseOrgLk.setText("<a>Change</a>");

			Text labelTxt = ConnectUiUtils.createRDText(toolkit, parent, "A custom label", "A custom label",
					120);

			Combo catCmb = new Combo(parent, SWT.BOTTOM | SWT.READ_ONLY);
			catCmb.setItems(peopleService.getContactService().getContactPossibleCategories(contactNode));

			final PickUpOrgDialog diag = new PickUpOrgDialog(chooseOrgLk.getShell(), "Choose an organisation",
					contactNode.getSession(), systemWorkbenchService, contactNode.getParent().getParent());

			// REFRESH VALUES
			ConnectWorkbenchUtils.refreshFormCombo(editor, catCmb, contactNode, Property.JCR_TITLE);
			ConnectWorkbenchUtils.refreshFormText(editor, labelTxt, contactNode, Property.JCR_DESCRIPTION,
					"Description");

			if (contactNode.hasProperty(PeopleNames.PEOPLE_REF_UID)) {
				Node linkedOrg = peopleService.getEntityByUid(contactNode.getSession(), null,
						contactNode.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
				if (linkedOrg != null) {
					nameLkListener.setOrg(linkedOrg);
					nameLk.setText("<a>" + ConnectJcrUtils.get(linkedOrg, Property.JCR_TITLE) + "</a>");
				}
			}

			// Listeners
			ConnectWorkbenchUtils.addTxtModifyListener(formPart, catCmb, contactNode, Property.JCR_TITLE,
					PropertyType.STRING);
			ConnectWorkbenchUtils.addTxtModifyListener(formPart, labelTxt, contactNode, Property.JCR_DESCRIPTION,
					PropertyType.STRING);

			chooseOrgLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7118320199160680131L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					diag.open();
					Node currNode = diag.getSelected();
					if (currNode != null) {
						nameLkListener.setOrg(currNode);
						nameLk.setText("<a>" + ConnectJcrUtils.get(currNode, Property.JCR_TITLE) + "</a>");

						String uid = ConnectJcrUtils.get(currNode, ConnectNames.CONNECT_UID);
						if (ConnectJcrUtils.setJcrProperty(contactNode, PeopleNames.PEOPLE_REF_UID, PropertyType.STRING,
								uid))
							formPart.markDirty();
					}
				}
			});
			parent.pack(true);
		} catch (RepositoryException e1) {
			throw new PeopleException("Unable to refresh editable panel for work address", e1);
		}
	}

	private class OrgLinkListener extends SelectionAdapter {
		private static final long serialVersionUID = 1L;
		private Node org;

		protected void setOrg(Node org) {
			this.org = org;
		}

		@Override
		public void widgetSelected(final SelectionEvent event) {
			if (org != null) {
				CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
						ConnectEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(org));
			}

		}
	}

	private void populateAdresseCmp(Composite parent, Node contactNode) {

		EclipseUiUtils.clear(parent);
		if (editor.isEditing()) {
			// specific for addresses
			final Text streetTxt = ConnectUiUtils.createRDText(toolkit, parent, "Street", "Street", 0);
			final Text street2Txt = ConnectUiUtils.createRDText(toolkit, parent, "Street Complement", "", 0);
			final Text zipTxt = ConnectUiUtils.createRDText(toolkit, parent, "Zip code", "", 0);
			final Text cityTxt = ConnectUiUtils.createRDText(toolkit, parent, "City", "", 0);
			final Text stateTxt = ConnectUiUtils.createRDText(toolkit, parent, "State", "", 0);
			Text countryTxt = ConnectUiUtils.createRDText(toolkit, parent, "Country", "", 110);

			// The country drop down
			Session session = ConnectJcrUtils.getSession(contactNode);
			final TagLikeDropDown countryDD = new TagLikeDropDown(session, resourcesService,
					ConnectConstants.RESOURCE_COUNTRY, countryTxt);

			final Text geoPointTxt = ConnectUiUtils.createRDText(toolkit, parent, "Geopoint", "", 0);
			final Text labelTxt = ConnectUiUtils.createRDText(toolkit, parent, "Label", "", 0);

			Combo catCmb = new Combo(parent, SWT.READ_ONLY);
			catCmb.setItems(peopleService.getContactService().getContactPossibleCategories(contactNode));

			// Refresh
			ConnectWorkbenchUtils.refreshFormText(editor, streetTxt, contactNode, PeopleNames.PEOPLE_STREET, "Street");
			ConnectWorkbenchUtils.refreshFormText(editor, street2Txt, contactNode, PeopleNames.PEOPLE_STREET_COMPLEMENT,
					"Street complement");
			ConnectWorkbenchUtils.refreshFormText(editor, zipTxt, contactNode, PeopleNames.PEOPLE_ZIP_CODE, "Zip code");
			ConnectWorkbenchUtils.refreshFormText(editor, cityTxt, contactNode, PeopleNames.PEOPLE_CITY, "City");
			ConnectWorkbenchUtils.refreshFormText(editor, stateTxt, contactNode, PeopleNames.PEOPLE_STATE, "State");
			ConnectWorkbenchUtils.refreshFormText(editor, geoPointTxt, contactNode, PeopleNames.PEOPLE_GEOPOINT,
					"Geo point");
			ConnectWorkbenchUtils.refreshFormText(editor, labelTxt, contactNode, Property.JCR_DESCRIPTION,
					"Description");
			ConnectWorkbenchUtils.refreshFormCombo(editor, catCmb, contactNode, Property.JCR_TITLE);

			// add listeners
			addAddressTxtModifyListener(formPart, streetTxt, contactNode, PeopleNames.PEOPLE_STREET,
					PropertyType.STRING);
			addAddressTxtModifyListener(formPart, street2Txt, contactNode, PeopleNames.PEOPLE_STREET_COMPLEMENT,
					PropertyType.STRING);
			addAddressTxtModifyListener(formPart, zipTxt, contactNode, PeopleNames.PEOPLE_ZIP_CODE,
					PropertyType.STRING);
			addAddressTxtModifyListener(formPart, cityTxt, contactNode, PeopleNames.PEOPLE_CITY, PropertyType.STRING);
			addAddressTxtModifyListener(formPart, stateTxt, contactNode, PeopleNames.PEOPLE_STATE, PropertyType.STRING);
			ConnectWorkbenchUtils.addTxtModifyListener(formPart, geoPointTxt, contactNode, PeopleNames.PEOPLE_GEOPOINT,
					PropertyType.STRING);
			ConnectWorkbenchUtils.addTxtModifyListener(formPart, catCmb, contactNode, Property.JCR_TITLE,
					PropertyType.STRING);
			ConnectWorkbenchUtils.addTxtModifyListener(formPart, labelTxt, contactNode, Property.JCR_DESCRIPTION,
					PropertyType.STRING);

			// specific for drop downs
			String countryIso = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_COUNTRY);
			if (EclipseUiUtils.notEmpty(countryIso)) {
				String countryVal = resourcesService.getEncodedTagValue(session, ConnectConstants.RESOURCE_COUNTRY,
						countryIso);
				countryDD.reset(countryVal);
			}
			addCountryTxtModifyListener(formPart, countryTxt);
		}
	}

	private void addCountryTxtModifyListener(final AbstractFormPart part, final Text text) {

		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {

				String label = text.getText();
				if (EclipseUiUtils.isEmpty(label))
					return;
				Session session = ConnectJcrUtils.getSession(contactNode);
				String iso = resourcesService.getEncodedTagCodeFromValue(session, ConnectConstants.RESOURCE_COUNTRY,
						label);
				if (EclipseUiUtils.notEmpty(iso) && ConnectJcrUtils.setJcrProperty(contactNode,
						PeopleNames.PEOPLE_COUNTRY, PropertyType.STRING, iso)) {
					part.markDirty();
				}
			}
		});
	}

	private void addAddressTxtModifyListener(final AbstractFormPart part, final Text text, final Node entity,
			final String propName, final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (ConnectJcrUtils.setJcrProperty(entity, propName, propType, text.getText())) {
					part.markDirty();
					PeopleJcrUtils.updateDisplayAddress(resourcesService, entity);
				}
			}
		});
	}
}
