package org.argeo.connect.demo.gr;

/** JCR node and property names used in GR */
public interface GrNames {

	public final static String GR_NAMESPACE = "http://www.ignfi.com/ns/gr";

	/* Parent node for all networks */
	public final static String GR_NETWORKS = "gr:networks";

	/* Common */
	public final static String GR_UUID = "gr:uuid";

	/* Site */
	public final static String GR_SITE_TYPE = "gr:siteType";
	public final static String GR_SITE_COMMENTS = "gr:siteComments";
	public final static String GR_SITE_MAIN_POINT = "gr:mainPoint";
	// public final static String GR_SITE_SECONDARY_POINT = "gr:secondaryPoint";

	/* Comment */
	public final static String GR_COMMENT_CONTENT = "gr:content";

	/* Point */
	// public final static String GR_POINT_TYPE = "gr:pointType";
	public final static String GR_WGS84_LONGITUDE = "gr:wgs84Longitude";
	public final static String GR_WGS84_LATITUDE = "gr:wgs84Latitude";
}
