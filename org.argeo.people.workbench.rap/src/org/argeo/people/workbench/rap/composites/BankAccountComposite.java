package org.argeo.people.workbench.rap.composites;

import javax.jcr.Node;

import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.ui.workbench.parts.AbstractConnectEditor;
import org.argeo.people.PeopleNames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Simple composite to display and edit information about a bank account */
public class BankAccountComposite extends Composite {
	private static final long serialVersionUID = -3303030374442774568L;

	private final Node currAccount;

	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;
	private final IManagedForm form;
	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;

	public BankAccountComposite(Composite parent, int style, AbstractConnectEditor editor, Node bankAccount) {
		super(parent, style);
		this.currAccount = bankAccount;
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.form = editor.getManagedForm();
		populate();
	}

	private void populate() {
		// initialization
		Composite parent = this;
		this.setLayout(new GridLayout(6, false));

		// Main Info
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Account Holder");
		final Text holderTxt = toolkit.createText(parent, "", SWT.BORDER);
		holderTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Bank Name");
		final Text bankNameTxt = toolkit.createText(parent, "", SWT.BORDER);
		bankNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Currency");
		final Text currencyTxt = toolkit.createText(parent, "", SWT.BORDER);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 50;
		currencyTxt.setLayoutData(gd);

		// Bank number
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Account Number");
		final Text accNbTxt = toolkit.createText(parent, "", SWT.BORDER);
		accNbTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Bank Number");
		final Text bankNbTxt = toolkit.createText(parent, "", SWT.BORDER);
		bankNbTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		// BIC / IBAN
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "BIC");
		final Text bicTxt = toolkit.createText(parent, "", SWT.BORDER);
		bicTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "IBAN");
		final Text ibanTxt = toolkit.createText(parent, "", SWT.BORDER);
		ibanTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (!holderTxt.isDisposed()) {
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, holderTxt, currAccount,
							PeopleNames.PEOPLE_ACCOUNT_HOLDER);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, bankNameTxt, currAccount,
							PeopleNames.PEOPLE_BANK_NAME);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, currencyTxt, currAccount, PeopleNames.PEOPLE_CURRENCY);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, accNbTxt, currAccount, PeopleNames.PEOPLE_ACCOUNT_NB);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, bankNbTxt, currAccount, PeopleNames.PEOPLE_BANK_NB);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, bicTxt, currAccount, PeopleNames.PEOPLE_BIC);
					ConnectWorkbenchUtils.refreshFormTextWidget(editor, ibanTxt, currAccount, PeopleNames.PEOPLE_IBAN);
					holderTxt.getParent().layout(true, true);
				}
			}
		};

		// TODO must explicitly be called because it is created by the refresh
		// of a form part
		formPart.refresh();

		// Listeners
		ConnectWorkbenchUtils.addModifyListener(holderTxt, currAccount, PeopleNames.PEOPLE_ACCOUNT_HOLDER, formPart);
		ConnectWorkbenchUtils.addModifyListener(bankNameTxt, currAccount, PeopleNames.PEOPLE_BANK_NAME, formPart);
		ConnectWorkbenchUtils.addModifyListener(currencyTxt, currAccount, PeopleNames.PEOPLE_CURRENCY, formPart);
		ConnectWorkbenchUtils.addModifyListener(accNbTxt, currAccount, PeopleNames.PEOPLE_ACCOUNT_NB, formPart);
		ConnectWorkbenchUtils.addModifyListener(bankNbTxt, currAccount, PeopleNames.PEOPLE_BANK_NB, formPart);
		ConnectWorkbenchUtils.addModifyListener(bicTxt, currAccount, PeopleNames.PEOPLE_BIC, formPart);
		ConnectWorkbenchUtils.addModifyListener(ibanTxt, currAccount, PeopleNames.PEOPLE_IBAN, formPart);

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
