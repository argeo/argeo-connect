package org.argeo.cms.viewers;

import java.util.List;

import org.argeo.cms.CmsUtils;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

public class CompositeViewer extends AbstractTreeViewer {
	private static final long serialVersionUID = 4090208690157687961L;
	private final Composite tree;

	private CompositeViewerRow cachedRow;

	public CompositeViewer(Composite composite) {
		this.tree = composite;
		tree.setData(CompositeItem.ITEM_DATAKEY, new CompositeItem(tree,
				SWT.NONE));
		composite.setLayout(CmsUtils.noSpaceGridLayout());
	}

	@Override
	protected ViewerRow getViewerRowFromItem(Widget item) {
		if (cachedRow == null) {
			cachedRow = new CompositeViewerRow((CompositeItem) item);
		} else {
			cachedRow.setItem((CompositeItem) item);
		}

		return cachedRow;
	}

	protected Widget getColumnViewerOwner(int columnIndex) {
		if (columnIndex == 0)
			return tree;
		else
			return null;
	}

	@Override
	protected void addTreeListener(Control control, TreeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Item[] getChildren(Widget widget) {
		if (widget instanceof Composite)
			return CompositeItem.getItems((Composite) widget);
		return ((CompositeItem) widget).getItems();
	}

	@Override
	protected boolean getExpanded(Item item) {
		return ((CompositeItem) item).getExpanded();
	}

	@Override
	protected int getItemCount(Control control) {
		return CompositeItem.getItemCount(((Composite) control));
	}

	@Override
	protected int getItemCount(Item item) {
		return ((CompositeItem) item).getItemCount();
	}

	@Override
	protected Item[] getItems(Item item) {
		return ((CompositeItem) item).getItems();
	}

	@Override
	protected Item getParentItem(Item item) {
		return ((CompositeItem) item).getParentItem();
	}

	@Override
	protected Item[] getSelection(Control control) {
		return new Item[0];
	}

	@Override
	protected Item newItem(Widget parent, int style, int rowIndex) {
		if (parent instanceof CompositeItem) {
			if (rowIndex >= 0) {
				return new CompositeItem((CompositeItem) parent, style,
						rowIndex);
			}

			return new CompositeItem((CompositeItem) parent, style);
		} else {
			if (rowIndex >= 0) {
				return new CompositeItem(CompositeItem.getItem(tree), style,
						rowIndex);
			}
			return new CompositeItem(CompositeItem.getItem(tree), style);
		}
	}

	@Override
	protected void removeAll(Control control) {
		for (Control c : ((Composite) control).getChildren()) {
			c.dispose();
		}
	}

	@Override
	protected void setExpanded(Item item, boolean expand) {
		((CompositeItem) item).setExpanded(expand);
	}

	@Override
	protected void setSelection(List items) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void showItem(Item item) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ColumnViewerEditor createViewerEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control getControl() {
		return tree;
	}

}
