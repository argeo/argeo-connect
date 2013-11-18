package org.argeo.connect.people.ui.toolkits;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.composites.BankAccountComposite;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class LegalInfoToolkit {

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private Node entity;

	// private PeopleService peopleService;

	// private DateFormat dateTimeFormat = new SimpleDateFormat(
	// PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	public LegalInfoToolkit(FormToolkit toolkit, IManagedForm form, Node entity) {
		// PeopleService peopleService,
		this.toolkit = toolkit;
		this.form = form;
		this.entity = entity;
		// this.peopleService = peopleService;
	}

	public void populateLegalInfoPanel(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite adminInfoCmp = toolkit.createComposite(parent);
		adminInfoCmp
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateAdminInfoCmp(adminInfoCmp);

		Composite payAccCmp = toolkit.createComposite(parent);
		payAccCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateBankAccountGroup(payAccCmp);

	}

	private void populateAdminInfoCmp(Composite parent) {
		parent.setLayout(new GridLayout(4, false));

		// Legal Name
		toolkit.createLabel(parent, "Legal Name");
		final Text legalNameTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1));

		// Legal form
		toolkit.createLabel(parent, "Legal form");
		final Text legalFormTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalFormTxt
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// VAT ID Number
		toolkit.createLabel(parent, "VAT ID Number");
		final Text vatIDTxt = toolkit.createText(parent, "", SWT.BORDER);
		vatIDTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		final AbstractFormPart notePart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleUiUtils.refreshFormTextWidget(legalNameTxt, entity,
						PeopleNames.PEOPLE_LEGAL_NAME);
				PeopleUiUtils.refreshFormTextWidget(legalFormTxt, entity,
						PeopleNames.PEOPLE_LEGAL_STATUS);
				PeopleUiUtils.refreshFormTextWidget(vatIDTxt, entity,
						PeopleNames.PEOPLE_VAT_ID_NB);
			}
		};

		// PeopleUiUtils.addModifyListener(legalNameTxt, entity,
		// PeopleNames.PEOPLE_LEGAL_NAME, notePart);

		// Specific listeners to manage correctly display name
		legalNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 6068716814124883599L;

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					if (JcrUiUtils.setJcrProperty(entity,
							PeopleNames.PEOPLE_LEGAL_NAME, PropertyType.STRING,
							legalNameTxt.getText())) {
						if (entity.getProperty(
								PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME)
								.getBoolean()) {
							entity.setProperty(Property.JCR_TITLE,
									legalNameTxt.getText());
						}
						notePart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update property", e);
				}
			}
		});
		PeopleUiUtils.addModifyListener(legalFormTxt, entity,
				PeopleNames.PEOPLE_LEGAL_STATUS, notePart);
		PeopleUiUtils.addModifyListener(vatIDTxt, entity,
				PeopleNames.PEOPLE_VAT_ID_NB, notePart);

		parent.layout();

		notePart.initialize(form);
		form.addPart(notePart);
	}

	private void populateBankAccountGroup(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Payment accounts");
		group.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				// TODO add "create account button"
				super.refresh();
				try {
					if (!entity.hasNode(PeopleNames.PEOPLE_PAYMENT_ACCOUNTS)
							&& CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
						PeopleJcrUtils.createPaymentAccount(entity,
								PeopleTypes.PEOPLE_BANK_ACCOUNT, "new");
						entity.getSession().save();
					}
				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Control[] children = group.getChildren();
				for (Control child : children) {
					child.dispose();
				}

				NodeIterator ni = PeopleJcrUtils.getPaymentAccounts(entity);
				while (ni != null && ni.hasNext()) {
					Composite cmp = new BankAccountComposite(group, 0, toolkit,
							form, ni.nextNode());
					cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
							false));
				}
				group.layout();
			}
		};
		// notePart.refresh();
		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
	}
}
