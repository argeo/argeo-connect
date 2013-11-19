package org.argeo.connect.people.ui.composites;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit a contact of type post mail
 * address information.
 * 
 */
public class ContactAddressComposite extends Composite {

	private static final long serialVersionUID = 4475049051062923873L;

	private final Node contactNode;
	private final Node parentVersionableNode;
	private final FormToolkit toolkit;
	private final IManagedForm form;

	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;
	private AbstractFormPart roFormPart;
	private AbstractFormPart editFormPart;

	public ContactAddressComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.contactNode = contactNode;
		this.toolkit = toolkit;
		this.form = form;
		this.parentVersionableNode = parentVersionableNode;

		populate();
	}

	private void populate() {
		// Initialization
		Composite parent = this;
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		// BUTTONS
		Composite buttCmp = new ContactButtonsComposite(parent, SWT.NONE,
				toolkit, form, contactNode, parentVersionableNode);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// DATA
		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dataCmp.setLayout(new FormLayout());
		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(dataCmp,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		populateReadOnlyPanel(readOnlyPanel);

		// EDIT
		final Composite editPanel = toolkit.createComposite(dataCmp,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		populateEditPanel(editPanel);

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					// Workaround: form part list has already been retrieved
					// when the Contact composite is disposed
					if (editPanel.isDisposed())
						return;

					boolean checkedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parentVersionableNode);

					// Manage switch
					editPanel.pack(true);
					editPanel.setVisible(checkedOut);
					readOnlyPanel.setVisible(!checkedOut);
					if (checkedOut) {
						editPanel.moveAbove(readOnlyPanel);
					} else {
						editPanel.moveBelow(readOnlyPanel);
					}
				} catch (Exception e) {
					if (e instanceof InvalidItemStateException) {
						// TODO clean: this exception normally means node
						// has already been removed.
					} else
						throw new PeopleException(
								"unexpected error while refreshing", e);
				}
			}
		};

		formPart.refresh();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		roFormPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (CommonsJcrUtils.nodeStillExists(contactNode)) {
					readOnlyInfoLbl.setText(PeopleHtmlUtils
							.getContactDisplaySnippet(contactNode,
									parentVersionableNode));
					readOnlyInfoLbl.pack(true);
					readOnlyPanel.pack(true);
				}
			}
		};

		roFormPart.refresh();
		roFormPart.initialize(form);
		form.addPart(roFormPart);
	}

	protected void populateEditPanel(final Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		parent.setLayout(rl);

		final AbstractFormPart sPart = new AbstractFormPart() {

			@Override
			public void commit(boolean onSave) {
				if (onSave)
					PeopleJcrUtils.updateDisplayAddress(contactNode);
				super.commit(onSave);
			}

			public void refresh() {
				super.refresh();
				if (CommonsJcrUtils.nodeStillExists(contactNode)) {
					final AbstractFormPart afp = this;
					populateAdresseCmp(parent, contactNode, afp);
				}
			}
		};

		sPart.refresh();
		sPart.initialize(form);
		form.addPart(sPart);
	}

	private void populateAdresseCmp(Composite parent, Node contactNode,
			AbstractFormPart part) {
		boolean isCheckedOut = CommonsJcrUtils
				.isNodeCheckedOutByMe(contactNode);

		for (Control control : parent.getChildren()) {
			control.dispose();
		}

		if (isCheckedOut) {
			// specific for addresses
			final Text streetTxt = PeopleUiUtils.createRDText(toolkit, parent,
					"Street", "Street", 0);
			final Text street2Txt = createAddressTxt(true, parent,
					"Street Complement", 0);
			final Text zipTxt = createAddressTxt(true, parent, "Zip code", 0);
			final Text cityTxt = createAddressTxt(true, parent, "City", 0);
			final Text stateTxt = createAddressTxt(true, parent, "State", 0);
			final Text countryTxt = createAddressTxt(true, parent, "Country", 0);
			final Text geoPointTxt = createAddressTxt(true, parent, "Geopoint",
					0);

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
			addAddressTxtModifyListener(part, streetTxt, contactNode,
					PeopleNames.PEOPLE_STREET, PropertyType.STRING);
			addAddressTxtModifyListener(part, street2Txt, contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT, PropertyType.STRING);
			addAddressTxtModifyListener(part, zipTxt, contactNode,
					PeopleNames.PEOPLE_ZIP_CODE, PropertyType.STRING);
			addAddressTxtModifyListener(part, cityTxt, contactNode,
					PeopleNames.PEOPLE_CITY, PropertyType.STRING);
			addAddressTxtModifyListener(part, stateTxt, contactNode,
					PeopleNames.PEOPLE_STATE, PropertyType.STRING);
			addAddressTxtModifyListener(part, countryTxt, contactNode,
					PeopleNames.PEOPLE_COUNTRY, PropertyType.STRING);
			PeopleUiUtils.addTxtModifyListener(part, geoPointTxt, contactNode,
					PeopleNames.PEOPLE_GEOPOINT, PropertyType.STRING);
		}
		parent.pack(true);
		parent.layout();
		parent = parent.getParent(); // the switching panel
		parent = parent.getParent(); // One line of contacts
		parent = parent.getParent(); // body for scrollable composite
		parent = parent.getParent(); // the scollable composite
		parent = parent.getParent(); // the fullTab
		parent.pack(true);
		parent.layout();
	}

	// TODO remove this.
	protected Text createAddressTxt(boolean create, Composite parent,
			String msg, int width) {
		if (create) {
			Text text = toolkit.createText(parent, null, SWT.BORDER);
			text.setMessage(msg);
			text.setLayoutData(width == 0 ? new RowData() : new RowData(width,
					SWT.DEFAULT));
			return text;
		} else
			return null;
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