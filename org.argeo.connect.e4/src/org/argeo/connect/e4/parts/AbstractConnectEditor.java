package org.argeo.connect.e4.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IFormPart;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.parts.CompositeManagedForm;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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

/**
 * Parent Abstract Form editor for all nodes that derive from people:base.
 * Insure the presence of a corresponding people services and manage a life
 * cycle of the JCR session that is bound to it. It provides no UI layout except
 * from the header with some buttons.
 */
public abstract class AbstractConnectEditor implements ConnectEditor {
	private final static Log log = LogFactory.getLog(AbstractConnectEditor.class);

	// private BundleContext bc =
	// FrameworkUtil.getBundle(AbstractConnectEditor.class).getBundleContext();

	// Services dependency injection
	@Inject
	private Repository repository;
	@Inject
	private UserAdminService userAdminService;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private SystemAppService systemAppService;

	private Session session;

	// UI dependency injection
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private MPart mPart;
	@Inject
	private MDirtyable mDirtyable;

	// private ResourcesService resourcesService;
	// private ActivitiesService activitiesService;
	// private PeopleService peopleService;

	// New approach: we do not rely on the checkin status of the underlying node
	// which should always be in a checkout state.
	// We rather perform a checkPoint when save is explicitly called
	private Boolean isEditing = false;

	/* CONSTANTS */
	// length for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;

	private final static DateFormat df = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

	// Context
	private Node node;

	// UI Context
	private ConnectManagedForm mForm;
	private FormToolkit toolkit;
	private Composite main;

	// public AbstractConnectEditor() {
	// try {
	// // TODO centralise in Activator?
	// Filter homeRepositoryFilter = bc
	// .createFilter("(&(" + Constants.OBJECTCLASS +
	// "=javax.jcr.Repository)(cn=home))");
	// ServiceTracker<Repository, Repository> repositoryST = new
	// ServiceTracker<>(bc, homeRepositoryFilter, null);
	// repositoryST.open();
	// repository = repositoryST.waitForService(60 * 1000);
	// repositoryST.close();
	//
	// userAdminService =
	// bc.getService(bc.getServiceReference(UserAdminService.class));
	// resourcesService =
	// bc.getService(bc.getServiceReference(ResourcesService.class));
	// systemAppService =
	// bc.getService(bc.getServiceReference(SystemAppService.class));
	// systemWorkbenchService =
	// bc.getService(bc.getServiceReference(SystemWorkbenchService.class));
	// } catch (Exception e) {
	// throw new ConnectException("Cannot retrieve services", e);
	// }
	// }

	// LIFE CYCLE
	public void init(String entityId) {
		// setSite(site);
		// setInput(input);
		try {
			session = repository.login();
			// String entityId=null;
			// EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			node = getSession().getNodeByIdentifier(entityId);
			// Set a default part name and tooltip
			updatePartName();
			updateToolTip();
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to create new session" + " to use with current editor", e);
		}
	}

	protected void init() {

	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		String entityId = mPart.getPersistedState().get("entityId");
		init(entityId);
		init();

		// mPart.setLabel(getPartName());

		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		// Form form = toolkit.createForm(parent);
		mForm = new ConnectManagedForm(parent, toolkit);
		mForm.setContainer(AbstractConnectEditor.this);
		// main = form.getBody();
		main = toolkit.createComposite(parent);
		createMainLayout(main);
		forceRefresh();
	}

	// protected String getPartName() {
	// Node node = getNode();
	// try {
	// if (node.isNodeType(NodeType.MIX_TITLE)) {
	// return node.getProperty(Property.JCR_TITLE).getString();
	// } else {
	// return node.getName();
	// }
	// } catch (RepositoryException e) {
	// throw new ConnectException("Cannot retrieve part name based on " + node, e);
	// }
	// }

	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// Internal main Layout
		// Header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS | SWT.NO_SCROLL | SWT.NO_TRIM);
		GridLayout gl = ConnectUiUtils.noSpaceGridLayout(2);
		gl.marginRight = 5; // otherwise buttons are too close from right border
		header.setLayout(gl);
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// header content
		Composite left = toolkit.createComposite(header, SWT.NO_FOCUS | SWT.NO_SCROLL | SWT.NO_TRIM);
		left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateHeader(left);

		// header buttons
		Composite right = toolkit.createComposite(header, SWT.NO_FOCUS | SWT.NO_SCROLL | SWT.NO_TRIM);
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.verticalIndent = 5;
		right.setLayoutData(gd);
		populateButtonsComposite(right);

		// body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayoutData(EclipseUiUtils.fillAll());
		populateBody(body);
	}

	/** Overwrite to provide a specific part Name */
	protected void updatePartName() {
		String name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(name)) {
			if (name.length() > SHORT_NAME_LENGHT)
				name = name.substring(0, SHORT_NAME_LENGHT - 1) + "...";
			setPartName(name);
		}
	}

	protected void setPartName(String name) {
		mPart.setLabel(name);
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		String displayName = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.isEmpty(displayName))
			displayName = "current item";
		// EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		// sei.setTooltipText("Display and edit information for " + displayName);
	}

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateBody(Composite parent);

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateHeader(Composite parent);

	protected void populateButtonsComposite(final Composite buttons) {
		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons, SWT.NO_FOCUS);
		ConnectUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setLayout(new RowLayout(SWT.VERTICAL));

		// Do not show the edit button if the user does not have sufficient
		// rights
		if (canEdit()) {
			Button editBtn = toolkit.createButton(roPanelCmp, "Edit", SWT.PUSH);
			editBtn.setLayoutData(new RowData(60, 20));
			editBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					// Superstition? we can only be here when we can edit.
					if (canEdit()) {
						// Map<String, String> params = new HashMap<String, String>();
						// params.put(ChangeEditingState.PARAM_NEW_STATE, ChangeEditingState.EDITING);
						// params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
						// ChangeEditingState.PRIOR_ACTION_CHECKOUT);
						// CommandUtils.callCommand(ChangeEditingState.ID, params);
						changeEditingState(PRIOR_ACTION_CHECKOUT, EDITING);
					}
				}
			});
		}
		// Add a refresh button to enable forcing refresh when having some UI
		// glitches.
		Button refreshBtn = toolkit.createButton(roPanelCmp, "Refresh", SWT.PUSH);
		refreshBtn.setLayoutData(new RowData(60, 20));
		refreshBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				forceRefresh();
			}
		});

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(buttons, SWT.NONE);
		ConnectUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(new RowLayout(SWT.VERTICAL));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
		saveBtn.setLayoutData(new RowData(60, 20));
		saveBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (isEditing())
						if (node.getSession().hasPendingChanges()) {
							// CommandUtils.callCommand(IWorkbenchCommandConstants.FILE_SAVE);
							// TODO use a command in order to centralise it
							doSave(null);
						} else {
							// Nothing has changed we in fact call cancel
							// Map<String, String> params = new HashMap<String, String>();
							// params.put(ChangeEditingState.PARAM_NEW_STATE,
							// ChangeEditingState.NOT_EDITING);
							// params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
							// ChangeEditingState.PRIOR_ACTION_CANCEL);
							// CommandUtils.callCommand(ChangeEditingState.ID, params);
							changeEditingState(PRIOR_ACTION_CANCEL, NOT_EDITING);
						}
				} catch (RepositoryException re) {
					throw new ConnectException("Unable to save pending changes on " + node, re);
				}
			}
		});

		Button cancelBtn = toolkit.createButton(editPanelCmp, "Cancel", SWT.PUSH);
		cancelBtn.setLayoutData(new RowData(60, 20));
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isEditing()) {
					// Map<String, String> params = new HashMap<String, String>();
					// params.put(ChangeEditingState.PARAM_NEW_STATE,
					// ChangeEditingState.NOT_EDITING);
					// params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
					// ChangeEditingState.PRIOR_ACTION_CANCEL);
					// CommandUtils.callCommand(ChangeEditingState.ID, params);
					changeEditingState(PRIOR_ACTION_CANCEL, NOT_EDITING);
				}
			}
		});

		if (showDeleteButton()) {
			Button deleteBtn = toolkit.createButton(editPanelCmp, "Delete", SWT.PUSH);
			deleteBtn.setLayoutData(new RowData(60, 20));
			deleteBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					// Map<String, String> params = new HashMap<String, String>();
					// params.put(DeleteEntity.PARAM_TOREMOVE_JCR_ID,
					// ConnectJcrUtils.getIdentifier(node));
					// CommandUtils.callCommand(DeleteEntity.ID, params);
				}
			});
		}

		// add other edition buttons
		addEditButtons(editPanelCmp);

		editPanelCmp.layout();
		roPanelCmp.layout();

		AbstractFormPart editPart = new AbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				if (isEditing())
					editPanelCmp.moveAbove(roPanelCmp);
				else
					editPanelCmp.moveBelow(roPanelCmp);
				roPanelCmp.getParent().layout();
			}
		};
		editPart.initialize(mForm);
		mForm.addPart(editPart);
	}

	/** Overwrite this call back method to add buttons when in edit Mode */
	protected void addEditButtons(Composite parent) {
	}

	final static String EDITING = "editing";
	final static String NOT_EDITING = "notEditing";
	final static String PRIOR_ACTION_SAVE = "save";
	final static String PRIOR_ACTION_CHECKOUT = "checkout";
	final static String PRIOR_ACTION_CANCEL = "cancel";

	protected void changeEditingState(String priorAction, String newState) {
		// prior action
		Node node = getNode();
		if (PRIOR_ACTION_SAVE.equals(priorAction))
			ConnectJcrUtils.saveAndPublish(node, true);
		else if (PRIOR_ACTION_CANCEL.equals(priorAction))
			JcrUtils.discardUnderlyingSessionQuietly(node);
		else if (PRIOR_ACTION_CHECKOUT.equals(priorAction)) {
			if (!ConnectJcrUtils.checkCOStatusBeforeUpdate(node))
				log.warn("Referencing node " + node + " was checked in when we wanted to update");
		}
		// new State
		if (EDITING.equals(newState))
			startEditing();
		else if (NOT_EDITING.equals(newState))
			stopEditing();

	}

	/* PUBLIC ABILITIES AND EXPOSED OBJECTS */
	/** Enables definition of a new "main" node for this editor */
	public void setNode(Node node) {
		this.node = node;
	}

	public FormToolkit getFormToolkit() {
		return toolkit;
	}

	public ConnectManagedForm getManagedForm() {
		return mForm;
	}

	/** Overwrite to hide the delete button */
	protected boolean showDeleteButton() {
		return canEdit();
	}

	@Persist
	public void doSave(@Optional IProgressMonitor monitor) {
		if (canSave())
			mForm.commit(true);
		stopEditing();
	}

	/**
	 * Overwrite to provide specific behaviour on save.
	 * 
	 * Introduced as a consequence of the use of a specific ManagedForm
	 */
	protected void commitInternalLinkedForm(boolean onSave) {
		// Enable specific pre-save processing in each form part before the
		// session.save();
		for (IFormPart part : mForm.getParts()) {
			if (part.isDirty())
				part.commit(onSave);
		}
		if (onSave) {
			systemAppService.saveEntity(node, true);
			updatePartName();
		}
	}

	public void startEditing() {
		if (!isEditing) {
			isEditing = true;
			markAllStale();
			// Must force the refresh: the stale mechanism is broken due to the
			// use of our twicked form.
			forceRefresh();
			notifyEditionStateChange();
			main.layout(true, true);
		}
	}

	public void stopEditing() {
		if (isEditing) {
			isEditing = false;
			markAllStale();
			forceRefresh();
			notifyEditionStateChange();
			main.layout(true, true);
		}
	}

	protected void markAllStale() {
		if (mForm != null)
			for (IFormPart part : mForm.getParts())
				if (part instanceof AbstractFormPart)
					((AbstractFormPart) part).markStale();
	}

	/**
	 * Notify the workbench that editable status as changed in order to update
	 * external extension (typically the toolbar buttons)
	 */
	protected void notifyEditionStateChange() {
		mDirtyable.setDirty(isEditing);
		// ISourceProviderService sourceProviderService = (ISourceProviderService)
		// this.getSite().getWorkbenchWindow()
		// .getService(ISourceProviderService.class);
		// EditionSourceProvider csp = (EditionSourceProvider) sourceProviderService
		// .getSourceProvider(EditionSourceProvider.EDITING_STATE);
		// csp.setCurrentItemEditingState(canEdit(), isEditing());
		// firePropertyChange(PROP_DIRTY);
	}

	public Boolean isEditing() {
		return isEditing;
	}

	public Boolean canEdit() {
		try {
			AccessControlManager acm = session.getAccessControlManager();
			Privilege[] privs = { acm.privilegeFromName(Privilege.JCR_WRITE) };
			return acm.hasPrivileges(node.getPath(), privs);
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to check privilege on " + node, e);
		}
	}

	/** Forces refresh of all form parts of the current editor */
	public void forceRefresh(Object object) {
		if (log.isTraceEnabled())
			log.trace("Starting Editor refresh");
		long start, lap, lapEnd, end;
		start = System.currentTimeMillis();
		for (IFormPart part : mForm.getParts()) {
			lap = System.currentTimeMillis();
			part.refresh();
			lapEnd = System.currentTimeMillis();
			if (log.isTraceEnabled())
				log.trace("FormPart " + part.getClass().getName() + " refreshed in " + (lapEnd - lap) + " ms");
		}
		lap = System.currentTimeMillis();
		mForm.reflow(true);
		end = System.currentTimeMillis();
		if (log.isTraceEnabled()) {
			log.trace("Reflow done in " + (end - lap) + " ms");
			log.trace("Full refresh of " + this.getClass().getName() + " in " + (end - start) + " ms");
		}
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

	public String getStatusLineMessage() {
		return getLastModifiedMessage();
	}

	public String getLastModifiedMessage() {
		Node currNode = getNode();
		StringBuilder builder = new StringBuilder();
		try {
			if (currNode.isNodeType(NodeType.MIX_TITLE)) {
				builder.append(ConnectJcrUtils.get(currNode, Property.JCR_TITLE)).append(" - ");
			}
			if (currNode.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("Last updated on ");
				builder.append(df.format(currNode.getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime()));
				builder.append(", by ");
				String lstModByDn = currNode.getProperty(Property.JCR_LAST_MODIFIED_BY).getString();
				builder.append(userAdminService.getUserDisplayName(lstModByDn));
				builder.append(". ");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to create last " + "modified message for " + currNode, re);
		}
	}

	public boolean isDirty() {
		try {
			boolean isDirty = session.hasPendingChanges();
			return isDirty;
		} catch (Exception e) {
			throw new ConnectException("Error getting session status on " + node, e);
		}
	}

	public void dispose() {
		try {
			if (node != null)
				JcrUtils.discardUnderlyingSessionQuietly(node);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		// super.dispose();
	}

	@Focus
	public void setFocus() {
	}

	public void doSaveAs() {
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public class ConnectManagedForm extends CompositeManagedForm {
		public ConnectManagedForm(Composite parent, FormToolkit toolkit) {
			super(parent, toolkit);
		}

		/** <code>super.dirtyStateChanged()</code> does nothing */
		public void dirtyStateChanged() {
			// AbstractConnectEditor.this.firePropertyChange(PROP_DIRTY);
		}

		/**
		 * Enable clean management of the "dirty" status display in this part table
		 */
		public void commit(boolean onSave) {
			commitInternalLinkedForm(onSave);
		}

		public AbstractConnectEditor getEditor() {
			return (AbstractConnectEditor) getContainer();
		}
	}

	/* EXPOSES TO CHILDREN CLASSES */
	protected Session getSession() {
		return session;
	}

	protected Repository getRepository() {
		return repository;
	}

	/** Returns the entity Node that is bound to this editor */
	public Node getNode() {
		return node;
	}

	protected UserAdminService getUserAdminService() {
		return userAdminService;
	}

	protected ResourcesService getResourcesService() {
		return resourcesService;
	}

	protected SystemAppService getSystemAppService() {
		return systemAppService;
	}

	protected SystemWorkbenchService getSystemWorkbenchService() {
		return systemWorkbenchService;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		log.trace("setRepository is deprecated and ignored");
		// this.repository = repository;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		log.trace("setUserAdminService is deprecated and ignored");
		// this.userAdminService = userAdminService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		log.trace("setResourcesService is deprecated and ignored");
		// this.resourcesService = resourcesService;
	}

	public void setSystemAppService(SystemAppService systemAppService) {
		log.trace("setSystemAppService is deprecated and ignored");
		// this.systemAppService = systemAppService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		log.trace("setSystemWorkbenchService is deprecated and ignored");
		// this.systemWorkbenchService = systemWorkbenchService;
	}
}
