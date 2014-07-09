package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/** Provides method interfaces to manage a people repository */
public interface PeopleService {

	/* BASE PATHS MANAGEMENT */
	/**
	 * Centralises the management of known types to provide corresponding base
	 * path
	 * 
	 * getBasePath(null) returns the application specific parent business path
	 */
	public String getBasePath(String entityType);

	/**
	 * provide a system specific tmp path typically for imports
	 */
	public String getTmpPath();

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
	 * @param session
	 * @param tagableParentPath
	 *            the path to the business parent node of all nodes that are
	 *            tagables and that already have some tags set
	 * @param tagParentPath
	 *            the path to the parent node of all cache nodes
	 */
	public void refreshKnownTags(Session session, String tagResourceType,
			String tagParentPath, String tagableNodeType,
			String tagableParentPath);

	/**
	 * Updates the repository cache that list all tags known in the current
	 * application using default values for path to parent nodes.
	 * 
	 */
	public void refreshKnownTags(Session session);

	/**
	 * Register a new tag if such a tag does not exist, does nothing otherwise.
	 * Corresponding session is not saved
	 * 
	 * Comparison is case insensitive and a trim() is applied on the passed
	 * String
	 */
	public Node registerTag(Session session, String resourceType,
			String tagParentPath, String tag) throws RepositoryException;

	/**
	 * Retrieve the cached tag node or null if such a tag has not yet been
	 * registered
	 * 
	 * Comparison is case insensitive and a trim() is applied on the passed
	 * String
	 */
	public Node getRegisteredTag(Session session, String tagParentPath,
			String tag);

	/**
	 * Unregister an existing tag and remove all references to this tag on all
	 * nodes under the tagableParentPath that have this tag
	 */
	public void unregisterTag(Session session, String tagParentPath,
			String tag, String tagableParentPath);
}
