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
package org.argeo.connect.people.workbench.rap;

import org.eclipse.swt.graphics.Image;

/** Shared icons used for contact management */
public class ContactImages {
	private final static String PREFIX = "icons/contacts/";

	// Email
	public final static Image DEFAULT_MAIL = createImg("mail_black.png");
	// Phone
	public final static Image DEFAULT_PHONE = createImg("phone_vintage.png");
	public final static Image PHONE_DIRECT = createImg("telephone.png");
	public final static Image FAX = createImg("fax.png");
	public final static Image MOBILE = createImg("mobile.png");
	// Addresses
	public final static Image DEFAULT_ADDRESS = createImg("home.png");
	public final static Image WORK = createImg("company.png");
	// Phones
	public final static Image DEFAULT_URL = createImg("link.png");
	public final static Image PRIVATE_HOME_PAGE = createImg("house_link.png");
	// Social media
	public final static Image DEFAULT_SOCIAL_MEDIA = createImg("socialmedia.png");
	public final static Image GOOGLEPLUS = createImg("googleplus.png");
	public final static Image LINKEDIN = createImg("linkedin.png");
	public final static Image FACEBOOK = createImg("facebook.png");
	public final static Image XING = createImg("xing.png");
	// Impp
	public final static Image DEFAULT_IMPP = createImg("impp.png");

	private static Image createImg(String fileName) {
		return PeopleRapPlugin.getImageDescriptor(PREFIX + fileName).createImage();
	}
}
