package org.argeo.connect.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TrackingPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(ConnectUiPlugin.ID+".feedView");
		topLeft.addView(ConnectUiPlugin.ID+".featureSourcesView");
		topLeft.addView(ConnectUiPlugin.ID+".layersView");
	}

}
