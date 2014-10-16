package org.argeo.cms.viewers;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

public class CompositeItem extends Item {
	private static final long serialVersionUID = -3339865943635985288L;
	public final static String ITEM_DATA_KEY = "item";

	private CompositeItem parentItem;
	private boolean expanded = true;

	private final Composite parent;
	private Composite composite;
	private int index;

	private boolean hasChildren = false;

	public CompositeItem(Composite parent, int style) {
		this(parent, null, style);
	}

	public CompositeItem(CompositeItem parentItem, int style) {
		this(parentItem != null ? parentItem.parent : null, parentItem, style);
	}

	public CompositeItem(Composite parent, int style, int index) {
		this(parent, null, style);
		moveToIndex(index);
	}

	public CompositeItem(CompositeItem parentItem, int style, int index) {
		this(parentItem != null ? parentItem.parent : null, parentItem, style);
		moveToIndex(index);
	}

	public Composite getComposite() {
		checkComposite();
		return composite;
	}

	CompositeItem(Composite parent, CompositeItem parentItem, int style) {
		super(parent, style);
		this.parentItem = parentItem;
		this.parent = parent;
		if (this.parentItem == null) {
			composite = this.parent;
		} else {
			parentItem.hasChildren = true;
			if (parentItem.expanded) {
				composite = new Composite(parentItem.composite, style);
				composite.setLayout(CmsUtils.noSpaceGridLayout());
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
			}
		}

		if (composite != null)
			composite.setData(ITEM_DATA_KEY, this);
	}

	public Rectangle getBounds() {
		checkComposite();
		return composite.getBounds();
	}

	public CompositeItem[] getItems() {
		if (!hasChildren)
			return new CompositeItem[0];
		checkComposite();
		return getItems(composite);
	}

	public CompositeItem getItem(int index) {
		checkComposite();
		return getItem(composite, index);
	}

	public static CompositeItem getItem(Composite composite, int index) {
		Control[] children = composite.getChildren();
		return getItem(children[index]);
	}

	public static CompositeItem[] getItems(Composite composite) {
		Control[] children = composite.getChildren();
		CompositeItem[] items = new CompositeItem[children.length];
		for (int i = 0; i < children.length; i++) {
			items[i] = getItem(children[i]);
			if (items[i] == null)
				throw new CmsException("Item " + i + " not found in "
						+ children[i]);
		}
		return items;
	}

	public int indexOf(CompositeItem item) {
		checkComposite();
		return indexOf(composite, item);
	}

	public static int indexOf(Composite composite, CompositeItem item) {
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			CompositeItem it = getItem(children[i]);
			if (it.equals(item))
				return i;

		}
		throw new CmsException("Item " + item + " not found in " + composite
				+ " children");
	}

	public int getItemCount() {
		if (!hasChildren)
			return 0;
		checkComposite();
		return getItemCount(composite);
	}

	public static int getItemCount(Composite composite) {
		return composite.getChildren().length;
	}

	private void checkComposite() {
		if (composite == null)
			throw new CmsException("Composite is not ready");
		// TODO check disposed?
	}

	private void moveToIndex(int index) {
		if (this.index == index)
			return;
		if (composite != null) {
			Composite parentComposite = composite.getParent();
			Control sibling = parentComposite.getChildren()[index];
			composite.moveAbove(sibling);
		}
		this.index = index;
	}

	public boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public CompositeItem getParentItem() {
		// TODO consistent with composite?
		return parentItem;
	}

	public Composite getParent() {
		return parent;
	}

	@Override
	public void dispose() {
		if (parentItem != null) {
			if (parentItem.getItemCount() == 1)
				parentItem.hasChildren = false;
		}
		super.dispose();
	}

	public static CompositeItem getItem(Control composite) {
		return (CompositeItem) composite.getData(ITEM_DATA_KEY);
	}
}
