package org.argeo.connect;

/** JCR names. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectNames {
	final static String CONNECT_ = "connect:";

	public final static String CONNECT_AUTHOR = CONNECT_ + "author";
	public final static String CONNECT_PUBLISHED_DATE = CONNECT_
			+ "publishedDate";
	public final static String CONNECT_UPDATED_DATE = CONNECT_ + "updatedDate";
	public final static String CONNECT_SOURCE_URI = CONNECT_ + "sourceUri";

	/** Connect CRISIS */
	// Situation
	public final static String CONNECT_PRIORITY = CONNECT_ + "priority";
	public final static String CONNECT_STATUS = CONNECT_ + "status";
	public final static String CONNECT_UPDATE = CONNECT_ + "update";
	public final static String CONNECT_TEXT = CONNECT_ + "text";

	/** Connect GPS */
	// Session handling
	public final static String CONNECT_NAME = CONNECT_ + "name";
	// public final static String CONNECT_UUID = CONNECT_ + "uuid";
	public final static String CONNECT_IS_COMPLETE = CONNECT_ + "isComplete";
	public final static String CONNECT_DATE_COMPLETED = CONNECT_
			+ "dateCompleted";
	public final static String CONNECT_COMMENTS = CONNECT_ + "comments";
	public final static String CONNECT_DEFAULT_SENSOR = CONNECT_
			+ "defaultSensor";

	// Files to clean
	public final static String CONNECT_LINKED_FILE = CONNECT_ + "linkedFile";

	// Files to clean
	public final static String CONNECT_LINKED_FILE_REF = CONNECT_ + "fileRef";
	public final static String CONNECT_SENSOR_NAME = CONNECT_ + "sensorName";
	public final static String CONNECT_LINKED_FILE_NAME = CONNECT_ + "fileName";
	public final static String CONNECT_TO_BE_PROCESSED = CONNECT_
			+ "toBeProcessed";
	public final static String CONNECT_ALREADY_PROCESSED = CONNECT_
			+ "alreadyProcessed";

	// Clean parameters
	public final static String CONNECT_PARAM_NAME = CONNECT_ + "paramName";
	public final static String CONNECT_PARAM_VALUE = CONNECT_ + "paramValue";
	public final static String CONNECT_PARAM_LABEL = CONNECT_ + "paramLabel";
	public final static String CONNECT_PARAM_MIN_VALUE = CONNECT_
			+ "paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = CONNECT_
			+ "paramMaxValue";
	public final static String CONNECT_PARAM_IS_USED = CONNECT_ + "paramIsUsed";

}
