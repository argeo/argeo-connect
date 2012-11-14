package org.argeo.connect.demo.gr;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Repository;

/** Provides method interfaces to access the repository backend */
public interface GrBackend {
	/* Business objects */
	/** Create a new network node of the given name */
	public Node createNetwork(Node parent, String name);

	/* Application wide lists */
	public List<String> getSiteTypes();

	/* Users */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(Integer userRole);

	/** returns the current user ID **/
	public String getCurrentUserId();

	/** Returns a human readable display name using the user ID */
	public String getUserDisplayName(String userId);

	/** Returns the JCR repository used by this backend */
	public Repository getRepository();
}
