package org.argeo.tracker;

/** Jcr names used by the Tracker App */
public interface TrackerNames {

	/* PROJECT */
	String TRACKER_PROJECTS = "projects";
	String TRACKER_CP_GROUP_ID = "tracker:cpGroupId";
	String TRACKER_DATA = "data";
	String TRACKER_SPEC = "spec";
	String TRACKER_VERSIONS = "versions";
	String TRACKER_COMPONENTS = "components";
	String TRACKER_ISSUES = "issues";

	/* ISSUE & VERSION */
	String TRACKER_ID = "tracker:id"; // "office:id";
	String TRACKER_PROJECT_UID = "tracker:projectUid";
	String TRACKER_TARGET_ID = "tracker:milestoneId"; // "office:targetId";
	String TRACKER_VERSION_IDS = "tracker:versionIds"; // "office:versionIds";
	String TRACKER_COMPONENT_IDS = "tracker:componentIds"; // "office:componentIds";
	String TRACKER_COMMENTS = "comments";

	// (LONG) highest, high, normal, low, lowest
	String TRACKER_PRIORITY = "office:priority";
	// (LONG) highest, high, normal, low, lowest
	String TRACKER_IMPORTANCE = "office:importance";
	String TRACKER_TARGET_DATE = "office:targetDate";
	String TRACKER_RELEASE_DATE = "office:releaseDate";
}
