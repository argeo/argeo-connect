package org.argeo.connect.people.workbench.rap;

import org.eclipse.swt.graphics.Image;

/**
 * Shared icons specific to the People Rap Workbench UI
 * 
 * TODO Factorize icons that are not specific to the rap workbench
 */
public class PeopleRapImages {

	// order images in various folder to ease their management
	final private static String BASE_PATH = "icons/";
	final private static String ENTITY_PATH = BASE_PATH + "entities/";
	@SuppressWarnings("unused")
	final private static String MISC_PATH = BASE_PATH + "miscellaneous/";

	// Entities
	public final static Image ICON_PERSON = PeopleRapPlugin.getImageDescriptor(
			ENTITY_PATH + "person.png").createImage();
	public final static Image ICON_ORG = PeopleRapPlugin.getImageDescriptor(
			ENTITY_PATH + "company.png").createImage();
	public final static Image ICON_FILM = PeopleRapPlugin.getImageDescriptor(
			ENTITY_PATH + "film.png").createImage();

	public final static Image ICON_MAILING_LIST = PeopleRapPlugin
			.getImageDescriptor(ENTITY_PATH + "mailingList.png").createImage();

	public final static Image ICON_TAG = PeopleRapPlugin.getImageDescriptor(
			ENTITY_PATH + "tag.png").createImage();

	public final static Image ICON_GROUP = PeopleRapPlugin.getImageDescriptor(
			BASE_PATH + "group.gif").createImage();
	public final static Image ICON_USER = PeopleRapPlugin.getImageDescriptor(
			BASE_PATH + "person.gif").createImage();
	public final static Image ICON_ROLE = PeopleRapPlugin.getImageDescriptor(
			BASE_PATH + "role.gif").createImage();

	// Miscellaneous
	public final static Image LOGO = PeopleRapPlugin.getImageDescriptor(
			"icons/logo.gif").createImage();

	public final static Image LOGO_SMALL = PeopleRapPlugin.getImageDescriptor(
			"icons/smallerOrnamentLogo.png").createImage();

	public final static Image NO_PICTURE = PeopleRapPlugin.getImageDescriptor(
			"icons/noPicture.gif").createImage();

	public final static Image PERSON = PeopleRapPlugin.getImageDescriptor(
			"icons/person.gif").createImage();

	public final static Image DELETE_BTN = PeopleRapPlugin.getImageDescriptor(
			"icons/delete.gif").createImage();

	public final static Image DELETE_BTN_LEFT = PeopleRapPlugin
			.getImageDescriptor("icons/delete_left.gif").createImage();

	public final static Image ADD_BTN = PeopleRapPlugin.getImageDescriptor(
			"icons/add.gif").createImage();

	public final static Image PRIMARY_BTN = PeopleRapPlugin.getImageDescriptor(
			"icons/primary.gif").createImage();

	public final static Image PRIMARY_NOT_BTN = PeopleRapPlugin
			.getImageDescriptor("icons/primaryNOT.gif").createImage();

	public final static Image ORIGINAL_BTN = PeopleRapPlugin
			.getImageDescriptor("icons/first.png").createImage();

	public final static Image CALENDAR_BTN = PeopleRapPlugin
			.getImageDescriptor("icons/calendar.gif").createImage();

	// Check box icons
	public final static Image CHECK_SELECTED = PeopleRapPlugin
			.getImageDescriptor("icons/check-selected.png").createImage();

	public final static Image CHECK_UNSELECTED = PeopleRapPlugin
			.getImageDescriptor("icons/check-unselected.png").createImage();

	public final static Image MERGE_BTN = PeopleRapPlugin.getImageDescriptor(
			"icons/merge.gif").createImage();

	// User Management
	public final static Image ROLE_CHECKED = PeopleRapPlugin
			.getImageDescriptor("icons/security.gif").createImage();

}
