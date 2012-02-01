package org.argeo.connect.gpx;

import java.io.InputStream;
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
	 * Removes segments from this referential
	 * 
	 * @param referential
	 *            the technical name of the corresponding positions repository
	 */
	public void deleteCleanPositions(String referential,
			List<String> segmentUuuids);
}
