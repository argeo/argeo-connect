package org.argeo.connect.people.rap.editors.util;

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
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsEditable;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.ChangeEditingState;
import org.argeo.connect.people.rap.commands.DeleteEntity;
import org.argeo.connect.people.rap.util.EditionSourceProvider;
import org.argeo.connect.people.rap.util.Refreshable;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.workbench.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
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
		CmsEditable, IVersionedItemEditor, Refreshable {
	private final static Log log = LogFactory
			.getLog(AbstractPeopleEditor.class);

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;
	// There is *one session per editor*
	private Repository repository;
	private Session session;

	// New approach: we do not rely on the checkin status of the underlying node
	// which should always be in a checkout state.
	// We rather perform a checkPoint when save is explicitly called
	private Boolean isEditing = false;

	/* CONSTANTS */
	// length for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;

	private final static DateFormat df = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// Context
	private Node node;

	// UI Context
	private PeopleManagedForm mForm;
	private FormToolkit toolkit;
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
			// Set a default part name and tooltip
			updatePartName();
			updateToolTip();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		mForm = new PeopleManagedForm(parent, toolkit);
		mForm.setContainer(AbstractPeopleEditor.this);
		main = form.getBody();
		createMainLayout(main);
		forceRefresh();
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// Internal main Layout
		// Header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		GridLayout gl = PeopleUiUtils.noSpaceGridLayout(2);
		gl.marginRight = 5; // otherwise buttons are too close from right border
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

		// body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayoutData(EclipseUiUtils.fillAll());
		populateBody(body);
	}

	/** Overwrite to provide a specific part Name */
	protected void updatePartName() {
		String name = JcrUiUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(name)) {
			if (name.length() > SHORT_NAME_LENGHT)
				name = name.substring(0, SHORT_NAME_LENGHT - 1) + "...";
			setPartName(name);
		}
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		String displayName = JcrUiUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.isEmpty(displayName))
			displayName = "current item";
		sei.setTooltipText("Display and edit information for " + displayName);
		// Does not do what is expected (rather use the above workaround)
		// setTitleToolTip("Display and edit information for " +
		// displayName);
	}

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateBody(Composite parent);

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateHeader(Composite parent);

	protected void populateButtonsComposite(final Composite buttons) {
		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleRapUtils.setSwitchingFormData(roPanelCmp);
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
					if (canEdit()) {
						Map<String, String> params = new HashMap<String, String>();
						params.put(ChangeEditingState.PARAM_NEW_STATE,
								ChangeEditingState.EDITING);
						params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
								ChangeEditingState.PRIOR_ACTION_CHECKOUT);
						CommandUtils.callCommand(ChangeEditingState.ID, params);
					}
				}
			});
		}
		// Add a refresh button to enable forcing refresh when having some UI
		// glitches.
		Button refreshBtn = toolkit.createButton(roPanelCmp, "Refresh",
				SWT.PUSH);
		refreshBtn.setLayoutData(new RowData(60, 20));
		refreshBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				forceRefresh();
			}
		});

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(buttons,
				SWT.NONE);
		PeopleRapUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(new RowLayout(SWT.VERTICAL));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
		saveBtn.setLayoutData(new RowData(60, 20));
		saveBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (isEditing())
						if (node.getSession().hasPendingChanges())
							CommandUtils
									.callCommand(IWorkbenchCommandConstants.FILE_SAVE);
						else {
							// Nothing has changed we in fact call cancel
							Map<String, String> params = new HashMap<String, String>();
							params.put(ChangeEditingState.PARAM_NEW_STATE,
									ChangeEditingState.NOT_EDITING);
							params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
									ChangeEditingState.PRIOR_ACTION_CANCEL);
							CommandUtils.callCommand(ChangeEditingState.ID,
									params);
						}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to save pending changes on " + node, re);
				}
			}
		});

		Button cancelBtn = toolkit.createButton(editPanelCmp, "Cancel",
				SWT.PUSH);
		cancelBtn.setLayoutData(new RowData(60, 20));
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isEditing()) {
					Map<String, String> params = new HashMap<String, String>();
					params.put(ChangeEditingState.PARAM_NEW_STATE,
							ChangeEditingState.NOT_EDITING);
					params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
							ChangeEditingState.PRIOR_ACTION_CANCEL);
					CommandUtils.callCommand(ChangeEditingState.ID, params);
				}
			}
		});

		if (showDeleteButton()) {
			Button deleteBtn = toolkit.createButton(editPanelCmp, "Delete",
					SWT.PUSH);
			deleteBtn.setLayoutData(new RowData(60, 20));
			deleteBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					Map<String, String> params = new HashMap<String, String>();
					params.put(DeleteEntity.PARAM_TOREMOVE_JCR_ID,
							JcrUiUtils.getIdentifier(node));
					// params.put(DeleteEntity.PARAM_REMOVE_ALSO_PARENT,
					// deleteParentOnRemove().toString());
					CommandUtils.callCommand(DeleteEntity.ID, params);
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

	/* PUBLIC ABILITIES AND EXPOSED OBJECTS */
	/** Enables definition of a new "main" node for this editor */
	public void setNode(Node node) {
		this.node = node;
	}

	/** Returns the entity Node that is bound to this editor */
	public Node getNode() {
		return node;
	}

	public FormToolkit getFormToolkit() {
		return toolkit;
	}

	public PeopleManagedForm getManagedForm() {
		return mForm;
	}

	/* EXPOSES TO CHILDREN CLASSES */
	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected PeopleWorkbenchService getPeopleWorkbenchService() {
		return peopleWorkbenchService;
	}

	protected Session getSession() {
		return session;
	}

	protected Repository getRepository() {
		return repository;
	}

	/* UTILITES */
	/** Forces refresh of all form parts of the current editor */
	public void forceRefresh(Object object) {
		if (log.isTraceEnabled())
			log.trace("Starting Editor refresh");
		long start, tmpStart, tmpEnd;
		start = System.currentTimeMillis();
		for (IFormPart part : mForm.getParts()) {
			tmpStart = System.currentTimeMillis();
			part.refresh();
			tmpEnd = System.currentTimeMillis();

			if (log.isTraceEnabled())
				log.trace("FormPart " + part.getClass().getName()
						+ " refreshed in " + (tmpEnd - tmpStart) + " ms");
		}
		tmpStart = System.currentTimeMillis();
		main.layout(true);
		mForm.reflow(true);
		tmpEnd = System.currentTimeMillis();
		if (log.isTraceEnabled()) {
			log.trace("Layout and reflow in in " + (tmpEnd - tmpStart) + " ms");
			log.trace("Full refresh of " + this.getClass().getName() + " in "
					+ (tmpEnd - start) + " ms");
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

	// ////////////////////////////
	// IVersionedItemEditor methods

	/** Checks whether the current user can edit the node */

	/** Manage check out state and corresponding refresh of the UI */
	protected void notifyEditionStateChange() {
		// update enable state of the check out button
		ISourceProviderService sourceProviderService = (ISourceProviderService) this
				.getSite().getWorkbenchWindow()
				.getService(ISourceProviderService.class);
		EditionSourceProvider csp = (EditionSourceProvider) sourceProviderService
				.getSourceProvider(EditionSourceProvider.EDITING_STATE);
		csp.setCurrentItemEditingState(canEdit(), isEditing());
		firePropertyChange(PROP_DIRTY);
		forceRefresh();
	}

	// Enable life cycle management
	public Boolean isEditing() {
		return isEditing;
	}

	public Boolean canEdit() {
		try {
			AccessControlManager acm = session.getAccessControlManager();
			Privilege[] privs = { acm.privilegeFromName(Privilege.JCR_WRITE) };
			return acm.hasPrivileges(node.getPath(), privs);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to check privilege on " + node, e);
		}
	}

	public void startEditing() {
		if (!isEditing) {
			isEditing = true;
			notifyEditionStateChange();
		}
	}

	public void stopEditing() {
		if (isEditing) {
			isEditing = false;
			notifyEditionStateChange();
		}
	}

	public void saveAndStopEditing() {
		try {
			peopleService.saveEntity(node, true);
			mForm.commit(true);
			// TODO Why do we have to call the commit for each part.
			// (Problematic use case might be when trying to check-in newly
			// created versionable tags)
			// Why is it not done automagicaly ?
			for (IFormPart part : mForm.getParts()) {
				part.commit(true);
			}
			stopEditing();
			updatePartName();
		} catch (PeopleException pe) {
			MessageDialog.openError(this.getSite().getShell(),
					"Unable to save node " + node, pe.getMessage());
			if (log.isDebugEnabled()) {
				log.warn("Unable to save node " + node + " - "
						+ pe.getMessage());
				pe.printStackTrace();
			}
		}
	}

	public void cancelAndStopEditing() {
		JcrUtils.discardUnderlyingSessionQuietly(node);
		stopEditing();
	}

	@Override
	public String getlastUpdateMessage() {
		Node currNode = getNode();
		StringBuilder builder = new StringBuilder();
		try {
			if (currNode.isNodeType(NodeType.MIX_TITLE)) {
				builder.append(JcrUiUtils.get(currNode, Property.JCR_TITLE))
						.append(" - ");
			}

			if (currNode.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("Last updated on ");
				builder.append(df.format(currNode
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");

				String lstModByDn = currNode.getProperty(
						Property.JCR_LAST_MODIFIED_BY).getString();
				builder.append(peopleService.getUserAdminService()
						.getUserDisplayName(lstModByDn));
				builder.append(". ");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	/** Overwrite to hide the delete button */
	protected boolean showDeleteButton() {
		return canEdit();
	}

	/* EDITOR LIFE CYCLE MANAGEMENT */
	@Override
	public void dispose() {
		try {
			if (node != null)
				JcrUtils.discardUnderlyingSessionQuietly(node);
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
		if (canSave()) {
			saveAndStopEditing();
			notifyEditionStateChange();
			firePropertyChange(PROP_DIRTY);
		}
	}

	@Override
	public boolean isDirty() {
		try {
			boolean isDirty = session.hasPendingChanges();
			return isDirty;
		} catch (Exception e) {
			throw new PeopleException(
					"Error getting session status on " + node, e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	public class PeopleManagedForm extends CompositeManagedForm {
		public PeopleManagedForm(Composite parent, FormToolkit toolkit) {
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

		public AbstractPeopleEditor getEditor() {
			return (AbstractPeopleEditor) getContainer();
		}

	}

	/* DEPENDENCY INJECTION */

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}