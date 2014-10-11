package org.argeo.cms;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Widget;

/** Static utilities for the CMS framework. */
public class CmsUtils {
	public static RowData ROW_DATA_16px = new RowData(16, 16);

	public static String STYLE_DATAKEY = RWT.CUSTOM_VARIANT;

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

	public static void style(Widget widget, String style) {
		widget.setData(STYLE_DATAKEY, style);
	}

	private CmsUtils() {
	}
}
