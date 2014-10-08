package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	/* PATH MANAGEMENT */
	/**
	 * Centralises the management of known types to provide corresponding base
	 * path
	 * 
	 * getBasePath(null) returns the application specific parent business path
	 */
	public String getBasePath(String entityType);

	/**
	 * Provides a system specific tmp path typically for imports
	 */
	public String getTmpPath();

	/**
	 * Returns the path to a node that centralises information about the current
	 * instance
	 */
	public String getHomePath();

	/**
	 * Exposes the application specific parent path for this resource. Querying
	 * with null parameter will return the resource base bath
	 */
	public String getResourceBasePath(String resourceType);

	/**
	 * Exposes the application specific parent path for the given subtype of
	 * this resource.
	 */
	public String getResourcePath(String resourceType, String categoryId);

	/**
	 * Builds a default path for a known type based on the people:uid property
	 * of this business entity
	 */
	public String getDefaultPathForEntity(Node node, String nodeType);

	/**
	 * Builds a default path for a known type based on the people:uid property.
	 */
	public String getDefaultPathForEntity(String peopleUid, String nodeType);

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

	/**
	 * Try to save and optionally commit a business object after applying
	 * context specific rules and special behaviours (typically cache updates).
	 * 
	 * @param entity
	 * @param commit
	 *            also commit the corresponding node
	 * @throws PeopleException
	 *             If one a the rule defined for this type is not respected. Use
	 *             getMessage to display to the user if needed
	 */
	public void saveEntity(Node entity, boolean commit) throws PeopleException;

	/**
	 * Returns a display name that is app specific and that depends on one or
	 * more of the entity properties. The user can always set a flag to force
	 * the value to something else.
	 * 
	 * The Display name is usually stored in the JCR_TITLE property.
	 */
	public String getDisplayName(Node entity);

	/* CONTEXT SERVICES */
	/** Returns the corresponding {@link ActivityService} */
	public ActivityService getActivityService();

	/** Returns the corresponding {@link ContactService} */
	public ContactService getContactService();

	/** Returns the corresponding {@link UserManagementService} */
	public UserManagementService getUserManagementService();

	/** Returns the corresponding {@link TagService} */
	public TagService getTagService();

	/** Returns the corresponding {@link LebelService} */
	public LabelService getLabelService();

	/* MISCELLANEOUS */
	/**
	 * Retrieves a context specific property used to configure the current
	 * system
	 */
	public String getConfigProperty(String key);

}
