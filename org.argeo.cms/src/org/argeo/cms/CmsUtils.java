package org.argeo.cms;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/** Static utilities for the CMS framework. */
public class CmsUtils {
	/** @deprecated Use rowData16px() instead. GridData should not be reused. */
	@Deprecated
	public static RowData ROW_DATA_16px = new RowData(16, 16);

	public static GridLayout noSpaceGridLayout() {
		return noSpaceGridLayout(new GridLayout());
	}

	public static GridLayout noSpaceGridLayout(GridLayout layout) {
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		return layout;
	}

	//
	// GRID DATA
	//
	public static GridData fillWidth() {
		return new GridData(SWT.FILL, SWT.FILL, true, false);
	}

	public static RowData rowData16px() {
		return new RowData(16, 16);
	}

	public static void style(Widget widget, String style) {
		widget.setData(CmsConstants.STYLE, style);
	}

	/** @return the path or null if not instrumented */
	public static String getDataPath(Widget widget) {
		// JCR item
		Object data = widget.getData();
		if (data != null && data instanceof Item) {
			try {
				return ((Item) data).getPath();
			} catch (RepositoryException e) {
				throw new CmsException("Cannot find data path of " + data
						+ " for " + widget);
			}
		}

		// JCR path
		data = widget.getData(Property.JCR_PATH);
		if (data != null)
			return data.toString();

		return null;
	}

	/** @return the data, never null */
	// public static Item getDataItem(Widget widget, Item context) {
	// // JCR item
	// Object data = widget.getData();
	// if (data != null && data instanceof Item) {
	// return (Item) data;
	// }
	//
	// // JCR path
	// data = widget.getData(Property.JCR_PATH);
	// try {
	// if (data != null && context != null)
	// return context.getSession().getItem(data.toString());
	// } catch (RepositoryException e) {
	// throw new CmsException("Problem when looking for data item of "
	// + data + " for " + widget);
	// }
	//
	// throw new CmsException("Cannot find data item for " + widget);
	// }

	/** Dispose all children of a Composite */
	public static void clear(Composite composite) {
		for (Control child : composite.getChildren())
			child.dispose();
	}

	private CmsUtils() {
	}
}
