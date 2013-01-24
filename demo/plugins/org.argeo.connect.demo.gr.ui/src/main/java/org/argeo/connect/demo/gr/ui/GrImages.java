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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** Shared icons. */
public class GrImages {
	// Generic icons
	public final static ImageDescriptor IMDESC_NETWORKS = GrUiPlugin
			.getImageDescriptor("icons/networkList.gif");
	public final static Image ICON_NETWORKS = IMDESC_NETWORKS.createImage();

	public final static ImageDescriptor IMDESC_NETWORK = GrUiPlugin
			.getImageDescriptor("icons/network.png");
	public final static Image ICON_NETWORK = IMDESC_NETWORK.createImage();

	public final static ImageDescriptor IMDESC_NEW_NETWORK = GrUiPlugin
			.getImageDescriptor("icons/newNetwork.gif");
	public final static Image ICON_NEW_NETWORK = IMDESC_NEW_NETWORK
			.createImage();

	// FIXME use default refresh icon
	public final static ImageDescriptor IMDESC_REFRESH = GrUiPlugin
			.getImageDescriptor("icons/refresh.png");
	public final static Image ICON_REFRESH = IMDESC_REFRESH.createImage();

	public final static Image CHECKED = GrUiPlugin.getImageDescriptor(
			"icons/checked.gif").createImage();
	public final static Image ICON_UNCHECKED = GrUiPlugin.getImageDescriptor(
			"icons/unchecked.gif").createImage();

	public final static Image ICON_IMPORT_INSTANCES = GrUiPlugin
			.getImageDescriptor("icons/importInstances.png").createImage();
	public final static Image ICON_FORM_INSTANCE = GrUiPlugin
			.getImageDescriptor("icons/formInstance.gif").createImage();

	// various site types
	public final static Image ICON_MONITORED_TYPE = GrUiPlugin
			.getImageDescriptor(
					GrImages.getTypeIconResource(GrConstants.MONITORED))
			.createImage();
	public final static Image ICON_VISITED_TYPE = GrUiPlugin
			.getImageDescriptor(
					GrImages.getTypeIconResource(GrConstants.VISITED))
			.createImage();
	public final static Image ICON_REGISTERED_TYPE = GrUiPlugin
			.getImageDescriptor(
					GrImages.getTypeIconResource(GrConstants.REGISTERED))
			.createImage();

	public static String getTypeIconResource(String type) {
		return "icons/" + type + ".gif";
	}

}
