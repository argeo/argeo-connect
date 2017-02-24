package org.argeo.connect.people.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.people.ContactService;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PersonService;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.people.util.PersonJcrUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/** Concrete access to {@link PeopleService} */
public class PeopleServiceImpl implements PeopleService, PeopleNames {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* DEPENDENCY INJECTION */
	// private UserAdminService userAdminService;
	private ResourcesService resourcesService;
	// private ActivitiesService activitiesService;

	/* Centralizes the various specific People services */
	private PersonService personService;
	private ContactService contactService = new ContactServiceImpl(this);

	@Override
	public String getAppBaseName() {
		return PeopleConstants.PEOPLE_APP_BASE_NAME;
	}

	@Override
	public String getDisplayName(Node entity) {
		String displayName = null;
		try {
			boolean defineDistinct = false;
			if (entity.hasProperty(PEOPLE_USE_DISTINCT_DISPLAY_NAME))
				defineDistinct = entity.getProperty(PEOPLE_USE_DISTINCT_DISPLAY_NAME).getBoolean();
			if (defineDistinct)
				displayName = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				displayName = getPersonService().getDisplayName(entity);
			} else if (entity.isNodeType(NodeType.MIX_TITLE))
				displayName = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else
				throw new PeopleException("Display name not defined for type " + entity.getPrimaryNodeType().getName()
						+ " - node: " + entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get display name for node " + entity, e);
		}
		return displayName;
	}

	@Override
	public boolean isKnownType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_MAILING_LIST))
			return true;
		else
			return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) || PeopleTypes.PEOPLE_ORG.equals(nodeType)
				|| PeopleTypes.PEOPLE_MAILING_LIST.equals(nodeType))
			return true;
		else
			return false;
	}

	@Override
	public Node saveEntity(Node entity, boolean publish) throws PeopleException {
		try {
			if (entity.isNodeType(ResourcesTypes.RESOURCES_ENCODED_TAG)
					|| entity.isNodeType(ResourcesTypes.RESOURCES_TAG)
					|| entity.isNodeType(ResourcesTypes.RESOURCES_NODE_TEMPLATE)) {
				// Known types that does not have a specific save strategy
				ConnectJcrUtils.saveAndPublish(entity, publish);
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON) || entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				entity = getPersonService().saveEntity(entity, publish);
			// else if (entity.isNodeType(PeopleTypes.PEOPLE_ACTIVITY))
			// // TODO implement specific behavior for tasks and activities
			// ConnectJcrUtils.saveAndPublish(entity, publish);
			else
				throw new PeopleException("Unknown entity type for " + entity);
			return entity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to save " + entity, e);
		}
	}

	@Override
	public Node getDraftParent(Session session) throws RepositoryException {
		Node home = NodeUtils.getUserHome(session);
		String draftRelPath = ConnectConstants.HOME_APP_SYS_RELPARPATH + "/" + getAppBaseName();
		return JcrUtils.mkdirs(home, draftRelPath);
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		String peopleUid = ConnectJcrUtils.get(entity, ConnectNames.CONNECT_UID);
		if (isEmpty(peopleUid))
			throw new PeopleException(
					"Unable to define default path for " + entity + ". No property people:uid is defined");
		else
			return getDefaultRelPath(null, peopleUid);
	}

	@Override
	public String getDefaultRelPath(String nodeType, String peopleUid) {
		String path = JcrUtils.firstCharsToPath(peopleUid, 2) + "/" + peopleUid;
		return path;
	}

	/* PEOPLE APP SPECIFIC METHODS */

	//
	// @Override
	// public String getTmpPath() {
	// return PeopleConstants.PEOPLE_TMP_PATH;
	// }

	// @Override
	// public String getPublicPath() {
	// return PeopleConstants.PEOPLE_PUBLIC_PATH;
	// }

	// @Override
	// public String getInstanceConfPath() {
	// return getBasePath(null) + "/" + PeopleNames.PEOPLE_CONF;
	// }
	//
	// @Override
	// public String getResourceBasePath(String resourceType) {
	// if (resourceType == null)
	// return getBasePath(PeopleConstants.PEOPLE_RESOURCE);
	// else
	// throw new PeopleException("Undefined type: " + resourceType);
	// }
	//
	// @Override
	// public String getResourcePath(String resourceType, String resourceId) {
	// // TODO clean this
	// return getResourceBasePath(resourceType) + "/" + resourceId;
	// }

	// Clean this: retrieve a parent node name given a NodeType or a Property
	// name
	// protected String getParentNameFromType(String typeId) {
	// if (typeId.endsWith("y"))
	// return typeId.substring(0, typeId.length() - 1) + "ies";
	// else
	// return typeId + "s";
	// }

	// /* DEFINITION OF THE PEOPLE DEFAULT TREE STRUCTURE */
	// /** Creates various useful parent nodes if needed */
	// protected void initialiseModel(Session adminSession) throws
	// RepositoryException {
	//
	// // Root business node
	// if (EclipseUiUtils.notEmpty(getBasePath(null)))
	// JcrUtils.mkdirs(adminSession, getBasePath(null));
	//
	// // JcrUtils.mkdirs(adminSession, getTmpPath());// Root tmp node
	// // JcrUtils.mkdirs(adminSession, getPublicPath());// Root public node
	//
	// // Various business parents
	// JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_PERSON));
	// JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_ORG));
	// // JcrUtils.mkdirs(adminSession,
	// // getBasePath(PeopleTypes.PEOPLE_ACTIVITY));
	// JcrUtils.mkdirs(adminSession,
	// getBasePath(PeopleConstants.PEOPLE_RESOURCE)); // Resources
	//
	// if (adminSession.hasPendingChanges()) {
	// adminSession.save();
	// log.info("Repository has been initialised " + "with default People's
	// model");
	// }
	// }

	//
	// /**
	// * Typically used to move temporary import nodes to the main business
	// * repository
	// *
	// * FIXME: induce the necessity to give JCR_READ right to almost everyone
	// * that might modify a node in the repository: the mkdir method using the
	// * session needs having JCR read rights on all nodes from the root node to
	// * function correctly
	// */
	//
	// @Override
	// public Node checkPathAndMoveIfNeeded(Node entity, String entityNodeType)
	// throws RepositoryException {
	// String destPath = getDefaultPath(entityNodeType, entity);
	// if (destPath.equals(entity.getPath()))
	// return entity;
	// else {
	// // FIXME there are strange side effects for user that have no read
	// // access on root if the save is not made regularly on this method.
	// // Find the bug and fix.
	// Session session = entity.getSession();
	// String parPath = JcrUtils.parentPath(destPath);
	// String typeBasePath = getBasePath(entityNodeType);
	// String parRelPath = null;
	// if (parPath.startsWith(typeBasePath))
	// parRelPath = parPath.substring(typeBasePath.length() + 1);
	// else
	// throw new PeopleException("Unable to move entity of type " +
	// entityNodeType + ", Computed parent path "
	// + parPath + " does not match.");
	// session.save();
	// Node parNode = JcrUtils.mkdirs(session.getNode(typeBasePath), parRelPath,
	// NodeType.NT_UNSTRUCTURED,
	// NodeType.NT_UNSTRUCTURED);
	// session.save();
	// Node target = parNode.addNode(JcrUtils.lastPathElement(destPath),
	// entity.getPrimaryNodeType().getName());
	// RemoteJcrUtils.copy(entity, target, true);
	// updateReferenceAfterMove(target, entity.getIdentifier(),
	// target.getIdentifier());
	// session.save();
	// entity.remove();
	// session.save();
	// return target;
	// }
	// }
	//
	// protected void updateReferenceAfterMove(Node currentNode, String
	// oldJcrId, String newJcrId)
	// throws RepositoryException {
	// PropertyIterator pit = currentNode.getProperties();
	// while (pit.hasNext()) {
	// Property prop = pit.nextProperty();
	// if (prop.getType() == PropertyType.REFERENCE || prop.getType() ==
	// PropertyType.WEAKREFERENCE) {
	// if (prop.isMultiple()) {
	// Value[] values = prop.getValues();
	// List<String> newIds = new ArrayList<String>();
	// boolean hasChanged = false;
	//
	// for (Value val : values) {
	// String currValueStr = val.getString();
	// if (oldJcrId.equals(currValueStr)) {
	// newIds.add(newJcrId);
	// hasChanged = true;
	// } else
	// newIds.add(currValueStr);
	// }
	// if (hasChanged)
	// prop.setValue(newIds.toArray(new String[0]));
	// } else if (oldJcrId.equals(prop.getString()))
	// prop.setValue(newJcrId);
	// }
	// }
	//
	// NodeIterator nit = currentNode.getNodes();
	// while (nit.hasNext())
	// updateReferenceAfterMove(nit.nextNode(), oldJcrId, newJcrId);
	// }

	/**
	 * Simply look for primary information and update primary cache if needed
	 */
	@Override
	public void updatePrimaryCache(Node entity) throws PeopleException, RepositoryException {
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG)) {
			for (String currType : PeopleTypes.KNOWN_CONTACT_TYPES) {
				Node pNode = PeopleJcrUtils.getPrimaryContact(entity, currType);
				if (pNode != null)
					PeopleJcrUtils.updatePrimaryCache(resourcesService, this, entity, pNode, true);
			}

			// Also update primary job
			if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)) {
				Node pJob = PersonJcrUtils.getPrimaryJob(entity);
				if (pJob != null)
					PeopleJcrUtils.updatePrimaryCache(resourcesService, this, entity, pJob, true);
			}
		} else
			log.warn("Trying to update primary cache on " + entity + " - Unknown type.");
	}

	@Override
	public Node getEntityByUid(Session session, String parentPath, String uid) {
		if (isEmpty(uid))
			throw new PeopleException("Cannot get entity by id by providing an empty people:uid");
		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			String xpathQueryStr = XPathUtils.descendantFrom(parentPath) + "//element(*, " + ConnectTypes.CONNECT_ENTITY
					+ ")";
			String attrQuery = XPathUtils.getPropertyEquals(ConnectNames.CONNECT_UID, uid);
			if (notEmpty(attrQuery))
				xpathQueryStr += "[" + attrQuery + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			QueryResult result = xpathQuery.execute();
			NodeIterator ni = result.getNodes();

			long niSize = ni.getSize();
			if (niSize == 0)
				return null;
			else if (niSize > 1) {
				// TODO find a way to include the calling stack in the thrown
				// Exception
				log.error("Found " + niSize + " entities with PeopleUID [" + uid + "] - calling stack:\n "
						+ Thread.currentThread().getStackTrace().toString());
				Node first = ni.nextNode();
				throw new PeopleException(
						"Found " + niSize + " entities for People UID [" + uid + "], First occurence info:\npath: "
								+ first.getPath() + ", node type: " + first.getPrimaryNodeType().getName() + "");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve entity with people uid: [" + uid + "]", e);
		}
	}

	@Override
	public Node getEntityFromNodeReference(Node node, String propName) {
		try {
			String peopleUid = ConnectJcrUtils.get(node, propName);
			if (isEmpty(peopleUid))
				return null;
			else
				return getEntityByUid(node.getSession(), null, peopleUid);
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get entity from reference node " + node + " with ref property " + propName, re);
		}
	}

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link together. Overwrite to add some new
	 * link type
	 */
	@Override
	public Node createEntityReference(Node referencingNode, Node referencedNode, String role) {
		try {
			Node parentNode = null;
			String linkNodeType = null;
			if (referencingNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORG)) {
					linkNodeType = PeopleTypes.PEOPLE_JOB;
					parentNode = referencingNode.getNode(PeopleNames.PEOPLE_JOBS);
				}
			} else if (referencingNode.isNodeType(PeopleTypes.PEOPLE_GROUP)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORG)
						|| referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)
						|| (referencedNode.isNodeType(PeopleTypes.PEOPLE_GROUP)
								&& referencedNode.getIdentifier() != referencingNode.getIdentifier())) {
					linkNodeType = PeopleTypes.PEOPLE_MEMBER;
					parentNode = referencingNode.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			}
			if (parentNode == null || linkNodeType == null)
				throw new PeopleException(
						"Unsupported reference: from " + referencingNode + "(" + referencingNode.getPrimaryNodeType()
								+ ")" + " to " + referencedNode + "(" + referencedNode.getPrimaryNodeType() + ")");

			// Legacy: force node to be checked-out
			if (!ConnectJcrUtils.checkCOStatusBeforeUpdate(referencingNode))
				log.warn("Referencing node " + referencingNode + " was checked in when we wanted to update");

			Node link = parentNode.addNode(notEmpty(role) ? role : "Unnamed_role", linkNodeType);
			if (notEmpty(role))
				link.setProperty(PeopleNames.PEOPLE_ROLE, role);
			link.setProperty(PeopleNames.PEOPLE_REF_UID,
					referencedNode.getProperty(ConnectNames.CONNECT_UID).getString());
			referencingNode.getSession().save();

			return link;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve related entities", e);
		}
	}

	@Override
	public List<Node> getRelatedEntities(Node entity, String linkNodeType, String relatedEntityType) {
		try {
			if (!entity.hasProperty(ConnectNames.CONNECT_UID))
				return null;
			String xpathQueryStr = "//element(*, " + linkNodeType + ")";
			String attrQuery = XPathUtils.getPropertyEquals(PeopleNames.PEOPLE_REF_UID,
					entity.getProperty(ConnectNames.CONNECT_UID).getString());
			if (notEmpty(attrQuery))
				xpathQueryStr += "[" + attrQuery + "]";
			Query xpathQuery = XPathUtils.createQuery(entity.getSession(), xpathQueryStr);
			QueryResult result = xpathQuery.execute();
			NodeIterator ni = result.getNodes();
			if (relatedEntityType == null)
				return JcrUtils.nodeIteratorToList(ni);
			else {
				List<Node> cleaned = new ArrayList<Node>();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.getParent().getParent().isNodeType(relatedEntityType))
						cleaned.add(currNode);
				}
				return cleaned;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve " + linkNodeType + " related "
					+ (relatedEntityType == null ? "" : relatedEntityType) + " entities for " + entity, e);
		}
	}

	/** Do not use this, there is a problem with the checkPoint method */
	@Deprecated
	// public long publishAll(Session session, JcrMonitor monitor) {
	// Query query;
	// long nodeNb = 0;
	// try {
	// query = session.getWorkspace().getQueryManager().createQuery("SELECT *
	// FROM [" + NodeType.MIX_VERSIONABLE
	// + "] ORDER BY [" + Property.JCR_LAST_MODIFIED + "] DESC ",
	// Query.JCR_SQL2);
	// if (monitor != null && !monitor.isCanceled())
	// monitor.beginTask("Gathering versionnable items", -1);
	// NodeIterator nit = query.execute().getNodes();
	//
	// if (nit.hasNext() && monitor != null && !monitor.isCanceled()) {
	// nodeNb = nit.getSize();
	// int shortNb = (int) nodeNb / 100;
	// monitor.beginTask("Committing " + nodeNb + " nodes", shortNb);
	//
	// }
	// long i = 0;
	// VersionManager vm = session.getWorkspace().getVersionManager();
	// while (nit.hasNext()) {
	// String currPath = nit.nextNode().getPath();
	// vm.checkpoint(currPath);
	// if (i % 100 == 0 && monitor != null && !monitor.isCanceled())
	// monitor.worked(1);
	// i++;
	// }
	// return nodeNb;
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to publish the workspace for " +
	// session, e);
	// }
	//
	// }

	protected void importCatalogue(Session session, Resource resource, String templateId) {
		InputStream stream = null;
		// try {
		// if (resourceService.getNodeTemplate(session, templateId) == null &&
		// resource != null) {
		// Node template = resourceService.createTemplateForType(session,
		// PeopleTypes.PEOPLE_NODE_TEMPLATE,
		// templateId);
		// stream = resource.getInputStream();
		// new TemplateCatalogueCsvFileParser(template).parse(stream, "UTF-8");
		// }
		// } catch (IOException ioe) {
		// throw new PeopleException("Unable to initialise template " +
		// templateId, ioe);
		// } finally {
		// IOUtils.closeQuietly(stream);
		// }
	}

	protected InputStream getStreamFromUrl(String url) throws IOException {
		InputStream inputStream = null;
		if (url.startsWith("classpath:")) {
			url = url.substring("classpath:".length());
			Resource resultbasepath = new ClassPathResource(url);
			if (resultbasepath.exists())
				inputStream = resultbasepath.getInputStream();
		} else if (url.startsWith("file:")) {
			url = url.substring("file:".length());
			File file = new File(url);
			// String tmpPath = file.getAbsolutePath();
			if (file.exists())
				inputStream = new FileInputStream(url);
		}
		return inputStream;
	}

	/* EXPOSED SERVICES */
	@Override
	public PersonService getPersonService() {
		return personService;
	}

	@Override
	public ContactService getContactService() {
		return contactService;
	}

	/* LIFE CYCLE MANAGEMENT */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		personService = new PersonServiceImpl(this, resourcesService);
		if (log.isDebugEnabled())
			log.info("People's backend has been initialized");
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
	}

	/* DEPENDENCY INJECTION */
	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}
}
