package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people {@link ResourceService} */
public class ResourceServiceImpl implements ResourceService {
	private final static Log log = LogFactory.getLog(ResourceServiceImpl.class);

	private PeopleService peopleService;

	public ResourceServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
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
	public List<String> getPossibleValues(Session session, String templateId,
			String propertyName, String filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPossibleValues(Node templateNode,
			String propertyName, String filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNodeTemplate(Session session, String templateId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* TAG LIKE INSTANCES MANAGEMENT */

	@Override
	public Node createTemplateForType(Session session, String nodeType,
			String templateId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createTagLikeResourceParent(Session session, String tagId,
			String tagInstanceType, String codePropName,
			String taggableParentPath, String taggableNodeType,
			String taggablePropName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getTagInstanceParent(Session session, String tagId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeIterator getTaggedEntities(Session session, String tagId,
			String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeIterator getTaggedEntities(Node tagInstancesParent, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshKnownTags(Node tagParent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Node registerTag(Session session, String tagId, String tagValue)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateTag(Node tagInstance, String newValue)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unregisterTag(Session session, String tagId, String tag) {
		// TODO Auto-generated method stub

	}

	@Override
	public long countMembers(Node tag) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> getRegisteredTagValueList(Session session,
			String tagId, String filter) {
		// TODO Auto-generated method stub
		return null;
	}

	/* LEGACY, TO BE REMOVED */
	@Override
	public List<String> getValueList(Session session, String basePath,
			String filter) {
		return getValueList(session, NodeType.MIX_TITLE, basePath, filter);
	}

	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter) {
		List<String> values = new ArrayList<String>();
		try {
			String queryStr = "select * from [" + nodeType
					+ "] as nodes where ISDESCENDANTNODE('" + basePath + "')";
			if (CommonsJcrUtils.checkNotEmptyString(filter))
				queryStr += " AND LOWER(nodes.[" + Property.JCR_TITLE
						+ "]) like \"%" + filter.toLowerCase() + "%\"";
			queryStr += " ORDER BY nodes.[" + Property.JCR_TITLE + "]";

			Query query = session.getWorkspace().getQueryManager()
					.createQuery(queryStr, Query.JCR_SQL2);
			NodeIterator nit = query.execute().getNodes();

			while (nit.hasNext()) {
				Node curr = nit.nextNode();
				if (curr.hasProperty(Property.JCR_TITLE))
					values.add(curr.getProperty(Property.JCR_TITLE).getString());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get tags values for node ", re);
		}
		return values;
	}

	public List<String> getPossibleValuesOld(Session session,
			String resourcePath, String propertyName, String filter) {
		List<String> result = new ArrayList<String>();
		try {
			if (session.nodeExists(resourcePath)) {
				Node node = session.getNode(resourcePath);
				if (node.hasProperty(propertyName)) {
					Value[] values = node.getProperty(propertyName).getValues();
					for (Value value : values) {
						String curr = value.getString();
						if (CommonsJcrUtils.isEmptyString(filter)
								|| CommonsJcrUtils.checkNotEmptyString(curr)
								&& curr.toLowerCase().contains(
										filter.toLowerCase()))
							result.add(curr);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get " + propertyName
					+ " values for node at path " + resourcePath, re);
		}

		return result;
	}

	@Override
	public Node getRegisteredTag(Session session, String tagParentPath,
			String tag) {
		try {
			// remove trailing and starting space
			tag = tag.trim();
			String path = tagParentPath + "/" + getTagRelPath(tag);

			if (session.nodeExists(path)) {
				Node existing = session.getNode(path);
				if (tag.equalsIgnoreCase(CommonsJcrUtils.get(existing,
						Property.JCR_TITLE))) {
					return existing;
				}
			}
		} catch (RepositoryException ee) {
			throw new PeopleException("Unable to get registered tag " + tag
					+ " under " + tagParentPath);
		}
		return null;
	}

	@Override
	public Node registerTag(Session session, String resourceType,
			String tagParentPath, String tag) throws RepositoryException {
		// remove trailing and starting space
		tag = tag.trim();

		String path = tagParentPath + "/" + getTagRelPath(tag);
		if (session.nodeExists(path)) {
			Node existing = session.getNode(path);
			if (tag.equalsIgnoreCase(CommonsJcrUtils.get(existing,
					Property.JCR_TITLE))) {
				// Tag already exists, we do nothing.
				if (log.isTraceEnabled())
					log.debug("Tag ["
							+ CommonsJcrUtils.get(existing, Property.JCR_TITLE)
							+ "] already exists. Cannot add [" + tag
							+ "], nothing has been done.");
				return existing;
			}
		}
		Node newTag = JcrUtils.mkdirs(session, path, resourceType);
		if (!newTag.isNodeType(NodeType.MIX_TITLE))
			newTag.addMixin(NodeType.MIX_TITLE);
		newTag.setProperty(Property.JCR_TITLE, tag);

		if (newTag.isNodeType(PeopleTypes.PEOPLE_ENTITY)) {
			String uuid = UUID.randomUUID().toString();
			newTag.setProperty(PeopleNames.PEOPLE_UID, uuid);
		}

		return newTag;
	}

	@Override
	public void unregisterTag(Session session, String tagParentPath,
			String tag, String tagableParentPath) {
		throw new PeopleException("unimplemented method.");
	}

	protected String getTagRelPath(String tag) {
		// remove trailing and starting space
		tag = tag.trim();
		String cleanedTag = JcrUtils.replaceInvalidChars(tag).trim();
		String relPath = JcrUtils.firstCharsToPath(cleanedTag, 2);
		return relPath + "/" + cleanedTag;
	}

	@Override
	public boolean updateTagTitle(Node tagInstance, String resourceType,
			String tagParentPath, String tag, String taggableNodeType,
			String tagPropName, String taggableParentPath)
			throws RepositoryException {
		Session session = tagInstance.getSession();
		String oldValue = tagInstance.getProperty(Property.JCR_TITLE)
				.getString();
		// boolean wasCheckedIn = false;
		// TODO use a transaction
		// Retrieve all node that reference this tag and update them
		NodeIterator nit = getTaggedEntities(session, taggableNodeType,
				taggableParentPath, tagPropName, oldValue);
		while (nit.hasNext())
			updateTag(nit.nextNode(), tagPropName, oldValue, tag);

		// update the tag
		String newPath = tagParentPath + "/" + getTagRelPath(tag);
		// insure the parent node is already existing
		JcrUtils.mkdirs(session, JcrUtils.parentPath(newPath));
		session.move(tagInstance.getPath(), newPath);
		tagInstance.setProperty(Property.JCR_TITLE, tag);
		session.save();
		return true;
	}

	@Override
	public NodeIterator getTaggedEntities(Session session, String nodeType,
			String parentPath, String propertyName, String value) {
		try {
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [" + nodeType
									+ "] as nodes where ISDESCENDANTNODE('"
									+ parentPath + "') AND nodes.["
									+ propertyName + "] = '" + value + "'",
							Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException ee) {
			throw new PeopleException("Unable to retrieve tagged entities "
					+ nodeType + " under " + parentPath + " for "
					+ propertyName + " = " + value, ee);
		}
	}

	/**
	 * Save the session for each node in order to manage the version. Should be
	 * cleaned when we handle transaction.
	 * 
	 * @param node
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	private void updateTag(Node node, String propertyName, String oldValue,
			String newValue) {
		try {
			boolean wasCheckedIn = false;
			if (node.isNodeType(NodeType.MIX_VERSIONABLE)
					&& !CommonsJcrUtils.isNodeCheckedOutByMe(node)) {
				wasCheckedIn = true;
				CommonsJcrUtils.checkout(node);
			}
			List<String> oldValues = CommonsJcrUtils.getMultiAsList(node,
					propertyName);
			List<String> newValues = new ArrayList<String>();
			for (String val : oldValues) {
				if (oldValue.equals(val))
					newValues.add(newValue);
				else
					newValues.add(val);
			}
			node.setProperty(propertyName, newValues.toArray(new String[0]));

			if (wasCheckedIn)
				CommonsJcrUtils.saveAndCheckin(node);
			else
				node.getSession().save();
		} catch (RepositoryException ee) {
			throw new PeopleException(
					"Unable to update tag like multiple value property "
							+ propertyName + " removing " + oldValue
							+ " and adding " + newValue + " on " + node, ee);
		}
	}

	@Override
	public long countMembers(Node tag, String tagableParentPath,
			String tagableType, String tagPropName) {
		Query query;
		NodeIterator nit;
		try {
			Session session = tag.getSession();
			String tagValue = CommonsJcrUtils.get(tag, Property.JCR_TITLE);
			// Retrieve existing tags
			if (session.nodeExists(tagableParentPath)) {
				String queryString = "select * from [" + tagableType
						+ "] as tagged where ISDESCENDANTNODE('"
						+ tagableParentPath + "') AND tagged.[" + tagPropName
						+ "]='" + tagValue + "'";
				query = session.getWorkspace().getQueryManager()
						.createQuery(queryString, Query.JCR_SQL2);
				nit = query.execute().getNodes();
				return nit.getSize();
			}
			return 0;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to count members for " + tag, re);
		}
	}

	@Override
	public void refreshKnownTags(Session session, String tagResourceType,
			String tagParentPath, String tagableNodeType, String tagPropName,
			String tagableParentPath) {

		List<String> existingValues = new ArrayList<String>();
		List<String> registeredTags = new ArrayList<String>();

		try {
			Query query;
			NodeIterator nit;
			// Retrieve existing tags
			if (session.nodeExists(tagParentPath)) {
				query = session
						.getWorkspace()
						.getQueryManager()
						.createQuery(
								"select * from [" + tagResourceType
										+ "] as tags where ISDESCENDANTNODE('"
										+ tagParentPath + "') ", Query.JCR_SQL2);
				nit = query.execute().getNodes();
				while (nit.hasNext()) {
					Node currNode = nit.nextNode();
					String currTag = CommonsJcrUtils.get(currNode,
							Property.JCR_TITLE);
					if (CommonsJcrUtils.checkNotEmptyString(currTag)
							&& !registeredTags.contains(currTag))
						registeredTags.add(currTag.trim());
				}
			}

			// Look for not yet registered tags
			query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [" + tagableNodeType
									+ "] as instances where ISDESCENDANTNODE('"
									+ tagableParentPath + "') ", Query.JCR_SQL2);
			nit = query.execute().getNodes();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.hasProperty(tagPropName)) {
					Value[] tags = currNode.getProperty(tagPropName)
							.getValues();
					for (Value tagV : tags) {
						String currTag = tagV.getString().trim();
						if (CommonsJcrUtils.checkNotEmptyString(currTag)
								&& !registeredTags.contains(currTag))
							existingValues.add(currTag);
					}
				}
			}

			// Create tag parent if needed
			if (!session.nodeExists(tagParentPath)) {
				JcrUtils.mkdirs(session, tagParentPath);
				session.save();
			}

			// Add the newly found tags.
			for (String tag : existingValues) {
				if (!registeredTags.contains(tag)) {
					registerTag(session, tagResourceType, tagParentPath, tag);
					session.save();
				}
			}
		} catch (RepositoryException ee) {
			throw new PeopleException("Unable to refresh cache of known tags",
					ee);
		}
	}

}