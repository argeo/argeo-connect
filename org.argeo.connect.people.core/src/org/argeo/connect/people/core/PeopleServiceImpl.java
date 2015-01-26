package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
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
import org.argeo.connect.people.ContactService;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PersonService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to {@link PeopleService}  */
public class PeopleServiceImpl implements PeopleService, PeopleNames {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* Other services */
	private UserManagementService userManagementService = new UserManagementServiceImpl(
			this);
	private PersonService personService = new PersonServiceImpl(this);

	private ContactService contactService = new ContactServiceImpl(this);

	private ActivityService activityService = new ActivityServiceImpl(this,
			userManagementService);
	private ResourceService resourceService = new ResourceServiceImpl(this);

	// private Repository repository;

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		// Does nothing
		log.info("People's backend has been initialized");
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		// Does nothing
	}

	/* PATH MANAGEMENT */
	@Override
	public String getBasePath(String entityType) {
		if (entityType == null)
			return PeopleConstants.PEOPLE_BASE_PATH;

		if (BUSINESS_REL_PATHES.containsKey(entityType))
			return getBasePath(null) + "/"
					+ BUSINESS_REL_PATHES.get(entityType);
		// String parentName = getParentNameFromType(entityType);
		// if (PeopleConstants.PEOPLE_KNOWN_PARENT_NAMES.contains(parentName))
		// return getBasePath(null) + "/" + parentName;
		else
			throw new PeopleException("Unable to find base path with ID "
					+ entityType);
	}

	@Override
	public String getTmpPath() {
		return PeopleConstants.PEOPLE_TMP_PATH;
	}

	@Override
	public String getHomePath() {
		return getBasePath(null) + "/" + PeopleNames.PEOPLE_HOME;
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
		if (typeId.endsWith("y"))
			return typeId.substring(0, typeId.length() - 1) + "ies";
		else
			return typeId + "s";
	}

	@Override
	public String getDefaultPathForEntity(Node node, String nodeType) {
		String peopleUid = CommonsJcrUtils.get(node, PEOPLE_UID);
		if (CommonsJcrUtils.isEmptyString(peopleUid))
			throw new PeopleException("Unable to define default path for "
					+ node + " of type " + nodeType
					+ ". No property people:uid is defined");
		else
			return getDefaultPathForEntity(peopleUid, nodeType);
	}

	@Override
	public String getDefaultPathForEntity(String peopleUid, String nodeType) {
		String path = getBasePath(nodeType) + "/";
		path += JcrUtils.firstCharsToPath(peopleUid, 2);
		return path;
	}

	/* DEFINITION OF THE PEOPLE DEFAULT TREE STRUCTURE */

	/** Creates various useful parent nodes if needed */
	protected void initialiseModel(Session adminSession)
			throws RepositoryException {
		JcrUtils.mkdirs(adminSession, getBasePath(null));// Root business node
		JcrUtils.mkdirs(adminSession, getTmpPath());// Root tmp node

		// Various business parents
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_PERSON));
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_ORG));
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_ACTIVITY));
		JcrUtils.mkdirs(adminSession,
				getBasePath(PeopleConstants.PEOPLE_RESOURCE)); // Resources

		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Repository has been initialised "
					+ "with default People's model");
		}
	}

	// Defines a mapping between main people concepts and their base path in the
	// system
	private static final Map<String, String> BUSINESS_REL_PATHES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(PeopleTypes.PEOPLE_ACTIVITY, PeopleNames.PEOPLE_ACTIVITIES);
		tmpMap.put(PeopleTypes.PEOPLE_PERSON, PeopleNames.PEOPLE_PERSONS);
		tmpMap.put(PeopleTypes.PEOPLE_ORG, PeopleNames.PEOPLE_ORGS);
		tmpMap.put(PeopleConstants.PEOPLE_RESOURCE,
				PeopleNames.PEOPLE_RESOURCES);
		tmpMap.put(PeopleConstants.PEOPLE_PROJECT, PeopleNames.PEOPLE_PROJECTS);
		tmpMap.put(PeopleTypes.PEOPLE_USER_GROUP,
				PeopleNames.PEOPLE_USER_GROUPS);
		BUSINESS_REL_PATHES = Collections.unmodifiableMap(tmpMap);
	}

	/* ENTITY SERVICES */
	public void saveEntity(Node entity, boolean commit) throws PeopleException {
		try {

			if (entity.isNodeType(PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE)
					|| entity.isNodeType(PeopleTypes.PEOPLE_TAG_INSTANCE)
					|| entity.isNodeType(PeopleTypes.PEOPLE_NODE_TEMPLATE)) {
				// Known types that does not have a specific save strategy
				if (commit)
					CommonsJcrUtils.saveAndCheckin(entity);
				else
					entity.getSession().save();
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)
					|| entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				personService.saveEntity(entity, commit);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ACTIVITY))
				// TODO implement generic People behaviour for tasks and
				// activities
				if (commit)
					CommonsJcrUtils.saveAndCheckin(entity);
				else
					entity.getSession().save();
			else
				throw new PeopleException("Unknown entity type for " + entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to save " + entity, e);
		}
	}

	/**
	 * Typically used to move temporary import nodes to the main business
	 * repository
	 */
	@Override
	public void checkPathAndMoveIfNeeded(Node entity, String entityNodeType)
			throws RepositoryException {
		String peopleUid = entity.getProperty(PEOPLE_UID).getString();
		String destPath = getDefaultPathForEntity(peopleUid, entityNodeType);
		if (!destPath.equals(entity.getParent().getPath())) {
			JcrUtils.mkdirs(entity.getSession(), destPath);
			entity.getSession().move(entity.getPath(),
					destPath + "/" + peopleUid);
		}
	}

	/** Simply look for primary information and update primary cache if needed */
	@Override
	public void updatePrimaryCache(Node entity) throws PeopleException,
			RepositoryException {
		if (CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG)) {
			for (String currType : PeopleTypes.KNOWN_CONTACT_TYPES) {
				Node pNode = PeopleJcrUtils.getPrimaryContact(entity, currType);
				if (pNode != null)
					PeopleJcrUtils
							.updatePrimaryCache(this, entity, pNode, true);
			}
		} else
			log.warn("Trying to update primary cache on " + entity
					+ " - Unknown type.");
	}

	@Override
	public String getDisplayName(Node entity) {
		String displayName = null;
		try {
			boolean defineDistinct = false;
			if (entity.hasProperty(PEOPLE_USE_DISTINCT_DISPLAY_NAME))
				defineDistinct = entity.getProperty(
						PEOPLE_USE_DISTINCT_DISPLAY_NAME).getBoolean();
			if (defineDistinct)
				displayName = CommonsJcrUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				displayName = getPersonService().getDisplayName(entity);
			} else if (entity.isNodeType(NodeType.MIX_TITLE))
				displayName = CommonsJcrUtils.get(entity, Property.JCR_TITLE);
			else
				throw new PeopleException("Display name not defined for type "
						+ entity.getPrimaryNodeType().getName() + " - node: "
						+ entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get display name for node "
					+ entity, e);
		}
		return displayName;
	}

	@Override
	public Node getEntityByUid(Session session, String uid) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);

			// only look in business path
			Constraint c1 = factory.descendantNode(source.getSelectorName(),
					getBasePath(null));

			// Retrieve correct ID
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(uid));
			Constraint c2 = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);

			// effecetive query
			QueryObjectModel query = factory.createQuery(source,
					factory.and(c1, c2), null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 0)
				return null;
			else if (ni.getSize() > 1) {
				throw new PeopleException(
						"Problem retrieving entity by UID, we found "
								+ ni.getSize() + " corresponding entity(ies)");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve entity of uid: "
					+ uid, e);
		}
	}

	@Override
	public Node getEntityFromNodeReference(Node node, String propName) {
		try {
			String peopleUid = CommonsJcrUtils.get(node, propName);
			if (CommonsJcrUtils.isEmptyString(peopleUid))
				return null;
			else
				return getEntityByUid(node.getSession(), peopleUid);
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get entity from reference node " + node
							+ " with ref property " + propName, re);
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
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORG)) {
					linkNodeType = PeopleTypes.PEOPLE_JOB;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_JOBS);
				}
				// } else if (referencingNode
				// .isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				// if (referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				// linkNodeType = PeopleTypes.PEOPLE_MAILING_LIST_ITEM;
				// parentNode = referencingNode
				// .getNode(PeopleNames.PEOPLE_MEMBERS);
				// }
			} else if (referencingNode.isNodeType(PeopleTypes.PEOPLE_GROUP)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORG)
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
			if (!entity.hasProperty(PeopleNames.PEOPLE_UID))
				return null;

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

	/* EXPOSED SERVICES */
	@Override
	public ActivityService getActivityService() {
		return activityService;
	}

	@Override
	public PersonService getPersonService() {
		return personService;
	}

	@Override
	public ContactService getContactService() {
		return contactService;
	}

	@Override
	public ResourceService getResourceService() {
		return resourceService;
	}

	@Override
	public UserManagementService getUserManagementService() {
		return userManagementService;
	}

	// protected Repository getRepository() {
	// return repository;
	// }

	// HELPERS

	/* MISCEALLENEOUS */
	@Override
	/** Override to define app specific properties that are not system properties */
	public String getConfigProperty(String key) {
		return System.getProperty(key);
	}

	// /* DEPENDENCY INJECTION */
	// TODO remove this unused method and the corresponding injection in the
	// spring XML files.
	public void setBusinessCatalogs(Map<String, Object> businessCatalogs) {
		// Inject a map with all business catalogs
		// this.businessCatalogs = businessCatalogs;
	}

}
