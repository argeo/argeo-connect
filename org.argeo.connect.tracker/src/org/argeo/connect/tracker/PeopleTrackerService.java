package org.argeo.connect.tracker;

import org.argeo.connect.people.PeopleService;

public interface PeopleTrackerService extends PeopleService {
	public TrackerService getTrackerService();
}
