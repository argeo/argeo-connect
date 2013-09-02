package org.argeo.connect.people.ui.editors;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.utils.CheckoutSourceProvider;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
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
	private final static Log log = LogFactory
			.getLog(AbstractEntityEditor.class);

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

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
	private Image itemPicture = null;

	// Manage tab Folder
	// We rather use CTabFolder to enable further customization
	private CTabFolder folder;
	protected String CTAB_INSTANCE_ID = "CTabId";

	// Form and corresponding life cycle
	private MyManagedForm mForm;
	protected FormToolkit toolkit;
	private boolean isCheckedOutByMe = false;

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
				} else
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

			// Free the resources.
			if (itemPicture != null
					&& !itemPicture.equals(PeopleImages.NO_PICTURE))
				itemPicture.dispose();
			else
				log.debug("Undisposed image: " + itemPicture.toString());

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
			session.save();
			String path = entityNode.getPath();
			if (session.itemExists(path))
				vm.checkin(path);
			mForm.commit(true);
			setCheckOutState(false);
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
	protected void forceRefresh() {
		for (IFormPart part : mForm.getParts())
			part.refresh();
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// mainComposite = parent;
		mForm = new MyManagedForm(parent);
		toolkit = mForm.getToolkit();
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
	}

	protected void createHeaderPart(final Composite header) {
		header.setLayout(new GridLayout(2, false));

		// image linked to the current entity
		Label image = toolkit.createLabel(header, "", SWT.NO_FOCUS);
		image.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_ITEM_IMAGE);
		image.setBackground(header.getBackground());
		image.setImage(getPicture());

		// General information
		final Composite mainInfoComposite = toolkit.createComposite(header,
				SWT.NO_FOCUS);
		mainInfoComposite.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL));
		mainInfoComposite.setLayout(new GridLayout());

		// The buttons
		Composite buttonPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		populateButtonsComposite(buttonPanel);

		// Main info panel
		Composite switchingPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		populateMainInfoComposite(switchingPanel);
	}

	protected void populateButtonsComposite(final Composite parent) {
		parent.setLayout(new GridLayout());

		Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		buttons.setLayout(new GridLayout());
		Button switchBtn = toolkit.createButton(buttons, "Switch", SWT.PUSH
				| SWT.RIGHT);

		switchBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (entityNode.isCheckedOut()) {
						entityNode.checkin();
						forceRefresh();
					} else {
						entityNode.checkout();
						forceRefresh();
					}
				} catch (RepositoryException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
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
		innerPannel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		innerPannel.setLayout(new GridLayout(1, false));
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

	// Only to provide a basic content while developing
	// TODO remove this
	protected void createDummyItemContent(CTabItem item) {
		if (item.getControl() == null) {
			CTabFolder folder = item.getParent();
			Composite body = toolkit.createComposite(folder);
			body.setLayout(new GridLayout(1, false));
			toolkit.createLabel(body, "Add content here.");
			item.setControl(body);
		}
	}

	protected TableViewerColumn createTableViewerColumn(TableViewer parent,
			String name, int style, int width) {
		TableViewerColumn tvc = new TableViewerColumn(parent, style);
		final TableColumn column = tvc.getColumn();
		column.setText(name);
		column.setWidth(width);
		column.setResizable(true);
		return tvc;
	}

	protected void setSwitchingFormData(Composite composite) {
		FormData fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(fdLabel);
	}

	// * MANAGE CHECK IN & OUT - Enables management of editable fields. */
	/** Checks whether the current user can edit the node */
	@Override
	public boolean canBeCheckedOutByMe() {
		// FIXME add an error/warning message in the editor if the node has
		// already been checked out by someone else.
		if (isCheckedOutByMe)
			return false;
		else
			// TODO add a check depending on current user rights
			return true; // getMsmBackend().isUserInRole(MsmConstants.ROLE_CONSULTANT);
	}

	protected boolean isCheckedOutByMe() {
		return isCheckedOutByMe;
	}

	/** Manage check out state and corresponding refresh of the UI */
	protected void setCheckOutState(boolean checkedOut) {
		isCheckedOutByMe = checkedOut;
		forceRefresh();
		// update enable state of the check out button
		IWorkbench iw = PeopleUiPlugin.getDefault().getWorkbench();
		IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
		ISourceProviderService sourceProviderService = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		CheckoutSourceProvider csp = (CheckoutSourceProvider) sourceProviderService
				.getSourceProvider(CheckoutSourceProvider.CHECKOUT_STATE);
		csp.setIsCurrentItemCheckedOut(isCheckedOutByMe);
	}

	@Override
	public void checkoutItem() {
		try {
			boolean isCheckedOut = vm.isCheckedOut(entityNode.getPath());
			if (isCheckedOut) {
				// Do nothing, for the time being JCR implementation does not
				// care about who has checked out the node.
				if (log.isTraceEnabled())
					log.trace("Current node "
							+ " is already checked out. Nothing has been done.");
			} else
				vm.checkout(entityNode.getPath());
			setCheckOutState(true);
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while managing check out state", re);
		}
	}

	@Override
	public void cancelAndCheckInItem() {
		try {
			boolean isCheckedOut = vm.isCheckedOut(entityNode.getPath());
			if (isCheckedOut) {
				String path = entityNode.getPath();
				JcrUtils.discardUnderlyingSessionQuietly(entityNode);
				// if a newly created node has been discarded before the first
				// save,
				// corresponding node does not exists
				if (session.itemExists(path))
					vm.checkin(path);
			}
			setCheckOutState(false);
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
}