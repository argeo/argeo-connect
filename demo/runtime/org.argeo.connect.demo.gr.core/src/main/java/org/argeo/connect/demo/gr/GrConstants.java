package org.argeo.connect.demo.gr;

/** Constants used across the application. */
public interface GrConstants {

	/*
	 * NAMESPACES
	 */
	/** GR prefix (gr:) */

	/*
	 * PATHS
	 */
	/** Base path for all GR specific nodes */
	public final static String GR_BASE_PATH = "/com/ignfi/gr/";

	/* NODES METADATA */
	// TODO : it mights not be the cleanest way to access JCR NODES UID
	// public final static String GR_NODE_UID = "gr:nodeUid";
	// public final static String GR_NODE_NAME = "gr:nodeName";

	/*
	 * USER ROLES
	 */
	public final static Integer ROLE_CONSULTANT = 0;
	public final static Integer ROLE_MANAGER = 1;
	public final static Integer ROLE_ADMIN = 2;

	/*
	 * MISCEALLENEOUS
	 */
	public final static String DATE_FORMAT = "dd/MM/yyyy";
	public final static String DATE_TIME_FORMAT = "dd/MM/yyyy, HH:mm";

	/* SITE TYPES */
	public final static String NATIONAL = "national";
	public final static String BASE = "base";
	public final static String NORMAL = "normal";

	/* POINT TYPES */
	public final static String MAIN = "main";
}
