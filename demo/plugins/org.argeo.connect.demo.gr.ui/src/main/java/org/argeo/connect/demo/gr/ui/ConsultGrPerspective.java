/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.demo.gr.ui;

import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.ui.views.NetworkBrowserView;
import org.argeo.connect.demo.gr.ui.views.NetworkListView;
import org.argeo.connect.demo.gr.ui.views.SiteListView;
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
		left.addView(SiteListView.ID);
		// TODO better deal with unauthorized
		left.addView(NetworkListView.ID);
	
	}
}
