package org.argeo.connect.crisis.manager;

import org.argeo.gis.ui.views.FeatureSourcesView;
import org.argeo.gis.ui.views.LayersView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class CrisisTrackingPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(LayersView.ID);
		topLeft.addView(FeatureSourcesView.ID);
		topLeft.addView("org.argeo.connect.crisis.manager.feedView");
	}

}
