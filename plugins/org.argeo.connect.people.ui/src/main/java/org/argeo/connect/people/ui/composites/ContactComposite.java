package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit contact information. Must be
 * disposed and re-created when the corresponding form part refreshes
 * 
 */
public class ContactComposite extends Composite {
	// private final static Log log = LogFactory.getLog(ContactComposite.class);

	private static final long serialVersionUID = -789885142022513273L;

	private final Node contactNode;
	private final Node parentVersionableNode;
	private final FormToolkit toolkit;
	private final AbstractFormPart formPart;
	private final boolean isCheckedOut;

	public ContactComposite(Composite parent, int style, FormToolkit toolkit,
			AbstractFormPart formPart, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.contactNode = contactNode;
		this.parentVersionableNode = parentVersionableNode;
		this.toolkit = toolkit;
		this.formPart = formPart;
		this.isCheckedOut = CommonsJcrUtils
				.isNodeCheckedOutByMe(parentVersionableNode);
		populate();
	}

	private void populate() {
		// Initialization
		Composite parent = this;
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		// buttons
		Composite buttCmp = new ContactButtonsComposite(parent, SWT.NONE,
				toolkit, formPart, contactNode, parentVersionableNode);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		if (!isCheckedOut) // READ ONLY
			populateReadOnlyPanel(dataCmp);
		else
			populateEditPanel(dataCmp);

	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		// TODO RAP specific, refactor.
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		String addressHtml = PeopleHtmlUtils.getContactDisplaySnippet(
				contactNode, parentVersionableNode);
		readOnlyInfoLbl.setText(addressHtml);

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
		final Combo catCmb = hasCat ? new Combo(parent, SWT.READ_ONLY) : null;
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

		PeopleUiUtils.refreshFormTextWidget(labelTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
		PeopleUiUtils.refreshFormTextWidget(valueTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_VALUE, "Value");
		if (catCmb != null)
			PeopleUiUtils.refreshFormComboValue(catCmb, contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY);

		// Listeners
		PeopleUiUtils.addTxtModifyListener(formPart, valueTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_VALUE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(formPart, labelTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_LABEL, PropertyType.STRING);
		if (catCmb != null)
			PeopleUiUtils.addComboSelectionListener(formPart, catCmb,
					contactNode, PeopleNames.PEOPLE_CONTACT_CATEGORY,
					PropertyType.STRING);
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
