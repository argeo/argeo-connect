package org.argeo.connect;

import javax.jcr.Session;

/** Define API to manage the life cycle of a Connect App */
public interface AppMaintenanceService {

	/**
	 * Creates the base JCR tree structure expected for this app if necessary.
	 * 
	 * Expects a clean session ({@link Session#hasPendingChanges()} should
	 * return false) and saves it once the changes have been done. Thus the
	 * session can be rolled back if an exception occurs.
	 * 
	 * @return true if something as been updated
	 */
	public boolean prepareJcrTree(Session session);

	/**
	 * Adds app specific default privileges.
	 * 
	 * Expects a clean session ({@link Session#hasPendingChanges()} should
	 * return false} and saves it once the changes have been done. Thus the
	 * session can be rolled back if an exception occurs.
	 * 
	 * Warning: no check is done and corresponding privileges are always added,
	 * so only call this when necessary
	 */
	public void configurePrivileges(Session session);

	// public void importResources(Session session, Map<String, URI> resources);
	//
	// public void importData(Session session, URI uri, Map<String, URI>
	// dataSources);
	//
	// default public void doBackup(Session session, URI uri, Object resource) {
	// }
}
