package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit a contact of type post mail
 * address information.
 * 
 */
public class ContactAddressComposite extends ContactComposite {

	private static final long serialVersionUID = 4475049051062923873L;

	public ContactAddressComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style, toolkit, form, contactNode, parentVersionableNode);

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
					Composite cmp2 = parent.getParent();
					cmp2.pack();
					cmp2.getParent().layout(true);
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
			if (!(control instanceof Button))
				control.dispose();
		}

		if (isCheckedOut) {
			// specific for addresses
			final Text streetTxt = createAddressTxt(true, parent, "Street", 0);
			final Text street2Txt = createAddressTxt(true, parent,
					"Street Complement", 0);
			final Text zipTxt = createAddressTxt(true, parent, "Zip code", 0);
			final Text cityTxt = createAddressTxt(true, parent, "City", 0);
			final Text stateTxt = createAddressTxt(true, parent, "State", 0);
			final Text countryTxt = createAddressTxt(true, parent, "Country", 0);
			final Text geoPointTxt = createAddressTxt(true, parent, "Geopoint",
					0);

			// TODO Factorize this using utilitary method
			streetTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STREET));
			streetTxt.setEnabled(isCheckedOut);
			streetTxt.setMessage(isCheckedOut ? "Street" : "");
			street2Txt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT));
			street2Txt.setEnabled(isCheckedOut);
			street2Txt.setMessage(isCheckedOut ? "Street complement" : "");
			zipTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_ZIP_CODE));
			zipTxt.setEnabled(isCheckedOut);
			zipTxt.setMessage(isCheckedOut ? "Zip code" : "");
			cityTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_CITY));
			cityTxt.setEnabled(isCheckedOut);
			cityTxt.setMessage(isCheckedOut ? "City" : "");
			stateTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STATE));
			stateTxt.setEnabled(isCheckedOut);
			stateTxt.setMessage(isCheckedOut ? "State" : "");
			countryTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_COUNTRY));
			countryTxt.setEnabled(isCheckedOut);
			countryTxt.setMessage(isCheckedOut ? "Country" : "");
			geoPointTxt.setText(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_GEOPOINT));
			geoPointTxt.setEnabled(isCheckedOut);
			geoPointTxt.setMessage(isCheckedOut ? "Geo point" : "");

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
		parent.layout();
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