package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people services */
public class PeopleServiceImpl implements PeopleService, PeopleNames {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* DEPENDENCY INJECTION */
	// private Repository repository;

	/* Other services */
	private UserManagementService userManagementService = new UserManagementServiceImpl(
			this);
	private ActivityService activityService = new ActivityServiceImpl(this,
			userManagementService);

	// private Map<Integer, String> managedRoles;

	// business catalogs maintained in file business catalogs of the specs
	// bundle
	// private Map<String, Object> businessCatalogs;

	// private Session adminSession = null;

	// SHOULD BE IN PeopleConstants but it does not work
	// TODO investigate
	// Initialize the map between node types and their names
	// public static Map<String, String> BASE_TYPE_NAMES;
	// static {
	// Map<String, String> tmpMap = new HashMap<String, String>();
	// {
	// tmpMap.put(PeopleTypes.PEOPLE_ACTIVITY,
	// PeopleNames.PEOPLE_ACTIVITIES);
	// }
	// BASE_TYPE_NAMES = Collections.unmodifiableMap(tmpMap);
	// }

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		// Do nothing
		log.info("People's backend has been initialized");
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		// Do nothing
	}

	/* BASE PATH MANAGEMENT */
	@Override
	@Deprecated
	public String getBasePath() {
		return getBasePath(null);
	}

	@Override
	public String getBasePath(String entityType) {
		if (entityType == null)
			return PeopleConstants.PEOPLE_BASE_PATH;

		String parentName = getParentNameFromType(entityType);
		if (PeopleConstants.PEOPLE_KNOWN_PARENT_NAMES.contains(parentName))
			return getBasePath(null) + "/" + parentName;
		else
			throw new PeopleException("Unable to find base path for type: "
					+ parentName);
	}

	@Override
	public String getResourceBasePath(String resourceType) {
		// resourceType
		if (resourceType == null)
			return getBasePath(PeopleConstants.PEOPLE_RESOURCE);

		String parentName = getParentNameFromType(resourceType);
		if (PeopleConstants.KNOWN_RESOURCE_NAMES.contains(parentName))
			return getResourceBasePath(null) + "/" + parentName;
		else
			throw new PeopleException("Undefined type: " + parentName);
	}

	@Override
	public String getResourcePath(String resourceType, String resourceId) {
		// TODO clean this
		return getResourceBasePath(resourceType) + "/" + resourceId;
	}

	// Clean this: small helper to retrieve the parent node name given a
	// NodeType or a Property name
	protected String getParentNameFromType(String typeId) {
		// TODO refactor to clean this
		if (typeId.equals(PeopleTypes.PEOPLE_ORGANIZATION))
			return PeopleNames.PEOPLE_ORGS;
		else if (typeId.endsWith("y"))
			return typeId.substring(0, typeId.length() - 1) + "ies";
		else
			return typeId + "s";
	}

	/* ENTITY SERVICES */
	@Override
	public Node getEntityByUid(Session session, String uid) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					"entities");
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(uid));
			Constraint defaultC = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 0)
				return null;
			else if (ni.getSize() > 1) {
				throw new PeopleException(
						"Problem retrieving entity by UID, we found "
								+ ni.getSize() + " correspnding entity(ies)");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to retrive entity of uid: " + uid, e);
		}
	}

	@Override
	public Node getEntityFromNodeReference(Node node, String propName) {
		try {
			return getEntityByUid(node.getSession(), node.getProperty(propName)
					.getString());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get entity from reference node", re);
		}
	}

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link together. Overwrite to add some new
	 * link type
	 * */
	@Override
	public Node createEntityReference(Node referencingNode,
			Node referencedNode, String role) {
		try {
			Node parentNode = null;
			String linkNodeType = null;
			if (referencingNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)) {
					linkNodeType = PeopleTypes.PEOPLE_JOB;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_JOBS);
				}
			} else if (referencingNode
					.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					linkNodeType = PeopleTypes.PEOPLE_MAILING_LIST_ITEM;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			} else if (referencingNode.isNodeType(PeopleTypes.PEOPLE_GROUP)
					&& !referencingNode
							.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)
						|| referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)
						|| (referencedNode.isNodeType(PeopleTypes.PEOPLE_GROUP) && referencedNode
								.getIdentifier() != referencingNode
								.getIdentifier())) {
					linkNodeType = PeopleTypes.PEOPLE_MEMBER;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			}
			if (parentNode == null || linkNodeType == null)
				throw new PeopleException("Unsupported reference: from "
						+ referencingNode + "("
						+ referencingNode.getPrimaryNodeType() + ")" + " to "
						+ referencedNode + "("
						+ referencedNode.getPrimaryNodeType() + ")");

			// TODO manage duplicates
			boolean wasCheckedout = CommonsJcrUtils
					.isNodeCheckedOutByMe(referencingNode);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(referencingNode);

			// add the corresponding node

			Node link = parentNode.addNode(CommonsJcrUtils
					.checkNotEmptyString(role) ? role : "Unnamed_role",
					linkNodeType);
			if (CommonsJcrUtils.checkNotEmptyString(role))
				link.setProperty(PeopleNames.PEOPLE_ROLE, role);
			link.setProperty(PeopleNames.PEOPLE_REF_UID, referencedNode
					.getProperty(PeopleNames.PEOPLE_UID).getString());

			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(referencingNode);
			else
				referencingNode.getSession().save();

			return link;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve related entities", e);
		}
	}

	@Override
	public List<Node> getRelatedEntities(Node entity, String linkNodeType,
			String relatedEntityType) {
		try {
			Session session = entity.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			final String typeSelector = "relatedType";
			Selector source = factory.selector(linkNodeType, typeSelector);
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(
							entity.getProperty(PeopleNames.PEOPLE_UID)
									.getString()));
			Constraint defaultC = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (relatedEntityType == null)
				return JcrUtils.nodeIteratorToList(ni);
			else {

				List<Node> result = new ArrayList<Node>();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.getParent().getParent()
							.isNodeType(relatedEntityType))
						result.add(currNode);
				}
				return result;
			}

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve related entities", e);
		}

	}

	/* PERSONS AND ORGANISATIONS */

	@Override
	public Node createOrUpdateJob(Node oldJob, Node person, Node organisation,
			String position, String department, boolean isPrimary) {
		// A shortcut to have a node name even when no position is given
		String newNodeName = CommonsJcrUtils.checkNotEmptyString(position) ? position
				: "Unnamed_job";

		// The job on which to update various info
		Node newJob = null;

		try {
			// First check if we must remove the old job when linked person has
			// changed
			if (oldJob != null) {
				Node oldPerson = oldJob.getParent().getParent();
				String oldPath = oldPerson.getPath();
				String newPath = person.getPath();
				if (!newPath.equals(oldPath)) {
					// remove old
					boolean wasCO = checkCOStatusBeforeUpdate(oldPerson);
					oldJob.remove();
					checkCOStatusAfterUpdate(oldPerson, wasCO);
				} else
					newJob = oldJob;
			}

			boolean wasCO = checkCOStatusBeforeUpdate(person);
			// Create node if necessary
			if (newJob == null) {
				Node parentNode = JcrUtils.mkdirs(person,
						PeopleNames.PEOPLE_JOBS, NodeType.NT_UNSTRUCTURED);
				newJob = parentNode
						.addNode(newNodeName, PeopleTypes.PEOPLE_JOB);
			}

			// update properties

			// Related org
			newJob.setProperty(PeopleNames.PEOPLE_REF_UID, organisation
					.getProperty(PeopleNames.PEOPLE_UID).getString());

			// position
			if (position == null)
				position = ""; // to be able to remove an existing position
			newJob.setProperty(PeopleNames.PEOPLE_ROLE, position);
			// TODO update node name ??

			// department
			if (department == null)
				department = "";
			newJob.setProperty(PeopleNames.PEOPLE_DEPARTMENT, department);

			// primary flag
			if (isPrimary)
				PeopleJcrUtils.markAsPrimary(person, newJob);
			else
				newJob.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, isPrimary);

			checkCOStatusAfterUpdate(person, wasCO);
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create or update job "
					+ oldJob + " for person " + person + " and org "
					+ organisation, re);
		}
		return null;
	}

	// TODO mov this in a filmService
	@Override
	public Node createOrUpdateParticipation(Node oldParticipation, Node film,
			Node contact, String role) {
		// A shortcut to have a node name even when no position is given
		String newNodeName = CommonsJcrUtils.checkNotEmptyString(role) ? role
				: "Unnamed_role";

		// The position on which to update various info
		Node newParticipation = null;

		try {
			// First check if we must remove the old job when linked person has
			// changed
			if (oldParticipation != null) {
				Node oldFilm = oldParticipation.getParent().getParent();
				String oldPath = oldFilm.getPath();
				String newPath = film.getPath();
				if (!newPath.equals(oldPath)) {
					// remove old
					boolean wasCO = checkCOStatusBeforeUpdate(oldFilm);
					oldParticipation.remove();
					checkCOStatusAfterUpdate(oldFilm, wasCO);
				} else
					newParticipation = oldParticipation;
			}

			boolean wasCO = checkCOStatusBeforeUpdate(film);
			// Create node if necessary
			if (newParticipation == null) {
				Node parentNode = JcrUtils.mkdirs(film,
						PeopleNames.PEOPLE_MEMBERS, NodeType.NT_UNSTRUCTURED);
				newParticipation = parentNode.addNode(newNodeName,
						PeopleTypes.PEOPLE_MEMBER);
			}

			// update properties

			// Related org
			newParticipation.setProperty(PeopleNames.PEOPLE_REF_UID, contact
					.getProperty(PeopleNames.PEOPLE_UID).getString());

			// position
			if (role == null)
				role = ""; // to be able to reset value if an existing role
			newParticipation.setProperty(PeopleNames.PEOPLE_ROLE, role);
			// TODO update node name ??

			checkCOStatusAfterUpdate(film, wasCO);
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create or update job "
					+ oldParticipation + " for person " + film + " and org "
					+ contact, re);
		}
		return null;
	}

	private boolean checkCOStatusBeforeUpdate(Node node) {
		boolean wasCheckedOut = CommonsJcrUtils.isNodeCheckedOutByMe(node);
		if (!wasCheckedOut)
			CommonsJcrUtils.checkout(node);
		return wasCheckedOut;
	}

	private void checkCOStatusAfterUpdate(Node node, boolean wasCheckedOut)
			throws RepositoryException {
		if (!wasCheckedOut)
			CommonsJcrUtils.saveAndCheckin(node);
		else
			node.getSession().save();
	}

	// ///////////////////////
	// TAGS Management
	@Override
	public void refreshKnownTags(Session session) {
		refreshKnownTags(session,
				getResourceBasePath(PeopleConstants.RESOURCE_TAG),
				getBasePath(null));
	}

	@Override
	public void refreshKnownTags(Session session, String tagParentPath,
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
								"select * from [" + NodeType.MIX_TITLE
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
							"select * from [" + PeopleTypes.PEOPLE_TAGABLE
									+ "] as instances where ISDESCENDANTNODE('"
									+ tagableParentPath + "') ", Query.JCR_SQL2);
			nit = query.execute().getNodes();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.hasProperty(PeopleNames.PEOPLE_TAGS)) {
					Value[] tags = currNode
							.getProperty(PeopleNames.PEOPLE_TAGS).getValues();
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

			// real update
			for (String tag : existingValues) {
				if (!registeredTags.contains(tag)) {
					registerTag(session, tagParentPath, tag);
					session.save();
				}
			}
		} catch (RepositoryException ee) {
			throw new PeopleException("Unable to refresh cache of known tags",
					ee);
		}
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
	public Node registerTag(Session session, String tagParentPath, String tag)
			throws RepositoryException {
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
		Node newTag = JcrUtils.mkdirs(session, path);
		newTag.addMixin(NodeType.MIX_TITLE);
		newTag.setProperty(Property.JCR_TITLE, tag);
		// } catch (RepositoryException ee) {
		// throw new PeopleException("Unable to add new tag " + tag
		// + " under " + tagParentPath);
		// }
		return newTag;
	}

	@Override
	public void unregisterTag(Session session, String tagParentPath,
			String tag, String tagableParentPath) {
		// TODO Auto-generated method stub
		throw new PeopleException("unimplemented method.");
	}

	protected String getTagRelPath(String tag) {
		// remove trailing and starting space
		tag = tag.trim();
		String cleanedTag = JcrUtils.replaceInvalidChars(tag).trim();
		String relPath = JcrUtils.firstCharsToPath(cleanedTag, 2);
		return relPath + "/" + cleanedTag;
	}

	/* EXPOSED CLASSES */
	@Override
	public ActivityService getActivityService() {
		return activityService;
	}

	@Override
	public UserManagementService getUserManagementService() {
		return userManagementService;
	}

	//
	// // TODO remove this we rather want to directly inject the repository if
	// // needed
	// /** Expose injected repository */
	// private Repository getRepository() {
	// return repository;
	// }

	/* DEPENDENCY INJECTION */
	// Inject a map with all business catalogs
	public void setBusinessCatalogs(Map<String, Object> businessCatalogs) {
		// this.businessCatalogs = businessCatalogs;
	}

	public void setRepository(Repository repository) {
		// this.repository = repository;
	}

	// public void setManagedRoles(Map<Integer, String> managedRoles) {
	// this.managedRoles = managedRoles;
	// }
}
