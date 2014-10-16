package org.argeo.cms.viewers;

import java.util.List;

import org.argeo.cms.CmsUtils;
import org.argeo.cms.text.TextViewerEditor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
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
		tree.setData(CompositeItem.ITEM_DATA_KEY, new CompositeItem(tree,
				SWT.NONE));
		composite.setLayout(CmsUtils.noSpaceGridLayout());
		hookControl(tree);
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
	protected void createTreeItem(Widget parent, Object element, int index) {
		Item item = newItem(parent, SWT.NULL, index);
		addControls(((CompositeItem) item).getComposite(), element);
		updateItem(item, element);
		updatePlus(item, element);
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		CompositeItem.getItem(tree).setData(input);
	}

	/** Does nothing by default */
	protected void addControls(Composite parent, Object element) {

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
		CompositeItem item = findSelectedItem(tree, null);
		if (item != null)
			return new Item[] { findSelectedItem(tree, null) };
		else
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
	protected Item getItemAt(Point point) {
		return findItem(tree, point);
	}

	private Item findItem(Composite parent, Point point) {
		if (!parent.getBounds().contains(point))
			return null;
		CompositeItem lastItemSeen = null;
		for (Control child : parent.getChildren()) {
			if (child instanceof Composite && child.getBounds().contains(point)) {
				Object item = child.getData(CompositeItem.ITEM_DATA_KEY);
				if (item != null)
					lastItemSeen = (CompositeItem) item;
				item = findItem((Composite) child, point);
				if (item != null)
					lastItemSeen = (CompositeItem) item;
				break;// no need to look further
			}
		}
		return lastItemSeen;
	}

	private CompositeItem findSelectedItem(Composite parent, CompositeItem last) {
		CompositeItem lastItemSeen = last;
		for (Control child : parent.getChildren()) {
			Object item = child.getData(CompositeItem.ITEM_DATA_KEY);
			if (item != null)
				lastItemSeen = (CompositeItem) item;
			if (child.isFocusControl())
				return lastItemSeen;
			if (child instanceof Composite) {
				item = findSelectedItem((Composite) child, lastItemSeen);
				if (item != null) {
					lastItemSeen = (CompositeItem) item;
					return lastItemSeen;
				}
			}
		}
		return null;
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
		return null;
	}

	@Override
	public Control getControl() {
		return tree;
	}

}
