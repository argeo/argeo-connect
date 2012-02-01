package org.argeo.connect;

/** JCR names. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectNames {

	public final static String CONNECT_NAMESPACE = "http://www.argeo.org/ns/connect";

		public final static String CONNECT_AUTHOR = "connect:author";
	public final static String CONNECT_PUBLISHED_DATE = "connect:publishedDate";
	public final static String CONNECT_UPDATED_DATE = "connect:updatedDate";
	public final static String CONNECT_SOURCE_URI = "connect:sourceUri";

	/** Connect CRISIS */
	// Situation
	public final static String CONNECT_PRIORITY = "connect:priority";
	public final static String CONNECT_STATUS = "connect:status";
	public final static String CONNECT_UPDATE = "connect:update";
	public final static String CONNECT_TEXT = "connect:text";

	/** Connect GPS */
	// Session handling
	public final static String CONNECT_IS_SESSION_COMPLETE = "connect:isSessionComplete";
	public final static String CONNECT_DEFAULT_SENSOR = "connect:defaultSensor";
	public final static String CONNECT_DEFAULT_DEVICE = "connect:defaultDevice";
	public final static String CONNECT_LOCAL_REPO_NAME = "connect:localRepoName";

	// Files to clean
	public final static String CONNECT_LINKED_FILE = "connect:linkedFile";

	// Imported files
	public final static String CONNECT_LINKED_FILE_REF = "connect:fileRef";
	public final static String CONNECT_SENSOR_NAME = "connect:sensorName";
	public final static String CONNECT_DEVICE_NAME = "connect:deviceName";
	public final static String CONNECT_TO_BE_PROCESSED = "connect:toBeProcessed";
	public final static String CONNECT_ALREADY_PROCESSED = "connect:alreadyProcessed";
	public final static String CONNECT_SEGMENT_UUID =  "connect:segmentUuid";
	
	// Clean parameters
	public final static String CONNECT_PARAM_VALUE = "connect:paramValue";
	public final static String CONNECT_PARAM_MIN_VALUE = "connect:paramMinValue";
	public final static String CONNECT_PARAM_MAX_VALUE = "connect:paramMaxValue";
	public final static String CONNECT_PARAM_IS_USED = "connect:paramIsUsed";
}