package org.argeo.connect.ui.gps;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Shared icons. Returns either ImageDescriptors or Image objects.
 */
public class GpsImages {
	private final static String ICON_REL_PATH = "icons/";

	private static ImageDescriptor getImageDesc(String imgName) {
		return ConnectGpsUiPlugin.getImageDescriptor(ICON_REL_PATH + imgName);
	}

	// Generic objects icons
	public final static ImageDescriptor ICON_NETWORKS = getImageDesc("networkList.gif");
	public final static ImageDescriptor ICON_ADD_FOLDER = getImageDesc("addFolder.gif");
	public final static ImageDescriptor ICON_REPOSITORY = getImageDesc("repo.gif");

	// Generic action
	public final static ImageDescriptor ICON_CHECKED = getImageDesc("checked.gif");
	public final static ImageDescriptor ICON_UNCHECKED = getImageDesc("unchecked.gif");

	// Connect GPS specific
	public final static ImageDescriptor ICON_IMPORT_FOLDER = getImageDesc("import_fs.png");
	public final static ImageDescriptor ICON_ADD_CLEANING_SESSION = getImageDesc("sessionAdd.gif");
	public final static ImageDescriptor ICON_REMOVE_CLEANING_SESSION = getImageDesc("sessionRemove.gif");

}
