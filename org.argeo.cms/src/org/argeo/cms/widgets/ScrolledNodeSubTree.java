package org.argeo.cms.widgets;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ScrolledNodeSubTree extends ScrolledPage {
	private static final long serialVersionUID = 4566882270649377366L;

	private String basePath = null;

	private int itemCount;
	private ItemItem[] items;

	public ScrolledNodeSubTree(Composite parent, int style) {
		super(parent, style);
	}

	public String getBasePath() {
		return basePath.toString();
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
		setData(Property.JCR_PATH, basePath);
	}

	/**
	 * Returns the composite related to this item in the composite tree, or null
	 * if not found.
	 */
	public Composite find(Item item) {
		try {
			if (basePath == null)
				throw new CmsException("Base path must be set");
			Node baseNode = item.getSession().getNode(basePath);
			if (!item.getAncestor(baseNode.getDepth()).getPath()
					.equals(baseNode.getPath()))
				throw new CmsException(item + " is not a descendant of "
						+ baseNode);

			Composite currComposite = this;
			for (int i = baseNode.getDepth() + 1; i <= item.getDepth(); i++) {
				currComposite = findChild(currComposite, item.getAncestor(i)
						.getPath());
				// not found if one of the parent is not found
				if (currComposite == null)
					return null;
			}
			return currComposite;
		} catch (RepositoryException e) {
			throw new CmsException("Cannot find composite for " + item, e);
		}
	}

	protected Composite findChild(Composite composite, String path) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Composite) {
				String childPath = control.getData(Property.JCR_PATH)
						.toString();
				if (childPath == null) {
					// if not a registered composite, scan Composite subtree,
					// it allows to deal with intermediary composites used for
					// layout purpose
					Composite subTreeScanResult = findChild(
							(Composite) control, path);
					if (subTreeScanResult != null)
						return subTreeScanResult;
				} else {
					if (childPath.equals(path))
						return (Composite) control;
				}
			}
		}
		return null;
	}

	/*
	 * HACKED FROM SWT TREE
	 */
	void createItem(ItemItem item, int index) {
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

	void destroyItem(ItemItem treeItem, int index) {
		itemCount--;
		if (itemCount == 0) {
			setTreeEmpty();
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

	private void setTreeEmpty() {
		items = new ItemItem[4];
	}

	public int getItemCount() {
		checkWidget();
		return itemCount;
	}

}
