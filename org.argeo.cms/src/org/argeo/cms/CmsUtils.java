package org.argeo.cms;

import org.eclipse.swt.layout.GridLayout;

/** Static utilities for the CMS framework. */
public class CmsUtils {
	public static GridLayout noSpaceGridLayout() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		return layout;
	}

	private CmsUtils() {
	}
}
