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
package org.argeo.connect;

/** JCR names. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectNames {

	public final static String CONNECT_NAMESPACE = "http://www.argeo.org/ns/connect";
	public final static String CONNECT_AUTHOR = "connect:author";
	public final static String CONNECT_PUBLISHED_DATE = "connect:publishedDate";
	public final static String CONNECT_UPDATED_DATE = "connect:updatedDate";
	public final static String CONNECT_SOURCE_URI = "connect:sourceUri";

	/** Connect CRISIS */
	// Situation
	public final static String CONNECT_PRIORITY = "connect:priority";
	public final static String CONNECT_STATUS = "connect:status";
	public final static String CONNECT_UPDATE = "connect:update";
	public final static String CONNECT_TEXT = "connect:text";

	/** Connect GPS */
	// Session handling
	public final static String CONNECT_IS_SESSION_COMPLETE = "connect:isSessionComplete";
	public final static String CONNECT_DEFAULT_SENSOR = "connect:defaultSensor";
	public final static String CONNECT_DEFAULT_DEVICE = "connect:defaultDevice";
	public final static String CONNECT_LOCAL_REPO_NAME = "connect:localRepoName";

	// Files to clean
	public final static String CONNECT_LINKED_FILE = "connect:linkedFile";

	// Imported files
	public final static String CONNECT_LINKED_FILE_REF = "connect:fileRef";
	public final static String CONNECT_SENSOR_NAME = "connect:sensorName";
	public final static String CONNECT_DEVICE_NAME = "connect:deviceName";
	public final static String CONNECT_TO_BE_PROCESSED = "connect:toBeProcessed";
	public final static String CONNECT_ALREADY_PROCESSED = "connect:alreadyProcessed";
	public final static String CONNECT_SEGMENT_UUID = "connect:segmentUuid";

	// Clean parameters
	public final static String CONNECT_PARAM_VALUE = "connect:paramValue";
	public final static String CONNECT_PARAM_MIN_VALUE = "connect:paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = "connect:paramMaxValue";
	public final static String CONNECT_PARAM_IS_USED = "connect:paramIsUsed";
}