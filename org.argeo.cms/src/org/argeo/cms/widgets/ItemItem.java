package org.argeo.cms.widgets;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.eclipse.swt.SWT;

public class ItemItem extends org.eclipse.swt.widgets.Item {
	private static final long serialVersionUID = 1963083581296980852L;

	private final ScrolledNodeSubTree parent;
	private final String path;

	private final ItemItem parentItem;
	private ItemItem[] items;
	private int itemCount;
	private int depth;
	int index;
	private boolean expanded;

	public ItemItem(ScrolledNodeSubTree parent, int style, int index, Item item)
			throws RepositoryException {
		this(parent, null, style, index, item.getPath(), true);
	}

	public ItemItem(ScrolledNodeSubTree parent, int style, Item item)
			throws RepositoryException {
		this(parent, null, style, parent == null ? 0 : parent.getItemCount(),
				item.getPath(), true);
	}

	public ItemItem(ItemItem parentItem, int style, int index, Item item)
			throws RepositoryException {
		this(parentItem.parent, parentItem, style, index, item.getPath(), true);
	}

	public ItemItem(ItemItem parentItem, int style, Item item)
			throws RepositoryException {
		this(parentItem == null ? null : parentItem.parent, parentItem, style,
				parentItem == null ? 0 : parentItem.itemCount, item.getPath(),
				true);
	}

	protected void checkPath(String parentPath, String itemPath) {
		if (!itemPath.startsWith(parentPath) || parentPath.equals(itemPath))
			throw new CmsException(parentPath + " is not an ancestor of "
					+ itemPath);
	}

	/*
	 * HACKED FROM TREE ITEM
	 */

	ItemItem(ScrolledNodeSubTree parent, ItemItem parentItem, int style,
			int index, String path, boolean create) {
		super(parent, style);
		if(parent==null)
			throw new CmsException("Parnet control cannot be null");
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
}
