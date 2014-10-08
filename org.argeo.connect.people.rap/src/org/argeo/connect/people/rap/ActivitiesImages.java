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
package org.argeo.connect.people.rap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** Shared icons used for activity and task management */
public class ActivitiesImages {

	private final static String PREFIX = "icons/activities/";

	// Activities icons
	// NOTE
	public final static Image NOTE = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "note.gif").createImage();

	// FIXME We use contact images for the time being
	// SENT EMAIL
	public final static Image SENT_MAIL = PeopleUiPlugin.getImageDescriptor(
			"icons/contacts/mail_black.png").createImage();

	// PHONE CALL
	public final static Image PHONE_CALL = PeopleUiPlugin.getImageDescriptor(
			"icons/contacts/telephone.png").createImage();

	// SEND FAX
	public final static Image SENT_FAX = PeopleUiPlugin.getImageDescriptor(
			"icons/contacts/fax.png").createImage();

	// FIXME find icons for other types:
	private final static Image DUMMY_UNDEFINED = PeopleUiPlugin
			.getImageDescriptor(PREFIX + "noImage.gif").createImage();
	public final static Image MEETING = DUMMY_UNDEFINED;
	public final static Image POST_MAIL = DUMMY_UNDEFINED;
	public final static Image PAYMENT = DUMMY_UNDEFINED;
	public final static Image REVIEW = DUMMY_UNDEFINED;
	public final static Image CHAT = DUMMY_UNDEFINED;
	public final static Image TWEET = DUMMY_UNDEFINED;
	public final static Image BLOG = DUMMY_UNDEFINED;

	// TASKS icons
	// Task
	public final static Image DONE_TASK = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "doneTask.png").createImage();

	public final static Image TODO = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "todo.gif").createImage();

	public final static ImageDescriptor TODO_IMGDESC = PeopleUiPlugin.getImageDescriptor(
			PREFIX + "todo.gif");
}
