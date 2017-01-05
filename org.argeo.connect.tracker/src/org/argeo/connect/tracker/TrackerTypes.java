package org.argeo.connect.tracker;

/** Types used by the tracker App */
public interface TrackerTypes {
	// A project
	String TRACKER_PROJECT = "tracker:project";
	// A Ticket for a given project
	String TRACKER_ISSUE = "tracker:issue";
	// A comment on an issue
	String TRACKER_COMMENT = "tracker:comment";
	// A version
	String TRACKER_VERSION = "tracker:version";
	// A component of a given project
	String TRACKER_COMPONENT = "tracker:component";
}
