package org.argeo.connect.activities.workbench;

import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class ActivitiesImages {
	final private static String BASE_PATH = "/theme/icons/types/";
	// Various types
	public final static Image ICON_PROJECT = createImage(BASE_PATH, "project.gif");
	public final static Image ICON_MILESTONE = createImage(BASE_PATH, "milestone.gif");

	public final static Image ICON_GROUP = createImage(BASE_PATH, "group.gif");
	public final static Image ICON_USER = createImage(BASE_PATH, "person.gif");
	public final static Image ICON_ROLE = createImage(BASE_PATH, "role.gif");

	
	private static Image createImage(String path, String fileName) {
		return ActivitiesUiPlugin.getImageDescriptor(path + fileName).createImage();
	}
}
