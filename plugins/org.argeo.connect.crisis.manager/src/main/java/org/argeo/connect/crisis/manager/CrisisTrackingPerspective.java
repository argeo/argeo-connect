package org.argeo.connect.crisis.manager;

import org.argeo.gis.ui.views.DataStoresView;
import org.argeo.gis.ui.views.LayersView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class CrisisTrackingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(LayersView.ID);
		topLeft.addView(DataStoresView.ID);
		topLeft.addView("org.argeo.jcr.ui.explorer.browserView");
	}

}
