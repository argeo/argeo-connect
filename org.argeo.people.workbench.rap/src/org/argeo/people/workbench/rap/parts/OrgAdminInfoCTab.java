package org.argeo.people.workbench.rap.parts;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.OrgJcrUtils;
import org.argeo.people.workbench.rap.composites.BankAccountComposite;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that displays all administrative and
 * legal information for a given organization
 * 
 * TODO Legacy code. Should be reviewed and enhanced.
 */
public class OrgAdminInfoCTab extends LazyCTabControl {
	private static final long serialVersionUID = -7033074223243935324L;

	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;
	private final Node entity;

	// this page UI Objects
	private AbstractFormPart notePart;
	private AbstractFormPart formPart;

	public OrgAdminInfoCTab(Composite parent, int style, AbstractConnectEditor editor, Node entity) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.entity = entity;
	}

	@Override
	public void refreshPartControl() {
		notePart.refresh();
		formPart.refresh();
		layout(true, true);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite adminInfoCmp = toolkit.createComposite(parent);
		adminInfoCmp.setLayoutData(EclipseUiUtils.fillWidth());
		populateAdminInfoCmp(adminInfoCmp);

		Composite payAccCmp = toolkit.createComposite(parent);
		payAccCmp.setLayoutData(EclipseUiUtils.fillAll());
		populateBankAccountGroup(payAccCmp);
	}

	private void populateAdminInfoCmp(Composite parent) {
		parent.setLayout(new GridLayout(4, false));

		// Legal Name
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Legal Name");
		final Text legalNameTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		// Legal form
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Legal Form");
		final Text legalFormTxt = toolkit.createText(parent, "", SWT.BORDER);
		legalFormTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// VAT ID Number
		ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "VAT ID");
		final Text vatIDTxt = toolkit.createText(parent, "", SWT.BORDER);
		vatIDTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		notePart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				ConnectWorkbenchUtils.refreshFormTextWidget(editor, legalNameTxt, entity,
						PeopleNames.PEOPLE_LEGAL_NAME);
				ConnectWorkbenchUtils.refreshFormTextWidget(editor, legalFormTxt, entity,
						PeopleNames.PEOPLE_LEGAL_FORM);
				ConnectWorkbenchUtils.refreshFormTextWidget(editor, vatIDTxt, entity, PeopleNames.PEOPLE_VAT_ID_NB);
			}
		};

		// Listeners
		ConnectWorkbenchUtils.addModifyListener(legalFormTxt, entity, PeopleNames.PEOPLE_LEGAL_FORM, notePart);
		ConnectWorkbenchUtils.addModifyListener(vatIDTxt, entity, PeopleNames.PEOPLE_VAT_ID_NB, notePart);

		// Specific listeners to manage correctly display name
		legalNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 6068716814124883599L;

			@Override
			public void modifyText(ModifyEvent event) {
				// try {
				if (ConnectJcrUtils.setJcrProperty(entity, PeopleNames.PEOPLE_LEGAL_NAME, PropertyType.STRING,
						legalNameTxt.getText())) {
					// Boolean defineDistinct = ConnectJcrUtils.getBooleanValue(
					// entity,
					// PeopleNames.PEOPLE_USE_DISTINCT_DISPLAY_NAME);
					// if (defineDistinct == null || !defineDistinct)
					// entity.setProperty(Property.JCR_TITLE,
					// legalNameTxt.getText());
					notePart.markDirty();
				}
				// } catch (RepositoryException e) {
				// throw new PeopleException("Unable to update property", e);
				// }
			}
		});
		notePart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(notePart);
	}

	private void populateBankAccountGroup(final Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Payment account");
		group.setLayout(EclipseUiUtils.noSpaceGridLayout());

		formPart = new AbstractFormPart() {
			public void refresh() {
				// TODO Manage multiple bank account
				super.refresh();
				try {
					if (!entity.hasNode(PeopleNames.PEOPLE_PAYMENT_ACCOUNTS) && editor.isEditing()) {
						OrgJcrUtils.createPaymentAccount(entity, PeopleTypes.PEOPLE_BANK_ACCOUNT, "new");
						entity.getSession().save();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to create bank account for " + entity, e);
				}

				Control[] children = group.getChildren();
				for (Control child : children) {
					child.dispose();
				}

				NodeIterator ni = OrgJcrUtils.getPaymentAccounts(entity);
				while (ni != null && ni.hasNext()) {
					Composite cmp = new BankAccountComposite(group, 0, editor, ni.nextNode());
					cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				}
				parent.layout(true, true);
			}
		};
		formPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(formPart);
	}
}