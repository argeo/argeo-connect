package org.argeo.connect;

public interface ConnectNames {
	public final static String CONNECT_ = "connect:";

	public final static String CONNECT_AUTHOR = CONNECT_ + "author";
	public final static String CONNECT_PUBLISHED_DATE = CONNECT_
			+ "publishedDate";
	public final static String CONNECT_UPDATED_DATE = CONNECT_ + "updatedDate";
	public final static String CONNECT_SOURCE_URI = CONNECT_ + "sourceUri";

	/** Connect CRISIS */
	// situation
	public final static String CONNECT_PRIORITY = CONNECT_ + "priority";
	public final static String CONNECT_STATUS = CONNECT_ + "status";
	public final static String CONNECT_UPDATE = CONNECT_ + "update";
	public final static String CONNECT_TEXT = CONNECT_ + "text";

	/** Connect GPS */
	// Session handling
	public final static String CONNECT_NAME = CONNECT_ + "name";
	// public final static String CONNECT_UUID = CONNECT_ + "uuid";
	public final static String CONNECT_IS_COMPLETE = CONNECT_ + "iscomplete";
	public final static String CONNECT_DATE_COMPLETED = CONNECT_
			+ "datecompleted";
	public final static String CONNECT_COMMENTS = CONNECT_ + "comments";
	public final static String CONNECT_DEFAULT_SENSOR = CONNECT_
			+ "defaultsensor";

	// Files to clean
	public final static String CONNECT_SESSION_LINKED_FILE = CONNECT_
			+ "linkedfiles";
	public final static String CONNECT_LINKED_FILE_REF = CONNECT_ + "fileref";
	public final static String CONNECT_SENSOR_NAME = CONNECT_ + "sensorname";
	public final static String CONNECT_LINKED_FILE_NAME = CONNECT_ + "filename";
	public final static String CONNECT_TO_BE_PROCESSED = CONNECT_
			+ "tobeprocessed";
	public final static String CONNECT_ALREADY_PROCESSED = CONNECT_
			+ "alreadyprocessed";

	// clean parameters
	public final static String CONNECT_PARAM_NAME = CONNECT_ + "paramName";
	public final static String CONNECT_PARAM_VALUE = CONNECT_ + "paramValue";
	public final static String CONNECT_PARAM_LABEL = CONNECT_ + "paramLabel";
	public final static String CONNECT_PARAM_MIN_VALUE = CONNECT_
			+ "paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = CONNECT_
	+ "paramMaxValue";
	public final static String CONNECT_PARAM_IS_USED = CONNECT_
	+ "paramIsUsed";

	// Possible parameters
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
