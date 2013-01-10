package org.argeo.connect.ui.gps;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Shared icons. Note that it returns ImageDescriptors object rather than
 * images.
 */
public class GpsImages {
	private final static String ICON_REL_PATH = "icons/";

	// Generic objects icons
	public final static ImageDescriptor ICON_NETWORKS = ConnectGpsUiPlugin
			.getImageDescriptor(ICON_REL_PATH + "networkList.gif");

	public final static ImageDescriptor ICON_ADD_FOLDER = ConnectGpsUiPlugin
			.getImageDescriptor(ICON_REL_PATH + "addFolder.gif");

	public final static ImageDescriptor ICON_REPOSITORY = ConnectGpsUiPlugin
	.getImageDescriptor(ICON_REL_PATH + "repo.gif");

	// Generic action
	
	// Connect GPS specific 
	public final static ImageDescriptor ICON_IMPORT_FOLDER = ConnectGpsUiPlugin
			.getImageDescriptor(ICON_REL_PATH + "import_fs.png");

	public final static ImageDescriptor ICON_ADD_CLEANING_SESSION = ConnectGpsUiPlugin
			.getImageDescriptor(ICON_REL_PATH + "sessionAdd.gif");

	public final static ImageDescriptor ICON_REMOVE_CLEANING_SESSION = ConnectGpsUiPlugin
			.getImageDescriptor(ICON_REL_PATH + "sessionRemove.gif");

}
