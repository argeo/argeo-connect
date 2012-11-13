package org.argeo.connect.demo.gr;

import java.io.File;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;

/** Provides method interfaces to access the repository backend */
public interface GrBackend {

	/* Queries */
	/** Creates a generic JCR_SQL2 query. */
	public Query createGenericQuery(String statement);

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

	/**
	 * Returns the current JCR session, useful to directly save nodes in the
	 * repository, by instance
	 */
	public Session getCurrentSession();

	public File getSiteReport(String siteUid);

	public File getFileFromNode(Node node);
}
