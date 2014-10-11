package org.argeo.cms.viewers;

import java.util.List;

import org.argeo.cms.widgets.ItemItem;
import org.argeo.cms.widgets.NodeSubTree;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

public class NodeSubTreeViewer extends AbstractTreeViewer {
	private static final long serialVersionUID = -2138340715024498378L;

	private NodeSubTree tree;

	/**
	 * The row object reused
	 */
	private ItemViewerRow cachedRow;

	public NodeSubTreeViewer(Composite parent, int style) {
		this(new NodeSubTree(parent, style));
	}

	public NodeSubTreeViewer(NodeSubTree tree) {
		this.tree = tree;
	}

	@Override
	protected void addTreeListener(Control control, TreeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Item[] getChildren(Widget o) {
		if (o instanceof ItemItem) {
			return ((ItemItem) o).getItems();
		}
		if (o instanceof Tree) {
			return ((NodeSubTree) o).getItems();
		}
		return null;
	}

	@Override
	protected boolean getExpanded(Item item) {
		return ((ItemItem) item).getExpanded();
	}

	protected int getItemCount(Control widget) {
		return ((NodeSubTree) widget).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected int getItemCount(Item item) {
		return ((ItemItem) item).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item[] getItems(Item item) {
		return ((ItemItem) item).getItems();
	}

	@Override
	protected Item getParentItem(Item item) {
		return ((ItemItem) item).getParentItem();
	}

	@Override
	protected Item[] getSelection(Control control) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeAll(Control control) {
		tree.removeAll();
	}

	@Override
	protected void setExpanded(Item item, boolean expand) {
		((ItemItem) item).setExpanded(expand);
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

	protected Item newItem(Widget parent, int flags, int ix) {
		ItemItem item;

		if (parent instanceof ItemItem) {
			item = (ItemItem) createNewRowPart(getViewerRowFromItem(parent),
					flags, ix).getItem();
		} else {
			item = (ItemItem) createNewRowPart(null, flags, ix).getItem();
		}

		return item;
	}

	private ViewerRow createNewRowPart(ViewerRow parent, int style, int rowIndex) {
		if (parent == null) {
			if (rowIndex >= 0) {
				return getViewerRowFromItem(new ItemItem(tree, style, rowIndex,
						null));
			}
			return getViewerRowFromItem(new ItemItem(tree, style, null));
		}

		if (rowIndex >= 0) {
			return getViewerRowFromItem(new ItemItem(
					(ItemItem) parent.getItem(), SWT.NONE, rowIndex, null));
		}

		return getViewerRowFromItem(new ItemItem((ItemItem) parent.getItem(),
				SWT.NONE, null));
	}

	protected ViewerRow getViewerRowFromItem(Widget item) {
		if (cachedRow == null) {
			cachedRow = new ItemViewerRow((ItemItem) item);
		} else {
			cachedRow.setItem((ItemItem) item);
		}

		return cachedRow;
	}

}
