package org.argeo.connect.demo.gr.ui;

import org.argeo.connect.demo.gr.GrConstants;
import org.eclipse.swt.graphics.Image;

/** Shared icons. */
public class GrImages {
	// Generic icons
	public final static Image ICON_NETWORKS = GrUiPlugin.getImageDescriptor(
			"icons/networkList.gif").createImage();
	public final static Image ICON_NETWORK = GrUiPlugin.getImageDescriptor(
			"icons/network.png").createImage();

	// various site types
	public final static Image ICON_MONITORED_TYPE = GrUiPlugin
			.getImageDescriptor("icons/" + GrConstants.MONITORED + ".gif")
			.createImage();
	public final static Image ICON_VISITED_TYPE = GrUiPlugin
			.getImageDescriptor("icons/" + GrConstants.VISITED + ".gif")
			.createImage();
	public final static Image ICON_REGISTERED_TYPE = GrUiPlugin
			.getImageDescriptor("icons/" + GrConstants.REGISTERED + ".gif")
			.createImage();

}
