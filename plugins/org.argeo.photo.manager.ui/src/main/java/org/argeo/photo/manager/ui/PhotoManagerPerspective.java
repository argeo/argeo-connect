package org.argeo.photo.manager.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PhotoManagerPerspective implements IPerspectiveFactory {

	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		layout.addStandaloneView("org.argeo.photo.manager.ui.navigatorView",
				true, IPageLayout.LEFT, 0.3f, editorArea);
		layout.addStandaloneView(RenameView.ID, false, IPageLayout.RIGHT, 0.7f,
				editorArea);
	}
}
