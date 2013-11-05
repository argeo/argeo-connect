package org.argeo.connect.people.ui.editors;

import java.io.InputStream;

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
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiPlugin;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Parent Abstract Form editor for a given entity. Insure the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a body
 * with some buttons and a filter.
 */
public abstract class AbstractEntityEditorNoCTab extends EditorPart implements
		IVersionedItemEditor {
	// private final static Log log = LogFactory
	// .getLog(AbstractEntityEditor.class);

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	/* CONSTANTS */
	// lenght for short strings (typically tab names)
	protected final static int SHORT_NAME_LENGHT = 10;
	protected final static int CTAB_COMP_STYLE = SWT.NO_FOCUS;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Repository repository;
	private Session session;

	// Business Objects
	private Node entity;
	// A corresponding picture that must be explicitly disposed
	protected Image itemPicture = null;

	// Form and corresponding life cycle
	private MyManagedForm mForm;
	protected FormToolkit toolkit;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			session = repository.login();
			EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			entity = getSession().getNodeByIdentifier(sei.getUid());

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
					itemPicture = PeopleImages.NO_PICTURE;
			} catch (Exception e) {
			} finally {
				IOUtils.closeQuietly(is);
			}

			// try to set a default part name for newly created entities
			String name = CommonsJcrUtils.get(entity, Property.JCR_TITLE);
			if (CommonsJcrUtils.checkNotEmptyString(name)) {
				if (name.length() > SHORT_NAME_LENGHT)
					name = name.substring(0, SHORT_NAME_LENGHT - 1) + "...";
				setPartName(name);
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	/** Override to create specific toolkits relevant for the current editor */
	protected void createToolkits() {
	}

	/** Overwrite following methods to create a nice editor... */
	protected abstract void populateMainInfoDetails(Composite parent);

	protected abstract void populateTitleComposite(Composite parent);;

	protected abstract void createBodyPart(Composite parent);;

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		mForm = new MyManagedForm(parent);
		toolkit = mForm.getToolkit();
		createToolkits();
		Composite main = mForm.getForm().getBody();
		GridLayout gl = gridLayoutNoBorder();
		main.setLayout(gl);

		// Internal main Layout
		Composite header = toolkit.createComposite(main, SWT.NO_FOCUS
				| SWT.NO_SCROLL);
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createHeaderPart(header);

		ScrolledForm bodySForm = toolkit.createScrolledForm(main);

		Composite body = bodySForm.getBody();
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createBodyPart(body);

		// We delegate the feed of all widgets with corresponding texts to the
		// refresh method of the various form parts.
		// We must then insure the refresh is done before display.
		notifyCheckOutStateChange();
		parent.layout();
	}

	protected void createHeaderPart(final Composite header) {
		header.setLayout(new GridLayout());
		toolkit.createLabel(header, "Le header ira là");

		// header.setLayout(new GridLayout(2, false));
		//
		// // We leave the place for an image
		// Label image = toolkit.createLabel(header, "", SWT.NO_FOCUS);
		// image.setData(RWT.CUSTOM_VARIANT,
		// PeopleUiConstants.PEOPLE_CSS_ITEM_IMAGE);
		// image.setBackground(header.getBackground());
		// // if (getPicture() != null)
		// // image.setImage(getPicture());
		// // else {
		// GridData gd = new GridData();
		// gd.widthHint = 30;
		// image.setLayoutData(gd);
		// // }
		//
		// // General information panel (Right of the image)
		// final Composite mainInfoComposite = toolkit.createComposite(header,
		// SWT.NO_FOCUS);
		// mainInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
		// true,
		// false));
		// createMainInfoPanel(mainInfoComposite);
	}

	protected void createMainInfoPanel(final Composite parent) {
		parent.setLayout(gridLayoutNoBorder());
		// First row: Title + Buttons.
		Composite firstRow = toolkit.createComposite(parent, SWT.NO_FOCUS);
		firstRow.setLayout(gridLayoutNoBorder());
		firstRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// left: title
		Composite title = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		title.setLayout(gridLayoutNoBorder());
		title.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		populateTitleComposite(title);

		// Right: buttons
		Composite buttons = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		populateButtonsComposite(buttons);

		// 2nd line: Main Info Details
		Composite details = toolkit.createComposite(parent, SWT.NO_FOCUS);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		populateMainInfoDetails(details);
	}

	protected void populateButtonsComposite(final Composite parent) {
		parent.setLayout(gridLayoutNoBorder());

		Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 30;
		buttons.setLayoutData(gd);

		buttons.setLayout(new FormLayout());

		// READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(buttons,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setLayout(gridLayoutNoBorder());
		Button editBtn = toolkit.createButton(roPanelCmp, "Edit", SWT.PUSH);
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
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 70;
		editBtn.setLayoutData(gd);

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(buttons,
				SWT.NONE);
		PeopleUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(new GridLayout(2, false));

		Button saveBtn = toolkit.createButton(editPanelCmp, "Save", SWT.PUSH);
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
		getManagedForm().addPart(editPart);
	}

	/** Implement here a entity specific header */
	// protected abstract void createMainInfoPanel(final Composite parent){
	// }

	/* EXPOSES TO CHILDREN CLASSES */
	/** Returns the entity Node that is bound to this editor */
	public Node getEntity() {
		return entity;
	}

	protected IManagedForm getManagedForm() {
		return mForm;
	}

	protected PeopleService getPeopleServices() {
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
	public void forceRefresh() {
		for (IFormPart part : mForm.getParts())
			part.refresh();
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

	protected GridLayout gridLayoutNoBorder() {
		GridLayout gl = new GridLayout(1, false);
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
			return true; // getMsmBackend().isUserInRole(MsmConstants.ROLE_CONSULTANT);
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
		CommonsJcrUtils.cancelAndCheckin(entity);
		notifyCheckOutStateChange();
	}

	@Override
	public void dispose() {
		try {
			CommonsJcrUtils.cancelAndCheckin(entity);
			// TODO clean default image management
			// Free the resources.
			if (itemPicture != null
					&& !itemPicture.equals(PeopleImages.NO_PICTURE))
				itemPicture.dispose();
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
	private class MyManagedForm extends ManagedForm {
		public MyManagedForm(Composite parent) {
			super(parent);
		}

		@Override
		public void dirtyStateChanged() {
			// AbstractEntityEditorNoCTab.this.firePropertyChange(PROP_DIRTY);
		}

		@Override
		public void commit(boolean onSave) {
			super.commit(onSave);
			// if (onSave) {
			// AbstractEntityEditorNoCTab.this.firePropertyChange(PROP_DIRTY);
			// }
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
		repository = peopleService.getRepository();
	}
}