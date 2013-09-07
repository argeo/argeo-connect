package org.argeo.connect.streams.ui;

import org.argeo.connect.streams.ui.views.RssSearchView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** default perspective for the Connect Stream RSS app */
public class RssPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.3f, editorArea);
		left.addView(RssSearchView.ID);
	}
}
