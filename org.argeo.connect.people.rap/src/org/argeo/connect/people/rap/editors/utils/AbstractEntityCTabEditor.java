package org.argeo.connect.people.rap.editors.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Extends the <code>AbstractEntityEditor</code> Form adding a
 * <code>CTabFolder</code> in the bottom part. Insures the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a body
 * to add tabs with further details.
 */
public abstract class AbstractEntityCTabEditor extends AbstractPeopleWithImgEditor
		implements IVersionedItemEditor {
	// private final static Log log = LogFactory
	// .getLog(AbstractEntityEditor.class);

	/* CONSTANTS */
	protected final static int CTAB_COMP_STYLE = SWT.NO_FOCUS;

	// Manage tab Folder
	private CTabFolder folder;
	protected String CTAB_INSTANCE_ID = "CTabId";

	/** Overwrite to populate the CTabFolder */
	protected abstract void populateTabFolder(CTabFolder tabFolder);

	/**
	 * Children class must not override this class or rather directly use the
	 * AbstractEntityEditor
	 */
	@Override
	protected final void populateBody(Composite parent) {
		// NO_FOCUS to solve our "tab browsing" issue
		folder = createCTabFolder(parent, SWT.NO_FOCUS);
		populateTabFolder(folder);
		folder.setSelection(0);
	}

	/* MANAGE TAB FOLDER */
	protected CTabFolder createCTabFolder(Composite parent, int style) {
		CTabFolder tabFolder = new CTabFolder(parent, style);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gd.grabExcessVerticalSpace = true;
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
		// GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		// innerPannel.setLayoutData(gd);
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

	/* UTILITES */
	/**
	 * Generally, generic entity editors displays the *business* node. Yet
	 * sometimes parent node should also be removed on delete.
	 */
	@Override
	protected Boolean deleteParentOnRemove() {
		return false;
	}

	protected boolean checkControl(Control control) {
		return control != null && !control.isDisposed();
	}
}