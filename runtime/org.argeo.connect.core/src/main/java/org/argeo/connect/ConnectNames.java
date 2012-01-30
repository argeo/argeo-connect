package org.argeo.connect;

/** JCR names. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectNames {

	public final static String CONNECT_NAMESPACE = "http://www.argeo.org/ns/connect";

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
	public final static String CONNECT_IS_SESSION_COMPLETE = CONNECT_
			+ "isSessionComplete";
	public final static String CONNECT_DEFAULT_SENSOR = CONNECT_
			+ "defaultSensor";
	public final static String CONNECT_LOCAL_REPO_NAME = CONNECT_
			+ "localRepoName";

	// Files to clean
	public final static String CONNECT_LINKED_FILE = CONNECT_ + "linkedFile";

	// Imported files
	public final static String CONNECT_LINKED_FILE_REF = CONNECT_ + "fileRef";
	public final static String CONNECT_SENSOR_NAME = CONNECT_ + "sensorName";
	public final static String CONNECT_TO_BE_PROCESSED = CONNECT_
			+ "toBeProcessed";
	public final static String CONNECT_ALREADY_PROCESSED = CONNECT_
			+ "alreadyProcessed";
	public final static String CONNECT_SEGMENT_UUID =  "connect:segmentUuid";
	
	// Clean parameters
	public final static String CONNECT_PARAM_VALUE = CONNECT_ + "paramValue";
	public final static String CONNECT_PARAM_MIN_VALUE = CONNECT_
			+ "paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = CONNECT_
			+ "paramMaxValue";
	public final static String CONNECT_PARAM_IS_USED = CONNECT_ + "paramIsUsed";
}