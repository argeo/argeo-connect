package org.argeo.connect.activities.workbench;

import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class ActivitiesImages {
	final private static String BASE_PATH = "/theme/icons/types/";
	// Various types
	public final static Image ICON_PROJECT = createImage(BASE_PATH, "project.gif");
	public final static Image ICON_MILESTONE = createImage(BASE_PATH, "milestone.gif");

	private static Image createImage(String path, String fileName) {
		return ActivitiesUiPlugin.getImageDescriptor(path + fileName).createImage();
	}
}
