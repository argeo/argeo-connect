/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.people.ui;

import org.eclipse.swt.graphics.Image;

/** Shared icons. */
public class PeopleImages {

	// order images in various folder to ease their management
	final private static String BASE_PATH = "icons/";
	final private static String ENTITY_PATH = BASE_PATH + "entities/";
	@SuppressWarnings("unused")
	final private static String MISC_PATH = BASE_PATH + "miscellaneous/";

	// Entities
	public final static Image ICON_PERSON = PeopleUiPlugin.getImageDescriptor(
			ENTITY_PATH + "person.png").createImage();
	public final static Image ICON_ORG = PeopleUiPlugin.getImageDescriptor(
			ENTITY_PATH + "company.png").createImage();
	public final static Image ICON_FILM = PeopleUiPlugin.getImageDescriptor(
			ENTITY_PATH + "film.png").createImage();
	public final static Image ICON_MAILING_LIST = PeopleUiPlugin
			.getImageDescriptor(ENTITY_PATH + "mailingList.gif").createImage();
	public final static Image ICON_GROUP = PeopleUiPlugin.getImageDescriptor(
			ENTITY_PATH + "group.gif").createImage();

	// Miscellaneous
	public final static Image LOGO = PeopleUiPlugin.getImageDescriptor(
			"icons/logo.gif").createImage();

	public final static Image LOGO_SMALL = PeopleUiPlugin.getImageDescriptor(
			"icons/smallerOrnamentLogo.png").createImage();

	public final static Image NO_PICTURE = PeopleUiPlugin.getImageDescriptor(
			"icons/noPicture.gif").createImage();

	public final static Image PERSON = PeopleUiPlugin.getImageDescriptor(
			"icons/person.gif").createImage();

	public final static Image DELETE_BTN = PeopleUiPlugin.getImageDescriptor(
			"icons/delete.gif").createImage();

	public final static Image DELETE_BTN_LEFT = PeopleUiPlugin
			.getImageDescriptor("icons/delete_left.gif").createImage();

	public final static Image ADD_BTN = PeopleUiPlugin.getImageDescriptor(
			"icons/add.gif").createImage();

	public final static Image PRIMARY_BTN = PeopleUiPlugin.getImageDescriptor(
			"icons/primary.gif").createImage();

	public final static Image PRIMARY_NOT_BTN = PeopleUiPlugin
			.getImageDescriptor("icons/primaryNOT.gif").createImage();

	public final static Image ORIGINAL_BTN = PeopleUiPlugin.getImageDescriptor(
			"icons/first.png").createImage();

	// Check box icons
	public final static Image CHECKED = PeopleUiPlugin.getImageDescriptor(
			"icons/checked.gif").createImage();
	public final static Image UNCHECKED = PeopleUiPlugin.getImageDescriptor(
			"icons/unchecked.gif").createImage();

	// User Management
	public final static Image ROLE_CHECKED = PeopleUiPlugin.getImageDescriptor(
			"icons/security.gif").createImage();

}