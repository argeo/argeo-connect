/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012-2017 Argeo GmbH
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
package org.argeo.people.workbench.rap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** Shared icons used for activity and task management */
public class ActivitiesImages {

	private final static String BASE_PREFIX = "theme/icons/";
	private final static String ACT_PREFIX = BASE_PREFIX + "activities/";

	// Activities icons
	// TODO We still use contact images: get more specific icons
	public final static Image NOTE = img(ACT_PREFIX, "note.gif");
	public final static Image SENT_MAIL = img(ACT_PREFIX, "sentMail.png");
	public final static Image PHONE_CALL = img(ACT_PREFIX, "phoneCall.png");
	public final static Image SENT_FAX = img(ACT_PREFIX, "sentFax.png");
	// TODO find icons for other types:
	private final static Image DUMMY_UNDEFINED = img(ACT_PREFIX, "noImage.gif");
	public final static Image MEETING = DUMMY_UNDEFINED;
	public final static Image POST_MAIL = DUMMY_UNDEFINED;
	public final static Image PAYMENT = DUMMY_UNDEFINED;
	public final static Image REVIEW = DUMMY_UNDEFINED;
	public final static Image CHAT = DUMMY_UNDEFINED;
	public final static Image TWEET = DUMMY_UNDEFINED;
	public final static Image BLOG = DUMMY_UNDEFINED;

	// TASKS icons
	public final static Image DONE_TASK = img(ACT_PREFIX, "doneTask.png");
	public final static Image TODO = img(ACT_PREFIX, "todo.gif");

	public final static ImageDescriptor TODO_IMGDESC = getDesc(ACT_PREFIX, "todo.gif");

	private static Image img(String prefix, String fileName) {
		return getDesc(prefix, fileName).createImage();
	}

	private static ImageDescriptor getDesc(String prefix, String fileName) {
		return PeopleRapPlugin.getImageDescriptor(prefix + fileName);
	}
}
