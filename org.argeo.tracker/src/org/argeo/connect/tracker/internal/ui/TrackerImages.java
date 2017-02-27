package org.argeo.connect.tracker.internal.ui;

import org.argeo.connect.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/** Shared icons. */
public class TrackerImages {
	public final static String PATH = "theme/icons/";
	public final static String ACTION_RELPATH = "actions/";
	public final static String TYPE_RELPATH = "types/";

	public final static ImageDescriptor IMG_DESC_EDIT = getDesc(ACTION_RELPATH + "edit.gif");
	public final static Image ICON_ADD = getImage(ACTION_RELPATH + "add.gif");

	public final static Image ICON_ISSUE = getImage(TYPE_RELPATH + "issue.png");
	public final static Image ICON_PROJECT = getImage(TYPE_RELPATH + "project.gif");
	public final static Image ICON_SPEC = getImage(TYPE_RELPATH + "specification.gif");

	// FIXME: currently use an image created by the plugin to create the color
	public final static Color BG_COLOR_RED = new Color(ICON_ISSUE.getDevice(), 210, 108, 120);

	// local shortcuts
	private static Image getImage(String fileName) {
		return getDesc(fileName).createImage();
	}

	private static ImageDescriptor getDesc(String fileName) {
		return TrackerUiPlugin.getImageDescriptor(PATH + fileName);
	}
}
