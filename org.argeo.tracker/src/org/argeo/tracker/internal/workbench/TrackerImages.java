package org.argeo.tracker.internal.workbench;

import org.argeo.connect.ui.ConnectImages;
import org.eclipse.swt.graphics.Color;

/** Shared icons in a Workbench context */
public class TrackerImages {
	public final static String PATH = "theme/icons/";
	public final static String ACTION_RELPATH = "actions/";
	public final static String TYPE_RELPATH = "types/";

	// FIXME: currently use an image created by the plugin to create the color
	public final static Color BG_COLOR_RED = new Color(ConnectImages.ISSUE.getDevice(), 210, 108, 120);

	// local shortcuts
//	public static Image getImage(String fileName) {
//		return getDesc(fileName).createImage();
//	}
//
//	public static ImageDescriptor getDesc(String fileName) {
//		return TrackerUiPlugin.getImageDescriptor(PATH + fileName);
//	}
}
