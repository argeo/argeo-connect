package org.argeo.connect.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.jcr.JcrMonitor;

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

	/** Provides a system specific tmp path typically for imports */
	public String getTmpPath();

//	/**
//	 * Provides a system specific public path typically for CMS or users with
//	 * limited access rights
//	 */
//	public String getPublicPath();

	// /**
	// * Returns the path to a node that centralises information about the
	// current
	// * instance
	// */
	// public String getInstanceConfPath();

	// /**
	// * Exposes the application specific parent path for this resource.
	// Querying
	// * with null parameter will return the resource base bath
	// */
	// public String getResourceBasePath(String resourceType);

	// /**
	// * Exposes the application specific parent path for the given subtype of
	// * this resource.
	// */
	// public String getResourcePath(String resourceType, String categoryId);

	/**
	 * Builds a default path for a known type based on the people:uid property
	 * of this business entity
	 */
	public String getDefaultPath(String nodeType, Node node);

	/**
	 * Builds a default path for a known type based on the people:uid property.
	 */
	public String getDefaultPath(String nodeType, String peopleUid);

	/**
	 * Typically used to move temporary import nodes to the main business
	 * repository. Returns the new node: warning if a move happened, the node
	 * has been copied and the returned node is not the same as the passed one
	 */
	public Node checkPathAndMoveIfNeeded(Node entity, String entityNodeType) throws RepositoryException;

	/* GENERIC */
	/**
	 * Try to save and optionally publish a business object after applying
	 * context specific rules and special behaviours (typically cache updates).
	 * 
	 * @return the entity that has been saved (and optionally published): note
	 *         that is some cases (typically, the first save of a draft node in
	 *         the business sub tree) the returned node is not the same as the
	 *         one that has been passed
	 * @param entity
	 * @param publish
	 *            also publish the corresponding node
	 * @throws PeopleException
	 *             If one a the rule defined for this type is not respected. Use
	 *             getMessage to display to the user if needed
	 */
	public Node saveEntity(Node entity, boolean publish) throws PeopleException;

	/**
	 * Searches the workspace corresponding to the passed session under the
	 * default base path. It returns the corresponding entity or null if none
	 * has been found. This UID is implementation specific and is not a JCR
	 * Identifier
	 */
	public Node getEntityByUid(Session session, String uid);

	/**
	 * Searches the workspace corresponding to the passed session. It returns
	 * the corresponding entity or null if none has been found. This UID is
	 * implementation specific and is not a JCR Identifier.
	 * 
	 * It will throw a PeopleException if more than one item with this ID has
	 * been found
	 */
	public Node getEntityByUid(Session session, String parentPath, String uid);

	/**
	 * Returns the corresponding people entity using the People UID that is
	 * stored under propName. Returns null if the property is undefined or if
	 * there is no entity with this people UID
	 */
	public Node getEntityFromNodeReference(Node node, String propName);

	/**
	 * Returns a display name that is app specific and that depends on one or
	 * more of the entity properties. The user can always set a flag to force
	 * the value to something else.
	 * 
	 * The Display name is usually stored in the JCR_TITLE property.
	 */
	public String getDisplayName(Node entity);

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link together
	 */
	public Node createEntityReference(Node referencingNode, Node referencedNode, String role);

	/**
	 * Returns all entities with the given NodeType related to this entity or
	 * null if none has been found. Key for relation is implementation specific:
	 * it might be a JCR Identifier but must not.
	 * 
	 * @param relatedEntityType
	 *            Optionally, the type of the grand-parent node typically to
	 *            choose between an organisation, a group or a person in a group
	 */
	public List<Node> getRelatedEntities(Node entity, String linkNodeType, String relatedEntityType);

	/* MISCELLANEOUS */
//	/**
//	 * Retrieves a context specific property used to configure the current
//	 * system
//	 */
//	public String getConfigProperty(String key);

	/**
	 * Simply look for primary information and update primary cache if needed
	 */
	public void updatePrimaryCache(Node entity) throws PeopleException, RepositoryException;

	/**
	 * Use with caution. Publishes all versionable Nodes that are in this
	 * workspace and in a "checked out" state.
	 */
	public long publishAll(Session session, JcrMonitor monitor);

	/* EXPOSE THE VARIOUS BUSINESS SERVICES */
	/** Returns the corresponding {@link PersonService} */
	public PersonService getPersonService();

	// /** Returns the corresponding {@link ActivityService} */
	// public ActivityService getActivityService();

	/** Returns the corresponding {@link ContactService} */
	public ContactService getContactService();

	// /** Returns the corresponding {@link UserAdminService} */
	// public UserAdminService getUserAdminService();

	// /** Returns the corresponding {@link ResourceService} */
	// public ResourceService getResourceService();

	// /** Returns the corresponding {@link ImportService} */
	// public ImportService getImportService();
	//
	// /** Returns the corresponding {@link MaintenanceService} */
	// public MaintenanceService getMaintenanceService();
}
