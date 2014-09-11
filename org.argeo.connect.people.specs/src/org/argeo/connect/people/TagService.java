package org.argeo.connect.people;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Provides method interfaces to manage Tag like multi value properties on
 * various objects of a people repository.
 * 
 * The correct instance of this interface must be acquired through the
 * peopleService.
 */
public interface TagService {

	/**
	 * Retrieve all entities that have this tag under the given parentPath
	 * 
	 * @param session
	 * @param nodeType
	 * @param parentPath
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public NodeIterator getTaggedEntities(Session session, String nodeType,
			String parentPath, String propertyName, String value);

	/**
	 * @param session
	 *            with write rights
	 * @param resourceNodeType
	 *            The nodeType of the cached instance if needed (used for
	 *            instance for the mailing lists)
	 * @param resourceInstancesParentPath
	 *            the application specific path to the parent of all cached
	 *            instance for this tag-like property
	 * @param taggableNodeType
	 *            the NodeType of the taggable object for instance people:base
	 * @param tagPropName
	 *            the multi-value property of the taggable node: for instance
	 *            people:tags
	 * @param taggableParentPath
	 *            reduce search of taggable objects in a sub tree of the
	 *            repository
	 */
	public void refreshKnownTags(Session session, String resourceNodeType,
			String resourceInstancesParentPath, String taggableNodeType,
			String tagPropName, String taggableParentPath);

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

	/**
	 * Change the title (e.g. the value that is stored in the various
	 * corresponding multi-value property) of an already registered tag. It also
	 * updates this value in all business objects of the repository that have
	 * this tag.
	 * 
	 * TODO do this in a transaction and revert if the process is unsuccessful.
	 * 
	 */
	public boolean updateTagTitle(Node tagInstance, String resourceType,
			String tagParentPath, String tag, String taggableNodeType,
			String tagPropName, String taggableParentPath)
			throws RepositoryException;

}
