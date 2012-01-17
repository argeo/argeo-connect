package org.argeo.connect.gpx;

import java.io.InputStream;

/** Low level access to the geo-storage. */
public interface TrackDao {
	/**
	 * Import track points from a GPX stream, filling temporary tables to be
	 * used for track cleaning.
	 */
	public Object importTrackPoints(String source, String sensor, InputStream in);

	/** Returns the path to the track speeds feature source */
	public String getTrackSpeedsSource();

	/** Import the cleaned-up positions to the core repository */
	public void importCleanPositions(String source, String toRemoveCql);
}
