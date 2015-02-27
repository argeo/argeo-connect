package org.argeo.connect.people;

/** Provides method interfaces to maintain and monitor a people repository */
public interface MaintenanceService {

	/**
	 * Centralize the definition of the path used to store the monitoring log
	 * files.
	 */
	public String getMonitoringLogFolderPath();

}
