package org.argeo.connect.people.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people {@link ResourceService} */
public class ResourceServiceImpl implements ResourceService {
	private final static Log log = LogFactory.getLog(ResourceServiceImpl.class);

	private PeopleService peopleService;

	public ResourceServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public void initialiseResources(Session adminSession)
			throws RepositoryException {
		Node resourceParent = JcrUtils.mkdirs(adminSession,
				peopleService.getBasePath(PeopleConstants.PEOPLE_RESOURCE));
		JcrUtils.mkdirs(resourceParent,
				PeopleConstants.PEOPLE_RESOURCE_TEMPLATE,
				NodeType.NT_UNSTRUCTURED);
		JcrUtils.mkdirs(resourceParent,
				PeopleConstants.PEOPLE_RESOURCE_TAG_LIKE,
				NodeType.NT_UNSTRUCTURED);
	}

	/* LABEL FOR NODE TYPES AND PROPERTY NAMES */

	// Maps the used item types with some English labels.
	// TODO internationalise and generalise.
	private final static Map<String, String> PEOPLE_ITEM_LABELS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(PeopleTypes.PEOPLE_PERSON, "Person");
			put(PeopleTypes.PEOPLE_ORG, "Organisation");
			put(PeopleTypes.PEOPLE_MAILING_LIST, "Mailing list");
			put(PeopleTypes.PEOPLE_GROUP, "Group");
			put(PeopleTypes.PEOPLE_TASK, "Task");
		}
	};

	@Override
	public String getItemDefaultEnLabel(String itemName) {
		if (PEOPLE_ITEM_LABELS.containsKey(itemName))
			return PEOPLE_ITEM_LABELS.get(itemName);
		else
			return itemName;
	}

	@Override
	public String getItemLabel(String itemName, String langIso) {
		log.warn("Item label retrieval is not yet internationnalized. "
				+ "Returning english default label for " + itemName
				+ " instead.");
		return getItemDefaultEnLabel(itemName);
	}

	/* TEMPLATES AND CORRESPONDING CATALOGUES */
	@Override
	public List<String> getTemplateCatalogue(Session session,
			String templateId, String propertyName, String filter) {
		Node node = getNodeTemplate(session, templateId);
		if (node != null)
			return getTemplateCatalogue(node, propertyName, filter);
		else
			// we have the contract to always return a list, even empty
			return new ArrayList<String>();
	}

	@Override
	public List<String> getTemplateCatalogue(Node node, String propertyName,
			String filter) {
		List<String> result = new ArrayList<String>();
		try {
			if (node.hasProperty(propertyName)) {
				Value[] values = node.getProperty(propertyName).getValues();
				for (Value value : values) {
					String curr = value.getString();
					if (isEmpty(filter)
							|| notEmpty(curr)
							&& curr.toLowerCase()
									.contains(filter.toLowerCase()))
						result.add(curr);
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get " + propertyName
					+ " values for template " + node, re);
		}
		return result;
	}

	@Override
	public Node getNodeTemplate(Session session, String templateId) {
		String path = getPathForId(PeopleConstants.RESOURCE_TYPE_ID_TEMPLATE,
				templateId);
		try {
			if (session.nodeExists(path)) {
				return session.getNode(path);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve template instance "
					+ "at path " + path + " for templateId " + templateId, e);
		}
		return null;
	}

	@Override
	public Node createTemplateForType(Session session, String nodeType,
			String templateId) {
		String currId = templateId == null ? nodeType : templateId;
		Node node = getNodeTemplate(session, currId);
		if (node != null)
			return node;
		else {
			String path = getPathForId(
					PeopleConstants.RESOURCE_TYPE_ID_TEMPLATE, currId);
			String parPath = JcrUtils.parentPath(path);
			try {
				if (!session.nodeExists(parPath))
					throw new PeopleException(
							"Default tree structure "
									+ "for template resources must have been created. "
									+ "Please fix this before trying to create template "
									+ currId);
				Node parent = session.getNode(parPath);
				Node template = parent.addNode(currId,
						PeopleTypes.PEOPLE_NODE_TEMPLATE);
				template.setProperty(PeopleNames.PEOPLE_TEMPLATE_ID, currId);
				return template;
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to create new temaple "
						+ currId + " at path " + path, e);
			}
		}
	}

	@Override
	public NodeIterator getCatalogueValueInstances(Session session,
			String entityType, String propName, String value) {
		try {
			QueryManager qm = session.getWorkspace().getQueryManager();
			Query query = qm.createQuery("select * from [" + entityType
					+ "] as nodes where [" + propName + "]='" + value + "'"
					+ " ", Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException ee) {
			throw new PeopleException(
					"Unable to retrieve catalogue value instances for type "
							+ entityType + " and property " + propName
							+ " for value " + value, ee);
		}
	}

	@Override
	public void updateCatalogueValue(Node templateNode, String taggableType,
			String propertyName, String oldValue, String newValue) {
		try {

			if (isEmpty(oldValue))
				throw new PeopleException("Old value cannot be empty");

			Value[] values = templateNode.getProperty(propertyName).getValues();
			List<String> newValues = new ArrayList<String>();

			for (Value value : values) {
				String currValStr = value.getString();
				if (oldValue.equals(currValStr)) {
					if (notEmpty(newValue))
						newValues.add(newValue);
				} else
					newValues.add(currValStr);
			}
			templateNode.setProperty(propertyName,
					newValues.toArray(new String[0]));

			// TODO use a transaction
			// Retrieve all node that reference this tag and update them
			NodeIterator nit = getCatalogueValueInstances(
					JcrUiUtils.getSession(templateNode), taggableType,
					propertyName, oldValue);
			while (nit.hasNext())
				updateOneTag(nit.nextNode(), propertyName, oldValue, newValue);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to update " + templateNode
					+ " with values on " + taggableType + " for property "
					+ propertyName + " with old value '" + oldValue
					+ "' and new value '" + newValue + "'.", e);
		}
	}

	/* TAG LIKE INSTANCES MANAGEMENT */
	@Override
	public Node createTagLikeResourceParent(Session session, String tagId,
			String tagInstanceType, String codePropName,
			String taggableParentPath, String taggableNodeType,
			String taggablePropName) {
		List<String> names = new ArrayList<String>();
		names.add(taggablePropName);
		return createTagLikeResourceParent(session, tagId, tagInstanceType,
				codePropName, taggableParentPath, taggableNodeType, names);
	}

	public Node createTagLikeResourceParent(Session session, String tagId,
			String tagInstanceType, String codePropName,
			String taggableParentPath, String taggableNodeType,
			List<String> taggablePropNames) {
		String currId = tagId == null ? tagInstanceType : tagId;
		Node node = getTagLikeResourceParent(session, currId);
		if (node != null)
			return node;
		else {
			String path = getPathForId(
					PeopleConstants.RESOURCE_TYPE_ID_TAG_LIKE, currId);
			String parPath = JcrUtils.parentPath(path);
			try {
				if (!session.nodeExists(parPath))
					throw new PeopleException("Default tree structure "
							+ "for tag like resources must have been created. "
							+ "Please fix this before trying to create "
							+ "tag like resource parent " + currId);
				Node parent = session.getNode(parPath);
				Node tagLikeParent = parent.addNode(currId,
						PeopleTypes.PEOPLE_TAG_PARENT);
				tagLikeParent.setProperty(PeopleNames.PEOPLE_TAG_ID, currId);
				tagLikeParent.setProperty(PeopleNames.PEOPLE_TAG_INSTANCE_TYPE,
						tagInstanceType);
				// If this property is not set, the key property of the tag
				// instance is the JCR_TITLE Property
				if (notEmpty(codePropName))
					tagLikeParent
							.setProperty(PeopleNames.PEOPLE_TAG_CODE_PROP_NAME,
									codePropName);
				tagLikeParent.setProperty(
						PeopleNames.PEOPLE_TAGGABLE_PARENT_PATH,
						taggableParentPath);
				tagLikeParent
						.setProperty(PeopleNames.PEOPLE_TAGGABLE_NODE_TYPE,
								taggableNodeType);
				tagLikeParent.setProperty(
						PeopleNames.PEOPLE_TAGGABLE_PROP_NAME,
						taggablePropNames.toArray(new String[0]));
				return tagLikeParent;
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to create new temaple "
						+ currId + " at path " + path, e);
			}
		}
	}

	@Override
	public Node getTagLikeResourceParent(Session session, String tagId) {
		String path = getPathForId(PeopleConstants.RESOURCE_TYPE_ID_TAG_LIKE,
				tagId);
		try {
			if (session.nodeExists(path)) {
				return session.getNode(path);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve tag like parent "
					+ "at path " + path + " for tagId " + tagId, e);
		}
		return null;
	}

	@Override
	public Node registerTag(Session session, String tagId, String tagValue)
			throws RepositoryException {
		Node tagInstance = getRegisteredTag(session, tagId, tagValue);
		if (tagInstance != null) {
			// Tag already exists, we do nothing.
			if (log.isTraceEnabled()) {
				String registeredKey = JcrUiUtils.get(
						tagInstance,
						getTagKeyPropName(getTagLikeResourceParent(session,
								tagId)));
				log.debug("Tag [" + tagId + "] with key " + tagValue
						+ " already exists (registered key is ["
						+ registeredKey + "]), nothing has been done.");
			}
			return tagInstance;
		} else {
			Node newTag = createTagInstanceInternal(
					getExistingTagLikeParent(session, tagId), tagValue);
			return newTag;
		}
	}

	@Override
	public Node registerTag(Session session, String tagId, String tagCode,
			String tagValue) throws RepositoryException {
		// Check if such a tag already exists
		Node tagInstance = getRegisteredTag(session, tagId, tagCode);
		if (tagInstance != null) {
			// Tag already exists, we do nothing.
			if (log.isTraceEnabled()) {
				String registeredKey = JcrUiUtils.get(
						tagInstance,
						getTagKeyPropName(getTagLikeResourceParent(session,
								tagId)));
				log.debug("Tag [" + tagId + "] with key " + tagCode
						+ " already exists (registered key is ["
						+ registeredKey + "]), nothing has been done.");
			}
			return tagInstance;
		} else {
			Node newTag = createTagInstanceInternal(
					getExistingTagLikeParent(session, tagId), tagCode);
			// remove trailing and starting space
			tagValue = tagValue.trim();
			JcrUiUtils.setJcrProperty(newTag, Property.JCR_TITLE,
					PropertyType.STRING, tagValue);
			return newTag;
		}
	}

	@Override
	public Node getRegisteredTag(Session session, String tagId,
			String instanceKey) {
		return getRegisteredTag(getExistingTagLikeParent(session, tagId),
				instanceKey);
	}

	@Override
	public Node getRegisteredTag(Node tagParent, String instanceKey) {
		try {
			String relPath = getTagRelPath(instanceKey);
			if (tagParent.hasNode(relPath)) {
				Node existing = tagParent.getNode(relPath);
				String existingValue = JcrUiUtils.get(existing,
						getTagKeyPropName(tagParent));
				if (instanceKey.equalsIgnoreCase(existingValue)) {
					return existing;
				}
			}
			return null;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve existing tag "
					+ tagParent + " for key " + instanceKey, e);
		}
	}

	@Override
	public String getEncodedTagValue(Session session, String tagId, String code) {
		try {
			Node tag = getRegisteredTag(session, tagId, code);
			if (tag != null && tag.hasProperty(Property.JCR_TITLE))
				return tag.getProperty(Property.JCR_TITLE).getString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve tag " + tagId
					+ " value with key " + code, e);
		}
		return code;
	}

	@Override
	public String getEncodedTagCodeFromValue(Session session, String tagId,
			String value) {
		try {
			Node tagParent = getExistingTagLikeParent(session, tagId);
			String tagNodeType = tagParent.getProperty(
					PeopleNames.PEOPLE_TAG_INSTANCE_TYPE).getString();
			String tagCodePropName = tagParent.getProperty(
					PeopleNames.PEOPLE_TAG_CODE_PROP_NAME).getString();

			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(tagParent.getPath()));
			builder.append("//element(*, ").append(tagNodeType).append(")");
			builder.append("[");
			builder.append(XPathUtils.getPropertyEquals(PeopleNames.JCR_TITLE,
					value));
			builder.append("]");
			Query query = queryManager.createQuery(builder.toString(),
					PeopleConstants.QUERY_XPATH);

			// QueryObjectModelFactory factory = queryManager.getQOMFactory();
			// Selector source = factory.selector(tagNodeType, tagNodeType);
			//
			// Constraint c1 = factory.descendantNode(source.getSelectorName(),
			// tagParent.getPath());
			//
			// DynamicOperand dynOp = factory.propertyValue(
			// source.getSelectorName(), Property.JCR_TITLE);
			// StaticOperand statOp = factory.literal(session.getValueFactory()
			// .createValue(value));
			// Constraint c2 = factory.comparison(dynOp,
			// QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			//
			// Constraint defaultC = factory.and(c1, c2);
			//
			// QueryObjectModel query = factory.createQuery(source, defaultC,
			// null, null);

			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();
			if (ni.hasNext())
				return ni.nextNode().getProperty(tagCodePropName).getString();
			else {
				return null;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get code for encodedTag "
					+ tagId + " with value " + value, e);
		}
	}

	@Override
	public String getEncodedTagValuesAsString(String tagId, Node node,
			String propertyName, String separator) {
		try {
			Session session = node.getSession();

			if (!node.hasProperty(propertyName))
				return "";
			else {
				Value[] codes = node.getProperty(propertyName).getValues();
				StringBuilder builder = new StringBuilder();
				for (Value val : codes) {
					String currCode = val.getString();
					if (notEmpty(currCode)) {
						builder.append(
								getEncodedTagValue(session, tagId, currCode))
								.append(separator);
					}
				}
				if (builder.lastIndexOf(separator) >= 0)
					return builder.substring(0,
							builder.length() - separator.length());
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get values of " + tagId
					+ " for property " + propertyName + " of " + node, e);
		}
	}

	@Override
	public NodeIterator getTaggedEntities(Session session, String tagId,
			String key) {
		Node tagParent = getExistingTagLikeParent(session, tagId);
		return getTaggedEntities(tagParent, key);
	}

	@Override
	public NodeIterator getTaggedEntities(Node tagParent, String key) {
		try {
			// TODO clean this
			String cleanedKey = key.replaceAll("(?:')", "''");

			String nodeType = tagParent.getProperty(
					PeopleNames.PEOPLE_TAGGABLE_NODE_TYPE).getString();
			String parentPath = tagParent.getProperty(
					PeopleNames.PEOPLE_TAGGABLE_PARENT_PATH).getString();
			List<String> propNames = JcrUiUtils.getMultiAsList(tagParent,
					PeopleNames.PEOPLE_TAGGABLE_PROP_NAME);

			// String sName = "nodes";
			// StringBuilder builder = new StringBuilder();
			// for (String propName : propNames) {
			// builder.append(sName);
			// builder.append(".[" + propName + "] = ");
			// builder.append("'" + cleanedKey + "'");
			// builder.append(" OR ");
			// }
			// String propClause = builder.toString();
			// if (propClause.endsWith(" OR "))
			// propClause = propClause.substring(0, propClause.length() - 4);
			//
			// // TODO will it fails if propNames is empty?
			// QueryManager qm = tagParent.getSession().getWorkspace()
			// .getQueryManager();
			// Query query = qm.createQuery("select * from [" + nodeType +
			// "] as "
			// + sName + " where ISDESCENDANTNODE('" + parentPath
			// + "') AND (" + propClause + ") ", Query.JCR_SQL2);

			// Build xpath for property names;
			StringBuilder builder = new StringBuilder();
			for (String propName : propNames) {
				builder.append(XPathUtils.getPropertyEquals(propName,
						cleanedKey));
				builder.append(" OR ");
			}
			String condition = builder.toString();
			// if (condition.endsWith(" OR "))
			// force error if no property is defined for this tag
			condition = condition.substring(0, condition.length() - 4);

			String xpathQueryStr = XPathUtils.descendantFrom(parentPath)
					+ "//element(*, " + nodeType + ")";

			xpathQueryStr += "[" + condition + "]";
			QueryManager queryManager = tagParent.getSession().getWorkspace()
					.getQueryManager();
			Query query = queryManager.createQuery(xpathQueryStr,
					PeopleConstants.QUERY_XPATH);
			NodeIterator nit = query.execute().getNodes();
			return nit;
		} catch (RepositoryException ee) {
			throw new PeopleException(
					"Unable to retrieve tagged entities for key " + key
							+ " on parent " + tagParent, ee);
		}
	}

	@Override
	public void refreshKnownTags(Session session, String tagId) {
		refreshKnownTags(getExistingTagLikeParent(session, tagId));
	}

	@Override
	public void refreshKnownTags(Node tagParent) {
		try {
			// Initialisation
			List<String> newExistingValues = new ArrayList<String>();
			List<String> registeredTags = new ArrayList<String>();
			Session session = tagParent.getSession();
			if (log.isDebugEnabled())
				log.debug("Starting known tag refresh for " + tagParent);
			String keyPropName = getTagKeyPropName(tagParent);
			String taggableNodeType = tagParent.getProperty(
					PeopleNames.PEOPLE_TAGGABLE_NODE_TYPE).getString();
			String taggableParentPath = tagParent.getProperty(
					PeopleNames.PEOPLE_TAGGABLE_PARENT_PATH).getString();
			String codeProp = JcrUiUtils
					.get(tagParent, PeopleNames.PEOPLE_CODE);
			List<String> propNames = JcrUiUtils.getMultiAsList(tagParent,
					PeopleNames.PEOPLE_TAGGABLE_PROP_NAME);

			if (log.isTraceEnabled())
				log.trace("Getting already registered tags");
			NodeIterator nit = getRegisteredTags(tagParent, null);
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				String currKey = JcrUiUtils.get(currNode, keyPropName);
				if (notEmpty(currKey) && !registeredTags.contains(currKey))
					registeredTags.add(currKey);
			}
			if (log.isTraceEnabled())
				log.trace("Found already " + registeredTags.size()
						+ " registered tags");

			// Look for not yet registered tags
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [" + taggableNodeType
									+ "] as instances where ISDESCENDANTNODE('"
									+ taggableParentPath + "') ",
							Query.JCR_SQL2);
			nit = query.execute().getNodes();
			if (log.isDebugEnabled())
				log.debug("Searching new tags on " + nit.getSize()
						+ " elements of type " + taggableNodeType);
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				for (String propName : propNames) {
					if (currNode.hasProperty(propName)) {
						Value[] tags = currNode.getProperty(propName)
								.getValues();
						for (Value tagV : tags) {
							String currTag = tagV.getString().trim();

							if (notEmpty(currTag)
									&& !registeredTags.contains(currTag)) {
								if (currTag.length() < 2) {
									log.warn("Unable to cache tag ["
											+ currTag
											+ "] for "
											+ JcrUiUtils.get(currNode,
													Property.JCR_TITLE) + " - "
											+ currNode);
								} else if (!newExistingValues.contains(currTag))
									newExistingValues.add(currTag);
							}
						}
					}
				}
			}

			if (log.isTraceEnabled())
				log.trace("Start processing the " + newExistingValues.size()
						+ " new used tags found.");

			// Add the newly found tags.
			if (isEmpty(codeProp))
				for (String tag : newExistingValues) {
					createTagInstanceInternal(tagParent, tag);
					session.save();
				}
			else {
				for (String tag : newExistingValues) {
					Node curr = createTagInstanceInternal(tagParent, tag);
					JcrUiUtils.setJcrProperty(curr, Property.JCR_TITLE,
							PropertyType.STRING, tag);
					session.save();
				}
				log.warn("We refreshed an encoded tag like subtree, for "
						+ tagParent + ". All tag values have been set "
						+ "with the same value as the code because we "
						+ "have no information on the corresponding label.");
			}
			if (log.isDebugEnabled())
				log.debug("Tag refresh for " + tagParent
						+ " has been done, creating "
						+ newExistingValues.size() + " new tag instances.");
		} catch (RepositoryException ee) {
			throw new PeopleException("Unable to refresh cache of known tags",
					ee);
		}
	}

	@Override
	public boolean updateTag(Node tagInstance, String newValue)
			throws RepositoryException {
		// TODO use a transaction
		Node tagParent = retrieveTagParentFromTag(tagInstance);
		Value[] values = tagParent.getProperty(
				PeopleNames.PEOPLE_TAGGABLE_PROP_NAME).getValues();
		if (values.length > 1)
			// unimplemented multiple value
			return false;
		String propName = values[0].getString();
		String oldValue = tagInstance.getProperty(Property.JCR_TITLE)
				.getString();
		JcrUiUtils.checkCOStatusBeforeUpdate(tagInstance);
		Session session = tagInstance.getSession();

		// Retrieve all node that reference this tag and update them
		NodeIterator nit = getTaggedEntities(tagParent, oldValue);
		while (nit.hasNext())
			updateOneTag(nit.nextNode(), propName, oldValue, newValue);

		String newRelPath = "/" + getTagRelPath(newValue);
		// Insure the parent node is already existing
		// Rather use the parent node than the abs path: we might have not the
		// right on the full workspace
		Node newIntancePar = JcrUtils.mkdirs(tagParent,
				JcrUtils.parentPath(newRelPath), NodeType.NT_UNSTRUCTURED);
		session.move(tagInstance.getPath(), newIntancePar.getPath() + "/"
				+ JcrUtils.lastPathElement(newRelPath));
		tagInstance.setProperty(Property.JCR_TITLE, newValue);
		tagInstance.getSession().save();
		return true;
	}

	/**
	 * Save the session for each node in order to manage the version. Should be
	 * cleaned when we handle transaction.
	 * 
	 * @param taggable
	 * @param tagPropName
	 * @param oldValue
	 * @param newValue
	 */
	private void updateOneTag(Node taggable, String tagPropName,
			String oldValue, String newValue) {
		try {
			Node versionable = JcrUiUtils.getParentVersionableNode(taggable);
			if (versionable != null
					&& !JcrUiUtils.checkCOStatusBeforeUpdate(versionable))
				log.warn(versionable + " was checked-in as we want to update "
						+ tagPropName + " of " + taggable + " form " + oldValue
						+ " to " + newValue);
			Property property = taggable.getProperty(tagPropName);
			if (property.isMultiple()) {
				List<String> oldValues = JcrUiUtils.getMultiAsList(taggable,
						tagPropName);
				List<String> newValues = new ArrayList<String>();
				for (String val : oldValues) {
					if (oldValue.equals(val) && newValue != null)
						newValues.add(newValue);
					else
						newValues.add(val);
				}
				taggable.setProperty(tagPropName,
						newValues.toArray(new String[0]));
			} else
				taggable.setProperty(tagPropName, newValue);
			taggable.getSession().save();
		} catch (RepositoryException ee) {
			throw new PeopleException(
					"Unable to update tag like multiple value property "
							+ tagPropName + " removing " + oldValue
							+ " and adding " + newValue + " on " + taggable, ee);
		}
	}

	@Override
	public void unregisterTag(Session session, String tagId, String tag) {
		throw new PeopleException("Unimplemented method "
				+ "ResourceService.unregisterTag("
				+ "Session session, String tagId, String tag) ");
	}

	@Override
	public long countMembers(Node tag) {
		Node parent = retrieveTagParentFromTag(tag);
		String keyPropName = getTagKeyPropName(parent);
		NodeIterator nit = getTaggedEntities(parent,
				JcrUiUtils.get(tag, keyPropName));
		return nit.getSize();
	}

	@Override
	public List<String> getRegisteredTagValueList(Session session,
			String tagId, String filter) {
		Node tagParent = getTagLikeResourceParent(session, tagId);
		List<String> values = new ArrayList<String>();
		try {
			NodeIterator nit = getRegisteredTags(tagParent, filter);
			while (nit.hasNext()) {
				Node curr = nit.nextNode();
				if (curr.hasProperty(Property.JCR_TITLE))
					values.add(curr.getProperty(Property.JCR_TITLE).getString());
			}
			return values;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get values for tag of ID "
					+ tagId, re);
		}
	}

	// HELPERS FOR TAGS
	/** Already existing check must have been done before */
	private Node createTagInstanceInternal(Node tagParent, String tagKey) {
		try {
			// retrieve parameters for this tag
			String relPath = getTagRelPath(tagKey);
			String instanceType = tagParent.getProperty(
					PeopleNames.PEOPLE_TAG_INSTANCE_TYPE).getString();
			// create and set props
			Node newTag = JcrUtils.mkdirs(tagParent, relPath, instanceType);
			newTag.setProperty(getTagKeyPropName(tagParent), tagKey);
			if (newTag.isNodeType(PeopleTypes.PEOPLE_ENTITY)) {
				String uuid = UUID.randomUUID().toString();
				newTag.setProperty(PeopleNames.PEOPLE_UID, uuid);
			}
			return newTag;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create tag instance " + tagKey
					+ " for tag " + tagParent, e);
		}
	}

	protected String getTagRelPath(String tag) {
		// remove trailing and starting space
		tag = tag.trim();
		String cleanedTag = JcrUtils.replaceInvalidChars(tag).trim();
		// corner case when second character is a space
		if (cleanedTag.charAt(1) == ' ')
			cleanedTag = cleanedTag.charAt(0) + ' ' + cleanedTag.substring(2);
		String relPath = JcrUtils.firstCharsToPath(cleanedTag, 2);
		return relPath + "/" + cleanedTag;
	}

	protected Node retrieveTagParentFromTag(Node tag) {
		Node parent = tag;
		while (!JcrUiUtils.isNodeType(parent, PeopleTypes.PEOPLE_TAG_PARENT))
			parent = JcrUiUtils.getParent(parent);
		return parent;
	}

	protected String getTagKeyPropName(Node tagParent) {
		try {
			if (tagParent.hasProperty(PeopleNames.PEOPLE_TAG_CODE_PROP_NAME))
				return tagParent.getProperty(
						PeopleNames.PEOPLE_TAG_CODE_PROP_NAME).getString();
			else
				return JcrUiUtils.getLocalJcrItemName(Property.JCR_TITLE);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unable to retrieve key property name for " + tagParent);
		}
	}

	// Helper that will throw an exception if the tag parent is not found
	private Node getExistingTagLikeParent(Session session, String tagId) {
		Node tagInstanceParent = getTagLikeResourceParent(session, tagId);
		if (tagInstanceParent == null)
			throw new PeopleException("Tag like resource with ID " + tagId
					+ " does not exist. Cannot process current action.");
		else
			return tagInstanceParent;
	}

	private NodeIterator getRegisteredTags(Node tagParent, String filter) {
		try {
			String nodeType = tagParent.getProperty(
					PeopleNames.PEOPLE_TAG_INSTANCE_TYPE).getString();
			String queryStr = "select * from [" + nodeType
					+ "] as nodes where ISDESCENDANTNODE('"
					+ tagParent.getPath() + "')";
			if (notEmpty(filter))
				queryStr += " AND LOWER(nodes.[" + Property.JCR_TITLE
						+ "]) like \"%" + filter.toLowerCase() + "%\"";
			queryStr += " ORDER BY nodes.[" + Property.JCR_TITLE + "]";

			Query query = tagParent.getSession().getWorkspace()
					.getQueryManager().createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get values for " + tagParent
					+ " with filter " + filter, re);
		}
	}

	private String getPathForId(String resourceType, String id) {
		if (PeopleConstants.RESOURCE_TYPE_ID_TEMPLATE.equals(resourceType))
			return peopleService.getBasePath(PeopleConstants.PEOPLE_RESOURCE)
					+ "/" + PeopleConstants.PEOPLE_RESOURCE_TEMPLATE + "/" + id;
		else if (PeopleConstants.RESOURCE_TYPE_ID_TAG_LIKE.equals(resourceType))
			return peopleService.getBasePath(PeopleConstants.PEOPLE_RESOURCE)
					+ "/" + PeopleConstants.PEOPLE_RESOURCE_TAG_LIKE + "/" + id;
		else
			throw new PeopleException("Unknown resource type " + resourceType
					+ " for id " + id);
	}

	// @Override
	// public Node getRegisteredTag(Session session, String tagParentPath,
	// String tag) {
	// try {
	// // remove trailing and starting space
	// tag = tag.trim();
	// String path = tagParentPath + "/" + getTagRelPath(tag);
	//
	// if (session.nodeExists(path)) {
	// Node existing = session.getNode(path);
	// if (tag.equalsIgnoreCase(JcrUiUtils.get(existing,
	// Property.JCR_TITLE))) {
	// return existing;
	// }
	// }
	// } catch (RepositoryException ee) {
	// throw new PeopleException("Unable to get registered tag " + tag
	// + " under " + tagParentPath);
	// }
	// return null;
	// }

	/* LEGACY, TO BE REMOVED */
	// @Override
	// public List<String> getValueList(Session session, String basePath,
	// String filter) {
	// return getValueList(session, NodeType.MIX_TITLE, basePath, filter);
	// }
	//
	// public List<String> getValueList(Session session, String nodeType,
	// String basePath, String filter) {
	// List<String> values = new ArrayList<String>();
	// try {
	// String queryStr = "select * from [" + nodeType
	// + "] as nodes where ISDESCENDANTNODE('" + basePath + "')";
	// if (JcrUiUtils.checkNotEmptyString(filter))
	// queryStr += " AND LOWER(nodes.[" + Property.JCR_TITLE
	// + "]) like \"%" + filter.toLowerCase() + "%\"";
	// queryStr += " ORDER BY nodes.[" + Property.JCR_TITLE + "]";
	//
	// Query query = session.getWorkspace().getQueryManager()
	// .createQuery(queryStr, Query.JCR_SQL2);
	// NodeIterator nit = query.execute().getNodes();
	//
	// while (nit.hasNext()) {
	// Node curr = nit.nextNode();
	// if (curr.hasProperty(Property.JCR_TITLE))
	// values.add(curr.getProperty(Property.JCR_TITLE).getString());
	// }
	// } catch (RepositoryException re) {
	// throw new PeopleException("Unable to get tags values for node ", re);
	// }
	// return values;
	// }
	//
	// public List<String> getPossibleValuesOld(Session session,
	// String resourcePath, String propertyName, String filter) {
	// List<String> result = new ArrayList<String>();
	// try {
	// if (session.nodeExists(resourcePath)) {
	// Node node = session.getNode(resourcePath);
	// if (node.hasProperty(propertyName)) {
	// Value[] values = node.getProperty(propertyName).getValues();
	// for (Value value : values) {
	// String curr = value.getString();
	// if (isEmptyString(filter)
	// || JcrUiUtils.checkNotEmptyString(curr)
	// && curr.toLowerCase().contains(
	// filter.toLowerCase()))
	// result.add(curr);
	// }
	// }
	// }
	// } catch (RepositoryException re) {
	// throw new PeopleException("Unable to get " + propertyName
	// + " values for node at path " + resourcePath, re);
	// }
	//
	// return result;
	// }
	//
	// @Override
	// public void unregisterTag(Session session, String tagParentPath,
	// String tag, String tagableParentPath) {
	// throw new PeopleException("unimplemented method.");
	// }
	//
	// @Override
	// public boolean updateTagTitle(Node tagInstance, String resourceType,
	// String tagParentPath, String tag, String taggableNodeType,
	// String tagPropName, String taggableParentPath)
	// throws RepositoryException {
	// Session session = tagInstance.getSession();
	// String oldValue = tagInstance.getProperty(Property.JCR_TITLE)
	// .getString();
	// // boolean wasCheckedIn = false;
	// // TODO use a transaction
	// // Retrieve all node that reference this tag and update them
	// NodeIterator nit = getTaggedEntities(session, taggableNodeType,
	// taggableParentPath, tagPropName, oldValue);
	// while (nit.hasNext())
	// updateTag(nit.nextNode(), tagPropName, oldValue, tag);
	//
	// // update the tag
	// String newPath = tagParentPath + "/" + getTagRelPath(tag);
	// // insure the parent node is already existing
	// JcrUtils.mkdirs(session, JcrUtils.parentPath(newPath));
	// session.move(tagInstance.getPath(), newPath);
	// tagInstance.setProperty(Property.JCR_TITLE, tag);
	// session.save();
	// return true;
	// }
	//
	// @Override
	// public NodeIterator getTaggedEntities(Session session, String nodeType,
	// String parentPath, String propertyName, String value) {
	// try {
	// Query query = session
	// .getWorkspace()
	// .getQueryManager()
	// .createQuery(
	// "select * from [" + nodeType
	// + "] as nodes where ISDESCENDANTNODE('"
	// + parentPath + "') AND nodes.["
	// + propertyName + "] = '" + value + "'",
	// Query.JCR_SQL2);
	// return query.execute().getNodes();
	// } catch (RepositoryException ee) {
	// throw new PeopleException("Unable to retrieve tagged entities "
	// + nodeType + " under " + parentPath + " for "
	// + propertyName + " = " + value, ee);
	// }
	// }
	//
	// /**
	// * Save the session for each node in order to manage the version. Should
	// be
	// * cleaned when we handle transaction.
	// *
	// * @param node
	// * @param propertyName
	// * @param oldValue
	// * @param newValue
	// */
	// private void updateTag(Node node, String propertyName, String oldValue,
	// String newValue) {
	// try {
	// boolean wasCheckedIn = false;
	// if (node.isNodeType(NodeType.MIX_VERSIONABLE)
	// && !JcrUiUtils.isNodeCheckedOutByMe(node)) {
	// wasCheckedIn = true;
	// JcrUiUtils.checkout(node);
	// }
	// List<String> oldValues = JcrUiUtils.getMultiAsList(node,
	// propertyName);
	// List<String> newValues = new ArrayList<String>();
	// for (String val : oldValues) {
	// if (oldValue.equals(val))
	// newValues.add(newValue);
	// else
	// newValues.add(val);
	// }
	// node.setProperty(propertyName, newValues.toArray(new String[0]));
	//
	// if (wasCheckedIn)
	// JcrUiUtils.saveAndCheckin(node);
	// else
	// node.getSession().save();
	// } catch (RepositoryException ee) {
	// throw new PeopleException(
	// "Unable to update tag like multiple value property "
	// + propertyName + " removing " + oldValue
	// + " and adding " + newValue + " on " + node, ee);
	// }
	// }
	//
	// @Override
	// public long countMembers(Node tag, String tagableParentPath,
	// String tagableType, String tagPropName) {
	// Query query;
	// NodeIterator nit;
	// try {
	// Session session = tag.getSession();
	// String tagValue = JcrUiUtils.get(tag, Property.JCR_TITLE);
	// // Retrieve existing tags
	// if (session.nodeExists(tagableParentPath)) {
	// String queryString = "select * from [" + tagableType
	// + "] as tagged where ISDESCENDANTNODE('"
	// + tagableParentPath + "') AND tagged.[" + tagPropName
	// + "]='" + tagValue + "'";
	// query = session.getWorkspace().getQueryManager()
	// .createQuery(queryString, Query.JCR_SQL2);
	// nit = query.execute().getNodes();
	// return nit.getSize();
	// }
	// return 0;
	// } catch (RepositoryException re) {
	// throw new PeopleException("Unable to count members for " + tag, re);
	// }
	// }
	//
	// @Override
	// public void refreshKnownTags(Session session, String tagResourceType,
	// String tagParentPath, String tagableNodeType, String tagPropName,
	// String tagableParentPath) {
	//
	// List<String> existingValues = new ArrayList<String>();
	// List<String> registeredTags = new ArrayList<String>();
	//
	// try {
	// Query query;
	// NodeIterator nit;
	// // Retrieve existing tags
	// if (session.nodeExists(tagParentPath)) {
	// query = session
	// .getWorkspace()
	// .getQueryManager()
	// .createQuery(
	// "select * from [" + tagResourceType
	// + "] as tags where ISDESCENDANTNODE('"
	// + tagParentPath + "') ", Query.JCR_SQL2);
	// nit = query.execute().getNodes();
	// while (nit.hasNext()) {
	// Node currNode = nit.nextNode();
	// String currTag = JcrUiUtils.get(currNode,
	// Property.JCR_TITLE);
	// if (JcrUiUtils.checkNotEmptyString(currTag)
	// && !registeredTags.contains(currTag))
	// registeredTags.add(currTag.trim());
	// }
	// }
	//
	// // Look for not yet registered tags
	// query = session
	// .getWorkspace()
	// .getQueryManager()
	// .createQuery(
	// "select * from [" + tagableNodeType
	// + "] as instances where ISDESCENDANTNODE('"
	// + tagableParentPath + "') ", Query.JCR_SQL2);
	// nit = query.execute().getNodes();
	// while (nit.hasNext()) {
	// Node currNode = nit.nextNode();
	// if (currNode.hasProperty(tagPropName)) {
	// Value[] tags = currNode.getProperty(tagPropName)
	// .getValues();
	// for (Value tagV : tags) {
	// String currTag = tagV.getString().trim();
	// if (JcrUiUtils.checkNotEmptyString(currTag)
	// && !registeredTags.contains(currTag))
	// existingValues.add(currTag);
	// }
	// }
	// }
	//
	// // Create tag parent if needed
	// if (!session.nodeExists(tagParentPath)) {
	// JcrUtils.mkdirs(session, tagParentPath);
	// session.save();
	// }
	//
	// // Add the newly found tags.
	// for (String tag : existingValues) {
	// if (!registeredTags.contains(tag)) {
	// registerTag(session, tagResourceType, tagParentPath, tag);
	// session.save();
	// }
	// }
	//
	// } catch (RepositoryException ee) {
	// throw new PeopleException("Unable to refresh cache of known tags",
	// ee);
	// }
	// }
	//

}