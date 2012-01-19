package org.argeo.connect;

/** JCR types. see src/main/resources/org/argeo/connect/connect.cnd */
public interface ConnectTypes {
	final static String CONNECT_ = "connect:";

	/** Connect Crisis */
	public final static String CONNECT_SYND_FEED = CONNECT_ + "syndFeed";
	public final static String CONNECT_SYND_ENTRY = CONNECT_ + "syndEntry";

	/** Connect GPS */
	// root nodes
	public final static String CONNECT_LOCAL_REPOSITORIES = CONNECT_
			+ "localRepositories";
	public final static String CONNECT_SESSION_REPOSITORY = CONNECT_
			+ "sessionRepository";
	public final static String CONNECT_FILE_REPOSITORY = CONNECT_
			+ "fileRepository";
	
	// business objects
	public final static String CONNECT_LOCAL_REPOSITORY = CONNECT_
			+ "localRepository";
	public final static String CONNECT_CLEAN_TRACK_SESSION = CONNECT_
			+ "cleanTrackSession";
	public final static String CONNECT_CLEAN_PARAMETER = CONNECT_
			+ "cleanParameter";
	public final static String CONNECT_FILE_TO_IMPORT = CONNECT_
			+ "fileToImport";

}
