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
	public final static String CONNECT_UUID = CONNECT_ + "uuid";
	public final static String CONNECT_SESSION_STATUS = CONNECT_
			+ "sessionStatus";
	public final static String CONNECT_COMPLETED = CONNECT_ + "completed";
	public final static String CONNECT_COMMENTS = CONNECT_ + "comments";
	public final static String CONNECT_DEFAULT_SENSOR = CONNECT_
			+ "defaultSensor";

	// Files to clean

	// clean parameters
	public final static String CONNECT_PARAMETER_NAME = CONNECT_ + "paramName";
	public final static String CONNECT_PARAM_VALUE = CONNECT_ + "paramValue";
	public final static String CONNECT_PARAM_DEFAULT_VALUE = CONNECT_
			+ "paramDefaultValue";
	public final static String CONNECT_PARAM_MIN_VALUE = CONNECT_
			+ "paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = CONNECT_
			+ "paramMaxValue";

}
