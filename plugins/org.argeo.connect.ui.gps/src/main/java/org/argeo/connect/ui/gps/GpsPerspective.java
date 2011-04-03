package org.argeo.connect.ui.gps;

import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.connect.ui.gps.views.GpsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class GpsPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(GpsBrowserView.ID);
		topLeft.addView(GpsView.ID);

	}

}
