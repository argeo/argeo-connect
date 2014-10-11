package org.argeo.cms.viewers;

import java.util.LinkedList;

import org.argeo.cms.CmsException;
import org.argeo.cms.widgets.ItemItem;
import org.argeo.cms.widgets.NodeSubTree;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class ItemViewerRow extends ViewerRow {
	private static final long serialVersionUID = 645599909114685149L;

	private ItemItem item;

	public ItemViewerRow(ItemItem item) {
		this.item = item;
	}

	@Override
	public Rectangle getBounds(int columnIndex) {
		return item.getBounds();
	}

	@Override
	public Rectangle getBounds() {
		return getBounds(0);
	}

	@Override
	public Control getControl() {
		return item.getParent();
	}

	@Override
	public TreePath getTreePath() {
		ItemItem tItem = item;
		LinkedList<Object> segments = new LinkedList<Object>();
		while (tItem != null) {
			Object segment = tItem.getData();
			if (segment == null)
				throw new CmsException("Data of " + tItem
						+ " cannot be null when generating tree path for "
						+ item);
			segments.addFirst(segment);
			tItem = tItem.getParentItem();
		}
		return new TreePath(segments.toArray());
	}

	@Override
	public Object clone() {
		return new ItemViewerRow(item);
	}

	@Override
	public Object getElement() {
		return item.getData();
	}

	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		if (direction == ViewerRow.ABOVE) {
			return getRowAbove(sameLevel);
		} else if (direction == ViewerRow.BELOW) {
			return getRowBelow(sameLevel);
		} else {
			throw new IllegalArgumentException(
					"Illegal value of direction argument."); //$NON-NLS-1$
		}
	}

	private ViewerRow getRowBelow(boolean sameLevel) {
		NodeSubTree tree = item.getParent();

		// This means we have top-level item
		if (item.getParentItem() == null) {
			if (sameLevel || !item.getExpanded()) {
				int index = tree.indexOf(item) + 1;

				if (index < tree.getItemCount()) {
					return new ItemViewerRow(tree.getItem(index));
				}
			} else if (item.getExpanded() && item.getItemCount() > 0) {
				return new ItemViewerRow(item.getItem(0));
			}
		} else {
			if (sameLevel || !item.getExpanded()) {
				ItemItem parentItem = item.getParentItem();

				int nextIndex = parentItem.indexOf(item) + 1;
				int totalIndex = parentItem.getItemCount();

				ItemItem itemAfter;

				// This would mean that it was the last item
				if (nextIndex == totalIndex) {
					itemAfter = findNextItem(parentItem);
				} else {
					itemAfter = parentItem.getItem(nextIndex);
				}

				if (itemAfter != null) {
					return new ItemViewerRow(itemAfter);
				}

			} else if (item.getExpanded() && item.getItemCount() > 0) {
				return new ItemViewerRow(item.getItem(0));
			}
		}

		return null;
	}

	private ViewerRow getRowAbove(boolean sameLevel) {
		NodeSubTree tree = item.getParent();

		// This means we have top-level item
		if (item.getParentItem() == null) {
			int index = tree.indexOf(item) - 1;
			ItemItem nextTopItem = null;

			if (index >= 0) {
				nextTopItem = tree.getItem(index);
			}

			if (nextTopItem != null) {
				if (sameLevel) {
					return new ItemViewerRow(nextTopItem);
				}

				return new ItemViewerRow(findLastVisibleItem(nextTopItem));
			}
		} else {
			ItemItem parentItem = item.getParentItem();
			int previousIndex = parentItem.indexOf(item) - 1;

			ItemItem itemBefore;
			if (previousIndex >= 0) {
				if (sameLevel) {
					itemBefore = parentItem.getItem(previousIndex);
				} else {
					itemBefore = findLastVisibleItem(parentItem
							.getItem(previousIndex));
				}
			} else {
				itemBefore = parentItem;
			}

			if (itemBefore != null) {
				return new ItemViewerRow(itemBefore);
			}
		}

		return null;
	}

	private ItemItem findLastVisibleItem(ItemItem parentItem) {
		ItemItem rv = parentItem;

		while (rv.getExpanded() && rv.getItemCount() > 0) {
			rv = rv.getItem(rv.getItemCount() - 1);
		}

		return rv;
	}

	private ItemItem findNextItem(ItemItem item) {
		ItemItem rv = null;
		NodeSubTree tree = item.getParent();
		ItemItem parentItem = item.getParentItem();

		int nextIndex;
		int totalItems;

		if (parentItem == null) {
			nextIndex = tree.indexOf(item) + 1;
			totalItems = tree.getItemCount();
		} else {
			nextIndex = parentItem.indexOf(item) + 1;
			totalItems = parentItem.getItemCount();
		}

		// This is once more the last item in the tree
		// Search on
		if (nextIndex == totalItems) {
			if (item.getParentItem() != null) {
				rv = findNextItem(item.getParentItem());
			}
		} else {
			if (parentItem == null) {
				rv = tree.getItem(nextIndex);
			} else {
				rv = parentItem.getItem(nextIndex);
			}
		}

		return rv;
	}

	@Override
	public Widget getItem() {
		return item;
	}

	public void setItem(ItemItem item) {
		this.item = item;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Image getImage(int columnIndex) {
		return null;
	}

	@Override
	public void setImage(int columnIndex, Image image) {
	}

	@Override
	public String getText(int columnIndex) {
		return null;
	}

	@Override
	public void setText(int columnIndex, String text) {
	}

	@Override
	public Color getBackground(int columnIndex) {
		return null;
	}

	@Override
	public void setBackground(int columnIndex, Color color) {
	}

	@Override
	public Color getForeground(int columnIndex) {
		return null;
	}

	@Override
	public void setForeground(int columnIndex, Color color) {
	}

	@Override
	public Font getFont(int columnIndex) {
		return null;
	}

	@Override
	public void setFont(int columnIndex, Font font) {
	}

}
