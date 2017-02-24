package org.argeo.connect.tracker;

/** Types used by the tracker App */
public interface TrackerTypes {
	// A project
	String TRACKER_PROJECT = "tracker:project";
	String TRACKER_IT_PROJECT = "tracker:itProject";
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
