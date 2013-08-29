package org.argeo.connect.people;

import javax.jcr.Repository;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	/* Users */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(Integer userRole);

	/** returns the current user ID **/
	public String getCurrentUserId();

	/** Returns a human readable display name using the user ID */
	public String getUserDisplayName(String userId);

	/* MISCELLANEOUS */

	/** Returns the JCR repository used by this service */
	public Repository getRepository();
}
