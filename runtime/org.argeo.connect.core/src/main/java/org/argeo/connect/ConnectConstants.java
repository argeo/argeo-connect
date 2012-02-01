package org.argeo.connect;

/** Constants used across the framework and applications */
public interface ConnectConstants {
	/** Namespace prefix */
	public final static String CONNECT_ = "connect:";

	/** PATHS */
	public final static String CONNECT_TECHNICAL_BASE_PATH = "/.connect";
	public final static String CONNECT_BASE_PATH = "/connect";
	public final static String TRACK_SESSIONS_PARENT_PATH = CONNECT_TECHNICAL_BASE_PATH
			+ "/importTrackSessions";
	public final static String LOCAL_REPO_PARENT_PATH = CONNECT_TECHNICAL_BASE_PATH
			+ "/localRepositories";
	public final static String GPX_FILE_DIR_PATH = CONNECT_BASE_PATH + "/gpx";

	// Possible parameters for GPS clean
	public final static String CONNECT_PARAM_SPEED_MAX = "maxSpeed";
	public final static String CONNECT_PARAM_ROTATION_MAX = "maxRotation";
	public final static String CONNECT_PARAM_ACCELERATION_MAX = "maxAcceleration";

}
