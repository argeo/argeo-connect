package org.argeo.connect.people.ui.editors.utils;

import org.argeo.connect.people.ui.utils.PeopleUiUtils;
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

/**
 * Extends the <code>AbstractEntityEditor</code> Form adding a
 * <code>CTabFolder</code> in the bottom part. Insures the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a body
 * to add tabs with further details.
 */
public abstract class AbstractEntityCTabEditor extends AbstractEntityEditor
		implements IVersionedItemEditor {
	// private final static Log log = LogFactory
	// .getLog(AbstractEntityEditor.class);

	/* DEPENDENCY INJECTION */
	// private PeopleUiService peopleUiService;

	/* CONSTANTS */
	protected final static int CTAB_COMP_STYLE = SWT.NO_FOCUS;

	// Manage tab Folder
	// We rather use CTabFolder to enable further customization
	private CTabFolder folder;
	protected String CTAB_INSTANCE_ID = "CTabId";

	/* CONTENT CREATION */
	protected void createMainLayout(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		header.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createHeaderPart(header);

		// Create a body that is a CTabFolder
		createBodyPart(parent);
	}

	/**
	 * Children class should not override this class or rather directly use the
	 * AbstractEntityEditor
	 */
	@Override
	protected final void createBodyPart(Composite parent) {
		// NO_FOCUS to solve our "tab browsing" issue
		folder = createCTabFolder(parent, SWT.NO_FOCUS);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateTabFolder(folder);
		folder.setSelection(0);
	}

	/** Overwrite to populate the CTabFolder */
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
		Composite innerPannel = toolkit
				.createComposite(tabFolder, SWT.V_SCROLL);
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

	/* EXPOSES TO CHILDREN CLASSES */
//	protected PeopleUiService getPeopleUiService() {
//		return peopleUiService;
//	}

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

	/** Generic entity editors are normally directly the node to remove */
	@Override
	protected Boolean deleteParentOnRemove() {
		return false;
	}

	/* DEPENDENCY INJECTION */
	// public void setPeopleUiService(PeopleUiService peopleUiService) {
	// this.peopleUiService = peopleUiService;
	// }
}