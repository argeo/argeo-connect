package org.argeo.connect.people.rap.composites;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple composite widget to display and edit information about a bank account
 * 
 */
public class BankAccountComposite extends Composite {
	private static final long serialVersionUID = -3303030374442774568L;

	private final Node currAccount;
	private final FormToolkit toolkit;
	private final IManagedForm form;
	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;

	public BankAccountComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node bankAccount) {
		super(parent, style);
		this.currAccount = bankAccount;
		this.toolkit = toolkit;
		this.form = form;
		populate();
	}

	private void populate() {
		// initialization
		Composite parent = this;
		this.setLayout(new GridLayout(6, false));

		// Main Info
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Account Holder");
		final Text holderTxt = toolkit.createText(parent, "", SWT.BORDER);
		holderTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Bank Name");
		final Text bankNameTxt = toolkit.createText(parent, "", SWT.BORDER);
		bankNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Currency");
		final Text currencyTxt = toolkit.createText(parent, "", SWT.BORDER);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 50;
		currencyTxt.setLayoutData(gd);

		// Bank number
		PeopleRapUtils.createBoldLabel(toolkit, parent, "Account Number");
		final Text accNbTxt = toolkit.createText(parent, "", SWT.BORDER);
		accNbTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		PeopleRapUtils.createBoldLabel(toolkit, parent, "Bank Number");
		final Text bankNbTxt = toolkit.createText(parent, "", SWT.BORDER);
		bankNbTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3,
				1));

		// BIC / IBAN
		PeopleRapUtils.createBoldLabel(toolkit, parent, "BIC");
		final Text bicTxt = toolkit.createText(parent, "", SWT.BORDER);
		bicTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		PeopleRapUtils.createBoldLabel(toolkit, parent, "IBAN");
		final Text ibanTxt = toolkit.createText(parent, "", SWT.BORDER);
		ibanTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				if (!holderTxt.isDisposed()) {
					PeopleRapUtils.refreshFormTextWidget(holderTxt, currAccount,
							PeopleNames.PEOPLE_ACCOUNT_HOLDER);
					PeopleRapUtils.refreshFormTextWidget(bankNameTxt,
							currAccount, PeopleNames.PEOPLE_BANK_NAME);
					PeopleRapUtils.refreshFormTextWidget(currencyTxt,
							currAccount, PeopleNames.PEOPLE_CURRENCY);
					PeopleRapUtils.refreshFormTextWidget(accNbTxt, currAccount,
							PeopleNames.PEOPLE_ACCOUNT_NB);
					PeopleRapUtils.refreshFormTextWidget(bankNbTxt, currAccount,
							PeopleNames.PEOPLE_BANK_NB);
					PeopleRapUtils.refreshFormTextWidget(bicTxt, currAccount,
							PeopleNames.PEOPLE_BIC);
					PeopleRapUtils.refreshFormTextWidget(ibanTxt, currAccount,
							PeopleNames.PEOPLE_IBAN);
				}
			}
		};

		// Listeners
		PeopleRapUtils.addModifyListener(holderTxt, currAccount,
				PeopleNames.PEOPLE_ACCOUNT_HOLDER, formPart);
		PeopleRapUtils.addModifyListener(bankNameTxt, currAccount,
				PeopleNames.PEOPLE_BANK_NAME, formPart);
		PeopleRapUtils.addModifyListener(currencyTxt, currAccount,
				PeopleNames.PEOPLE_CURRENCY, formPart);
		PeopleRapUtils.addModifyListener(accNbTxt, currAccount,
				PeopleNames.PEOPLE_ACCOUNT_NB, formPart);
		PeopleRapUtils.addModifyListener(bankNbTxt, currAccount,
				PeopleNames.PEOPLE_BANK_NB, formPart);
		PeopleRapUtils.addModifyListener(bicTxt, currAccount,
				PeopleNames.PEOPLE_BIC, formPart);
		PeopleRapUtils.addModifyListener(ibanTxt, currAccount,
				PeopleNames.PEOPLE_IBAN, formPart);

		// Superstition?
		formPart.refresh();
		parent.layout();

		formPart.initialize(form);
		form.addPart(formPart);
	}

	@Override
	public boolean setFocus() {
		return true;
	}

	@Override
	public void dispose() {
		form.removePart(formPart);
		formPart.dispose();
		super.dispose();
	}
}