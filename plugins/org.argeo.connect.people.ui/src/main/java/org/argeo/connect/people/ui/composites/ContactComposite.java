package org.argeo.connect.people.ui.composites;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.ContactImages;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit contact information.
 * 
 */
public class ContactComposite extends Composite {
	// private final static Log log = LogFactory.getLog(ContactComposite.class);

	private static final long serialVersionUID = -789885142022513273L;

	protected final Node contactNode;
	protected final Node parentVersionableNode;
	protected final FormToolkit toolkit;
	protected final IManagedForm form;
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
		Composite buttCmp = toolkit.createComposite(parent);
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		buttCmp.setLayout(rl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp, contactNode);
		final Button primaryBtn = createPrimaryButton(buttCmp);
		final Button deleteBtn = createDeleteButton(buttCmp);

		Composite dataCmp = toolkit.createComposite(parent);
		dataCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
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

					// refresh buttons
					boolean isPrimary = contactNode.getProperty(
							PeopleNames.PEOPLE_IS_PRIMARY).getBoolean();
					if (isPrimary)
						primaryBtn.setImage(PeopleImages.PRIMARY_BTN);
					else
						primaryBtn.setImage(PeopleImages.PRIMARY_NOT_BTN);

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

		configureDeleteButton(deleteBtn, contactNode, parentVersionableNode);
		configurePrimaryButton(primaryBtn, contactNode, parentVersionableNode);

		formPart.refresh();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());

		// TODO rap specific refactor
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
		parent.setLayout(rl);

		final Text valueTxt = createAddressTxt(true, parent, "Value", 150);
		final Text labelTxt = createAddressTxt(true, parent, "", 120);

		AbstractFormPart sPart = new AbstractFormPart() {

			public void refresh() {
				super.refresh();
				if (CommonsJcrUtils.nodeStillExists(contactNode)) {
					boolean isCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parentVersionableNode);
					String label = CommonsJcrUtils.get(contactNode,
							PeopleNames.PEOPLE_CONTACT_LABEL);
					labelTxt.setText(label);
					labelTxt.setEnabled(isCheckedOut);
					labelTxt.setMessage(isCheckedOut ? "Label" : "");
					valueTxt.setText(CommonsJcrUtils.get(contactNode,
							PeopleNames.PEOPLE_CONTACT_VALUE));
					valueTxt.setEnabled(isCheckedOut);
					String nature = CommonsJcrUtils.get(contactNode,
							PeopleNames.PEOPLE_CONTACT_LABEL);
					String category = CommonsJcrUtils.get(contactNode,
							PeopleNames.PEOPLE_CONTACT_CATEGORY);
					String toolTip = nature + " " + category;
					if (CommonsJcrUtils.checkNotEmptyString(toolTip))
						valueTxt.setToolTipText(toolTip);
					Composite cmp2 = parent.getParent();
					cmp2.pack();
					cmp2.getParent().layout(true);
				}
			}
		};

		PeopleUiUtils.addTxtModifyListener(sPart, valueTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_VALUE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(sPart, labelTxt, contactNode,
				PeopleNames.PEOPLE_CONTACT_LABEL, PropertyType.STRING);
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

	private Button createCategoryButton(Composite parent, Node contactNode) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);

		try {
			String category = null;
			if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_CATEGORY))
				category = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_CATEGORY);
			String nature = null;
			if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_NATURE))
				nature = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_NATURE);

			String contactType = contactNode.getPrimaryNodeType().getName();
			String entityType = contactNode.getParent().getParent()
					.getPrimaryNodeType().getName();

			btn.setImage(ContactImages.getImage(entityType, contactType,
					nature, category));

			RowData rd = new RowData();
			rd.height = 16;
			rd.width = 16;
			btn.setLayoutData(rd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get image for contact");
		}
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private void configureDeleteButton(Button btn, final Node node,
			final Node parNode) { // , final AbstractFormPart
									// genericContactFormPart
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					node.remove();
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
				for (IFormPart part : form.getParts()) {
					((AbstractFormPart) part).markStale();
					part.refresh();
				}
			}
		});
	}

	private void configurePrimaryButton(Button btn, final Node node,
			final Node parNode) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					boolean wasPrimary = false;
					if (node.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							&& node.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
									.getBoolean())
						wasPrimary = true;
					PeopleJcrUtils.markAsPrimary(node, !wasPrimary);
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
					for (IFormPart part : form.getParts()) {
						((AbstractFormPart) part).markStale();
						part.refresh();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
			}
		});
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
