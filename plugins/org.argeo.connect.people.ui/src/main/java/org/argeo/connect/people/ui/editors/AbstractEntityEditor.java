package org.argeo.connect.people.ui.editors;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.apache.commons.io.IOUtils;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.commands.CancelAndCheckInItem;
import org.argeo.connect.people.ui.commands.CheckOutItem;
import org.argeo.connect.people.ui.utils.CheckoutSourceProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Parent Abstract Form editor for a given entity. Insure the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a
 * <code>CTabFolder</code> to add tabs with further details.
 */
public abstract class AbstractEntityEditor extends EditorPart implements
		IVersionedItemEditor {
	// private final static Log log = LogFactory
	// .getLog(AbstractEntityEditor.class);

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	/* CONSTANTS */
	// lenght for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Repository repository;
	private Session session;
	private VersionManager vm;

	// Business Objects
	private Node entityNode;
	// A corresponding picture that must be explicitly disposed
	protected Image itemPicture = null;

	// Manage tab Folder
	// We rather use CTabFolder to enable further customization
	private CTabFolder folder;
	protected String CTAB_INSTANCE_ID = "CTabId";

	// Form and corresponding life cycle
	private MyManagedForm mForm;
	protected FormToolkit toolkit;

	// private boolean isCheckedOutByMe = false;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			session = repository.login();
			EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			vm = session.getWorkspace().getVersionManager();
			entityNode = getSession().getNodeByIdentifier(sei.getUid());

			InputStream is = null;
			try {
				if (entityNode.hasNode(PeopleNames.PEOPLE_PICTURE)) {
					Node imageNode = entityNode.getNode(
							PeopleNames.PEOPLE_PICTURE).getNode(
							Node.JCR_CONTENT);
					is = imageNode.getProperty(Property.JCR_DATA).getBinary()
							.getStream();
					itemPicture = new Image(this.getSite().getShell()
							.getDisplay(), is);
				}
				// TODO repair this
				else
					itemPicture = PeopleImages.NO_PICTURE;
			} catch (Exception e) {
				// No image found. silent
			} finally {
				IOUtils.closeQuietly(is);
			}

		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	@Override
	public void dispose() {
		try {
			String path = entityNode.getPath();
			JcrUtils.discardUnderlyingSessionQuietly(entityNode);
			// if a newly created node has been discarded before the first save,
			// corresponding node does not exists
			if (session.itemExists(path))
				vm.checkin(path);

			// TODO clean default image management
			if (itemPicture != null
					&& !itemPicture.equals(PeopleImages.NO_PICTURE))
				itemPicture.dispose(); // Free the resources.

			// else
			// log.debug("Undisposed image: " + itemPicture.toString());

		} catch (RepositoryException e) {
			throw new ArgeoException("unexpected error "
					+ "while disposing the control", e);
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
			saveAndCheckInItem();
			mForm.commit(true);
			// notifyCheckOutStateChange();
			// setCheckOutState(false);
		} catch (Exception e) {
			throw new ArgeoException("Error while saving jcr node.", e);
		}

	}

	@Override
	public boolean isDirty() {
		try {
			return session.hasPendingChanges();
		} catch (Exception e) {
			throw new ArgeoException("Error while getting session status.", e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	// specific refresh
	public void forceRefresh() {
		for (IFormPart part : mForm.getParts())
			part.refresh();
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		mForm = new MyManagedForm(parent);
		toolkit = mForm.getToolkit();
		createToolkits();

		Composite body = mForm.getForm().getBody();
		body.setLayout(new GridLayout(1, false));
		// Main Layout
		Composite header = toolkit.createComposite(body, SWT.NO_FOCUS);
		header.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		header.setBackground(body.getBackground());
		createHeaderPart(header);
		// NO_FOCUS to solve our "tab browsing" issue
		folder = createCTabFolder(body, SWT.NO_FOCUS);
		populateTabFolder(folder);
		folder.setSelection(0);

		// We delegate the feed of all widgets with corresponding texts to the
		// refresh method of the various form parts.
		// We must then insure the refresh is done before display.
		forceRefresh();
		notifyCheckOutStateChange();
	}

	/** Override to create specific toolkits relevant for the current editor */
	protected void createToolkits() {
	}

	protected void createHeaderPart(final Composite header) {
		header.setLayout(new GridLayout(2, false));

		// image linked to the current entity
		Label image = toolkit.createLabel(header, "", SWT.NO_FOCUS);
		image.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_ITEM_IMAGE);
		image.setBackground(header.getBackground());
		if (getPicture() != null)
			image.setImage(getPicture());
		else {
			GridData gd = new GridData();
			gd.widthHint = 30;
			image.setLayoutData(gd);
		}

		// Text testFileDrop = new Text(header, SWT.NONE);
		//
		// // Add drag and drop support to organize items
		// int operations = DND.DROP_MOVE | DND.DROP_COPY;
		// Transfer[] tt = new Transfer[] { FileTransfer.getInstance() };
		// testFileDrop.addDropSupport(operations, tt, new ItemDropListener(
		// itemsViewer, sPart));
		//
		// sPart.refresh();
		// form.addPart(sPart);
		//

		// General information panel (on the right of the image)
		final Composite mainInfoComposite = toolkit.createComposite(header,
				SWT.NO_FOCUS);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;

		// gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		mainInfoComposite.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 0;
		mainInfoComposite.setLayout(gl);

		// The buttons
		Composite buttonPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.TOP;

		buttonPanel.setLayoutData(gd);
		populateButtonsComposite(buttonPanel);

		// Main info panel
		Composite switchingPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;

		populateMainInfoComposite(switchingPanel);
	}

	protected void populateButtonsComposite(final Composite parent) {
		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 0;
		parent.setLayout(gl);

		Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.grabExcessHorizontalSpace = true;
		gd.heightHint = 30;
		buttons.setLayoutData(gd);

		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		roPanelCmp.setLayout(new GridLayout());

		Button editBtn = toolkit.createButton(roPanelCmp, "Edit", SWT.PUSH);
		editBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (canBeCheckedOutByMe())
					// CommandUtils.callCommand(IWorkbenchCommandConstants.FILE_SAVE);
					CommandUtils.callCommand(CheckOutItem.ID);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 70;
		editBtn.setLayoutData(gd);

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(buttons,
				SWT.NONE);
		PeopleUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		editPanelCmp.setLayout(new GridLayout(2, false));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
		saveBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (isCheckedOutByMe()
							&& entityNode.getSession().hasPendingChanges())
						CommandUtils
								.callCommand(IWorkbenchCommandConstants.FILE_SAVE);
				} catch (RepositoryException re) {
					throw new PeopleException("Unable to save pending changes",
							re);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		gd = new GridData();
		gd.widthHint = 70;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.RIGHT;
		saveBtn.setLayoutData(gd);

		Button cancelBtn = toolkit.createButton(editPanelCmp, "Cancel",
				SWT.PUSH);
		gd = new GridData();
		gd.widthHint = 70;
		gd.horizontalAlignment = SWT.FILL;
		cancelBtn.setLayoutData(gd);
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

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
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
		getManagedForm().addPart(editPart);
	}

	/** Implement here a entity specific header */
	protected abstract void populateMainInfoComposite(final Composite parent);

	protected abstract void populateTabFolder(CTabFolder tabFolder);

	/* MANAGE TAB FOLDER */
	protected CTabFolder createCTabFolder(Composite parent, int style) {
		CTabFolder tabFolder = new CTabFolder(parent, style);
		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL
				| GridData.GRAB_HORIZONTAL);
		gd.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gd);
		return tabFolder;
	}

	protected Composite addTabToFolder(CTabFolder tabFolder, int style,
			String label, String id, String tooltip) {
		CTabItem item = new CTabItem(tabFolder, style);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		item.setToolTipText(tooltip);
		Composite innerPannel = toolkit.createComposite(tabFolder, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, true);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		innerPannel.setLayoutData(gd);
		// must set control
		item.setControl(innerPannel);
		return innerPannel;
	}

	protected CTabItem createCTab(CTabFolder tabFolder, String tabId) {
		CTabItem item = new CTabItem(tabFolder, SWT.NO_FOCUS);
		item.setData(CTAB_INSTANCE_ID, tabId);
		item.setText(tabId);
		Composite body = toolkit.createComposite(tabFolder);
		body.setLayout(new GridLayout(1, false));
		toolkit.createLabel(body, "Add content here.");
		item.setControl(body);
		return item;
	}

	/** create or open the corresponding tab */
	public void openTabItem(String id) {
		CTabItem[] items = folder.getItems();

		for (CTabItem item : items) {
			String currId = (String) item.getData(CTAB_INSTANCE_ID);
			if (currId != null && currId.equals(id)) {
				folder.setSelection(item);
				return;
			}
		}
		CTabItem item = createCTab(folder, id);
		folder.setSelection(item);
	}

	protected boolean checkControl(Control control) {
		return control != null && !control.isDisposed();
	}

	private class MyManagedForm extends ManagedForm {
		public MyManagedForm(Composite parent) {
			super(parent);
		}

		public void dirtyStateChanged() {
			AbstractEntityEditor.this.firePropertyChange(PROP_DIRTY);
		}

		public void commit(boolean onSave) {
			super.commit(onSave);
			if (onSave) {
				AbstractEntityEditor.this.firePropertyChange(PROP_DIRTY);
			}
		}
	}

	/* EXPOSES TO CHILDREN CLASSES */
	protected IManagedForm getManagedForm() {
		return mForm;
	}

	protected PeopleService getPeopleServices() {
		return peopleService;
	}

	protected PeopleUiService getPeopleUiServices() {
		return peopleUiService;
	}

	protected Session getSession() {
		return session;
	}

	protected Node getNode() {
		return entityNode;
	}

	protected Image getPicture() {
		return itemPicture;
	}

	/* UTILITES */
	protected TableViewerColumn createTableViewerColumn(TableViewer parent,
			String name, int style, int width) {
		TableViewerColumn tvc = new TableViewerColumn(parent, style);
		final TableColumn column = tvc.getColumn();
		column.setText(name);
		column.setWidth(width);
		column.setResizable(true);
		return tvc;
	}

	// * MANAGE CHECK IN & OUT - Enables management of editable fields. */
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

	public boolean isCheckedOutByMe() {
		return CommonsJcrUtils.isNodeCheckedOutByMe(entityNode);
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
			CommonsJcrUtils.checkout(entityNode);
		notifyCheckOutStateChange();
	}

	@Override
	public void saveAndCheckInItem() {
		if (!isCheckedOutByMe()) // Do nothing
			;
		else
			CommonsJcrUtils.saveAndCheckin(entityNode);
		notifyCheckOutStateChange();
	}

	@Override
	public void cancelAndCheckInItem() {
		try {
			boolean isCheckedOut = isCheckedOutByMe();
			if (isCheckedOut) {
				String path = entityNode.getPath();
				JcrUtils.discardUnderlyingSessionQuietly(entityNode);
				// if a newly created node has been discarded before the first
				// save, corresponding node does not exists
				if (session.itemExists(path))
					vm.checkin(path);
			}
			notifyCheckOutStateChange();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while managing check out state", re);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
		repository = peopleService.getRepository();
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}