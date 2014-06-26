package org.argeo.connect.people.ui.editors.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.CancelAndCheckInItem;
import org.argeo.connect.people.ui.commands.CheckOutItem;
import org.argeo.connect.people.ui.commands.DeleteEntity;
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
import org.eclipse.swt.layout.GridLayout;
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
	private PeopleUiService peopleUiService;
	// private String openEntityEditorCmdId = OpenEntityEditor.ID;

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
		// The header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder(2);
		// So that the buttons are not too close to the right border of the
		// composite.
		gl.marginRight = 5;
		header.setLayout(gl);
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// header content
		Composite left = toolkit.createComposite(header, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateHeader(left);

		// header buttons
		Composite right = toolkit.createComposite(header, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.verticalIndent = 5;
		right.setLayoutData(gd);
		populateButtonsComposite(right);

		// the body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateBody(body);
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

	// protected String getOpenEditorCommandId() {
	// return openEntityEditorCmdId;
	// }

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateBody(Composite parent);;

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateHeader(Composite parent);;

	protected void populateButtonsComposite(final Composite buttons) {
		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setLayout(new RowLayout(SWT.VERTICAL));

		// Do not show the edit button if the user does not have sufficient
		// rights
		if (getPeopleService().getUserManagementService().isUserInRole(
				PeopleConstants.ROLE_MEMBER)) {
			Button editBtn = toolkit.createButton(roPanelCmp, "Edit", SWT.PUSH);
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
		}
		// Add a refresh button to enable forcing refresh when having some UI
		// glitches.
		Button refreshBtn = toolkit.createButton(roPanelCmp, "Refresh",
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
		editPanelCmp.setLayout(new RowLayout(SWT.VERTICAL));

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
								CommonsJcrUtils.getIdentifier(node));
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

	protected PeopleUiService getPeopleUiService() {
		return peopleUiService;
	}

	protected Session getSession() {
		return session;
	}

	protected Repository getRepository() {
		return repository;
	}

	/* UTILITES */
	protected Constraint localAnd(QueryObjectModelFactory factory,
			Constraint defaultC, Constraint newC) throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	/** Forces refresh of all form parts of the current editor */
	public void forceRefresh(Object object) {
		for (IFormPart part : mForm.getParts()) {
			// if (part instanceof AbstractFormPart)
			// ((AbstractFormPart) part).markStale();
			part.refresh();
		}
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

	// ////////////////////////////
	// IVersionedItemEditor methods

	/** Checks whether the current user can edit the node */
	@Override
	public boolean canBeCheckedOutByMe() {
		// TODO add an error/warning message in the editor if the node has
		// already been checked out by someone else.
		// TODO add a check depending on current user rights
		if (isCheckedOutByMe())
			return false;
		else
			return getPeopleService().getUserManagementService().isUserInRole(
					PeopleConstants.ROLE_MEMBER);
	}

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
		String path = null;
		try {
			if (node.hasProperty(PeopleNames.PEOPLE_IS_DRAFT)
					&& node.getProperty(PeopleNames.PEOPLE_IS_DRAFT)
							.getBoolean()) {
				path = node.getPath();
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
			throw new PeopleException("Unable to correctly remove newly "
					+ "created node at path " + path
					+ " The Jcr repository is probably corrupted", re);
		}

	}

	private final static DateFormat df = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	@Override
	public String getlastUpdateMessage() {
		Node currNode = getNode();
		StringBuilder builder = new StringBuilder();
		try {
			if (currNode.isNodeType(NodeType.MIX_TITLE)) {
				builder.append(
						CommonsJcrUtils.get(currNode, Property.JCR_TITLE))
						.append(" - ");
			}

			if (currNode.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				// .append("<i>")
				builder.append("Last updated on ");
				builder.append(df.format(currNode
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");
				builder.append(currNode.getProperty(
						Property.JCR_LAST_MODIFIED_BY).getString());
				builder.append(". "); // </i>
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	// ///////////////////////////////
	// // EDITOR life cycle management

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
				// FIXME necessary duplicate call to the commit
				// (used to check-in newly created versionnable tags)
				// Why is it not done automagicaly ?
				for (IFormPart part : mForm.getParts()) {
					part.commit(true);
				}
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

	// /**
	// * Exposes the id of the openEntityEditor command. By default it is
	// * {@code OpenEntityEditor.ID} but might be changed by injection
	// */
	// final protected String getOpenEntityEditorCmdId() {
	// return openEntityEditorCmdId;
	// }

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	// public void setOpenEntityEditorCmdId(String openEntityEditorCmdId) {
	// this.openEntityEditorCmdId = openEntityEditorCmdId;
	// }
}