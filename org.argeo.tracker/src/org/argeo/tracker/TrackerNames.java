package org.argeo.tracker;

/** Jcr names used by the Tracker App */
public interface TrackerNames {

	/* MANAGED */
	String TRACKER_MANAGER = "tracker:manager";
	String TRACKER_DEFAULT_ASSIGNEE = "tracker:defaultAssignee";

	/* PROJECT */
	String TRACKER_PROJECTS = "projects";
	String TRACKER_CP_GROUP_ID = "tracker:cpGroupId";
	String TRACKER_DATA = "data";
	String TRACKER_SPEC = "spec";
	String TRACKER_MILESTONES = "milestones"; // was versions
	String TRACKER_COMPONENTS = "components";
	String TRACKER_ISSUES = "issues";

	/* MILESTONE */
	String TRACKER_TARGET_DATE = "tracker:targetDate"; // "office:targetDate";

	/* VERSION */
	String TRACKER_RELEASE_DATE = "office:releaseDate";

	/* ISSUE */

	String TRACKER_PARENT_UID = "tracker:parentUid";

	// FIXME migrate commented out properties
	String TRACKER_ID = "tracker:id"; // "office:id";
	String TRACKER_PROJECT_UID = "tracker:projectUid";
	String TRACKER_MILESTONE_UID = "tracker:milestoneUid"; // "office:targetId";
	@Deprecated
	String TRACKER_MILESTONE_ID = "tracker:milestoneId"; // "office:targetId";
	String TRACKER_VERSION_IDS = "tracker:versionIds"; // "office:versionIds";
	String TRACKER_COMPONENT_IDS = "tracker:componentIds"; // "office:componentIds";
	String TRACKER_COMMENTS = "comments";

	// (LONG) highest, high, normal, low, lowest
	String TRACKER_PRIORITY = "tracker:priority"; // "office:priority"
	// (LONG) highest, high, normal, low, lowest
	String TRACKER_IMPORTANCE = "tracker:importance"; // "office:importance";

	// TODO also migrate
	// - connect:closeDate (DATE) <- activities:closeDate (DATE)
	// - connect:closedBy <- activities:closedBy (STRING) // The dn of the user
	// who closed this task

	// Add a connect uid to tracker:milestone nodes

}
