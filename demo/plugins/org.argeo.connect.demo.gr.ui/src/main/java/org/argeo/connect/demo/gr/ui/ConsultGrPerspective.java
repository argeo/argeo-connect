package org.argeo.connect.demo.gr.ui;

import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.ui.views.NetworkBrowserView;
import org.argeo.connect.demo.gr.ui.views.NetworkListView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Default IGNFI GR perspective. */
public class ConsultGrPerspective implements IPerspectiveFactory, GrConstants {
	public final String ID = GrUiPlugin.PLUGIN_ID + ".consultGR";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.4f, editorArea);
		left.addView(NetworkBrowserView.ID);
		// TODO better deal with unauthorized
		left.addView(NetworkListView.ID);

	}
}
