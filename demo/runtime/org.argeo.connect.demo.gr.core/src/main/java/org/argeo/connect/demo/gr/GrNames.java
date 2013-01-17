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
package org.argeo.connect.demo.gr;

/** JCR node and property names used in GR */
public interface GrNames {

	public final static String GR_NAMESPACE = "http://www.argeo.org/ns/gr";

	/* Parent node for all networks */
	public final static String GR_NETWORKS = "gr:networks";
	/* Parent node for all imports */
	public final static String GR_IMPORTS = "gr:imports";

	/* Common */
	public final static String GR_UUID = "gr:uuid";

	/* Site */
	public final static String GR_SITE_TYPE = "gr:siteType";
	public final static String GR_SITE_COMMENTS = "gr:siteComments";
	public final static String GR_SITE_MAIN_POINT = "gr:mainPoint";
	// public final static String GR_SITE_SECONDARY_POINT = "gr:secondaryPoint";

	/* Date */
	public final static String GR_WATER_LEVEL = "gr:waterLevel";
	public final static String GR_ECOLI_RATE = "gr:eColiRate";
	public final static String GR_WITHDRAWN_WATER = "gr:withdrawnWater";

	/* Comment */
	public final static String GR_COMMENT_CONTENT = "gr:content";

	/* Point */
	// public final static String GR_POINT_TYPE = "gr:pointType";
	public final static String GR_WGS84_LONGITUDE = "gr:wgs84Longitude";
	public final static String GR_WGS84_LATITUDE = "gr:wgs84Latitude";
}
