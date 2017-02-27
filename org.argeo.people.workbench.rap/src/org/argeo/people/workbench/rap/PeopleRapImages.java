package org.argeo.people.workbench.rap;

import org.eclipse.swt.graphics.Image;

/** Shared icons specific to the People Rap Workbench UI */
public class PeopleRapImages {

	// order images in various folder to ease their management
	final private static String BASE_PATH = "theme/icons/";
	final private static String IMG_PATH = "theme/img/";
	final private static String TYPE_PATH = BASE_PATH + "types/";
	final private static String ACTION_PATH = BASE_PATH + "actions/";

	// Various entity types
	public final static Image ICON_PERSON = createImg(TYPE_PATH, "person.gif");
	public final static Image ICON_ORG = createImg(TYPE_PATH, "organisation.png");
	public final static Image ICON_MAILING_LIST = createImg(TYPE_PATH, "mailingList.gif");
	public final static Image ICON_TAG = createImg(TYPE_PATH, "tag.png");

	// Actions Icons
	public final static Image DELETE_BTN = createImg(ACTION_PATH, "delete.gif");
	public final static Image DELETE_BTN_LEFT = createImg(ACTION_PATH, "delete_left.gif");
	public final static Image ADD_BTN = createImg(ACTION_PATH, "add.png");
	public final static Image MERGE_BTN = createImg(ACTION_PATH, "merge.gif");

	// Miscellaneous
	// public final static Image LOGO_SMALL = createImg(IMG_PATH,
	// "argeo-smallLogo.png");
	public final static Image NO_PICTURE = createImg(IMG_PATH, "noPicture.gif");
	public final static Image PRIMARY_BTN = createImg(BASE_PATH, "primary.gif");
	public final static Image PRIMARY_NOT_BTN = createImg(BASE_PATH, "primaryNOT.gif");
	public final static Image ORIGINAL_BTN = createImg(BASE_PATH, "first.png");
	// public final static Image CALENDAR_BTN = createImg(BASE_PATH,
	// "calendar.gif");
	// public final static Image CHECK_SELECTED = createImg(BASE_PATH,
	// "check-selected.png");
	// public final static Image CHECK_UNSELECTED = createImg(BASE_PATH,
	// "check-unselected.png");

	private static Image createImg(String prefix, String fileName) {
		return PeopleRapPlugin.getImageDescriptor(prefix + fileName).createImage();
	}
}
