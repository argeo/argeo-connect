package org.argeo.connect.people.ui;

import org.argeo.connect.people.ui.views.CategorizedSearchView;
import org.argeo.connect.people.ui.views.PeopleBrowserView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PeoplePerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.5f, editorArea);
		left.addView(PeopleBrowserView.ID);
		left.addView(CategorizedSearchView.ID);
	}
}
