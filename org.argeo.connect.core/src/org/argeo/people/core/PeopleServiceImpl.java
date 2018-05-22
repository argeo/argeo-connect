package org.argeo.people.core;

import static org.argeo.connect.util.ConnectUtils.isEmpty;
import static org.argeo.connect.util.ConnectUtils.notEmpty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.core.AbstractAppService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.RemoteJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ContactService;
import org.argeo.people.PeopleConstants;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.PersonService;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.util.PersonJcrUtils;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Concrete access to {@link PeopleService} */
public class PeopleServiceImpl extends AbstractAppService implements PeopleService, PeopleNames {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private ResourcesService resourcesService;
	private UserAdminService userAdminService;

	/* Centralises the various specific People services */
	private PersonService personService;
	private ContactService contactService = new ContactServiceImpl();

	private SimpleDateFormat usersDatePath = new SimpleDateFormat("YYYY/MM");

	@Override
	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrc)
			throws RepositoryException {
		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) || PeopleTypes.PEOPLE_ORG.equals(nodeType)) {
			String peopleUid = ConnectJcrUtils.get(srcNode, ConnectNames.CONNECT_UID);
			if (isEmpty(peopleUid))
				throw new PeopleException(
						"Unable to define default path for " + srcNode + ". No property people:uid is defined");

			String relPath = getDefaultRelPath(srcNode);
			Node createdNode = JcrUtils.mkdirs(parent, relPath);

			// add primary contact
			Node personCreatedNode = null;
			if (srcNode.hasNode(PeopleNames.PEOPLE_ROLE)) {
				Node subNode = srcNode.getNode(PeopleNames.PEOPLE_ROLE);
				String personRelPath = getDefaultRelPath(subNode);
				personCreatedNode = JcrUtils.mkdirs(parent, personRelPath);
				RemoteJcrUtils.copy(subNode, personCreatedNode, true);
				personCreatedNode.addMixin(PeopleTypes.PEOPLE_PERSON);
				// remove draft before copying org
				subNode.removeMixin(PeopleTypes.PEOPLE_PERSON);
				subNode.remove();
			}

			RemoteJcrUtils.copy(srcNode, createdNode, true);
			createdNode.addMixin(nodeType);
			// integration with user admin
			configureUser(createdNode);
			if (personCreatedNode != null) {
				configureUser(personCreatedNode);
				JcrUtils.updateLastModified(personCreatedNode);
			}
			JcrUtils.updateLastModified(createdNode);

			if (removeSrc) {
				srcNode.removeMixin(nodeType);
				srcNode.remove();
			}
			// must be after removing source since there is a save
			if (personCreatedNode != null) {
				personService.createOrUpdateJob(null, personCreatedNode, createdNode, null, null, true);
			}
			return createdNode;
		} else
			return null;
	}

	private void configureUser(Node createdNode) throws RepositoryException {
		if (createdNode.hasProperty(PeopleNames.PEOPLE_USERNAME)) {
			createdNode.addMixin(PeopleTypes.PEOPLE_USER);
		} else if (createdNode.hasProperty(PeopleNames.PEOPLE_PRIMARY_EMAIL)) {
			String email = JcrUtils.get(createdNode, PEOPLE_PRIMARY_EMAIL);
			String dn = userAdminService.buildDefaultDN(email, Role.USER);
			User user = userAdminService.getUser(dn);
			if (user == null)
				user = userAdminService.createUserFromPerson(createdNode);
			createdNode.addMixin(PeopleTypes.PEOPLE_USER);
			createdNode.setProperty(PeopleNames.PEOPLE_USERNAME, dn);
		}
	}
	// private String generateUserPath(String username) {
	// LdapName dn;
	// try {
	// dn = new LdapName(username);
	// } catch (InvalidNameException e) {
	// throw new CmsException("Invalid name " + username, e);
	// }
	// String userId = dn.getRdn(dn.size() - 1).getValue().toString();
	// return usersDatePath.format(new Date()) + '/' + userId;
	// }

	@Override
	public Node saveEntity(Node entity, boolean publish) throws PeopleException {
		try {
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON) || entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				entity = getPersonService().saveEntity(entity, publish);
			else
				throw new PeopleException("Unknown entity type for " + entity);
			return entity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to save " + entity, e);
		}
	}

	@Override
	public String getAppBaseName() {
		return PeopleConstants.PEOPLE_APP_BASE_NAME;
	}

	@Override
	public String getBaseRelPath(String nodeType) {
		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) || ConnectTypes.CONNECT_LDAP_PERSON.equals(nodeType)
				|| PeopleTypes.PEOPLE_ORG.equals(nodeType) || ConnectTypes.CONNECT_LDAP_ORG.equals(nodeType)
				|| PeopleTypes.PEOPLE_ENTITY.equals(nodeType) || PeopleTypes.PEOPLE_CONTACTABLE.equals(nodeType))
			return getAppBaseName();
		else
			return null;
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		Calendar date;
		if (entity.hasProperty(Property.JCR_CREATED))
			date = entity.getProperty(Property.JCR_CREATED).getDate();
		else
			date = new GregorianCalendar();
		String creationDatePath = usersDatePath.format(date.getTime());

		String fullName;
		if (entity.hasProperty(PeopleNames.PEOPLE_DISPLAY_NAME))
			fullName = entity.getProperty(PeopleNames.PEOPLE_DISPLAY_NAME).getString();
		else if (entity.hasProperty(PeopleNames.PEOPLE_LEGAL_NAME))
			fullName = entity.getProperty(PeopleNames.PEOPLE_LEGAL_NAME).getString();
		else
			throw new IllegalArgumentException("Cannot find full name of " + entity);
		String relPath = creationDatePath + "/" + fullName;
		return relPath;
		// String peopleUid = ConnectJcrUtils.get(entity, ConnectNames.CONNECT_UID);
		// if (isEmpty(peopleUid))
		// throw new PeopleException(
		// "Unable to define default path for " + entity + ". No property people:uid is
		// defined");
		// else
		// return getDefaultRelPath(ConnectJcrUtils.getSession(entity), null,
		// peopleUid);
	}

	@Override
	public String getDefaultRelPath(Session session, String nodeType, String peopleUid) {
		// try {
		// return getDefaultRelPath(ConnectJcrUtils.getNodeByIdentifier(session,
		// peopleUid));
		// } catch (RepositoryException e) {
		// throw new ConnectException("Cannot get default relative path", e);
		// }
		String path = JcrUtils.firstCharsToPath(peopleUid, 2) + "/" + peopleUid;
		return path;
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
	public String getMainNodeType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON))
			return PeopleTypes.PEOPLE_PERSON;
		else if (ConnectJcrUtils.isNodeType(entity, ConnectTypes.CONNECT_LDAP_PERSON))
			return ConnectTypes.CONNECT_LDAP_PERSON;
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return PeopleTypes.PEOPLE_ORG;
		else if (ConnectJcrUtils.isNodeType(entity, ConnectTypes.CONNECT_LDAP_ORG))
			return ConnectTypes.CONNECT_LDAP_ORG;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_MAILING_LIST))
			return PeopleTypes.PEOPLE_MAILING_LIST;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_MOBILE))
			return PeopleTypes.PEOPLE_MOBILE;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_TELEPHONE_NUMBER))
			return PeopleTypes.PEOPLE_TELEPHONE_NUMBER;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_FAX))
			return PeopleTypes.PEOPLE_FAX;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PHONE))
			return PeopleTypes.PEOPLE_PHONE;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_MAIL))
			return PeopleTypes.PEOPLE_MAIL;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_URL))
			return PeopleTypes.PEOPLE_URL;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_SOCIAL_MEDIA))
			return PeopleTypes.PEOPLE_SOCIAL_MEDIA;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_IMPP))
			return PeopleTypes.PEOPLE_IMPP;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_POSTAL_ADDRESS))
			return PeopleTypes.PEOPLE_POSTAL_ADDRESS;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_CONTACT))
			return PeopleTypes.PEOPLE_CONTACT;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_JOB))
			return PeopleTypes.PEOPLE_JOB;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_POSITION))
			return PeopleTypes.PEOPLE_POSITION;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_BANK_ACCOUNT))
			return PeopleTypes.PEOPLE_BANK_ACCOUNT;
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_MEMBER))
			return PeopleTypes.PEOPLE_MEMBER;
		else
			return null;
	}

	@Override
	public String getDisplayName(Node entity) {
		String displayName = null;
		try {
			if (entity.hasProperty(PEOPLE_DISPLAY_NAME))
				displayName = entity.getProperty(PEOPLE_DISPLAY_NAME).getString();
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				displayName = getPersonService().getDefaultDisplayName(entity);
			else if (entity.isNodeType(NodeType.MIX_TITLE))
				displayName = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else
				throw new PeopleException("Display name not defined for type " + entity.getPrimaryNodeType().getName()
						+ " - node: " + entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get display name for node " + entity, e);
		}
		return displayName;
	}

	/**
	 * Simply looks for primary information and updates the primary cache if needed
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
	 * Creates and returns a model specific Node to store a reference, depending on
	 * the two object we want to link together. Overwrite to add some new link type
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
	@Deprecated
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
		personService = new PersonServiceImpl(this, resourcesService);
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

}
