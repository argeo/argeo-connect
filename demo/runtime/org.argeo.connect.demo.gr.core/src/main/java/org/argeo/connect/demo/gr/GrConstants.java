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

/** Constants used across the application. */
public interface GrConstants {

	/*
	 * NAMESPACES
	 */
	/** GR prefix (gr:) */

	/*
	 * PATHS
	 */
	/** Base path for all GR specific nodes */
	public final static String GR_BASE_PATH = "/gr:system";
	public final static String GR_NETWORKS_BASE_PATH = GR_BASE_PATH + '/'
			+ GrNames.GR_NETWORKS;
	public final static String GR_IMPORTS_BASE_PATH = GR_BASE_PATH + '/'
			+ GrNames.GR_IMPORTS;

	/* NODES METADATA */
	// TODO : it mights not be the cleanest way to access JCR NODES UID
	// public final static String GR_NODE_UID = "gr:nodeUid";
	// public final static String GR_NODE_NAME = "gr:nodeName";

	/*
	 * USER ROLES
	 */
	public final static Integer ROLE_CONSULTANT = 0;
	public final static Integer ROLE_MANAGER = 1;
	public final static Integer ROLE_ADMIN = 2;

	/*
	 * MISCEALLENEOUS
	 */
	public final static String DATE_FORMAT = "dd/MM/yyyy";
	public final static String DATE_TIME_FORMAT = "dd/MM/yyyy, HH:mm";
	public final static String NUMBER_FORMAT = "#,##0.00";

	/* SITE TYPES */
	public final static String MONITORED = "monitored";
	public final static String VISITED = "visited";
	public final static String REGISTERED = "registered";
}
