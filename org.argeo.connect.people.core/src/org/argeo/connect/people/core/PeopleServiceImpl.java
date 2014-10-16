package org.argeo.connect.people.core;

import java.util.ArrayList;
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
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people services */
public class PeopleServiceImpl implements PeopleService, PeopleNames {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* Other services */
	private UserManagementService userManagementService = new UserManagementServiceImpl(
			this);
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

		String parentName = getParentNameFromType(entityType);
		if (PeopleConstants.PEOPLE_KNOWN_PARENT_NAMES.contains(parentName))
			return getBasePath(null) + "/" + parentName;
		else
			throw new PeopleException("Unable to find base path for type: "
					+ parentName);
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

	/* ENTITY SERVICES */
	public void saveEntity(Node entity, boolean commit) throws PeopleException {
		try {
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				savePerson(entity, commit);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				saveOrganisation(entity, commit);
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
	 * Business specific save of a business object of type person. Among other,
	 * it updates cache information. Extend to provide business specific rules
	 * before save and commit
	 */
	protected void savePerson(Node person, boolean commit)
			throws PeopleException, RepositoryException {
		String lastName = CommonsJcrUtils.get(person,
				PeopleNames.PEOPLE_LAST_NAME);
		String firstName = CommonsJcrUtils.get(person,
				PeopleNames.PEOPLE_FIRST_NAME);
		Boolean defineDistinctDefaultDisplay = CommonsJcrUtils.getBooleanValue(
				person, PEOPLE_USE_DISTINCT_DISPLAY_NAME);
		String displayName = null;

		// Update display name cache if needed
		if (defineDistinctDefaultDisplay == null
				|| !defineDistinctDefaultDisplay) {
			displayName = getDisplayName(person);
			person.setProperty(Property.JCR_TITLE, displayName);
		} else
			displayName = CommonsJcrUtils.get(person, Property.JCR_TITLE);

		// Check validity of main info
		if (CommonsJcrUtils.isEmptyString(lastName)
				&& CommonsJcrUtils.isEmptyString(firstName)
				&& CommonsJcrUtils.isEmptyString(displayName)) {
			String msg = "Please note that you must define a first name, a "
					+ "last name or a display name to be able to create or "
					+ "update this person.";
			throw new PeopleException(msg);
		}

		// Update cache
		updatePrimaryCache(person);

		checkPathAndMoveIfNeeded(person, PeopleTypes.PEOPLE_PERSON);
		if (commit)
			CommonsJcrUtils.saveAndCheckin(person);
		else
			person.getSession().save();
	}

	/** Override to provide business specific rules before save and commit */
	protected void saveOrganisation(Node org, boolean commit)
			throws PeopleException, RepositoryException {
		// Update cache
		updatePrimaryCache(org);

		// Check validity of main info
		String legalName = CommonsJcrUtils.get(org,
				PeopleNames.PEOPLE_LEGAL_NAME);

		Boolean defineDistinctDefaultDisplay = CommonsJcrUtils.getBooleanValue(
				org, PEOPLE_USE_DISTINCT_DISPLAY_NAME);

		String displayName;

		if (defineDistinctDefaultDisplay == null
				|| !defineDistinctDefaultDisplay) {
			displayName = getDisplayName(org);
			org.setProperty(Property.JCR_TITLE, legalName);
		} else
			displayName = CommonsJcrUtils.get(org, Property.JCR_TITLE);

		if (CommonsJcrUtils.isEmptyString(legalName)
				&& CommonsJcrUtils.isEmptyString(displayName)) {
			String msg = "Please note that you must define a legal "
					+ " or a display name to be able to create or "
					+ "update this organisation.";
			throw new PeopleException(msg);
		}

		// Finalise save process.
		checkPathAndMoveIfNeeded(org, PeopleTypes.PEOPLE_ORG);
		if (commit)
			CommonsJcrUtils.saveAndCheckin(org);
		else
			org.getSession().save();
	}

	/**
	 * Typically used to move temporary import nodes to the main business
	 * repository
	 */
	protected void checkPathAndMoveIfNeeded(Node entity, String entityNodeType)
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
	protected void updatePrimaryCache(Node entity) throws PeopleException,
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
				String lastName = CommonsJcrUtils.get(entity, PEOPLE_LAST_NAME);
				String firstName = CommonsJcrUtils.get(entity,
						PEOPLE_FIRST_NAME);
				if (CommonsJcrUtils.checkNotEmptyString(firstName)
						|| CommonsJcrUtils.checkNotEmptyString(lastName)) {
					displayName = lastName;
					if (CommonsJcrUtils.checkNotEmptyString(firstName)
							&& CommonsJcrUtils.checkNotEmptyString(lastName))
						displayName += ", ";
					displayName += firstName;
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				// Default display is simply the legal name
				displayName = CommonsJcrUtils.get(entity, PEOPLE_LEGAL_NAME);
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
				PeopleJcrUtils.markAsPrimary(this, person, newJob);
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

	protected boolean checkCOStatusBeforeUpdate(Node node) {
		boolean wasCheckedOut = CommonsJcrUtils.isNodeCheckedOutByMe(node);
		if (!wasCheckedOut)
			CommonsJcrUtils.checkout(node);
		return wasCheckedOut;
	}

	protected void checkCOStatusAfterUpdate(Node node, boolean wasCheckedOut)
			throws RepositoryException {
		if (!wasCheckedOut)
			CommonsJcrUtils.saveAndCheckin(node);
		else
			node.getSession().save();
	}

	/* EXPOSED SERVICES */
	@Override
	public ActivityService getActivityService() {
		return activityService;
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

	/* MISCEALLENEOUS */
	@Override
	/** Override to define app specific properties that are not system properties */
	public String getConfigProperty(String key) {
		return System.getProperty(key);
	}

	// /* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }

	// TODO remove this unused method and the corresponding injection in the
	// spring XML files.
	public void setBusinessCatalogs(Map<String, Object> businessCatalogs) {
		// Inject a map with all business catalogs
		// this.businessCatalogs = businessCatalogs;
	}

}
