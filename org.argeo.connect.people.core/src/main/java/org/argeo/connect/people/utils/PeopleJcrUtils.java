package org.argeo.connect.people.utils;

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
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.media.FilmNames;
import org.argeo.connect.media.FilmTypes;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;

/**
 * Static utility methods to manage generic CRM concepts within JCR. Rather use
 * these methods than direct JCR queries in order to ease model evolution.
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

		Node members = JcrUtils.mkdirs(group, PEOPLE_MEMBERS,
				NodeType.NT_UNSTRUCTURED);
		Node member = members.addNode(
				CommonsJcrUtils.get(entity, Property.JCR_TITLE),
				PeopleTypes.PEOPLE_MEMBER);
		member.setProperty(PEOPLE_REF_UID,
				CommonsJcrUtils.get(entity, PEOPLE_UID));
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

	// /////////////////////////
	// / CONTACTS

	public static void setContactLabel(Node contactNode, String label)
			throws RepositoryException {
		if (!CommonsJcrUtils.isEmptyString(label))
			contactNode.setProperty(PEOPLE_CONTACT_LABEL, label);
	}

	public static void setContactCategory(Node contactNode, String category)
			throws RepositoryException {
		if (!CommonsJcrUtils.isEmptyString(category))
			contactNode.setProperty(PEOPLE_CONTACT_CATEGORY, category);
	}

	public static void setContactNature(Node contactNode, String category,
			Node orga) throws RepositoryException {
		if (!CommonsJcrUtils.isEmptyString(category)) {
			contactNode.setProperty(PEOPLE_CONTACT_NATURE, category);
			if (category.equals(ContactValueCatalogs.CONTACT_NATURE_PRO)
					&& orga != null)
				contactNode.setProperty(PEOPLE_REF_UID, orga.getPath());
		}
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getCountryFromItem(Node item) {
		Node node = getPrimaryContact(item, PeopleTypes.PEOPLE_ADDRESS);
		if (node != null
				&& CommonsJcrUtils.isNodeType(node,
						PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = getEntityFromNodeReference(node, PEOPLE_REF_UID);
			if (referenced != null)
				node = getPrimaryContact(referenced, PeopleTypes.PEOPLE_ADDRESS);
		}
		if (node != null)
			return CommonsJcrUtils.get(node, PEOPLE_COUNTRY);
		return "";
	}

	/**
	 * Returns the country of an entity relying on its primary address, if
	 * defined
	 */
	public static String getTownFromItem(Node item) {
		Node node = getPrimaryContact(item, PeopleTypes.PEOPLE_ADDRESS);
		if (node != null
				&& CommonsJcrUtils.isNodeType(node,
						PeopleTypes.PEOPLE_CONTACT_REF)) {
			// retrieve primary address for the referenced Node
			Node referenced = getEntityFromNodeReference(node, PEOPLE_REF_UID);
			if (referenced != null)
				node = getPrimaryContact(referenced, PeopleTypes.PEOPLE_ADDRESS);
		}
		if (node != null)
			return CommonsJcrUtils.get(node, PEOPLE_CITY);
		return "";
	}

	/**
	 * Returns the primary contact for the type or null if no node with this
	 * type is defined as primary
	 */
	public static Node getPrimaryContact(Node item, String nodeType) {
		try {
			if (item.hasNode(PEOPLE_CONTACTS)) {
				Node contacts = item.getNode(PEOPLE_CONTACTS);
				NodeIterator ni = contacts.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.isNodeType(nodeType)
							&& currNode.hasProperty(PEOPLE_IS_PRIMARY)) {
						if (currNode.getProperty(PEOPLE_IS_PRIMARY)
								.getBoolean()) // && isPrimary.booleanValue())
							return currNode;
					}
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get city for item", re);
		}
	}

	/**
	 * Returns the primary contact value given a type or an empty String if no
	 * node with this type is defined as primary
	 */
	public static String getPrimaryContactValue(Node item, String nodeType) {
		Node primary = getPrimaryContact(item, nodeType);
		if (primary != null)
			return CommonsJcrUtils.get(primary, PEOPLE_CONTACT_VALUE);
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
	public static boolean markAsPrimary(Node parentNode, Node primaryChild) {
		try {
			Node parent = primaryChild.getParent();
			String thisNodeType = primaryChild.getPrimaryNodeType().getName();

			if (isPrimary(parentNode, primaryChild))
				return false;

			NodeIterator ni = parent.getNodes();
			Node firstNode = ni.nextNode();
			// update primary flag if needed
			if (primaryChild.isNodeType(PeopleTypes.PEOPLE_ORDERABLE)) {
				ni = parent.getNodes();
				while (ni.hasNext()) {
					Node nextNode = ni.nextNode();
					if (nextNode.isNodeType(thisNodeType)
							&& !primaryChild.getIdentifier().equals(
									nextNode.getIdentifier()))
						nextNode.setProperty(PeopleNames.PEOPLE_IS_PRIMARY,
								false);
				}
				primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, true);
			}

			// move first
			parent.orderBefore(
					JcrUtils.lastPathElement(primaryChild.getPath()),
					JcrUtils.lastPathElement(firstNode.getPath()));

			updatePrimaryCache(parentNode, primaryChild, true);
			return true;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to mark " + primaryChild
					+ " as primary", re);
		}
	}

	/** Check if current child is primary */
	public static boolean isPrimary(Node parentNode, Node primaryChild) {
		try {

			if (primaryChild.hasProperty(PEOPLE_IS_PRIMARY)
					&& primaryChild.getProperty(PEOPLE_IS_PRIMARY).getBoolean())
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
			throw new ArgeoException("Unable to check primary status for "
					+ primaryChild, re);
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
	 * */
	public static void updatePrimaryCache(Node parentNode, Node primaryChild,
			boolean isPrimary) {
		try {
			if (primaryChild.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PPHONE, CommonsJcrUtils
							.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PPHONE))
						parentNode.setProperty(PEOPLE_CACHE_PPHONE, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PMAIL, CommonsJcrUtils
							.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PMAIL))
						parentNode.setProperty(PEOPLE_CACHE_PMAIL, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_URL)) {
				if (isPrimary) {
					parentNode.setProperty(PEOPLE_CACHE_PURL, CommonsJcrUtils
							.get(primaryChild, PEOPLE_CONTACT_VALUE));
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PURL))
						parentNode.setProperty(PEOPLE_CACHE_PURL, "");
				}
			} else if (primaryChild.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
				if (isPrimary) {
					String cityStr = "", countryStr = "";

					if (primaryChild.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF)) {
						Node linkedOrg = PeopleJcrUtils
								.getEntityFromNodeReference(primaryChild,
										PEOPLE_REF_UID);
						if (linkedOrg != null) {
							cityStr = getTownFromItem(linkedOrg);
							countryStr = getCountryFromItem(linkedOrg);
						}
					} else {
						cityStr = CommonsJcrUtils
								.get(primaryChild, PEOPLE_CITY);
						countryStr = CommonsJcrUtils.get(primaryChild,
								PEOPLE_COUNTRY);
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
					Node linkedOrg = PeopleJcrUtils.getEntityFromNodeReference(
							primaryChild, PEOPLE_REF_UID);
					if (linkedOrg != null) {
						parentNode.setProperty(PEOPLE_CACHE_PORG,
								CommonsJcrUtils.get(linkedOrg,
										Property.JCR_TITLE));
					}
				} else {
					if (parentNode.hasProperty(PEOPLE_CACHE_PORG))
						parentNode.setProperty(PEOPLE_CACHE_PORG, "");
				}
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to mark " + primaryChild
					+ " as primary", re);
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

	public static Node createContact(Node parentNode, String nodeType,
			String name, String value, boolean primary, String nature,
			String category, String label) {
		return createContact(parentNode, nodeType, name, value, primary,
				nature, null, category, label);
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
	public static Node createContact(Node parentNode, String nodeType,
			String name, String value, boolean primary, String nature,
			Node linkedOrg, String category, String label) {
		try {
			Node contacts = JcrUtils.mkdirs(parentNode, PEOPLE_CONTACTS,
					NodeType.NT_UNSTRUCTURED);
			Node contact = contacts.addNode(name.trim(), nodeType);
			contact.setProperty(PEOPLE_CONTACT_VALUE, value);
			if (primary)
				markAsPrimary(parentNode, contact);
			setContactLabel(contact, label);
			setContactCategory(contact, category);
			setContactNature(contact, nature, linkedOrg);
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
			boolean primary, String contactNature, String contactCategory,
			String contactLabel) {
		return createContact(parentNode, PeopleTypes.PEOPLE_EMAIL,
				JcrUtils.replaceInvalidChars(emailAddress), emailAddress,
				primary, contactNature, contactCategory, contactLabel);
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
	public static Node createWebsite(Node parentNode, String urlString,
			boolean primary, String nature, String label) {
		return createContact(parentNode, PeopleTypes.PEOPLE_URL,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary,
				nature, null, label);
	}

	public static Node createSocialMedia(Node parentNode, String urlString,
			boolean primary, String nature, String category, String label) {
		return createContact(parentNode, PeopleTypes.PEOPLE_SOCIAL_MEDIA,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary,
				nature, category, label);
	}

	public static Node createImpp(Node parentNode, String urlString,
			boolean primary, String nature, String category, String label) {
		return createContact(parentNode, PeopleTypes.PEOPLE_IMPP,
				JcrUtils.replaceInvalidChars(urlString), urlString, primary,
				nature, category, label);
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
	public static Node createPhone(Node parentNode, String phoneNumber,
			boolean primary, String nature, String category, String label) {
		return createContact(parentNode, PeopleTypes.PEOPLE_PHONE,
				JcrUtils.replaceInvalidChars(phoneNumber), phoneNumber,
				primary, nature, category, label);
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
	public static Node createAddress(Node parentNode, String street1,
			String street2, String zipCode, String city, String state,
			String country, boolean primary, String nature, String category,
			String label) {
		return createAddress(parentNode, street1, street2, zipCode, city,
				state, country, null, primary, nature, category, label);
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
	public static Node createWorkAddress(Node parentNode, Node referencedOrg,
			boolean primary, String category, String label) {
		try {
			Node address = createContact(parentNode,
					PeopleTypes.PEOPLE_ADDRESS, PeopleTypes.PEOPLE_ADDRESS, "",
					false, ContactValueCatalogs.CONTACT_NATURE_PRO, category,
					label);
			address.addMixin(PeopleTypes.PEOPLE_CONTACT_REF);
			// set reference field
			if (referencedOrg != null)
				address.setProperty(PEOPLE_REF_UID,
						referencedOrg.getProperty(PEOPLE_UID).getString());

			if (primary)
				markAsPrimary(parentNode, address);

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
	public static Node createAddress(Node parentNode, String street1,
			String street2, String zipCode, String city, String state,
			String country, String geopoint, boolean primary, String nature,
			String category, String label) {
		try {

			// postpone primary flag management
			Node address = createContact(parentNode,
					PeopleTypes.PEOPLE_ADDRESS, PeopleTypes.PEOPLE_ADDRESS, "",
					false, nature, category, label);
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

			// update primary flag after contact creation
			if (primary)
				markAsPrimary(parentNode, address);

			return address;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new address node", re);
		}
	}

	public static void updateDisplayAddress(Node contactNode) {
		try {
			StringBuilder displayAddress = new StringBuilder();
			List<String> pieces = new ArrayList<String>();

			pieces.add(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STREET));
			pieces.add(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STREET_COMPLEMENT));
			pieces.add(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_ZIP_CODE));
			pieces.add(CommonsJcrUtils
					.get(contactNode, PeopleNames.PEOPLE_CITY));
			pieces.add(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_STATE));
			pieces.add(CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_COUNTRY));

			for (String piece : pieces) {
				if (CommonsJcrUtils.checkNotEmptyString(piece))
					displayAddress.append(piece).append(", ");
			}

			String res = displayAddress.toString();
			if (CommonsJcrUtils.checkNotEmptyString(res)) {
				contactNode.setProperty(PeopleNames.PEOPLE_CONTACT_VALUE,
						res.substring(0, res.lastIndexOf(", ")));
			}
		} catch (RepositoryException e) {
			throw new PeopleException("unable to update display address", e);
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

	/**
	 * Centralizes management of relPath creation for an entity given its type.
	 * Returns null if the node hasn't the necessary property set and thus
	 * cannot be inserted in the permanent repository (It then likely means that
	 * it is a draft entity)
	 */
	public static String getRelPathForEntity(Node node) {
		return getRelPathForEntity(node, null);
	}

	public static void checkPathAndMoveIfNeeded(Node node, String basePath) {
		try {
			String srcPath = node.getPath();
			String destPath = basePath + "/" + getRelPathForEntity(node);
			if (!destPath.equals(srcPath)) {
				JcrUtils.mkdirs(node.getSession(),
						JcrUtils.parentPath(destPath));
				node.getSession().move(srcPath, destPath);
				node.setProperty(PEOPLE_IS_DRAFT, false);
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to move node " + node
					+ " before saving under basePath " + basePath, re);
		}
	}

	/**
	 * workaround to generate rel path while importing existing data in the
	 * repository
	 */
	public static String getRelPathForEntity(Node node, String nodeType) {
		try {
			String relPath = null;

			if (node.isNodeType(PeopleTypes.PEOPLE_PERSON) || nodeType != null
					&& PeopleTypes.PEOPLE_PERSON.equals(nodeType)) {
				// init
				String lastName = "";
				String firstName = "";
				// String displayName = "";
				if (node.hasProperty(PEOPLE_LAST_NAME)) {
					lastName = replaceInvalidChars(node
							.getProperty(PEOPLE_LAST_NAME).getString().trim());
					// remove space
				}
				if (node.hasProperty(PEOPLE_FIRST_NAME))
					firstName = replaceInvalidChars(node
							.getProperty(PEOPLE_FIRST_NAME).getString().trim());

				// Effective building of the rel path
				if (lastName.length() > 1) {
					relPath = JcrUtils.firstCharsToPath(lastName, 2) + "/"
							+ lastName;
				} else
					relPath = PeopleConstants.UNKNOWN_NAME;
				if (firstName.length() > 0)
					relPath += "/" + firstName;
				else
					relPath += "/" + PeopleConstants.UNKNOWN_NAME;

			} else if (node.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)
					|| (nodeType != null && PeopleTypes.PEOPLE_ORGANIZATION
							.equals(nodeType))) {
				// init
				String legalName = "";
				String displayName = "";
				if (node.hasProperty(PEOPLE_LEGAL_NAME))
					legalName = replaceInvalidChars(node
							.getProperty(PEOPLE_LEGAL_NAME).getString().trim());
				if (node.hasProperty(Property.JCR_TITLE))
					displayName = replaceInvalidChars(node
							.getProperty(Property.JCR_TITLE).getString().trim());

				// Effective building of the rel path
				if (legalName.length() > 1)
					relPath = JcrUtils.firstCharsToPath(legalName, 2) + "/"
							+ legalName;
				else if (displayName.length() > 1)
					relPath = PeopleConstants.UNKNOWN_NAME + "/"
							+ JcrUtils.firstCharsToPath(displayName, 2) + "/"
							+ displayName;
			} else if (node.isNodeType(FilmTypes.FILM_FILM) || nodeType != null
					&& FilmTypes.FILM_FILM.equals(nodeType)) {

				// TODO remove this after refactoring the strategy to provide
				// path for entities.
				// init
				String origTitle = "";
				String origLatinTitle = "";
				String displayName = "";
				if (node.hasProperty(FilmNames.FILM_CACHE_PTITLE))
					origTitle = replaceInvalidChars(node
							.getProperty(FilmNames.FILM_CACHE_PTITLE)
							.getString().trim());
				if (node.hasProperty(FilmNames.FILM_CACHE_PTITLE_LATIN))
					origLatinTitle = replaceInvalidChars(node
							.getProperty(FilmNames.FILM_CACHE_PTITLE_LATIN)
							.getString().trim());
				if (node.hasProperty(Property.JCR_TITLE))
					displayName = replaceInvalidChars(node
							.getProperty(Property.JCR_TITLE).getString().trim());

				// Effective building of the rel path
				if (origTitle.length() > 1)
					relPath = JcrUtils.firstCharsToPath(origTitle, 2) + "/"
							+ origTitle;
				else if (origLatinTitle.length() > 1)
					relPath = PeopleConstants.UNKNOWN_NAME + "/"
							+ JcrUtils.firstCharsToPath(origLatinTitle, 2)
							+ "/" + origLatinTitle;
				else if (displayName.length() > 1)
					relPath = PeopleConstants.UNKNOWN_NAME + "/"
							+ PeopleConstants.UNKNOWN_NAME + "/"
							+ JcrUtils.firstCharsToPath(displayName, 2)
							+ displayName;
			} else if (node.isNodeType(PeopleTypes.PEOPLE_GROUP)
					|| (nodeType != null && PeopleTypes.PEOPLE_GROUP
							.equals(nodeType))) {
				// init
				String title = "";
				if (node.hasProperty(Property.JCR_TITLE))
					title = replaceInvalidChars(node
							.getProperty(Property.JCR_TITLE).getString().trim());

				if (title.length() > 1)
					relPath = JcrUtils.firstCharsToPath(title, 2) + "/" + title;
			}
			return relPath;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get rel path for node", re);
		}
	}

	/** Calls JcrUtils */
	public static String replaceInvalidChars(String string) {
		string = JcrUtils.replaceInvalidChars(string);
		return string.replace(' ', '_');
	}

	/**
	 * insure user to retrieve at most one single node in the current
	 * repository, independently of the implementation of the model
	 */
	public static Node getEntityByUid(Session session, String uid) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);
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
								+ ni.getSize() + " corresponding entity(ies)");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve entity of uid: "
					+ uid, e);
		}
	}

	/**
	 * Retrieves the referenced Node in the current repository, using a business
	 * defined ID and regardless of the implemented model. This will at most
	 * return one single node.
	 * 
	 * @param node
	 * @param propName
	 *            the name of the property that contains the used reference.
	 *            Usually we rely on {@link PeopleName#PEOPLE_REF_UID} *
	 * @return
	 */
	public static Node getEntityFromNodeReference(Node node, String propName) {
		try {
			if (node.hasProperty(propName))
				return getEntityByUid(node.getSession(),
						node.getProperty(propName).getString());
			else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get entity from reference node", re);
		}
	}
}