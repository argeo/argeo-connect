package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ContactComposite extends Composite {
	private final static Log log = LogFactory.getLog(ContactComposite.class);

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

		parent.setLayout(new FormLayout());
		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		populateReadOnlyPanel(readOnlyPanel);

		// EDIT
		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		populateEditPanel(editPanel);

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// Workaround: form part list has already been retrieved when
				// the Contact composite is disposed
				if (editPanel.isDisposed())
					return;
				// Manage switch
				if (CommonsJcrUtils.isNodeCheckedOutByMe(parentVersionableNode)) {
					editPanel.setVisible(true);
					editPanel.moveAbove(readOnlyPanel);
				} else {
					editPanel.setVisible(false);
					editPanel.pack(true);
					editPanel.moveBelow(readOnlyPanel);
				}
			}
		};

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
				if (readOnlyInfoLbl.isDisposed())
					return;
				readOnlyInfoLbl.setText(PeopleHtmlUtils
						.getContactDisplaySnippet(contactNode,
								parentVersionableNode));
				readOnlyInfoLbl.pack(true);
				readOnlyPanel.pack(true);
			}
		};

		roFormPart.refresh();
		roFormPart.initialize(form);
		form.addPart(roFormPart);

		// Context menu kept here as a reminder
		// TODO dig this to see what can be done.
		MenuManager menuManager = new MenuManager();
		@SuppressWarnings("unused")
		Menu menu = menuManager.createContextMenu(readOnlyInfoLbl);
		menuManager.addMenuListener(new IMenuListener() {
			private static final long serialVersionUID = 1L;

			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		menuManager.setRemoveAllWhenShown(true);
		// Uncomment the following to activate the menu
		// readOnlyInfoLbl.setMenu(menu);
	}

	protected void populateEditPanel(Composite editPanel) {
		editPanel.setLayout(new GridLayout());
		editPanel.pack();
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

	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		// IWorkbenchWindow window = PeopleUiPlugin.getDefault().getWorkbench()
		// .getActiveWorkbenchWindow();

		// Effective Refresh
		// CommandUtils.refreshCommand(menuManager, window, CheckOutItem.ID,
		// "Test", null, true);

		// Test to be removed
		// If you use this pattern, do not forget to call
		// menuManager.setRemoveAllWhenShown(true);
		// when creating the menuManager

		menuManager.add(new Action("Copy value") {
			public void run() {
				log.debug("do something");
			}
		});
	}

}
