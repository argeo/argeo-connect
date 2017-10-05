package org.argeo.people.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
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
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.naming.LdapAttrs;
import org.argeo.naming.NamingUtils;
import org.argeo.node.NodeUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.osgi.service.useradmin.User;

/**
 * Static utility methods to manage generic CRM concepts within JCR. Rather use
 * these methods than direct JCR queries in order to ease model evolution.
 */
public class PeopleJcrUtils implements PeopleNames {
	// private final static Log log = LogFactory.getLog(PeopleJcrUtils.class);

	public static Node getProfile(Session session, String username) throws RepositoryException {
		if (username == null)
			username = session.getUserID();
		Node userHome = NodeUtils.getUserHome(session, username);
		Node profile = null;
		NodeIterator children = userHome.getNodes();
		while (children.hasNext()) {
			Node child = children.nextNode();
			if (child.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				profile = child;
				break;
			}
		}
		return profile;
	}

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
		member.addMixin(PeopleTypes.PEOPLE_CONTACT_REF);
		member.setProperty(PEOPLE_REF_UID, ConnectJcrUtils.get(entity, ConnectNames.CONNECT_UID));
		member.setProperty(PEOPLE_REF_TITLE, ConnectJcrUtils.get(entity, Property.JCR_TITLE));
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

	public static void setContactCategory(Node contactNode, String contactType, String category)
			throws RepositoryException {
		if (EclipseUiUtils.isEmpty(category))
			category = ContactValueCatalogs.MAPS_CONTACT_TYPES.get(contactType);
		contactNode.setProperty(Property.JCR_TITLE, category);
	}

	/**
	 * Marks a contact as pro if orga in not null by adding a mixin and setting
	 * corresponding values. if orga is null and the contact is marked as pro,
	 * it will remove the mixin and the corresponding properties
	 * 
	 * @param contactNode
	 * @param orga
	 * @throws RepositoryException
	 */
	public static void markAsPro(Node contactNode, Node orga) throws RepositoryException {
		if (orga != null) {
			contactNode.setProperty(PEOPLE_REF_UID, ConnectJcrUtils.get(orga, ConnectNames.CONNECT_UID));
			contactNode.setProperty(PEOPLE_REF_TITLE, ConnectJcrUtils.get(orga, Property.JCR_TITLE));
		} else {
			if (ConnectJcrUtils.isNodeType(contactNode, PeopleTypes.PEOPLE_CONTACT_REF))
				contactNode.removeMixin(PeopleTypes.PEOPLE_CONTACT_REF);
			if (contactNode.hasProperty(PeopleNames.PEOPLE_REF_UID))
				contactNode.getProperty(PeopleNames.PEOPLE_REF_UID).remove();
			if (contactNode.hasProperty(PeopleNames.PEOPLE_REF_TITLE))
				contactNode.getProperty(PeopleNames.PEOPLE_REF_TITLE).remove();
		}
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getCountryFromItem(PeopleService peopleService, Node contactable) {
		Node primContact = getPrimaryContact(contactable, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
		if (primContact != null && ConnectJcrUtils.isNodeType(primContact, PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = peopleService.getEntityFromNodeReference(primContact, PEOPLE_REF_UID);
			if (referenced != null)
				primContact = getPrimaryContact(referenced, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
		}
		if (primContact != null)
			return ConnectJcrUtils.get(primContact, PEOPLE_COUNTRY);
		return "";
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getTownFromItem(PeopleService peopleService, Node contactable) {
		Node node = getPrimaryContact(contactable, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
		if (node != null && ConnectJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = peopleService.getEntityFromNodeReference(node, PEOPLE_REF_UID);
			if (referenced != null)
				node = getPrimaryContact(referenced, PeopleTypes.PEOPLE_POSTAL_ADDRESS);
		}
		if (node != null)
			return ConnectJcrUtils.get(node, PEOPLE_CITY);
		return "";
	}

	/**
	 * Returns the primary contact for the type or null if no node with this
	 * type is defined as primary
	 */
	public static Node getPrimaryContact(Node contactable, String contactType) {
		try {
			if (contactable.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = contactable.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(contactType) && currNode.hasProperty(PEOPLE_IS_PRIMARY)) {
						if (currNode.getProperty(PEOPLE_IS_PRIMARY).getBoolean())
							return currNode;
					}
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get primary contact of type " + contactType + " for " + contactable,
					re);
		}
	}

	/** Returns a list of contact for the given entity and type */
	public static List<Node> getContactOfType(Node contactable, String contactType) {
		List<Node> result = new ArrayList<Node>();
		try {
			if (contactable.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = contactable.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(contactType)) {
						result.add(currNode);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get contact list of type " + contactType + " for " + contactable, re);
		}
		return result;
	}

	/**
	 * Returns the primary contact value given a type or an empty String if no
	 * node with this type is defined as primary
	 * 
	 * @param contactable
	 * @param contactType
	 * @return
	 */
	public static String getPrimaryContactValue(Node contactable, String contactType) {
		Node primary = getPrimaryContact(contactable, contactType);
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
			String thisNodeType = peopleService.getMainNodeType(primaryChild);

			if (PeopleTypes.PEOPLE_CONTACT.equals(thisNodeType))
				throw new PeopleException("Unknown node type for " + primaryChild
						+ ", cannot be maked as primary.\n Mixin for this node: "
						+ primaryChild.getMixinNodeTypes().toString());

			if (isPrimary(parentNode, primaryChild)) {
				primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, false);
				return true;
			}

			NodeIterator ni = parent.getNodes();
			Node firstNode = ni.nextNode();
			// update primary flag if needed
			// if (primaryChild.isNodeType(PeopleTypes.PEOPLE_ORDERABLE)) {
			ni = parent.getNodes();
			while (ni.hasNext()) {
				Node nextNode = ni.nextNode();
				if (nextNode.isNodeType(thisNodeType) && !primaryChild.getIdentifier().equals(nextNode.getIdentifier()))
					nextNode.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, false);
			}
			primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, true);

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

			// TODO rather only rely on nodes order
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
			if (primaryChild.isNodeType(PeopleTypes.PEOPLE_MOBILE))
				internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_PMOBILE, isPrimary);
			else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_TELEPHONE_NUMBER))
				internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_PTELEPHONE_NUMBER, isPrimary);
			else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_MAIL))
				internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_PMAIL, isPrimary);
			else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_URL))
				internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_PURL, isPrimary);
			else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS))
				internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_PPOSTAL_ADDRESS, isPrimary);
			// {
			// if (isPrimary) {
			// String cityStr = "", countryStr = "";
			// if (primaryChild.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF)
			// && EclipseUiUtils.notEmpty(ConnectJcrUtils.get(primaryChild,
			// PEOPLE_REF_UID))) {
			// Node linkedOrg =
			// peopleService.getEntityFromNodeReference(primaryChild,
			// PEOPLE_REF_UID);
			// if (linkedOrg != null) {
			// cityStr = getTownFromItem(peopleService, linkedOrg);
			// countryStr = getCountryFromItem(peopleService, linkedOrg);
			// }
			// } else {
			// cityStr = ConnectJcrUtils.get(primaryChild, PEOPLE_CITY);
			// countryStr = ConnectJcrUtils.get(primaryChild, PEOPLE_COUNTRY);
			// if (EclipseUiUtils.notEmpty(countryStr))
			// countryStr =
			// resourcesService.getEncodedTagValue(ConnectJcrUtils.getSession(primaryChild),
			// ConnectConstants.RESOURCE_COUNTRY, countryStr);
			//
			// }
			//
			// List<String> localisation = new ArrayList<>();
			// if (EclipseUiUtils.notEmpty(cityStr))
			// localisation.add(cityStr);
			// if (EclipseUiUtils.notEmpty(cityStr))
			// localisation.add(cityStr);
			// if (!localisation.isEmpty())
			// parentNode.setProperty(PEOPLE_PPOSTAL_ADDRESS,
			// localisation.toArray(new String[0]));
			// else if (parentNode.hasProperty(PEOPLE_PPOSTAL_ADDRESS))
			// parentNode.getProperty(PEOPLE_PPOSTAL_ADDRESS).remove();
			//
			// } else if (parentNode.hasProperty(PEOPLE_PPOSTAL_ADDRESS))
			// parentNode.getProperty(PEOPLE_PPOSTAL_ADDRESS).remove();
			// }
			else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				if (isPrimary) {
					Node linkedOrg = peopleService.getEntityFromNodeReference(primaryChild, PEOPLE_REF_UID);
					if (linkedOrg != null) {
						parentNode.setProperty(PEOPLE_PORG, ConnectJcrUtils.get(linkedOrg, Property.JCR_TITLE));
					}
				} else {
					if (parentNode.hasProperty(PEOPLE_PORG))
						parentNode.setProperty(PEOPLE_PORG, "");
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to mark " + primaryChild + " as primary", re);
		}
	}

	private static void internalUpdatePrimCache(Node parentNode, Node primaryChild, String cachePropertyName,
			boolean isPrimary) throws RepositoryException {
		internalUpdatePrimCache(parentNode, primaryChild, PEOPLE_CONTACT_VALUE, cachePropertyName, isPrimary);
	}

	private static void internalUpdatePrimCache(Node parentNode, Node primaryChild, String valuePropertyName,
			String cachePropertyName, boolean isPrimary) throws RepositoryException {
		if (isPrimary)
			parentNode.setProperty(cachePropertyName, ConnectJcrUtils.get(primaryChild, valuePropertyName));
		else if (parentNode.hasProperty(cachePropertyName))
			parentNode.getProperty(cachePropertyName).remove();
	}

	/**
	 * Create a contact node and add basic info
	 * 
	 * @param contactable
	 * @param contactType
	 * @param value
	 * @param isPrimary
	 * @param title
	 *            business type of the current contact, for instance for social
	 *            media, tweeter, linkedin, ... or other
	 * @param description
	 *            an optional description
	 * @param name
	 * 
	 * @return
	 */

	public static Node createContact(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String contactType, String value, boolean isPrimary, String title, String description) {
		return createContact(resourcesService, peopleService, contactable, contactType, value, isPrimary, null, title,
				description);
	}

	/**
	 * Create a contact node and add basic info
	 * 
	 * @param contactable
	 * @param contactType
	 * @param value
	 * @param isPrimary
	 * @param linkedOrg
	 *            if nature = pro, then we can pass an optional organisation
	 *            linked to this contact
	 * @param title
	 *            business type of the current contact, for instance for social
	 *            media, tweeter, linkedin, ... or other
	 * @param description
	 *            an optional label
	 * @param name
	 * @param nature
	 *            for a person related contact, pro or private
	 * 
	 * @return
	 */
	public static Node createContact(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String contactType, String value, boolean isPrimary, Node linkedOrg, String title, String description) {
		try {
			Node contacts = JcrUtils.mkdirs(contactable, PEOPLE_CONTACTS, NodeType.NT_UNSTRUCTURED);
			String name = ContactValueCatalogs.MAPS_CONTACT_TYPES.get(contactType);
			Node contact = contacts.addNode(name);
			contact.addMixin(contactType);

			if (linkedOrg == null)
				contact.setProperty(PEOPLE_CONTACT_VALUE, value);
			else {
				Node orgPrimaryContact = getPrimaryContact(linkedOrg, contactType);
				if (orgPrimaryContact != null)
					copyContactValues(orgPrimaryContact, contact);
				markAsPro(contact, linkedOrg);
			}

			if (isPrimary)
				markAsPrimary(resourcesService, peopleService, contactable, contact);
			setContactCategory(contact, contactType, title);
			if (EclipseUiUtils.notEmpty(description))
				contact.setProperty(Property.JCR_DESCRIPTION, description);
			return contact;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Cannot create " + contactType + " contact with value [" + value + "] on " + contactable, re);
		}
	}

	private static void copyContactValues(Node srcContact, Node targetContact) throws RepositoryException {
		targetContact.setProperty(PEOPLE_CONTACT_VALUE, srcContact.getProperty(PEOPLE_CONTACT_VALUE).getString());

		if (targetContact.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
			for (String prop : PEOPLE_POSTAL_ADDRESS_PROPS) {
				String value = ConnectJcrUtils.get(srcContact, prop);
				if (EclipseUiUtils.notEmpty(value))
					targetContact.setProperty(prop, targetContact);
			}
		}
	}

	/**
	 * Create a mail address node. Corresponding nodes are not saved
	 * 
	 * @param resourcesService
	 * @param peopleService
	 * @param contactable
	 *            the parent item on which we want to add the email
	 * @param emailAddress
	 *            the value
	 * @param title
	 *            the generic label for this email
	 * @param description
	 *            an optional description
	 */
	public static Node createEmail(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String emailAddress, boolean isPrimary, String title, String description) {
		return createContact(resourcesService, peopleService, contactable, PeopleTypes.PEOPLE_MAIL, emailAddress,
				isPrimary, title, description);
	}

	/**
	 * Create a web site URL node. Corresponding nodes are not saved
	 * 
	 * @param parentNode
	 *            the parent item on which we want to add a contact
	 * @param urlString
	 * @param isPrimary
	 * @param title
	 *            an optional label
	 * 
	 * @return
	 */
	public static Node createWebsite(ResourcesService resourcesService, PeopleService peopleService, Node parentNode,
			String urlString, boolean isPrimary, String title) {
		return createContact(resourcesService, peopleService, parentNode, PeopleTypes.PEOPLE_URL, urlString, isPrimary,
				title, null);
	}

	public static Node createSocialMedia(ResourcesService resourcesService, PeopleService peopleService,
			Node contactable, String urlString, boolean isPrimary, String title, String description) {
		return createContact(resourcesService, peopleService, contactable, PeopleTypes.PEOPLE_SOCIAL_MEDIA, urlString,
				isPrimary, title, description);
	}

	public static Node createImpp(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String urlString, boolean isPrimary, String title, String description) {
		return createContact(resourcesService, peopleService, contactable, PeopleTypes.PEOPLE_IMPP, urlString,
				isPrimary, title, description);
	}

	/**
	 * Create a landline phone number node for the current entity
	 * 
	 * @param contactable
	 * @param phoneNumber
	 * @param isPrimary
	 * @param title
	 * @param description
	 *            an optional label
	 * 
	 * @return
	 */
	public static Node createPhone(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String phoneNumber, boolean isPrimary, String title, String description) {
		return createContact(resourcesService, peopleService, contactable, PeopleTypes.PEOPLE_TELEPHONE_NUMBER,
				phoneNumber, isPrimary, title, description);
	}

	/**
	 * Create an address for the current entity
	 * 
	 * @param contactable
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param isPrimary
	 * @param title
	 * @param description
	 *            an optional label
	 * 
	 * @return
	 */
	public static Node createAddress(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String street1, String street2, String zipCode, String city, String state, String country,
			boolean isPrimary, String title, String description) {
		return createAddress(resourcesService, peopleService, contactable, street1, street2, zipCode, city, state,
				country, null, isPrimary, title, description);
	}

	public static Node createAddress(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			Dictionary<String, Object> p, boolean isPrimary, String title, String description) {
		// TODO deal with multiline street
		return createAddress(resourcesService, peopleService, contactable, get(p, LdapAttrs.street), null,
				get(p, LdapAttrs.postalCode), get(p, LdapAttrs.l), get(p, LdapAttrs.st), get(p, LdapAttrs.c), null,
				isPrimary, title, description);
	}

	/**
	 * Create an address with a geopoint for the current entity
	 * 
	 * @param contactable
	 * @param isPrimary
	 * @param title
	 * @param description
	 *            an optional label
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param nature
	 *            optional: if parent node type is a person, then precise
	 *            private or pro
	 * 
	 * @return
	 */
	public static Node createWorkAddress(ResourcesService resourcesService, PeopleService peopleService,
			Node contactable, boolean isPrimary, Node linkedOrg, String title, String description) {
		// try {
		Node address = createContact(resourcesService, peopleService, contactable, PeopleTypes.PEOPLE_POSTAL_ADDRESS,
				"", false, linkedOrg, title, description);
		// address.addMixin(PeopleTypes.PEOPLE_CONTACT_REF);
		// // set reference field
		// if (referencedOrg != null)
		// address.setProperty(PEOPLE_REF_UID,
		// referencedOrg.getProperty(ConnectNames.CONNECT_UID).getString());
		//
		// if (primary)
		// markAsPrimary(resourcesService, peopleService, parentNode, address);
		//
		return address;
		// } catch (RepositoryException re) {
		// throw new PeopleException("Unable to add a new address node", re);
		// }
	}

	/**
	 * Create an address with a geopoint for the current entity
	 * 
	 * @param contactable
	 * @param street1
	 * @param street2
	 * @param zipCode
	 * @param city
	 * @param state
	 * @param country
	 * @param geopoint
	 * @param isPrimary
	 * @param title
	 * @param description
	 *            an optional label
	 * 
	 * @return
	 */
	public static Node createAddress(ResourcesService resourcesService, PeopleService peopleService, Node contactable,
			String street1, String street2, String zipCode, String city, String state, String country, String geopoint,
			boolean isPrimary, String title, String description) {
		try {
			// Postpone primary flag management
			Node address = createContact(resourcesService, peopleService, contactable,
					PeopleTypes.PEOPLE_POSTAL_ADDRESS, "", false, title, description);
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

			updateDisplayAddress(resourcesService, address);

			// update primary flag after contact creation
			if (isPrimary)
				markAsPrimary(resourcesService, peopleService, contactable, address);
			return address;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new address node", re);
		}
	}

	public static void updateDisplayAddress(ResourcesService resourcesService, Node contactNode) {
		try {
			String res = getPostalAddress(resourcesService, contactNode);
			if (EclipseUiUtils.notEmpty(res))
				contactNode.setProperty(PeopleNames.PEOPLE_CONTACT_VALUE, res);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot update human readable postal address for " + contactNode, e);
		}
	}

	public static String getPostalAddress(ResourcesService resourcesService, Node contactNode) {
		StringBuilder displayAddress = new StringBuilder();
		List<String> pieces = new ArrayList<String>();

		pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STREET));
		pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STREET_COMPLEMENT));

		String zip = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_ZIP_CODE);
		String city = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_CITY);
		// TODO use the inverse order when displaying addresses for UK like
		// countries
		String zc = ConnectJcrUtils.concatIfNotEmpty(zip, city, " ");
		pieces.add(zc);
		String countryStr = ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_STATE);
		if (EclipseUiUtils.notEmpty(countryStr) && resourcesService != null)
			countryStr = resourcesService.getEncodedTagValue(ConnectJcrUtils.getSession(contactNode),
					ConnectConstants.RESOURCE_COUNTRY, countryStr);
		pieces.add(countryStr);
		pieces.add(ConnectJcrUtils.get(contactNode, PeopleNames.PEOPLE_COUNTRY));

		for (String piece : pieces) {
			if (EclipseUiUtils.notEmpty(piece))
				displayAddress.append(piece).append(", ");
		}
		String res = displayAddress.toString();
		if (EclipseUiUtils.notEmpty(res))
			res = res.substring(0, res.lastIndexOf(", "));
		return res;
	}

	/**
	 * Set the default picture for the current entity, overwrite without asking
	 * but do not save and check in corresponding node
	 */
	public static void setEntityPicture(Node entity, InputStream picture, String fileName) throws RepositoryException {
		Node picNode = JcrUtils.mkdirs(entity, ConnectNames.CONNECT_PHOTO, NodeType.NT_FILE);
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

	public static Node createImportTmpParent(Session session, AppService appService) throws RepositoryException {
		Node peopleDraftParent = appService.getDraftParent(session);
		String relPath = "imports/"
				+ appService.getDefaultRelPath(session, ConnectTypes.CONNECT_ENTITY, session.getUserID());
		Node parent = JcrUtils.mkdirs(peopleDraftParent, ConnectJcrUtils.parentRelPath(relPath));
		return parent.addNode(session.getUserID());
	}

	public static void syncPerson(Dictionary<String, Object> user, Node person) throws RepositoryException {
		sync(user, LdapAttrs.mail, person, PeopleNames.PEOPLE_PRIMARY_EMAIL);
		sync(user, LdapAttrs.givenName, person, PeopleNames.PEOPLE_FIRST_NAME);
		sync(user, LdapAttrs.sn, person, PeopleNames.PEOPLE_LAST_NAME);
		sync(user, LdapAttrs.displayName, person, PeopleNames.PEOPLE_DISPLAY_NAME);
		Object dateOfBirth = user.get(LdapAttrs.dateOfBirth.name());
		if (dateOfBirth != null) {
			Calendar calendar = NamingUtils.ldapDateToCalendar(dateOfBirth.toString());
			person.setProperty(PeopleNames.PEOPLE_BIRTH_DATE, calendar);
		}
	}

	/** From {@link User} to a person or contact */
	private static void sync(Dictionary<String, Object> properties, LdapAttrs key, Node node, String property)
			throws RepositoryException {
		Object value = properties.get(key.name());
		if (value == null)
			return;
		node.setProperty(property, value.toString());
	}

	/** Safe get */
	private static String get(Dictionary<String, Object> properties, LdapAttrs key) {
		Object value = properties.get(key.name());
		if (value == null)
			return null;
		return value.toString();
	}
}
