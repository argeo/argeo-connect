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
public class ContactImages {

	private final static String PREFIX = "icons/contacts/";
	// Contact icons
	// EMAIL
	public final static Image DEFAULT_MAIL = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "mail_black.png").createImage();

	// PHONE
	public final static Image DEFAULT_PHONE = PeopleUiPlugin
			.getImageDescriptor(PREFIX + "phone_vintage.png").createImage();

	public final static Image PHONE_DIRECT = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "telephone.png").createImage();

	public final static Image FAX = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "fax.png").createImage();

	public final static Image MOBILE = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "mobile.png").createImage();

	// ADDRESS
	public final static Image DEFAULT_ADDRESS = PeopleUiPlugin
			.getImageDescriptor(PREFIX + "home.png").createImage();

	public final static Image WORK = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "company.png").createImage();

	// URL
	public final static Image DEFAULT_URL = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "link.png").createImage();

	public final static Image PRIVATE_HOME_PAGE = PeopleUiPlugin
			.getImageDescriptor(PREFIX + "house_link.png").createImage();

	// SOCIAL MEDIA
	public final static Image DEFAULT_SOCIAL_MEDIA = PeopleUiPlugin
			.getImageDescriptor(PREFIX + "socialmedia.png").createImage();

	public final static Image GOOGLEPLUS = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "googleplus.png").createImage();

	public final static Image LINKEDIN = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "linkedin.png").createImage();

	public final static Image FACEBOOK = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "facebook.png").createImage();

	public final static Image XING = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "xing" + ".png").createImage();

	// IMPP
	public final static Image DEFAULT_IMPP = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "impp.png").createImage();
}
