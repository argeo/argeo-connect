package org.argeo.connect.ui.crisis;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TrackingPerspective implements IPerspectiveFactory {

	private final static String PLUGIN_ID = "org.argeo.connect.ui.crisis";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(PLUGIN_ID + ".feedView");
		topLeft.addView(PLUGIN_ID + ".featureSourcesView");
		topLeft.addView(PLUGIN_ID + ".layersView");
	}

}
