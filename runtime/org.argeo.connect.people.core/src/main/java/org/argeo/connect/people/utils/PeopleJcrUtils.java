package org.argeo.connect.people.utils;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;

/**
 * Static utilitary methods to manage generic CRM concepts within JCR.
 * 
 * Rather use these methods than direct JCR queries in order to ease model
 * evolution.
 */
public class PeopleJcrUtils implements PeopleNames {

	/* GROUP MANAGEMENT */
	/**
	 * Add an entity to a given group
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 * @param title
	 *            OPTIONAL: the nature of the subject in this relation, for
	 *            instance "Actor" or "Engineer"
	 * */
	public static Node addEntityToGroup(Node group, Node entity, String role,
			String title, Calendar dateBegin, Calendar dateEnd,
			Boolean isCurrent) throws RepositoryException {

		Node members = CommonsJcrUtils
				.getOrCreateDirNode(group, PEOPLE_MEMBERS);
		Node member = members.addNode(entity.getName(),
				PeopleTypes.PEOPLE_MEMBER);
		member.setProperty(PEOPLE_REF_UID, entity.getIdentifier());
		member.setProperty(PEOPLE_ROLE, role);
		if (CommonsJcrUtils.checkNotEmptyString(title))
			member.setProperty(PEOPLE_TITLE, title);
		if (dateBegin != null)
			member.setProperty(PEOPLE_DATE_BEGIN, dateBegin);
		if (dateEnd != null)
			member.setProperty(PEOPLE_DATE_END, dateEnd);
		if (isCurrent != null)
			member.setProperty(PEOPLE_IS_CURRENT, isCurrent);
		return member;
	}

	/**
	 * Shortcut to add an entity to a given group using default values
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 * */
	public static Node addEntityToGroup(Node group, Node entity, String role)
			throws RepositoryException {
		return addEntityToGroup(group, entity, role, null, null, null, null);
	}

	public static void setContactType(Node contactNode, String label)
			throws RepositoryException {
		if (!CommonsJcrUtils.isEmptyString(label))
			contactNode.setProperty(PEOPLE_CONTACT_LABEL, label);
	}

	public static void setContactCategory(Node contactNode, String category,
			Node orga) throws RepositoryException {
		if (!CommonsJcrUtils.isEmptyString(category)) {
			contactNode.setProperty(PEOPLE_CONTACT_CATEGORY, category);
			if (category.equals(PeopleConstants.PEOPLE_CONTACT_CATEGORY_WORK)
					&& orga != null)
				contactNode.setProperty(PEOPLE_REF_UID, orga.getPath());
		}
	}

	public static String getTownFromItem(Node item) {
		try {
			if (item.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = item.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)
							&& CommonsJcrUtils.getStringValue(currNode,
									PEOPLE_CITY) != null)
						return CommonsJcrUtils.getStringValue(currNode,
								PEOPLE_CITY);
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get city for item", re);
		}
	}

	public static String getCountryFromItem(Node item) {
		try {
			if (item.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = item.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)
							&& CommonsJcrUtils.getStringValue(currNode,
									PEOPLE_COUNTRY) != null)
						return CommonsJcrUtils.getStringValue(currNode,
								PEOPLE_COUNTRY);
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get city for item", re);
		}
	}

	/** Return primary contact given a node type */
	public static String getDefaultContactValue(Node item, String nodeType) {
		try {
			if (item.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = item.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();

					if (currNode.isNodeType(nodeType))
						return CommonsJcrUtils.getStringValue(currNode,
								PEOPLE_CONTACT_VALUE);

					// TODO take preference into account
					// Boolean isPrimary = CommonsJcrUtils.getBooleanValue(
					// currNode, PEOPLE_IS_PRIMARY);
					// if (currNode.isNodeType(nodeType) && isPrimary != null
					// && isPrimary.booleanValue())
					// return CommonsJcrUtils.getStringValue(currNode,
					// PEOPLE_CONTACT_VALUE);
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get city for item", re);
		}
	}

	public static String getBranches(Node orga) {
		try {
			StringBuilder tags = new StringBuilder();
			if (orga.hasProperty(PEOPLE_ORG_BRANCHE)) {
				for (Value value : orga.getProperty(PEOPLE_ORG_BRANCHE)
						.getValues())
					tags.append(value.getString()).append(", ");
			}
			if (tags.lastIndexOf(", ") > 0)
				tags.delete(tags.lastIndexOf(", "), tags.length());
			return tags.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for node "
					+ orga, e);
		}
	}

	public static String getContactTypeAsString(Node contact) {
		try {
			if (contact.isNodeType(PeopleTypes.PEOPLE_EMAIL))
				return "E-Mail";
			else if (contact.isNodeType(PeopleTypes.PEOPLE_URL))
				return "Site";
			else if (contact.isNodeType(PeopleTypes.PEOPLE_PHONE))
				return "Phone";
			else if (contact.isNodeType(PeopleTypes.PEOPLE_ADDRESS))
				return "Address";
			return "Other";
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to add a new contact node", re);
		}

	}

	/** Create a contact node and ass basic info */
	public static Node createContact(Node parentNode, String nodeType,
			String name, String value, int pref, String contactCategory,
			String contactType) {
		try {
			Node contacts = CommonsJcrUtils.getOrCreateDirNode(parentNode,
					PEOPLE_CONTACTS);
			Node contact = contacts.addNode(name, nodeType);
			contact.setProperty(PEOPLE_CONTACT_VALUE, value);
			contact.setProperty(PEOPLE_PREF, pref);
			setContactType(contact, contactType);
			setContactCategory(contact, contactCategory, null);
			return contact;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to add a new contact node", re);
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
	public static Node createEmail(Node parentNode, String emailAddress,
			int pref, String contactCategory, String contactType) {
		return createContact(parentNode, PeopleTypes.PEOPLE_EMAIL,
				emailAddress, emailAddress, pref, contactCategory, contactType);
	}

	/**
	 * Create a web site URL node. Corresponding nodes are not saved
	 * 
	 * @param parentNode
	 *            the parent item on which we want to add an address
	 * @param urlString
	 *            the url
	 * 
	 * @param contactType
	 *            an optional label
	 */
	public static Node createWebsite(Node parentNode, String urlString,
			int pref, String contactCategory, String contactType) {
		return createContact(parentNode, PeopleTypes.PEOPLE_URL,
				urlString.replaceAll("[^a-zA-Z0-9]", ""), urlString, pref,
				contactCategory, contactType);
	}

	public static Node createPhone(Node parentNode, String phoneNumber,
			int pref, String contactCategory, String contactType) {
		return createContact(parentNode, PeopleTypes.PEOPLE_URL, "p"
				+ phoneNumber.replaceAll("[^a-zA-Z0-9]", ""), phoneNumber,
				pref, contactCategory, contactType);
	}

	public static Node createAddress(Node parentNode, String street1,
			String street2, String zipCode, String city, String state,
			String country, int pref, String contactCategory, String contactType) {
		try {
			Node address = createContact(parentNode,
					PeopleTypes.PEOPLE_ADDRESS, PeopleTypes.PEOPLE_ADDRESS, "",
					pref, contactCategory, contactType);
			// set address fields
			if (!CommonsJcrUtils.isEmptyString(street1))
				address.setProperty(PEOPLE_STREET, street1);

			if (!CommonsJcrUtils.isEmptyString(street2))
				address.setProperty(PEOPLE_STREET_COMPLEMENT, street2);

			if (!CommonsJcrUtils.isEmptyString(zipCode))
				address.setProperty(PEOPLE_ZIP_CODE, zipCode);

			if (!CommonsJcrUtils.isEmptyString(city))
				address.setProperty(PEOPLE_CITY, city);

			if (!CommonsJcrUtils.isEmptyString(state))
				address.setProperty(PEOPLE_STATE, state);

			if (!CommonsJcrUtils.isEmptyString(country))
				address.setProperty(PEOPLE_COUNTRY, country);
			return address;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new address node", re);
		}
	}

	/**
	 * Set the default picture for the current entity, overwrite without asking
	 * but do not save and check in corresponding node
	 */
	public static void setEntityPicture(Node entity, InputStream picture,
			String fileName) throws RepositoryException {
		Node picNode = JcrUtils
				.mkdirs(entity, PEOPLE_PICTURE, NodeType.NT_FILE);
		Node contentNode;
		if (picNode.hasNode(Node.JCR_CONTENT))
			contentNode = picNode.getNode(Node.JCR_CONTENT);
		else
			contentNode = picNode.addNode(Node.JCR_CONTENT,
					NodeType.NT_RESOURCE);
		Binary binary = null;
		binary = entity.getSession().getValueFactory().createBinary(picture);
		contentNode.setProperty(Property.JCR_DATA, binary);
		contentNode.setProperty(Property.JCR_MIMETYPE, fileName.substring(
				fileName.lastIndexOf("."), fileName.length()));
	}
}