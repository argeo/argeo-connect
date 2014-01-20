package org.argeo.connect.people.ui.editors.utils;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.commands.CancelAndCheckInItem;
import org.argeo.connect.people.ui.commands.CheckOutItem;
import org.argeo.connect.people.ui.commands.DeleteEntity;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.utils.CheckoutSourceProvider;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.ui.utils.Refreshable;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Parent Abstract Form editor for all nodes that derive from people:base.
 * Insure the presence of a corresponding people services and manage a life
 * cycle of the JCR session that is bound to it. It provides no UI layout except
 * from the header with some buttons.
 */
public abstract class AbstractPeopleEditor extends EditorPart implements
		IVersionedItemEditor, Refreshable {

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	/* CONSTANTS */
	// length for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Repository repository;
	private Session session;

	// Business Objects
	private Node node;
	// Enable mangement of new entities
	private boolean isDraft = false;

	// Form and corresponding life cycle
	private IManagedForm mForm;
	protected FormToolkit toolkit;

	// The body composite for the current editor
	private Composite main;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			session = repository.login();
			EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			node = getSession().getNodeByIdentifier(sei.getUid());

			if (node.hasProperty(PeopleNames.PEOPLE_IS_DRAFT))
				isDraft = node.getProperty(PeopleNames.PEOPLE_IS_DRAFT)
						.getBoolean();

			// try to set a default part name
			updatePartName();

			// update tooltip
			String displayName = CommonsJcrUtils.get(node, Property.JCR_TITLE);
			if (CommonsJcrUtils.isEmptyString(displayName))
				displayName = "current item";
			setTitleToolTip("Display and edit information for " + displayName);
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		mForm = new MyManagedForm(parent, toolkit);
		createToolkits();
		main = form.getBody();
		createMainLayout(main);
		forceRefresh();
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// Internal main Layout
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		header.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		header.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		// // Right: buttons
		// Composite buttons = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		// GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		// gd.heightHint = 30;
		// buttons.setLayoutData(gd);
		populateButtonsComposite(header);

		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createBodyPart(body);
	}

	/**
	 * Overwrite to provide a specific part Name
	 */
	protected void updatePartName() {
		String name = CommonsJcrUtils.get(node, Property.JCR_TITLE);
		if (CommonsJcrUtils.checkNotEmptyString(name)) {
			if (name.length() > SHORT_NAME_LENGHT)
				name = name.substring(0, SHORT_NAME_LENGHT - 1) + "...";
			setPartName(name);
		} else if (isDraft)
			setPartName("New...");
	}

	/** Overwrite to create specific toolkits relevant for the current editor */
	protected void createToolkits() {
	}

	protected abstract Boolean deleteParentOnRemove();

	/**
	 * Overwrite to provide a plugin specific open editor command and thus be
	 * able to open plugin specific editors
	 */
	protected String getOpenEditorCommandId() {
		return OpenEntityEditor.ID;
	}

	/** Overwrite following methods to create a nice editor... */
	protected abstract void createBodyPart(Composite parent);;

	protected void populateButtonsComposite(final Composite buttons) {
		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// Add a level to right align the buttons
		final Composite roSubPanelCmp = toolkit.createComposite(roPanelCmp,
				SWT.NO_FOCUS | SWT.RIGHT);
		roSubPanelCmp.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true,
				false));
		roSubPanelCmp.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button editBtn = toolkit.createButton(roSubPanelCmp, "Edit", SWT.PUSH);
		editBtn.setLayoutData(new RowData(60, 20));
		editBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (canBeCheckedOutByMe())
					CommandUtils.callCommand(CheckOutItem.ID);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Add a refresh button to enable forcing refresh when having some UI
		// glitches.
		Button refreshBtn = toolkit.createButton(roSubPanelCmp, "Refresh",
				SWT.PUSH);
		refreshBtn.setLayoutData(new RowData(60, 20));
		refreshBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				forceRefresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(buttons,
				SWT.NONE);
		PeopleUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
		saveBtn.setLayoutData(new RowData(60, 20));
		saveBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (isCheckedOutByMe())
						if (node.getSession().hasPendingChanges())
							CommandUtils
									.callCommand(IWorkbenchCommandConstants.FILE_SAVE);
						else
							CommandUtils.callCommand(CancelAndCheckInItem.ID);
				} catch (RepositoryException re) {
					throw new PeopleException("Unable to save pending changes",
							re);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button cancelBtn = toolkit.createButton(editPanelCmp, "Cancel",
				SWT.PUSH);
		cancelBtn.setLayoutData(new RowData(60, 20));

		cancelBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isCheckedOutByMe())
					CommandUtils.callCommand(CancelAndCheckInItem.ID);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		if (showDeleteButton()) {
			Button deleteBtn = toolkit.createButton(editPanelCmp, "Delete",
					SWT.PUSH);
			deleteBtn.setLayoutData(new RowData(60, 20));
			deleteBtn.addSelectionListener(new SelectionListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isCheckedOutByMe()) {
						Map<String, String> params = new HashMap<String, String>();
						params.put(DeleteEntity.PARAM_TOREMOVE_JCR_ID,
								CommonsJcrUtils.getIdentifierQuietly(node));
						params.put(DeleteEntity.PARAM_REMOVE_ALSO_PARENT,
								deleteParentOnRemove().toString());
						CommandUtils.callCommand(DeleteEntity.ID, params);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		addEditButtons(editPanelCmp);

		editPanelCmp.layout();
		roPanelCmp.layout();

		AbstractFormPart editPart = new AbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				// super.refresh();
				if (isCheckedOutByMe())
					editPanelCmp.moveAbove(roPanelCmp);
				else
					editPanelCmp.moveBelow(roPanelCmp);
				roPanelCmp.getParent().layout();
			}
		};
		// editPart.refresh();
		editPart.initialize(mForm);
		mForm.addPart(editPart);
	}

	/** Overwrite this call back method to add buttons when in edit Mode */
	protected void addEditButtons(Composite parent) {
	}

	/** Overwrite to hide the delete button */
	protected boolean showDeleteButton() {
		return true;
	}

	/* EXPOSES TO CHILDREN CLASSES */
	/** Returns the entity Node that is bound to this editor */
	public Node getNode() {
		return node;
	}

	protected IManagedForm getManagedForm() {
		return mForm;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected Session getSession() {
		return session;
	}

	/* UTILITES */
	/** Forces refresh of all form parts of the current editor */
	public void forceRefresh(Object object) {
		for (IFormPart part : mForm.getParts())
			part.refresh();
		main.layout(true);
		mForm.reflow(true);
	}

	public void forceRefresh() {
		forceRefresh(null);
	}

	/**
	 * Overwrite to provide entity specific before save validation
	 */
	protected boolean canSave() {
		return true;
	}

	// ///////////////////////////////////////////
	// LIFE CYCLE (among other check out policies)

	public boolean isCheckedOutByMe() {
		return CommonsJcrUtils.isNodeCheckedOutByMe(node);
	}

	/** Manage check out state and corresponding refresh of the UI */
	protected void notifyCheckOutStateChange() {
		// update enable state of the check out button
		IWorkbench iw = PeopleUiPlugin.getDefault().getWorkbench();
		IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
		ISourceProviderService sourceProviderService = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		CheckoutSourceProvider csp = (CheckoutSourceProvider) sourceProviderService
				.getSourceProvider(CheckoutSourceProvider.CHECKOUT_STATE);
		csp.setIsCurrentItemCheckedOut(isCheckedOutByMe());
		forceRefresh();
	}

	@Override
	public void checkoutItem() {
		if (isCheckedOutByMe())
			// Do nothing
			;
		else
			CommonsJcrUtils.checkout(node);
		notifyCheckOutStateChange();
	}

	/** Checks whether the current user can edit the node */
	@Override
	public boolean canBeCheckedOutByMe() {
		// TODO add an error/warning message in the editor if the node has
		// already been checked out by someone else.
		// TODO add a check depending on current user rights
		if (isCheckedOutByMe())
			return false;
		else
			return true; // getMsmBackend().isUserInRole(MsmConstants.ROLE_CONSULTANT);
	}

	@Override
	public void saveAndCheckInItem() {
		if (!isCheckedOutByMe()) // Do nothing
			;
		else
			CommonsJcrUtils.saveAndCheckin(node);
		notifyCheckOutStateChange();
	}

	@Override
	public void cancelAndCheckInItem() {
		// TODO best effort to keep a clean repository
		try {
			if (node.hasProperty(PeopleNames.PEOPLE_IS_DRAFT)
					&& node.getProperty(PeopleNames.PEOPLE_IS_DRAFT)
							.getBoolean()) {
				String path = node.getPath();
				session.removeItem(path);
				session.save();
				node = null;
				// close current editor
				this.getSite().getWorkbenchWindow().getActivePage()
						.closeEditor(this, false);

			} else {
				CommonsJcrUtils.cancelAndCheckin(node);
				notifyCheckOutStateChange();
				firePropertyChange(PROP_DIRTY);
			}
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to corrctly remove newly created node. Repo is probably corrupted",
					re);
		}

	}

	@Override
	public void dispose() {
		try {
			if (node != null)
				CommonsJcrUtils.cancelAndCheckin(node);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		super.dispose();
	}

	@Override
	public void doSaveAs() {
		// unused compulsory method
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			if (canSave()) {
				saveAndCheckInItem();
				mForm.commit(true);
				updatePartName();
			}
		} catch (Exception e) {
			throw new PeopleException("Error while saving JCR node.", e);
		}
	}

	@Override
	public boolean isDirty() {
		try {
			return session.hasPendingChanges();
		} catch (Exception e) {
			throw new PeopleException("Error getting session status.", e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	/* local classes */
	private class MyManagedForm extends CompositeManagedForm {
		public MyManagedForm(Composite parent, FormToolkit toolkit) {
			super(parent, toolkit);
		}

		/** <code>super.dirtyStateChanged()</code> does nothing */
		public void dirtyStateChanged() {
			AbstractPeopleEditor.this.firePropertyChange(PROP_DIRTY);
		}

		/**
		 * Enable clean management of the "dirty" status display in this part
		 * table
		 */
		public void commit(boolean onSave) {
			super.commit(onSave);
			if (onSave) {
				AbstractPeopleEditor.this.firePropertyChange(PROP_DIRTY);
			}
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}