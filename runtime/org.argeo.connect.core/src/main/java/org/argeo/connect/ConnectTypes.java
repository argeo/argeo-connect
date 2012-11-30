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

/** JCR types. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectTypes {
	final static String CONNECT_ = "connect:";

	/** Connect Crisis */
	public final static String CONNECT_SYND_FEED = CONNECT_ + "syndFeed";
	public final static String CONNECT_SYND_ENTRY = CONNECT_ + "syndEntry";

	/** Connect GPS */
	// root nodes
	public final static String CONNECT_LOCAL_REPOSITORIES = CONNECT_
			+ "localRepositories";
	public final static String CONNECT_SESSION_REPOSITORY = CONNECT_
			+ "sessionRepository";
	public final static String CONNECT_FILE_REPOSITORY = CONNECT_
			+ "fileRepository";
	
	// business objects
	public final static String CONNECT_LOCAL_REPOSITORY = CONNECT_
			+ "localRepository";
	public final static String CONNECT_CLEAN_TRACK_SESSION = CONNECT_
			+ "cleanTrackSession";
	public final static String CONNECT_CLEAN_PARAMETER = CONNECT_
			+ "cleanParameter";
	public final static String CONNECT_FILE_TO_IMPORT = CONNECT_
			+ "fileToImport";

}
