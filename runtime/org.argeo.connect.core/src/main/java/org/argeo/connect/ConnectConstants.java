package org.argeo.connect;

/** Constants used across the framework and applications */
public interface ConnectConstants {
	/** Namespace prefix */
	public final static String CONNECT_ = "connect:";

	// Possible parameters for GPS clean
	public final static String CONNECT_PARAM_SPEED_MIN = CONNECT_ + "speedMin";
	public final static String CONNECT_PARAM_SPEED_MAX = CONNECT_ + "speedMax";
	public final static String CONNECT_PARAM_RADIAL_SPEED_MIN = CONNECT_
			+ "radialSpeedMin";
	public final static String CONNECT_PARAM_RADIAL_SPEED_MAX = CONNECT_
			+ "radialSpeedMax";
	public final static String CONNECT_PARAM_ACCELERATION_MIN = CONNECT_
			+ "accelerationMin";
	public final static String CONNECT_PARAM_ACCELERATION_MAX = CONNECT_
			+ "accelerationMax";

}
