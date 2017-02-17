package org.argeo.connect.people.core;

import org.argeo.connect.people.MaintenanceService;
import org.argeo.connect.people.PeopleService;

/**
 * Default implementation of the maintenance and monitoring services in a People
 * repository
 */
public class MaintenanceServiceImpl implements MaintenanceService {

	private String pathToRepository = System.getProperty("user.dir");

	// private PeopleService peopleService;

	public MaintenanceServiceImpl(PeopleService peopleService) {
		// this.peopleService = peopleService;
	}
	
	@Override
	public String getMonitoringLogFolderPath(){
		return pathToRepository + "/log/monitoring";
	}
}
