package org.argeo.connect.activities.workbench;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** Shared icons for the file system RAP workbench */
public class ActivitiesImages {
	final private static String BASE_PATH = "/theme/icons/types/";

	// TASKS icons
	public final static Image DONE_TASK = img(BASE_PATH, "doneTask.png");
	public final static Image TODO = img(BASE_PATH, "todo.gif");
	public final static Image RATE = img(BASE_PATH, "todo.gif");

	// TODO We still use contact images: get more specific icons
	public final static Image ACTIVITY = img(BASE_PATH, "activity.gif");
	public final static Image NOTE = img(BASE_PATH, "note.gif");
	public final static Image SENT_MAIL = img(BASE_PATH, "sentMail.png");
	public final static Image PHONE_CALL = img(BASE_PATH, "phoneCall.png");
	public final static Image SENT_FAX = img(BASE_PATH, "sentFax.png");
	// TODO find icons for other types:
	private final static Image DUMMY_UNDEFINED = img(BASE_PATH, "noImage.gif");
	public final static Image MEETING = DUMMY_UNDEFINED;
	public final static Image POST_MAIL = DUMMY_UNDEFINED;
	public final static Image PAYMENT = DUMMY_UNDEFINED;
	public final static Image REVIEW = DUMMY_UNDEFINED;
	public final static Image CHAT = DUMMY_UNDEFINED;
	public final static Image TWEET = DUMMY_UNDEFINED;
	public final static Image BLOG = DUMMY_UNDEFINED;

	private static Image img(String prefix, String fileName) {
		return getDesc(prefix, fileName).createImage();
	}

	private static ImageDescriptor getDesc(String prefix, String fileName) {
		return ActivitiesUiPlugin.getImageDescriptor(prefix + fileName);
	}
}
