package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	// TODO Move this to a film service
	/**
	 * Creates or update a participation of a person or an org to a film
	 * 
	 * @param oldParticipation
	 * @param film
	 * @param contact
	 * @param role
	 * @return
	 */
	public Node createOrUpdateParticipation(Node oldParticipation, Node film,
			Node contact, String role);

	/* BASE PATHS MANAGEMENT */

	/** Exposes the application specific parent business path */
	public String getBasePath();

	/**
	 * Exposes the application specific parents path for resources. querying
	 * with null parameter will return the respource base bath
	 */
	public String getResourcesBasePath(String typeId);

	/**
	 * Centralises the management of known types to provide corresponding base
	 * path
	 */
	public String getBasePathForType(String typeId);

	/* PERSONS AND ORGANISATIONS */

	/**
	 * 
	 * Creates or update a job of a person in an organisation
	 * 
	 * @param oldJob
	 *            null if creation
	 * @param person
	 *            cannot be null
	 * @param organisation
	 *            cannot be null
	 * @param position
	 *            can be null
	 * @param department
	 *            can be null
	 * @param isPrimary
	 *            pass false by default
	 * @return
	 */
	public Node createOrUpdateJob(Node oldJob, Node person, Node organisation,
			String position, String department, boolean isPrimary);

	/* GENERIC */
	// /**
	// * returns the list of predefined values for a given property or null if
	// * none has been defined.
	// */
	// public Map<String, String> getMapValuesForProperty(String propertyName);

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
	 * on the two object we want to link together
	 * */
	public Node createEntityReference(Node referencingNode,
			Node referencedNode, String role);

	/**
	 * Returns all entities with the given NodeType related to this entity or
	 * null if not has been found. Key for relation is implementation specific:
	 * it might be a JCR Identifier but must not.
	 * 
	 * @param relatedEntityType
	 *            Optionally, the type of the grand-parent node typically to
	 *            choose between an organisation, a group or a person in a group
	 * */
	public List<Node> getRelatedEntities(Node entity, String linkNodeType,
			String relatedEntityType);

	/* MISCELLANEOUS */

	/** Returns the JCR repository used by this service */
	// public Repository getRepository();

	/** Returns the corresponding {@link ActivityService} */
	public ActivityService getActivityService();

	/** Returns the corresponding {@link UserManagementService} */
	public UserManagementService getUserManagementService();

	/* TAG MANAGEMENT */
	/**
	 * Updates the repository cache that list all tags known in the current
	 * application
	 * 
	 * TODO make it asynchronous
	 */
	public void refreshKnownTags(Node tagsParentNode, Node tagableParentNode);

	public void addTag(Node tagsParentNode, String tag);

	public void removeTag(Node tagsParentNode, String tag);
}
