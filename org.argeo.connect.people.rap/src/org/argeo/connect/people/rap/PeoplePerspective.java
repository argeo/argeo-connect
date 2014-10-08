package org.argeo.connect.people.ui;

import org.argeo.connect.people.ui.views.PeopleDefaultView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Base default perspective for the Connect People app */
public class PeoplePerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.3f, editorArea);
		left.addView(PeopleDefaultView.ID);
	}
}
