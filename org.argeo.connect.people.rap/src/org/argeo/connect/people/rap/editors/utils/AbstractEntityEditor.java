package org.argeo.connect.people.rap.editors.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.rap.PeopleImages;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.commands.CancelAndCheckInItem;
import org.argeo.connect.people.rap.commands.CheckOutItem;
import org.argeo.connect.people.rap.commands.DeleteEntity;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.utils.CheckoutSourceProvider;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Parent Abstract Form editor for a given entity. Insure the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a body
 * with some buttons and a filter.
 */
public abstract class AbstractEntityEditor extends EditorPart implements
		IVersionedItemEditor, Refreshable {
	// private final static Log log = LogFactory
	// .getLog(AbstractEntityEditor.class);

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private String openEntityEditorCmdId = OpenEntityEditor.ID;

	/* CONSTANTS */
	// length for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Repository repository;
	private Session session;

	// Business Objects
	private Node entity;
	// Enable mangement of new entities
	private boolean isDraft = false;
	// A corresponding picture that must be explicitly disposed
	protected Image itemPicture = null;

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
			entity = getSession().getNodeByIdentifier(sei.getUid());

//			if (entity.hasProperty(PeopleNames.PEOPLE_IS_DRAFT))
//				isDraft = entity.getProperty(PeopleNames.PEOPLE_IS_DRAFT)
//						.getBoolean();

			InputStream is = null;
			try {
				if (entity.hasNode(PeopleNames.PEOPLE_PICTURE)) {
					Node imageNode = entity.getNode(PeopleNames.PEOPLE_PICTURE)
							.getNode(Node.JCR_CONTENT);
					is = imageNode.getProperty(Property.JCR_DATA).getBinary()
							.getStream();
					itemPicture = new Image(this.getSite().getShell()
							.getDisplay(), is);
				} else
					// No default image
					// itemPicture = PeopleImages.NO_PICTURE;
					itemPicture = null;
			} catch (Exception e) {
			} finally {
				IOUtils.closeQuietly(is);
			}

			// try to set a default part name
			updatePartName();

			// update tooltip
			String displayName = CommonsJcrUtils.get(getEntity(),
					Property.JCR_TITLE);
			if (CommonsJcrUtils.isEmptyString(displayName))
				displayName = "current item";
			setTitleToolTip("Display and edit information for " + displayName);
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	/**
	 * Overwrite to provide a specific part Name
	 */
	protected void updatePartName() {
		String name = CommonsJcrUtils.get(entity, Property.JCR_TITLE);
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

	/**
	 * Exposes the id of the openEntityEditor command. By default it is
	 * {@code OpenEntityEditor.ID} but might be changed by injection
	 */
	final protected String getOpenEntityEditorCmdId() {
		return openEntityEditorCmdId;
	}

	/**
	 * Displays by default only the last update snippet. Overwrite to adapt to
	 * current object
	 */
	protected void populateMainInfoDetails(Composite parent) {
		parent.setLayout(PeopleRapUtils.noSpaceGridLayout());

		final Composite lastUpdateCmp = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		GridLayout gl = PeopleRapUtils.noSpaceGridLayout();
		gl.marginTop = 8;
		lastUpdateCmp.setLayout(gl);
		lastUpdateCmp.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true,
				true));
		final Label readOnlyInfoLbl = toolkit.createLabel(lastUpdateCmp, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(PeopleRapConstants.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider lastUpdateLP = new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				return PeopleRapSnippets.getLastUpdateSnippet((Node) element);
			}
		};

		AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// update display value
				String roText = lastUpdateLP.getText(entity);
				readOnlyInfoLbl.setText(roText);
			}
		};
		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);
	}

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateTitleComposite(Composite parent);;

	protected abstract void createBodyPart(Composite parent);;

	protected abstract Boolean deleteParentOnRemove();

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
		parent.setLayout(PeopleRapUtils.noSpaceGridLayout());
		// Internal main Layout
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		header.setLayout(PeopleRapUtils.noSpaceGridLayout());
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createHeaderPart(header);

		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayout(PeopleRapUtils.noSpaceGridLayout());
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createBodyPart(body);

	}

	protected void createHeaderPart(final Composite header) {
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		gl.marginWidth = 10;
		gl.marginBottom = 8;
		header.setLayout(gl);

		Label image = toolkit.createLabel(header, "", SWT.NO_FOCUS);
		image.setBackground(header.getBackground());
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		if (getPicture() != null) {
			image.setImage(getPicture());
			gd.horizontalIndent = 5;
			gd.verticalIndent = 5;
		} else {
			gd.widthHint = 10;
		}
		image.setLayoutData(gd);

		// test
		// int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		// Transfer[] tt = new Transfer[] { TextTransfer.getInstance(),
		// FileTransfer.getInstance(), ImageTransfer.getInstance(),
		// URLTransfer.getInstance() };
		// DropTarget target = new DropTarget(image, operations);
		// target.setTransfer(tt);
		// target.addDropListener(new ImageDropListener());

		// General information panel (Right of the image)
		Composite mainInfoComposite = toolkit.createComposite(header,
				SWT.NO_FOCUS);
		mainInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		createMainInfoPanel(mainInfoComposite);
	}

	// Implementation of the Drop Listener
	// private class ImageDropListener implements DropTargetListener {
	//
	// @Override
	// public void dragEnter(DropTargetEvent event) {
	// log.debug("drag enter ");
	// }
	//
	// @Override
	// public void dragLeave(DropTargetEvent event) {
	// log.debug("drag leave");
	// }
	//
	// @Override
	// public void dragOperationChanged(DropTargetEvent event) {
	// log.debug("drag OP change");
	// }
	//
	// @Override
	// public void dragOver(DropTargetEvent event) {
	// log.debug("drag Over");
	// }
	//
	// @Override
	// public void drop(DropTargetEvent event) {
	// log.debug("Dropped: " + event.data);
	// }
	//
	// @Override
	// public void dropAccept(DropTargetEvent event) {
	// log.debug("drop accept");
	//
	// }
	// }

	protected void createMainInfoPanel(final Composite parent) {
		parent.setLayout(PeopleRapUtils.noSpaceGridLayout());

		// First row: Title + Buttons.
		Composite firstRow = toolkit.createComposite(parent, SWT.NO_FOCUS);
		firstRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 5;
		gl.marginRight = 5;
		gl.marginTop = 5;
		firstRow.setLayout(gl);

		// left: title
		Composite title = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(title);

		// Right: buttons
		Composite buttons = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.heightHint = 30;
		buttons.setLayoutData(gd);
		populateButtonsComposite(buttons);

		// 2nd line: Main Info Details
		Composite details = toolkit.createComposite(parent, SWT.NO_FOCUS);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		populateMainInfoDetails(details);

		// parent.layout();
	}

	protected void populateButtonsComposite(final Composite buttons) {
		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleRapUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setLayout(PeopleRapUtils.noSpaceGridLayout());

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
		PeopleRapUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
		saveBtn.setLayoutData(new RowData(60, 20));
		saveBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (isCheckedOutByMe())
						if (entity.getSession().hasPendingChanges())
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
								CommonsJcrUtils.getIdentifier(entity));
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
	public Node getEntity() {
		return entity;
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

	protected Image getPicture() {
		return itemPicture;
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

	protected TableViewerColumn createTableViewerColumn(TableViewer parent,
			String name, int style, int width) {
		TableViewerColumn tvc = new TableViewerColumn(parent, style);
		final TableColumn column = tvc.getColumn();
		column.setText(name);
		column.setWidth(width);
		column.setResizable(true);
		return tvc;
	}

	// protected GridLayout gridLayoutNoBorder() {
	// return gridLayoutNoBorder(1);
	// }

	protected GridLayout gridLayoutNoBorder(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
	}

	// ///////////////////////////////////////////
	// LIFE CYCLE (among other check out policies)

	public boolean isCheckedOutByMe() {
		return CommonsJcrUtils.isNodeCheckedOutByMe(entity);
	}

	/** Manage check out state and corresponding refresh of the UI */
	protected void notifyCheckOutStateChange() {
		// update enable state of the check out button
		ISourceProviderService sourceProviderService = (ISourceProviderService) this
				.getSite().getWorkbenchWindow()
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
			CommonsJcrUtils.checkout(entity);
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
			return true;
	}

	@Override
	public void saveAndCheckInItem() {
		if (!isCheckedOutByMe()) // Do nothing
			;
		else
			CommonsJcrUtils.saveAndCheckin(entity);
		notifyCheckOutStateChange();
	}

	@Override
	public void cancelAndCheckInItem() {
		// TODO best effort to keep a clean repository
//		try {
//			if (entity.hasProperty(PeopleNames.PEOPLE_IS_DRAFT)
//					&& entity.getProperty(PeopleNames.PEOPLE_IS_DRAFT)
//							.getBoolean()) {
//				String path = entity.getPath();
//				session.removeItem(path);
//				session.save();
//				entity = null;
//				// close current editor
//				this.getSite().getWorkbenchWindow().getActivePage()
//						.closeEditor(this, false);
//
//			} else {
				CommonsJcrUtils.cancelAndCheckin(entity);
				notifyCheckOutStateChange();
				firePropertyChange(PROP_DIRTY);
		// }
		// } catch (RepositoryException re) {
		// throw new PeopleException(
		// "Unable to corrctly remove newly created node. Repo is probably corrupted",
		// re);
		// }

	}

	@Override
	public void dispose() {
		try {
			if (entity != null)
				CommonsJcrUtils.cancelAndCheckin(entity);

			// TODO clean default image management
			// Free the resources.
			if (itemPicture != null
					&& !itemPicture.equals(PeopleImages.NO_PICTURE))
				itemPicture.dispose();
		}

		finally {
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
			AbstractEntityEditor.this.firePropertyChange(PROP_DIRTY);
		}

		/**
		 * Enable clean management of the "dirty" status display in this part
		 * table
		 */
		public void commit(boolean onSave) {
			super.commit(onSave);
			if (onSave) {
				AbstractEntityEditor.this.firePropertyChange(PROP_DIRTY);
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

	public void setOpenEntityEditorCmdId(String openEntityEditorCmdId) {
		this.openEntityEditorCmdId = openEntityEditorCmdId;
	}
}