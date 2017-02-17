package org.argeo.connect.resources;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Provides method interfaces to manage resources like Labels, Catalogues or Tag
 * like multi value properties in a People repository.
 * 
 * The correct instance of this interface must be acquired through the
 * peopleService.
 */
public interface ResourceService {

	/* LABEL FOR NODE TYPES AND PROPERTY NAMES MANAGEMENT */
	/**
	 * Returns a canonical English label for each of the JCR Property or node
	 * types defined in the current People Repository. If no such label is
	 * defined, returns the given itemName
	 * 
	 **/
	public String getItemDefaultEnLabel(String itemName);

	/**
	 * Returns a label for each of the JCR property name or node type defined in
	 * the current People Repository in a internationalised context. If the
	 * correct item label is not found for this language, the English label is
	 * returned. If such a label is not defined, it returns the item name.
	 * 
	 **/
	public String getItemLabel(String itemName, String langIso);

	/* PREDEFINED TEMPLATES AND CORRESPONDING CATALOGUES */
	/**
	 * Returns the predefined possible English values of a property of a given
	 * node type as defined in the current People Repository and that match the
	 * corresponding filter. For the time being, a like "%value%" is assumed.
	 * 
	 * It returns an empty list if the corresponding template is not found, if
	 * it has no property with such a name or if the filter does not match any
	 * of the predefined values.
	 * 
	 * @param session
	 * @param templateId
	 *            generally, the corresponding NodeType
	 * @param propertyName
	 * @param filter
	 * @return a <code>List<String></code> of status
	 */
	public List<String> getTemplateCatalogue(Session session,
			String templateId, String propertyName, String filter);

	/**
	 * Returns the predefined possible English values as defined in the passed
	 * template node and that match the corresponding filter. For the time
	 * being, a like "%value%" is assumed.
	 * 
	 * It returns an empty the template does not have a with such a name or if
	 * the filter does not match any of the predefined values.
	 * 
	 * @param templateNode
	 *            the template, must not be null
	 * @param propertyName
	 * @param filter
	 * @return a <code>List<String></code> of status
	 */
	public List<String> getTemplateCatalogue(Node templateNode,
			String propertyName, String filter);

	/**
	 * 
	 * @param session
	 * @param nodeType
	 * @param templateId
	 *            optional distinct id for this template
	 * @return
	 */
	public Node createTemplateForType(Session session, String nodeType,
			String templateId);

	/**
	 * 
	 * @param session
	 * @param templateId
	 *            the template ID, it is by default the nodeType of the node for
	 *            which this node is a template
	 * @return
	 */
	public Node getNodeTemplate(Session session, String templateId);

	/**
	 * Retrieves all entities which property has the given value
	 * 
	 * @param session
	 * @param entityType
	 * @param propName
	 * @param value
	 * @return
	 */
	public NodeIterator getCatalogueValueInstances(Session session,
			String entityType, String propName, String value);

	/**
	 * Change the value of a given template catalogue. It also updates this
	 * value in all business objects of the repository that have this value.
	 * 
	 * If newValue is null it will only remove old value and unmark all entities
	 * 
	 * TODO do this in a transaction and revert if the process is unsuccessful.
	 */
	public void updateCatalogueValue(Node templateNode, String taggableType,
			String propertyName, String oldValue, String newTitle);

	/* TAG LIKE PROPERTIES MANAGEMENT */
	/**
	 * @param session
	 *            with write rights
	 * @param tagId
	 *            An optional ID to differentiate distinct parent that might be
	 *            similar
	 * @param tagInstanceType
	 *            The node type of the corresponding resources. It defines the
	 *            created parent if tagId is null
	 * @param codePropName
	 *            The name of the property that provides the code in case the
	 *            string we store in the taggable multi value property is not
	 *            the label that has to be displayed
	 * @param taggableParentPath
	 *            Absolute path to parent of the taggable nodes
	 * @param taggableNodeType
	 *            Node type of the taggable nodes
	 * @param taggablePropName
	 *            Name of the multi value property of the taggable node in which
	 *            the corresponding tag is stored
	 * @return the newly created parent for tag instances
	 */
	public Node createTagLikeResourceParent(Session session, String tagId,
			String tagInstanceType, String codePropName,
			String taggableParentPath, String taggableNodeType,
			String taggablePropName);

	/**
	 * @param session
	 *            with write rights
	 * @param tagId
	 *            An optional ID to differentiate distinct parent that might be
	 *            similar
	 * @param tagInstanceType
	 *            The node type of the corresponding resources. It defines the
	 *            created parent if tagId is null
	 * @param codePropName
	 *            The name of the property that provides the code in case the
	 *            string we store in the taggable multi value property is not
	 *            the label that has to be displayed
	 * @param taggableParentPath
	 *            Absolute path to parent of the taggable nodes
	 * @param taggableNodeType
	 *            Node type of the taggable nodes
	 * @param taggablePropNames
	 *            Names of the multi value property of the taggable node in
	 *            which the corresponding tag is stored
	 * @return the newly created parent for tag instances
	 */
	public Node createTagLikeResourceParent(Session session, String tagId,
			String tagInstanceType, String codePropName,
			String taggableParentPath, String taggableNodeType,
			List<String> taggablePropNames);

	/**
	 * Retrieves the parent for tag instances that correspond to this ID or null
	 * if non has been found
	 * 
	 * @param session
	 * @param tagId
	 * @return
	 */
	public Node getTagLikeResourceParent(Session session, String tagId);

	/**
	 * Register a new tag if such a tag does not exist, does nothing otherwise.
	 * Corresponding session is not saved
	 * 
	 * Comparison is case *insensitive* and a trim() is applied on the passed
	 * String
	 * 
	 * @param session
	 * @param tagId
	 * @param tagValue
	 * @return
	 * @throws RepositoryException
	 */
	public Node registerTag(Session session, String tagId, String tagValue)
			throws RepositoryException;

	/**
	 * Register a new tag if a tag with such a code does not yet exist, returns
	 * the existing one otherwise. Corresponding session is not saved
	 * 
	 * Comparison of the code is case *sensitive*.
	 * 
	 * A trim() is applied on the passed tagValue before it is stored
	 * 
	 * TODO also make a check if a tag already has such a value and prevent
	 * creation in this case. It is not blocker yet because we only use encode
	 * tags for country and languages that should not be manually updated
	 * 
	 * @param session
	 * @param tagId
	 * @param tagCode
	 * @param tagValue
	 * @return
	 * @throws RepositoryException
	 */
	public Node registerTag(Session session, String tagId, String tagCode,
			String tagValue) throws RepositoryException;

	/**
	 * Retrieve the instance node given its value or code if it is an encodedTag
	 * or null if such a tag has not yet been registered
	 * 
	 * Comparison is case insensitive and a trim() is applied on the passed
	 * String for not encoded tag
	 */
	public Node getRegisteredTag(Session session, String tagId, String tag);

	/**
	 * Retrieves the instance node given its value or code if it is an
	 * encodedTag or null if such a tag has not yet been registered
	 * 
	 * Comparison is case insensitive and a trim() is applied on the passed
	 * String for not encoded tag
	 */
	public Node getRegisteredTag(Node tagInstanceParent, String tag);

	/**
	 * Retrieves the value of an encoded tag or null if such a tag has not yet
	 * been registered
	 */
	public String getEncodedTagValue(Session session, String tagId, String code);

	/**
	 * Retrieves the code of an encoded tag given its English value or null if
	 * no such tag exists.
	 * 
	 * WARNING: if two tags have the same value first found code will be
	 * returned see {@link registerTag(Session session, String tagId, String
	 * tagCode, String tagValue)}
	 */
	public String getEncodedTagCodeFromValue(Session session, String tagId,
			String value);

	/**
	 * Shortcut to retrieve a string that concatenates the corresponding values
	 * of a multi value property that refers to multiple encoded tags
	 * 
	 * @return
	 */
	public String getEncodedTagValuesAsString(String tagId, Node taggedNode,
			String propertyName, String separator);

	/**
	 * Change the value of an already registered tag that does not rely on a
	 * code (e.g. the value that is stored in the various corresponding
	 * multi-value property). It also updates this value in all business objects
	 * of the repository that have this tag.
	 * 
	 * TODO do this in a transaction and revert if the process is unsuccessful.
	 * TODO rather throw an exception than returning false
	 */
	public boolean updateTag(Node tagInstance, String newValue)
			throws RepositoryException;

	/**
	 * Unregister an existing tag and remove all references to this tag on all
	 * nodes under the tagableParentPath that have this tag
	 */
	public void unregisterTag(Session session, String tagId, String tag);

	/**
	 * Retrieves all entities that have this tag key
	 * 
	 * @param key
	 *            the code in case of an encoded tag, the value itself otherwise
	 * @return null if the corresponding tag parent is not found, and a node
	 *         iterator (that might be empty) otherwise
	 */
	public NodeIterator getTaggedEntities(Session session, String tagId,
			String key);

	/**
	 * Retrieves all entities that have this key
	 * 
	 * @param key
	 *            the code in case of an encoded tag, the value itself otherwise
	 */
	public NodeIterator getTaggedEntities(Node tagInstancesParent, String key);

	/**
	 * Browses the repository using the parameters stored in the tagParent node
	 * and creates a new tag instance for all new value found in the taggable
	 * property of one of the valid node. It is meant to update the tag
	 * referential after importing entity instances that are tagged.
	 * 
	 * Please note that newly created encoded tags will be created by default
	 * with a label that is also the code. For the time being, this must be
	 * later manually fixed via the system UI.
	 */
	public void refreshKnownTags(Session session, String tagId);

	/**
	 * Browses the repository using the parameters stored in the tagParent node
	 * and creates a new tag instance for all new value found in the taggable
	 * property of one of the valid node. It is meant to update the tag
	 * referential after importing entity instances that are tagged.
	 * 
	 * Please note that newly created encoded tags will be created by default
	 * with a label that is also the code. For the time being, this must be
	 * later manually fixed via the system UI.
	 */
	public void refreshKnownTags(Node tagParent);

	/**
	 * 
	 * @param session
	 * @param tagId
	 * @param filter
	 * @return the value of the known instances for this ID. Note that in the
	 *         case of an encoded tag, the returned list contains values of the
	 *         tags, not the codes but the filter applies on both (with a or)
	 */
	public List<String> getRegisteredTagValueList(Session session,
			String tagId, String filter);

	/**
	 * Count members that have such a tag in the corresponding taggable sub tree
	 */
	public long countMembers(Node tag);
	
	
	public boolean canCreateTag(Session session);

}