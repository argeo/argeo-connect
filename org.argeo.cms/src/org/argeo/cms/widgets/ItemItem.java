package org.argeo.cms.widgets;

import org.argeo.cms.CmsException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ItemItem extends org.eclipse.swt.widgets.Item {
	private static final long serialVersionUID = 1963083581296980852L;

	final NodeSubTree parent;
	final String path;

	final ItemItem parentItem;
	ItemItem[] items;
	int itemCount;
	int depth;
	int index;
	boolean expanded;

	Composite composite;
	Composite childrenComposite;

	public ItemItem(NodeSubTree parent, int style, int index, String path) {
		this(parent, null, style, index, path, true);
	}

	public ItemItem(NodeSubTree parent, int style, String path) {
		this(parent, null, style, parent == null ? 0 : parent.getItemCount(),
				path, true);
	}

	public ItemItem(ItemItem parentItem, int style, int index, String path) {
		this(parentItem.parent, parentItem, style, index, path, true);
	}

	public ItemItem(ItemItem parentItem, int style, String path) {
		this(parentItem == null ? null : parentItem.parent, parentItem, style,
				parentItem == null ? 0 : parentItem.itemCount, path, true);
	}

	protected void checkPath(String parentPath, String itemPath) {
		if (!itemPath.startsWith(parentPath) || parentPath.equals(itemPath))
			throw new CmsException(parentPath + " is not an ancestor of "
					+ itemPath);
	}

	public Rectangle getBounds() {
		if (composite != null)
			return composite.getBounds();
		else
			throw new CmsException("Item is not realized");
		// TODO estimate bounds?
	}

	/*
	 * HACKED FROM TREE ITEM
	 */

	@Override
	public void dispose() {
		if (parentItem != null) {
			parentItem.destroyItem(this, index);
		} else {
			parent.destroyItem(this, index);
		}
		super.dispose();
	}

	ItemItem(NodeSubTree parent, ItemItem parentItem, int style, int index,
			String path, boolean create) {
		super(parent, style);
		if (parent == null)
			throw new CmsException("Parent control cannot be null");
		this.parent = parent;
		this.parentItem = parentItem;
		this.index = index;
		if (parentItem != null) {
			depth = parentItem.depth + 1;
			checkPath(parentItem.path, path);
		} else {
			checkPath(parent.getBasePath(), path);
		}
		this.path = path;

		setEmpty();
		if (create) {
			int numberOfItems;
			if (parentItem != null) {
				numberOfItems = parentItem.itemCount;
			} else {
				// If there is no parent item, get the next index of the tree
				numberOfItems = parent.getItemCount();
			}
			// check range
			if (index < 0 || index > numberOfItems) {
				SWT.error(SWT.ERROR_INVALID_RANGE);
			}
			if (parentItem != null) {
				parentItem.createItem(this, index);
			} else {
				parent.createItem(this, index);
			}
		}
	}

	private void createItem(ItemItem item, int index) {
		if (itemCount == items.length) {
			/*
			 * Grow the array faster when redraw is off or the table is not
			 * visible. When the table is painted, the items array is resized to
			 * be smaller to reduce memory usage.
			 */
			boolean small = /* drawCount == 0 && */isVisible();
			int length = small ? items.length + 4 : Math.max(4,
					items.length * 3 / 2);
			ItemItem[] newItems = new ItemItem[length];
			System.arraycopy(items, 0, newItems, 0, items.length);
			items = newItems;
		}
		System.arraycopy(items, index, items, index + 1, itemCount - index);
		items[index] = item;
		itemCount++;
		adjustItemIndices(index);

		// related UI
		if (composite != null) {
			item.composite = new Composite(
					childrenComposite != null ? childrenComposite : composite,
					SWT.NONE);
		}
	}

	private void destroyItem(ItemItem item, int index) {
		itemCount--;
		if (itemCount == 0) {
			setEmpty();
		} else {
			System.arraycopy(items, index + 1, items, index, itemCount - index);
			items[itemCount] = null;
		}
		adjustItemIndices(index);

		// related UI
		if (item.childrenComposite != null)
			item.childrenComposite.dispose();
		if (item.composite != null)
			item.composite.dispose();
	}

	private void adjustItemIndices(int start) {
		for (int i = start; i < itemCount; i++) {
			if (items[i] != null) {
				items[i].index = i;
			}
		}
	}

	private void setEmpty() {
		items = new ItemItem[4];
	}

	private boolean isVisible() {
		return getParentItem() == null || getParentItem().getExpanded();
	}

	public ItemItem getParentItem() {
		checkWidget();
		return parentItem;
	}

	public boolean getExpanded() {
		checkWidget();
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		checkWidget();
		if (this.expanded != expanded && (!expanded || itemCount > 0)) {
			this.expanded = expanded;
			// if( !expanded ) {
			// updateSelection();
			// }
			// markCached();
			// parent.invalidateFlatIndex();
			// parent.updateScrollBars();
			// parent.updateAllItems();
		}
	}

	public int indexOf(ItemItem item) {
		checkWidget();
		if (item == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		if (item.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return item.parentItem == this ? item.index : -1;
	}

	public ItemItem getItem(int index) {
		checkWidget();
		if (index < 0 || index >= itemCount) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
		return _getItem(index);
	}

	ItemItem _getItem(int index) {
		if (parent.isVirtual() && items[index] == null) {
			// FIXME path
			items[index] = new ItemItem(parent, this, SWT.NONE, index, null,
					false);
		}
		return items[index];
	}

	/*
	 * ACCESSORS
	 */

	public ItemItem[] getItems() {
		return items;
	}

	public String getPath() {
		return path;
	}

	public int getItemCount() {
		return itemCount;
	}

	public NodeSubTree getParent() {
		return parent;
	}

}
