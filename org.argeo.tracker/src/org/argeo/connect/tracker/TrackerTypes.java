package org.argeo.connect.tracker;

/** Mixin types used by the Tracker App */
public interface TrackerTypes {
	// Projects
	String TRACKER_IT_PROJECT = "tracker:itProject";
	String TRACKER_PROJECT = "tracker:project";
	// A Ticket for a given project
	String TRACKER_ISSUE = "tracker:issue";
	// A comment on an issue
	String TRACKER_COMMENT = "tracker:comment";
	// A version
	String TRACKER_VERSION = "tracker:version";
	String TRACKER_MILESTONE = "tracker:milestone";
	// A component of a given project
	String TRACKER_COMPONENT = "tracker:component";
}
