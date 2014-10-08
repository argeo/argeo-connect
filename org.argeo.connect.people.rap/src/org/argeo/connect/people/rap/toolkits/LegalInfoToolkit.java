package org.argeo.connect.people.rap.toolkits;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.composites.BankAccountComposite;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.OrgJcrUtils;
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
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Legal Name");
		final Text legalNameTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1));

		// Legal form
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Legal Form");
		final Text legalFormTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalFormTxt
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// VAT ID Number
		PeopleRapUtils.createBoldLabel(toolkit, parent, "VAT ID");
		final Text vatIDTxt = toolkit.createText(parent, "", SWT.BORDER);
		vatIDTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		final AbstractFormPart notePart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleRapUtils.refreshFormTextWidget(legalNameTxt, entity,
						PeopleNames.PEOPLE_LEGAL_NAME);
				PeopleRapUtils.refreshFormTextWidget(legalFormTxt, entity,
						PeopleNames.PEOPLE_LEGAL_FORM);
				PeopleRapUtils.refreshFormTextWidget(vatIDTxt, entity,
						PeopleNames.PEOPLE_VAT_ID_NB);
			}
		};

		// Listeners
		PeopleRapUtils.addModifyListener(legalFormTxt, entity,
				PeopleNames.PEOPLE_LEGAL_FORM, notePart);
		PeopleRapUtils.addModifyListener(vatIDTxt, entity,
				PeopleNames.PEOPLE_VAT_ID_NB, notePart);

		// Specific listeners to manage correctly display name
		legalNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 6068716814124883599L;

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					if (CommonsJcrUtils.setJcrProperty(entity,
							PeopleNames.PEOPLE_LEGAL_NAME, PropertyType.STRING,
							legalNameTxt.getText())) {
						Boolean defineDistinct = CommonsJcrUtils
								.getBooleanValue(
										entity,
										PeopleNames.PEOPLE_USE_DISTINCT_DISPLAY_NAME);
						if (defineDistinct == null || !defineDistinct)
							entity.setProperty(Property.JCR_TITLE,
									legalNameTxt.getText());
						notePart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update property", e);
				}
			}
		});

		parent.layout();
		notePart.initialize(form);
		form.addPart(notePart);
	}

	private void populateBankAccountGroup(Composite parent) {
		parent.setLayout(PeopleRapUtils.noSpaceGridLayout());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Payment account");
		group.setLayout(PeopleRapUtils.noSpaceGridLayout());

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				// TODO Manage multiple bank account
				super.refresh();
				try {
					if (!entity.hasNode(PeopleNames.PEOPLE_PAYMENT_ACCOUNTS)
							&& CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
						OrgJcrUtils.createPaymentAccount(entity,
								PeopleTypes.PEOPLE_BANK_ACCOUNT, "new");
						entity.getSession().save();
					}
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Unable to create bank account for " + entity, e);
				}

				Control[] children = group.getChildren();
				for (Control child : children) {
					child.dispose();
				}

				NodeIterator ni = OrgJcrUtils.getPaymentAccounts(entity);
				while (ni != null && ni.hasNext()) {
					Composite cmp = new BankAccountComposite(group, 0, toolkit,
							form, ni.nextNode());
					cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
							false));
				}
				group.layout();
			}
		};
		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
	}
}