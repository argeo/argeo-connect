package org.argeo.connect.people.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.AppService;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/**
 * Static utility methods to manage generic CRM concepts within JCR. Rather use
 * these methods than direct JCR queries in order to ease model evolution.
 */
public class PeopleJcrUtils implements PeopleNames {
	// private final static Log log = LogFactory.getLog(PeopleJcrUtils.class);

	/* GROUP MANAGEMENT */
	/**
	 * Add an entity to a given group
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 * @param title
	 *            OPTIONAL: the nature of the subject in this relation, for
	 *            instance "Actor" or "Engineer"
	 */
	public static Node addEntityToGroup(Node group, Node entity, String role, String title, Calendar dateBegin,
			Calendar dateEnd, Boolean isCurrent) throws RepositoryException {

		Node members = JcrUtils.mkdirs(group, PEOPLE_MEMBERS, NodeType.NT_UNSTRUCTURED);
		Node member = members.addNode(ConnectJcrUtils.get(entity, Property.JCR_TITLE), PeopleTypes.PEOPLE_MEMBER);
		member.setProperty(PEOPLE_REF_UID, ConnectJcrUtils.get(entity, ConnectNames.CONNECT_UID));
		member.setProperty(PEOPLE_ROLE, role);
		if (EclipseUiUtils.notEmpty(title))
			throw new PeopleException("Position Nature: Unimplemented property ");
		// member.setProperty(Property.JCR_TITLE, title);
		if (dateBegin != null)
			member.setProperty(PEOPLE_DATE_BEGIN, dateBegin);
		if (dateEnd != null)
			member.setProperty(PEOPLE_DATE_END, dateEnd);
		if (isCurrent != null)
			member.setProperty(PEOPLE_IS_CURRENT, isCurrent);
		return member;
	}

	// CONTACTS

	public static void setContactLabel(Node contactNode, String label) throws RepositoryException {
		if (EclipseUiUtils.notEmpty(label))
			contactNode.setProperty(PEOPLE_CONTACT_LABEL, label);
	}

	public static void setContactCategory(Node contactNode, String category) throws RepositoryException {
		if (EclipseUiUtils.notEmpty(category))
			contactNode.setProperty(PEOPLE_CONTACT_CATEGORY, category);
	}

	public static void setContactNature(Node contactNode, String category, Node orga) throws RepositoryException {
		if (EclipseUiUtils.notEmpty(category)) {
			contactNode.setProperty(PEOPLE_CONTACT_NATURE, category);
			if (category.equals(ContactValueCatalogs.CONTACT_NATURE_PRO) && orga != null)
				contactNode.setProperty(PEOPLE_REF_UID, orga.getPath());
		}
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getCountryFromItem(PeopleService peopleService, Node item) {
		Node node = getPrimaryContact(item, PeopleTypes.PEOPLE_ADDRESS);
		if (node != null && ConnectJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = peopleService.getEntityFromNodeReference(node, PEOPLE_REF_UID);
			if (referenced != null)
				node = getPrimaryContact(referenced, PeopleTypes.PEOPLE_ADDRESS);
		}
		if (node != null)
			return ConnectJcrUtils.get(node, PEOPLE_COUNTRY);
		return "";
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getTownFromItem(PeopleService peopleService, Node item) {
		Node node = getPrimaryContact(item, PeopleTypes.PEOPLE_ADDRESS);
		if (node != null && ConnectJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = peopleService.getEntityFromNodeReference(node, PEOPLE_REF_UID);
			if (referenced != null)
				node = getPrimaryContact(referenced, PeopleTypes.PEOPLE_ADDRESS);
		}
		if (node != null)
			return ConnectJcrUtils.get(node, PEOPLE_CITY);
		return "";
	}

	/**
	 * Returns the primary contact for the type or null if no node with this
	 * type is defined as primary
	 */
	public static Node getPrimaryContact(Node entity, String nodeType) {
		try {
			if (entity.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = entity.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(nodeType) && currNode.hasProperty(PEOPLE_IS_PRIMARY)) {
						if (currNode.getProperty(PEOPLE_IS_PRIMARY).getBoolean()) // &&
																					// isPrimary.booleanValue())
							return currNode;
					}
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get primary contact of type " + nodeType + " for " + entity, re);
		}
	}

	/** Returns a list of contact for the given entity and type */
	public static List<Node> getContactOfType(Node entity, String contactNodeType) {
		List<Node> result = new ArrayList<Node>();
		try {
			if (entity.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = entity.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(contactNodeType)) {
						result.add(currNode);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get contact list of type " + contactNodeType + " for " + entity, re);
		}
		return result;
	}

	/**
	 * Returns the primary contact value given a type or an empty String if no
	 * node with this type is defined as primary
	 */
	public static String getPrimaryContactValue(Node item, String nodeType) {
		Node primary = getPrimaryContact(item, nodeType);
		if (primary != null)
			return ConnectJcrUtils.get(primary, PEOPLE_CONTACT_VALUE);
		else
			return "";
	}

	/**
	 * Marks the given node as primary using people specific mechanism, that is
	 * 
	 * <ul>
	 * <li>it moves the corresponding node as first childnode of this node's
	 * parent</li>
	 * <li>if this node has people:orderable mixin, it sets the isPrimary flag
	 * to true for this node and to false for all siblings that have the same
	 * node type</li>
	 * <li>it returns true only if some changes have been performed.</li>
	 * </ul>
	 */
	public static boolean markAsPrimary(ResourcesService resourcesService, PeopleService peopleService, Node parentNode,
			Node primaryChild) {
		try {
			Node parent = primaryChild.getParent();
			String thisNodeType = primaryChild.getPrimaryNodeType().getName();

			if (isPrimary(parentNode, primaryChild)) {
				primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, false);
				return true;
			}

			NodeIterator ni = parent.getNodes();
			Node firstNode = ni.nextNode();
			// update primary flag if needed
			if (primaryChild.isNodeType(PeopleTypes.PEOPLE_ORDERABLE)) {
				ni = parent.getNodes();
				while (ni.hasNext()) {
					Node nextNode = ni.nextNode();
					if (nextNode.isNodeType(thisNodeType)
							&& !primaryChild.getIdentifier().equals(nextNode.getIdentifier()))
						nextNode.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, false);
				}
				primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, true);
			}

			// move first
			parent.orderBefore(JcrUtils.lastPathElement(primaryChild.getPath()),
					JcrUtils.lastPathElement(firstNode.getPath()));

			updatePrimaryCache(resourcesService, peopleService, parentNode, primaryChild, true);
			return true;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to mark " + primaryChild + " as primary", re);
		}
	}

	/** Check if current child is primary */
	public static boolean isPrimary(Node parentNode, Node primaryChild) {
		try {

			if (primaryChild.hasProperty(PEOPLE_IS_PRIMARY) && primaryChild.getProperty(PEOPLE_IS_PRIMARY).getBoolean())
				return true;

			return false;

			// TODO use also nodes order in the future

			// init
			// Node parent = primaryChild.getParent();
			// String thisNodeType =
			// primaryChild.getPrimaryNodeType().getName();

			// // Check if something must change
			// NodeIterator ni = parent.getNodes();
			// while (ni.hasNext()) {
			// Node nextNode = ni.nextNode();
			// if (primaryChild.getIdentifier().equals(
			// nextNode.getIdentifier()))
			// return true;
			// else if (nextNode.isNodeType(thisNodeType))
			// return false;
			// }
			//
			// throw new PeopleException("We should have found current "
			// + "node and never reach this point");
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to check primary status for " + primaryChild, re);
		}
	}

	/**
	 * After setting a given node as primary, it tries to update parent node
	 * corresponding cache properties. This properties are mainly used to enable
	 * full text search with no join.
	 * 
	 * If isPrimary = false e.g. corresponding child will be deleted,
	 * corresponding properties are removed.
	 * 
	 * Updated cache properties depend on the primary node type
	 */
	public static void updatePrimaryCache(ResourcesService resourcesService, PeopleService peopleService,
			Node parentNode, Node primaryChild, boolean isPrimary) {
		try {
			if (primaryChild.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PPHONE,
							ConnectJcrUtils.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PPHONE))
						parentNode.setProperty(PEOPLE_CACHE_PPHONE, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PMAIL, ConnectJcrUtils.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PMAIL))
						parentNode.setProperty(PEOPLE_CACHE_PMAIL, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_URL)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PURL, ConnectJcrUtils.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PURL))
						parentNode.setProperty(PEOPLE_CACHE_PURL, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
				if (isPrimary) {
					String cityStr = "", countryStr = "";

					if (primaryChild.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF)
							&& EclipseUiUtils.notEmpty(ConnectJcrUtils.get(primaryChild, PEOPLE_REF_UID))) {
						Node linkedOrg = peopleService.getEntityFromNodeReference(primaryChild, PEOPLE_REF_UID);
						if (linkedOrg != null) {
							cityStr = getTownFromItem(peopleService, linkedOrg);
							countryStr = getCountryFromItem(peopleService, linkedOrg);
						}
					} else {
						cityStr = ConnectJcrUtils.get(primaryChild, PEOPLE_CITY);
						countryStr = ConnectJcrUtils.get(primaryChild, PEOPLE_COUNTRY);
						if (EclipseUiUtils.notEmpty(countryStr))
							countryStr = resourcesService.getEncodedTagValue(ConnectJcrUtils.getSession(primaryChild),
									ConnectConstants.RESOURCE_COUNTRY, countryStr);

					}

					parentNode.setProperty(PEOPLE_CACHE_PCITY, cityStr);
					parentNode.setProperty(PEOPLE_CACHE_PCOUNTRY, countryStr);
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PCITY))
						parentNode.setProperty(PEOPLE_CACHE_PCITY, "");
					if (parentNode.hasProperty(PEOPLE_CACHE_PCOUNTRY))
						parentNode.setProperty(PEOPLE_CACHE_PCOUNTRY, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				if (isPrimary) {
					Node linkedOrg = peopleService.getEntityFromNodeReference(primaryChild, PEOPLE_REF_UID);
					if (linkedOrg != null) {
						parentNode.setProperty(PEOPLE_CACHE_PORG, ConnectJcrUtils.get(linkedOrg, Property.JCR_TITLE));
					}
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PORG))
						parentNode.setProperty(PEOPLE_CACHE_PORG, "");
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to mark " + primaryChild + " as primary", re);
		}
	}

	/**
	 * Create a contact node and add basic info
	 * 
	 * @param parentNode
	 * @param nodeType
	 * @param name
	 * @param value
	 * @param primary
	 * @param nature
	 *            pro or private
	 * @param category
	 *            business type of the current contact, for instance for social
	 *            media, tweeter, linkedin, ... or other
	 * @param label
	 *            an optional label
	 * @return
	 */

	public static Node createContact(PeopleService peopleService, ResourcesService resourcesService, Node parentNode,
			String nodeType, String name, String value, boolean primary, String nature, String category, String label) {
		return createContact(peopleService, resourcesService, parentNode, nodeType, name, value, primary, nature, null,
				category, label);
	}

	/**
	 * Create a contact node and add basic info
	 * 
	 * @param parentNode
	 * @param nodeType
	 * @param name
	 * @param value
	 * @param primary
	 * @param nature
	 *            for a person related contact, pro or private
	 * @param linkedOrg
	 *            if nature = pro, then we can pass an optional organisation
	 *            linked to this contact
	 * @param category
	 *            business type of the current contact, for instance for social
	 *            media, tweeter, linkedin, ... or other
	 * @param label
	 *            an optional label
	 * @return
	 */
	public static Node createContact(PeopleService peopleService, ResourcesService resourcesService, Node parentNode,
			String nodeType, String name, String value, boolean primary, String nature, Node linkedOrg, String category,
			String label) {
		try {
			Node contacts = JcrUtils.mkdirs(parentNode, PEOPLE_CONTACTS, NodeType.NT_UNSTRUCTURED);
			Node contact = contacts.addNode(name.trim(), nodeType);
			contact.setProperty(PEOPLE_CONTACT_VALUE, value);
			if (primary)
				markAsPrimary(resourcesService, peopleService, parentNode, contact);
			setContactLabel(contact, label);
			setContactCategory(contact, category);
			setContactNature(contact, nature, linkedOrg);
			return contact;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to add a new " + "contact node with value [" + value + "] on " + parentNode, re);
		}
	}

	/**
	 * Create a mail address node. Corresponding nodes are not saved
	 * 
	 * @param parentNode
	 *            the parent item on which we want to add an address
	 * @param emailAddress
	 *            the value
	 * @param contactType
	 *            an optional label
	 */
	public static Node createEmail(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String emailAddress, boolean primary, String contactNature, String contactCategory, String contactLabel) {
		return createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_EMAIL,
				JcrUtils.replaceInvalidChars(emailAddress), emailAddress, primary, contactNature, contactCategory,
				contactLabel);
	}

	/**
	 * Create a web site URL node. Corresponding nodes are not saved
	 * 
	 * @param parentNode
	 *            the parent item on which we want to add a contact
	 * @param urlString
	 * @param primary
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * @param label
	 *            an optional label
	 * 
	 * @return
	 */
	public static Node createWebsite(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String urlString, boolean primary, String nature, String label) {
		return createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_URL,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary, nature, null, label);
	}

	public static Node createSocialMedia(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String urlString, boolean primary, String nature, String category, String label) {
		return createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_SOCIAL_MEDIA,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary, nature, category, label);
	}

	public static Node createImpp(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String urlString, boolean primary, String nature, String category, String label) {
		return createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_IMPP,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary, nature, category, label);
	}

	/**
	 * Create a phone number node for the current entity
	 * 
	 * @param parentNode
	 * @param phoneNumber
	 * @param primary
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * @param category
	 * @param label
	 *            an optional label
	 * @return
	 */
	public static Node createPhone(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String phoneNumber, boolean primary, String nature, String category, String label) {
		// Dirty work around to address node name issue when some invalid phone
		// number has been added
		String nodeName = ConnectJcrUtils.cleanNodeName(phoneNumber);
		String numbers = nodeName.trim().replaceAll("[^0-9]", "");
		if (EclipseUiUtils.isEmpty(nodeName) || EclipseUiUtils.isEmpty(numbers))
			return null;
		else
			return createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_PHONE, nodeName,
					phoneNumber, primary, nature, category, label);
	}

	/**
	 * Create an address for the current entity
	 * 
	 * @param parentNode
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param primary
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * @param category
	 * @param label
	 *            an optional label
	 * @return
	 */
	public static Node createAddress(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			String street1, String street2, String zipCode, String city, String state, String country, boolean primary,
			String nature, String category, String label) {
		return createAddress(peopleService, resourceService, parentNode, street1, street2, zipCode, city, state,
				country, null, primary, nature, category, label);
	}

	/**
	 * Create an address with a geopoint for the current entity
	 * 
	 * @param parentNode
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param primary
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * @param category
	 * @param label
	 *            an optional label
	 * @return
	 */
	public static Node createWorkAddress(PeopleService peopleService, ResourcesService resourceService, Node parentNode,
			Node referencedOrg, boolean primary, String category, String label) {
		try {
			Node address = createContact(peopleService, resourceService, parentNode, PeopleTypes.PEOPLE_ADDRESS,
					PeopleTypes.PEOPLE_ADDRESS, "", false, ContactValueCatalogs.CONTACT_NATURE_PRO, referencedOrg,
					category, label);
			address.addMixin(PeopleTypes.PEOPLE_CONTACT_REF);
			// set reference field
			if (referencedOrg != null)
				address.setProperty(PEOPLE_REF_UID, referencedOrg.getProperty(ConnectNames.CONNECT_UID).getString());

			if (primary)
				markAsPrimary(resourceService, peopleService, parentNode, address);

			return address;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new address node", re);
		}
	}

	/**
	 * Create an address with a geopoint for the current entity
	 * 
	 * @param parentNode
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param primary
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * @param category
	 * @param label
	 *            an optional label
	 * @return
	 */
	public static Node createAddress(PeopleService peopleService, ResourcesService resourcesService, Node parentNode,
			String street1, String street2, String zipCode, String city, String state, String country, String geopoint,
			boolean primary, String nature, String category, String label) {
		try {

			// postpone primary flag management
			Node address = createContact(peopleService, resourcesService, parentNode, PeopleTypes.PEOPLE_ADDRESS,
					PeopleTypes.PEOPLE_ADDRESS, "", false, nature, category, label);
			// set address fields
			if (EclipseUiUtils.notEmpty(street1))
				address.setProperty(PEOPLE_STREET, street1);

			if (EclipseUiUtils.notEmpty(street2))
				address.setProperty(PEOPLE_STREET_COMPLEMENT, street2);

			if (EclipseUiUtils.notEmpty(zipCode))
				address.setProperty(PEOPLE_ZIP_CODE, zipCode);

			if (EclipseUiUtils.notEmpty(city))
				address.setProperty(PEOPLE_CITY, city);

			if (EclipseUiUtils.notEmpty(state))
				address.setProperty(PEOPLE_STATE, state);

			if (EclipseUiUtils.notEmpty(country))
				address.setProperty(PEOPLE_COUNTRY, country);

			if (EclipseUiUtils.notEmpty(geopoint))
				address.setProperty(PEOPLE_GEOPOINT, geopoint);

			// update primary flag after contact creation
			if (primary)
				markAsPrimary(resourcesService, peopleService, parentNode, address);
			return address;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new address node", re);
		}
	}

	public static void updateDisplayAddress(Node contactNode) {
		try {
			StringBuilder displayAddress = new StringBuilder();
			List<String> pieces = new ArrayList<String>();

			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STREET));
			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STREET_COMPLEMENT));
			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_ZIP_CODE));
			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_CITY));
			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STATE));
			pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_COUNTRY));

			for (String piece : pieces) {
				if (EclipseUiUtils.notEmpty(piece))
					displayAddress.append(piece).append(", ");
			}

			String res = displayAddress.toString();
			if (EclipseUiUtils.notEmpty(res)) {
				contactNode.setProperty(PeopleNames.PEOPLE_CONTACT_VALUE, res.substring(0, res.lastIndexOf(", ")));
			}
		} catch (RepositoryException e) {
			throw new PeopleException("unable to update display address", e);
		}
	}

	/**
	 * Set the default picture for the current entity, overwrite without asking
	 * but do not save and check in corresponding node
	 */
	public static void setEntityPicture(Node entity, InputStream picture, String fileName) throws RepositoryException {
		Node picNode = JcrUtils.mkdirs(entity, PEOPLE_PICTURE, NodeType.NT_FILE);
		Node contentNode;
		if (picNode.hasNode(Node.JCR_CONTENT))
			contentNode = picNode.getNode(Node.JCR_CONTENT);
		else
			contentNode = picNode.addNode(Node.JCR_CONTENT, NodeType.NT_RESOURCE);
		Binary binary = null;
		binary = entity.getSession().getValueFactory().createBinary(picture);
		contentNode.setProperty(Property.JCR_DATA, binary);
		contentNode.setProperty(Property.JCR_MIMETYPE,
				fileName.substring(fileName.lastIndexOf("."), fileName.length()));
	}

	/** Calls JcrUtils */
	public static String replaceInvalidChars(String string) {
		string = JcrUtils.replaceInvalidChars(string);
		return string.replace(' ', '_');
	}

	// // TODO Clean and generalize this
	// public static Node getDraftParent(Session session, PeopleService
	// peopleService) {
	//
	// // private String getCurrentHomePath(Session callingSession) {
	// // Session session = null;
	// // try {
	// // // tryAs is compulsory when not calling from the workbench
	// // Repository repo = callingSession.getRepository();
	// // session = CurrentUser.tryAs(() -> repo.login());
	// // String homepath = NodeUtils.getUserHome(session).getPath();
	// // return homepath;
	// // } catch (Exception e) {
	// // throw new DocumentsException("Cannot retrieve Current User Home
	// // Path",
	// // e);
	// // } finally {
	// // JcrUtils.logoutQuietly(session);
	// // }
	// // }
	// // String draftPath = peopleService.getTmpPath();
	// Node home = NodeUtils.getUserHome(session);
	// String draftRelPath = ConnectConstants.HOME_APP_SYS_RELPARPATH + "/" +
	// PeopleConstants.PEOPLE_APP_BASE_NAME
	// + "/" + PeopleConstants.PEOPLE_DRAFT;
	// String datePath = JcrUtils.dateAsPath(Calendar.getInstance(), false);
	// draftRelPath += "/" + datePath;
	// return JcrUtils.mkdirs(home, draftRelPath, NodeType.NT_UNSTRUCTURED);
	// }

	public static Node createImportTmpParent(Session session, AppService appService) throws RepositoryException {
		Node peopleDraftParent = appService.getDraftParent(session);
		String relPath = "imports/" + appService.getDefaultRelPath(ConnectTypes.CONNECT_ENTITY, session.getUserID());
		Node parent = JcrUtils.mkdirs(peopleDraftParent, ConnectJcrUtils.parentRelPath(relPath));
		return parent.addNode(session.getUserID());
	}
}
