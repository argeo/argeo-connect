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
package org.argeo.connect.gps;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** Geo-processing and geo-storage of the positions */
public interface TrackDao {
	/**
	 * Import track points from a GPX stream, filling temporary tables to be
	 * used for track cleaning.
	 * 
	 * @return the uuids of this GPX segments
	 */
	public List<String> importRawToCleanSession(String cleanSession,
			String sensor, InputStream in);

	/** Returns the path to the track speeds feature source */
	public String getTrackSpeedsSource(String cleanSession);

	/** Returns the path to the positions feature source */
	public String getPositionsSource(String positionsRepositoryName);

	/** Returns the path to the positions display feature source */
	public String getPositionsDisplaySource(String positionsRepositoryName);

	/** Publishes the cleaned-up positions to a positions referential. */
	public void publishCleanPositions(String cleanSession, String referential,
			String toRemoveCql);

	/**
	 * Publishes the cleaned-up positions to a positions referential Export in
	 * the GPX format.
	 */
	public void exportAsGpx(String cleanSession, String referential,
			String toRemoveCql, OutputStream out);

	/**
	 * Removes segments from this referential
	 * 
	 * @param referential
	 *            the technical name of the corresponding positions repository
	 */
	public void deleteCleanPositions(String referential,
			List<String> segmentUuuids);

	/** Whether backend is shapefile (thus with limitaitons) */
	public Boolean isShapefileBackend();
}
