package org.argeo.connect.gpx;

import java.io.InputStream;

/** Geo-processing and geo-storage of the positions */
public interface TrackDao {
	/**
	 * Import track points from a GPX stream, filling temporary tables to be
	 * used for track cleaning.
	 */
	public Object importRawToCleanSession(String cleanSession, String sensor,
			InputStream in);

	/** Returns the path to the track speeds feature source */
	public String getTrackSpeedsSource(String cleanSession);

	/** Publish the cleaned-up positions to a positions referential. */
	public void publishCleanPositions(String cleanSession, String referential,
			String toRemoveCql);
}
