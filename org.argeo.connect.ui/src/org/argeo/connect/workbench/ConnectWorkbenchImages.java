package org.argeo.connect.workbench;

import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class ConnectWorkbenchImages {
	final private static String BASE_PATH = "theme/icons/types/";
	// Various types
	public final static Image ICON_GROUP = createImage(BASE_PATH, "group.gif");
	public final static Image ICON_USER = createImage(BASE_PATH, "user.gif");
	public final static Image ICON_ROLE = createImage(BASE_PATH, "role.gif");
	
	public final static Image ICON_TAG = createImage(BASE_PATH, "tag.png");
	
	private static Image createImage(String path, String fileName) {
		return ConnectUiPlugin.getImageDescriptor(path + fileName).createImage();
	}
}
