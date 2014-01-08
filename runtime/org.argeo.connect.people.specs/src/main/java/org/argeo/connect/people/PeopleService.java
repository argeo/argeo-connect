package org.argeo.connect.people;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	/* ENTITIES */
	/**
	 * returns the list of predefined values for a given property or null if
	 * none has been defined.
	 */
	public Map<String, String> getMapValuesForProperty(String propertyName);

	/**
	 * Returns the corresponding entity or null if not found. Uid is
	 * implementation specific and is not a JCR Identifier
	 * */
	public Node getEntityByUid(Session session, String uid);

	/**
	 * Returns the corresponding entity or null if not found using the UID that
	 * is stored under propName.
	 * */
	public Node getEntityFromNodeReference(Node node, String propName);

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link togeteher
	 * */
	public Node createEntityReference(Node referencingNode,
			Node referencedNode, String role);

	/**
	 * Returns all entities with the given NodeType related to this entity or
	 * null if not has been found. Key for relation is implementation specific:
	 * it might be a JCR Identifier but must not.
	 * 
	 * @param relatedEntityType
	 *            optionaly, the type of the grand-parent node typically to
	 *            choose between an organisation, a group or a person in a group
	 * */
	public List<Node> getRelatedEntities(Node entity, String linkNodeType,
			String relatedEntityType);

	/* USERS */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(String userRole);

	/** returns the current user ID **/
	public String getCurrentUserId();

	/** Returns a human readable display name using the user ID */
	public String getUserDisplayName(String userId);

	/* MISCELLANEOUS */

	/** Returns the JCR repository used by this service */
	public Repository getRepository();
}
