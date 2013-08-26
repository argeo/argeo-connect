package org.argeo.connect.people.ui.editors;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;
import javax.swing.event.HyperlinkEvent;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;

/**
 * Parent Abstract Form editor for a given entity. Insure the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a tab
 * folder to add tabs with further details.
 */
public abstract class AbstractEntityEditor extends EditorPart {

	/* DEPENDENCY INJECTION */
	private PeopleService msmService;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Repository repository;
	private Session session;
	private VersionManager vm;

	// Business Objects
	private Node entityNode;

	// Manage tab Folder
	// We rather use CTabFolder to enable further customization
	private CTabFolder folder;
	protected String CTAB_INSTANCE_ID = "CTabId";

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
			NodeEditorInput sei = (NodeEditorInput) getEditorInput();
			vm = session.getWorkspace().getVersionManager();
			entityNode = getSession().getNodeByIdentifier(sei.getUid());
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
		// mForm.refresh();

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
		folder = createTabFolder(body, SWT.NO_FOCUS);

	}

	// utils to factorize some of the commons configurations to apply to all
	// tabs and folders.
	protected CTabFolder createTabFolder(Composite parent, int style) {
		CTabFolder tabFolder = new CTabFolder(parent, style);
		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL
				| GridData.GRAB_HORIZONTAL);
		gd.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gd);
		populateTabFolder(tabFolder);
		tabFolder.setSelection(0);
		return tabFolder;
	}

	protected CTabItem addTabToFolder(CTabFolder tabFolder, int style,
			String label, String id) {
		CTabItem item = new CTabItem(tabFolder, style);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		return item;
	}

	/* create or open the corresponding tab */
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

	protected abstract void createHeaderPart(Composite parent);

	protected abstract void populateTabFolder(CTabFolder tabFolder);

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

	/* JCR HELPERS */
	/**
	 * Centralizes management of updating property value. Among other to avoid
	 * infinite loop when the new value is the same as the ones that is already
	 * stored in JCR.
	 * 
	 * @return true if the value as changed
	 */
	protected boolean setJcrProperty(String propName, int propertyType,
			Object value) {
		try {
			switch (propertyType) {
			case PropertyType.STRING:
				if (getNode().hasProperty(propName)
						&& getNode().getProperty(propName).getString()
								.equals((String) value))
					// nothing changed yet
					return false;
				else {
					getNode().setProperty(propName, (String) value);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (getNode().hasProperty(propName)
						&& getNode().getProperty(propName).getBoolean() == (Boolean) value)
					// nothing changed yet
					return false;
				else {
					getNode().setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (getNode().hasProperty(propName)
						&& getNode().getProperty(propName).getDate()
								.equals((Calendar) value))
					// nothing changed yet
					return false;
				else {
					getNode().setProperty(propName, (Calendar) value);
					return true;
				}

			default:
				throw new ArgeoException("Unimplemented property save");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while setting property",
					re);
		}
	}

	protected boolean setJcrProperty(Node node, String propName,
			int propertyType, Object value) {
		try {
			// int propertyType = getPic().getProperty(propName).getType();
			switch (propertyType) {
			case PropertyType.STRING:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getString()
								.equals((String) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (String) value);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getBoolean() == (Boolean) value)
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getDate()
								.equals((Calendar) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Calendar) value);
					return true;
				}

			default:
				throw new ArgeoException("Unimplemented property save");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while setting property",
					re);
		}
	}

	protected void forceRefresh() {
		for (IFormPart part : mForm.getParts())
			part.refresh();
	}

	/* MANAGE TAB FOLDER */
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

	protected abstract class MyHyperlinkListener implements IHyperlinkListener {
		public void linkEntered(HyperlinkEvent e) {
		}

		public void linkExited(HyperlinkEvent e) {
		}

		/** Must be overriden **/
		public abstract void linkActivated(HyperlinkEvent e);
	}

	/* DEPENDENCY INJECTION */
	public void setMsmService(PeopleService peopleService) {
		this.peopleService = peopleService;
		repository = peopleService.getRepository();
	}
}