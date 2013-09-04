package org.argeo.connect.people;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	/* ENTITIES */
	/**
	 * returns the list of predefined values for a given property or null if
	 * none has been defined.
	 */
	public Map<String, String> getMapValuesForProperty(String propertyName);

	/**
	 * Returns the corresponding entity or null if not found. id is
	 * implementation specific and might but must not be a JCR Identifier
	 * */
	public Node getEntityById(Session session, String id);

	/**
	 * Returns all entities with the given NodeType related to this entity or
	 * null if not has been found. Key for relation is implementation specific:
	 * it might be a JCR Identifier but must not.
	 * */
	public List<Node> getRelatedEntities(Node entity,
			String relatedEntitiesType);

	/* USERS */
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
