package org.argeo.connect.people.ui.composites;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit contact information.
 * 
 */
public class ContactComposite extends Composite {
	// private final static Log log = LogFactory.getLog(ContactComposite.class);

	private static final long serialVersionUID = -789885142022513273L;

	private final Node contactNode;
	private final Node parentVersionableNode;
	private final FormToolkit toolkit;
	private final IManagedForm form;

	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;
	private AbstractFormPart roFormPart;
	private AbstractFormPart editFormPart;

	public ContactComposite(Composite parent, int style, FormToolkit toolkit,
			IManagedForm form, Node contactNode, Node parentVersionableNode) {
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

		// buttons
		Composite buttCmp = new ContactButtonsComposite(parent, SWT.NONE,
				toolkit, form, contactNode, parentVersionableNode);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

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

		// TODO RAP specific, refactor.
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		roFormPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (CommonsJcrUtils.nodeStillExists(contactNode)) {
					String addressHtml = PeopleHtmlUtils
							.getContactDisplaySnippet(contactNode,
									parentVersionableNode);
					readOnlyInfoLbl.setText(addressHtml);
					readOnlyInfoLbl.pack(true);
					readOnlyPanel.pack(true);
				}
			}
		};

		roFormPart.refresh();
		roFormPart.initialize(form);
		form.addPart(roFormPart);

		// Some tries to provide a copy/paste mechanism
		// TODO dig this to see what can be done.
		// Context menu kept here as a reminder

		// MenuManager menuManager = new MenuManager();
		// @SuppressWarnings("unused")
		// Menu menu = menuManager.createContextMenu(readOnlyInfoLbl);
		// menuManager.addMenuListener(new IMenuListener() {
		// private static final long serialVersionUID = 1L;
		//
		// public void menuAboutToShow(IMenuManager manager) {
		// contextMenuAboutToShow(manager);
		// }
		// });
		// menuManager.setRemoveAllWhenShown(true);
		// Uncomment the following to activate the menu
		// readOnlyInfoLbl.setMenu(menu);
	}

	protected void populateEditPanel(final Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		rl.marginWidth = 0;
		parent.setLayout(rl);

		boolean hasCat = !(CommonsJcrUtils.isNodeType(contactNode,
				PeopleTypes.PEOPLE_URL) || CommonsJcrUtils.isNodeType(
				contactNode, PeopleTypes.PEOPLE_EMAIL));

		// The widgets
		final Text valueTxt = createAddressTxt(true, parent, "Value", 150);
		final Combo catCmb = hasCat ? new Combo(parent, SWT.NONE) : null;
		final Text labelTxt = createAddressTxt(true, parent, "", 120);

		if (catCmb != null) {
			try {
				String nature = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_NATURE);
				catCmb.setItems(ContactValueCatalogs.getCategoryList(
						parentVersionableNode.getPrimaryNodeType().getName(),
						contactNode.getPrimaryNodeType().getName(), nature));
			} catch (RepositoryException e1) {
				throw new PeopleException(
						"unable to get initialise category list for contact",
						e1);
			}
			catCmb.select(0);
		}

		AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (CommonsJcrUtils.nodeStillExists(contactNode)) {

					PeopleUiUtils.refreshFormTextWidget(labelTxt, contactNode,
							PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
					PeopleUiUtils.refreshFormTextWidget(valueTxt, contactNode,
							PeopleNames.PEOPLE_CONTACT_VALUE, "Value");
					if (catCmb != null)
						PeopleUiUtils.refreshFormComboValue(catCmb,
								contactNode,
								PeopleNames.PEOPLE_CONTACT_CATEGORY);
					Composite cmp2 = parent.getParent();
					cmp2.pack();
					cmp2.getParent().layout(true);
				}
			}
		};

		// Listeners
		PeopleUiUtils.addTxtModifyListener(sPart, valueTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_VALUE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(sPart, labelTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_LABEL, PropertyType.STRING);
		if (catCmb != null)
			PeopleUiUtils.addComboSelectionListener(sPart, catCmb, contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY, PropertyType.STRING);

		sPart.refresh();
		sPart.initialize(form);
		form.addPart(sPart);
	}

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

	@Override
	public boolean setFocus() {
		return true;
	}

	protected void disposePart(AbstractFormPart part) {
		if (part != null) {
			form.removePart(part);
			part.dispose();
		}
	}

	@Override
	public void dispose() {
		disposePart(formPart);
		disposePart(editFormPart);
		disposePart(roFormPart);
		super.dispose();
	}

	// protected void contextMenuAboutToShow(IMenuManager menuManager) {
	// // IWorkbenchWindow window = PeopleUiPlugin.getDefault().getWorkbench()
	// // .getActiveWorkbenchWindow();
	//
	// // Effective Refresh
	// // CommandUtils.refreshCommand(menuManager, window, CheckOutItem.ID,
	// // "Test", null, true);
	//
	// // Test to be removed
	// // If you use this pattern, do not forget to call
	// // menuManager.setRemoveAllWhenShown(true);
	// // when creating the menuManager
	//
	// // menuManager.add(new Action("Copy value") {
	// // public void run() {
	// // log.debug("do something");
	// // }
	// // });
	// }

}
