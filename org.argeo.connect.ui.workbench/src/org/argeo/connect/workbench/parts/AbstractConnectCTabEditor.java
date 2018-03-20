package org.argeo.connect.workbench.parts;

import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Extends the <code>AbstractEntityEditor</code> Form adding a
 * <code>CTabFolder</code> in the bottom part. Insures the presence of a
 * corresponding people services and manage a life cycle of the JCR session that
 * is bound to it. It provides a header with some meta informations and a body
 * to add tabs with further details.
 */
public abstract class AbstractConnectCTabEditor extends AbstractConnectEditor {

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
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		folder = createCTabFolder(parent, SWT.NO_FOCUS);
		populateTabFolder(folder);
		folder.setSelection(0);
	}

	/* MANAGE TAB FOLDER */
	protected CTabFolder createCTabFolder(Composite parent, int style) {
		CTabFolder tabFolder = new CTabFolder(parent, style);
		tabFolder.setLayoutData(EclipseUiUtils.fillAll());
		return tabFolder;
	}

	@Override
	protected void addEditButtons(final Composite parent) {
//		if (ConnectJcrUtils.isNodeType(getNode(), NodeType.MIX_VERSIONABLE)) {
//			final Button showHistoryBtn = getFormToolkit().createButton(parent, "History", SWT.PUSH);
//			showHistoryBtn.setLayoutData(new RowData(60, 20));
//			showHistoryBtn.addSelectionListener(new SelectionAdapter() {
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					// History panel
//					String tooltip = "History of information about " + JcrUtils.get(getNode(), Property.JCR_TITLE);
//					HistoryLog historyLogCmp = new HistoryLog(folder, SWT.NO_FOCUS, AbstractConnectCTabEditor.this,
//							getUserAdminService(), getNode());
//					historyLogCmp.setLayoutData(EclipseUiUtils.fillAll());
//					addLazyTabToFolder(folder, historyLogCmp, "History", HistoryLog.CTAB_ID, tooltip);
//					if (!showHistoryBtn.isDisposed()) {
//						Composite par = showHistoryBtn.getParent();
//						showHistoryBtn.dispose();
//						par.layout(true, true);
//					}
//					openTabItem(HistoryLog.CTAB_ID);
//					historyLogCmp.refreshPartControl();
//				}
//			});
//		}
	}

	protected Composite addTabToFolder(CTabFolder tabFolder, int style, String label, String id, String tooltip) {
		CTabItem item = new CTabItem(tabFolder, style);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		item.setToolTipText(tooltip);
		Composite innerPannel = getManagedForm().getToolkit().createComposite(tabFolder, SWT.V_SCROLL);
		// must set control
		item.setControl(innerPannel);
		return innerPannel;
	}

	/**
	 * 
	 * @param tabFolder
	 * @param style
	 * @param label
	 * @param id
	 * @param tooltip
	 * @param afterTabId
	 *            the tab will be added after the tab that has this Id if such a
	 *            tab exists of at first place if null.
	 * @return
	 */
	protected Composite addTabToFolder(CTabFolder tabFolder, int style, String label, String id, String tooltip,
			String afterTabId) {
		// retrieve index of the existing tab
		CTabItem[] items = folder.getItems();
		int i = 0;
		if (afterTabId != null)
			loop: for (CTabItem item : items) {
				String currId = (String) item.getData(CTAB_INSTANCE_ID);
				i++;
				if (currId != null && currId.equals(afterTabId))
					break loop;
			}

		CTabItem item;
		if (i == items.length)
			item = new CTabItem(tabFolder, style);
		else
			item = new CTabItem(tabFolder, style, i);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		item.setToolTipText(tooltip);
		Composite innerPannel = getManagedForm().getToolkit().createComposite(tabFolder, SWT.V_SCROLL);
		// must set control
		item.setControl(innerPannel);
		return innerPannel;
	}

	protected void addLazyTabToFolder(CTabFolder tabFolder, LazyCTabControl contentCmp, String label, String id,
			String tooltip) {
		addLazyTabToFolder(tabFolder, contentCmp, label, id, tooltip, null);
	}

	protected void addLazyTabToFolder(CTabFolder tabFolder, LazyCTabControl contentCmp, String label, String id,
			String tooltip, String afterTabId) {
		// Retrieve index of the existing tab
		CTabItem[] items = folder.getItems();
		int i = 0;
		if (afterTabId != null)
			loop: for (CTabItem item : items) {
				String currId = (String) item.getData(CTAB_INSTANCE_ID);
				i++;
				if (currId != null && currId.equals(afterTabId))
					break loop;
			}
		else
			// put last
			i = items.length;

		CTabItem item;
		if (i == items.length)
			item = new CTabItem(tabFolder, SWT.NO_FOCUS);
		else
			item = new CTabItem(tabFolder, SWT.NO_FOCUS, i);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		item.setToolTipText(tooltip);
		// must set control
		item.setControl(contentCmp);
	}

	/** Opens the corresponding tab if it has been defined */
	public void openTabItem(String id) {
		CTabItem[] items = folder.getItems();
		for (CTabItem item : items) {
			String currId = (String) item.getData(CTAB_INSTANCE_ID);
			if (currId != null && currId.equals(id)) {
				folder.setSelection(item);
				return;
			}
		}
	}

	/** Retrieves a tab by ID if it has been defined */
	public CTabItem getTabItemById(String id) {
		CTabItem[] items = folder.getItems();
		for (CTabItem item : items) {
			String currId = (String) item.getData(CTAB_INSTANCE_ID);
			if (currId != null && currId.equals(id)) {
				return item;
			}
		}
		return null;
	}

	/* UTILITES */
	protected boolean checkControl(Control control) {
		return control != null && !control.isDisposed();
	}
}
