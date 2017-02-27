package org.argeo.connect.documents.workbench;

import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class DocumentsImages {
	final private static String BASE_PATH = "/theme/icons/types/";
	// Various types
	public final static Image ICON_FOLDER = DocumentsUiPlugin.getImageDescriptor(BASE_PATH + "folder.gif")
			.createImage();
	public final static Image ICON_SHARED_FOLDER = DocumentsUiPlugin.getImageDescriptor(BASE_PATH + "folder.gif")
			.createImage();
	public final static Image ICON_FILE = DocumentsUiPlugin.getImageDescriptor(BASE_PATH + "file.gif").createImage();
	public final static Image ICON_BOOKMARK = DocumentsUiPlugin.getImageDescriptor(BASE_PATH + "folder.gif")
			.createImage();
}
