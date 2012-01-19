package org.argeo.connect.gpx;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;

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
	
	/** Initialises local repository if needed */ 
	public boolean initialiseLocalRepository(Session curSession);
	
	/** Returns parent node for clean track sessions or null if the repository has not been initialized yet */
	public Node getTrackSessionsParentNode(Session curSession);
	
	/** Returns parent node for local cleaned data or null if the repository has not been initialized yet */
	public Node getLocalRepositoriesParentNode(Session curSession);
	
	/** Returns parent node to store GPX files or null if the repository has not been initialized yet */
	public Node getGpxFilesDirectory(Session curSession);
}
