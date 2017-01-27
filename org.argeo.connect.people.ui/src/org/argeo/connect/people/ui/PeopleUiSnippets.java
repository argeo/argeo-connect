package org.argeo.connect.people.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.JcrUiUtils;

/**
 * Some helper methods to generate html snippets TODO refactor this once the
 * internationalization has been implemented.
 */
public class PeopleUiSnippets {

	/**
	 * Creates a snippet with all information we have about the name of a given
	 * person
	 */
	public static String getFullMontyName(Node node) {
		String salutation = JcrUiUtils.get(node, PeopleNames.PEOPLE_SALUTATION);
		String firstName = JcrUiUtils.get(node, PeopleNames.PEOPLE_FIRST_NAME);
		String lastName = JcrUiUtils.get(node, PeopleNames.PEOPLE_LAST_NAME);
		String title = JcrUiUtils.get(node, PeopleNames.PEOPLE_HONORIFIC_TITLE);
		String suffix = JcrUiUtils.get(node, PeopleNames.PEOPLE_NAME_SUFFIX);
		String nickName = JcrUiUtils.get(node, PeopleNames.PEOPLE_NICKNAME);
		String maidenName = JcrUiUtils.get(node, PeopleNames.PEOPLE_MAIDEN_NAME);
		String middleName = JcrUiUtils.get(node, PeopleNames.PEOPLE_MIDDLE_NAME);

		if (notEmpty(salutation) || notEmpty(title) || notEmpty(suffix) || notEmpty(nickName) || notEmpty(maidenName)
				|| notEmpty(middleName)) {
			StringBuilder builder = new StringBuilder();
			// builder.append("<i>");
			builder.append(salutation);
			if (notEmpty(title))
				builder.append(" ").append(title);
			builder.append(" ").append(firstName);
			if (notEmpty(middleName))
				builder.append(" \"").append(middleName).append("\" ");
			builder.append(" ").append(lastName);
			builder.append(" ").append(suffix);

			if (notEmpty(maidenName) || notEmpty(nickName)) {
				builder.append(" <i>(");
				if (notEmpty(maidenName))
					builder.append("Born: ").append(maidenName);

				if (notEmpty(maidenName) && notEmpty(nickName))
					builder.append(", ");
				if (notEmpty(nickName))
					builder.append("aka: ").append(nickName);
				builder.append(")</i>");
			}
			return ConnectUiUtils.replaceAmpersand(builder.toString());
		} // useless otherwise
		else
			return "";
	}

	/** creates the display ReadOnly HTML snippet for various contacts */
	public static String getContactDisplaySnippet(PeopleService peopleService, Node node) {
		try {
			StringBuilder builder = new StringBuilder();

			if (node.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
				builder.append(getAddressDisplayValue(peopleService, node));
			} else {
				String value = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_VALUE);
				if (node.isNodeType(PeopleTypes.PEOPLE_URL) || node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA))
					builder.append(ConnectUiSnippets.getUrlLink(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_EMAIL))
					builder.append(ConnectUiSnippets.getMailLink(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_PHONE))
					builder.append(ConnectUiSnippets.getPhoneLink(value));
				else
					// if (node.isNodeType(PeopleTypes.PEOPLE_IMPP))
					builder.append(value);
			}
			builder.append(getContactMetaData(node));
			return ConnectUiUtils.replaceAmpersand(builder.toString());

		} catch (RepositoryException re) {
			throw new PeopleException("Error while generating contact " + "display HTML snippet ", re);
		}
	}

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddress(PeopleService peopleService, Node contactNode, Node referencedEntity) {
		StringBuilder builder = new StringBuilder();
		// the referenced org
		if (referencedEntity != null)
			builder.append(JcrUiUtils.get(referencedEntity, Property.JCR_TITLE));
		// current contact meta data
		builder.append(getContactMetaData(contactNode));
		// Referenced org primary address
		if (referencedEntity != null) {
			Node primaryAddress = PeopleJcrUtils.getPrimaryContact(referencedEntity, PeopleTypes.PEOPLE_ADDRESS);
			if (primaryAddress != null) {
				builder.append("<br />");
				builder.append(getAddressDisplayValue(peopleService, primaryAddress));
			}
		}
		return ConnectUiUtils.replaceAmpersand(builder.toString());
	}

	/**
	 * creates the display ReadOnly HTML snippet for various contact metadata
	 */
	public static String getContactMetaData(Node node) {
		StringBuilder builder = new StringBuilder();

		String nature = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_NATURE);
		String category = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_CATEGORY);
		String label = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_LABEL);

		if (notEmpty(nature) || notEmpty(category) || notEmpty(label)) {
			builder.append(ConnectUiConstants.NB_DOUBLE_SPACE + "[");

			if (notEmpty(nature)) {
				builder.append(nature).append(notEmpty(category) ? " " : "");
			}
			if (notEmpty(category)) {
				builder.append(category);
			}

			if (notEmpty(label)) {
				if (notEmpty(nature) || notEmpty(category))
					builder.append(", ");
				builder.append(label);
			}
			builder.append("]");
		}
		return ConnectUiUtils.replaceAmpersand(builder.toString());
	}

	/** creates an address Display value */
	public static String getAddressDisplayValue(PeopleService peopleService, Node node) {
		String street = JcrUiUtils.get(node, PeopleNames.PEOPLE_STREET);
		String street2 = JcrUiUtils.get(node, PeopleNames.PEOPLE_STREET_COMPLEMENT);
		String zip = JcrUiUtils.get(node, PeopleNames.PEOPLE_ZIP_CODE);
		String city = JcrUiUtils.get(node, PeopleNames.PEOPLE_CITY);
		String state = JcrUiUtils.get(node, PeopleNames.PEOPLE_STATE);
		String country = JcrUiUtils.get(node, PeopleNames.PEOPLE_COUNTRY);

		StringBuilder builder = new StringBuilder();

		if (notEmpty(street))
			builder.append(street);

		if (notEmpty(street2)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(street2);
		}

		if (notEmpty(zip)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(zip);
		}

		if (notEmpty(city)) {
			if (builder.length() > 0)
				if (notEmpty(zip))
					builder.append(" ");
				else
					builder.append(", ");
			builder.append(city);
		}

		if (notEmpty(state)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(state);
		}

		if (notEmpty(country)) {
			country = peopleService.getResourceService().getEncodedTagValue(JcrUiUtils.getSession(node),
					PeopleConstants.RESOURCE_COUNTRY, country);

			if (builder.length() > 0)
				builder.append(", ");
			builder.append(country.toUpperCase());
		}
		return builder.toString();
	}

	/** creates the localisation snippet */
	public static String getLocalisationInfo(PeopleService peopleService, Node entity) {
		String town = PeopleJcrUtils.getTownFromItem(peopleService, entity);
		String country = PeopleJcrUtils.getCountryFromItem(peopleService, entity);
		if (notEmpty(town) || notEmpty(country)) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			if (notEmpty(town)) {
				builder.append(town);
				if (notEmpty(country))
					builder.append(", ");
			}
			if (notEmpty(country)) {
				country = peopleService.getResourceService().getEncodedTagValue(JcrUiUtils.getSession(entity),
						PeopleConstants.RESOURCE_COUNTRY, country);
				builder.append(country);
			}
			builder.append("]");
			return builder.toString();
		} else
			return "";
	}

	/**
	 * Returns primary contacts (phone, mail, website) as links if they exist
	 */
	public static String getPrimaryContacts(Node entity) {
		StringBuilder builder = new StringBuilder();

		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_PHONE);
		if (notEmpty(tmpStr)) {
			builder.append(ConnectUiSnippets.getPhoneLink(tmpStr)).append(ConnectUiConstants.NB_DOUBLE_SPACE);
		}

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_EMAIL);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getMailLink(tmpStr)).append(ConnectUiConstants.NB_DOUBLE_SPACE);

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_URL);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getUrlLink(tmpStr)).append(ConnectUiConstants.NB_DOUBLE_SPACE);

		String result = builder.toString();
		if (result.lastIndexOf(ConnectUiConstants.NB_DOUBLE_SPACE) > 0)
			result = result.substring(0, result.lastIndexOf(ConnectUiConstants.NB_DOUBLE_SPACE));
		return ConnectUiUtils.replaceAmpersand(result);
	}

	/**
	 * 
	 * @param entity
	 * @param label
	 *            an optional label to be displayed first
	 * @return
	 */
	public static String getEntityContact(PeopleService peopleService, Node entity, String label) {
		try {
			// local cache
			Node person = null, org = null;

			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				person = entity;
				Node currContact = PeopleJcrUtils.getPrimaryContact(person, PeopleTypes.PEOPLE_ADDRESS);
				if (!(currContact == null || !currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))) {
					org = peopleService.getEntityByUid(JcrUiUtils.getSession(currContact),
							JcrUiUtils.get(currContact, PeopleNames.PEOPLE_REF_UID));
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				org = entity;

			StringBuilder builder = new StringBuilder();

			builder.append("<b>");
			if (notEmpty(label))
				builder.append(label);
			if (org != null)
				builder.append(JcrUiUtils.get(org, Property.JCR_TITLE)).append("<br/>");
			builder.append("</b>");
			if (person != null)
				builder.append(peopleService.getDisplayName(person)).append("<br/>");

			String pam = getEntityPhoneAndMailFormatted(entity);
			if (notEmpty(pam))
				builder.append(pam);
			return ConnectUiUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create contact snippet for node " + entity, re);
		}
	}

	public static String getEntityPhoneAndMailFormatted(Node entity) {
		StringBuilder builder = new StringBuilder();
		// phone
		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_PHONE);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getPhoneLink(tmpStr)).append("<br/>");

		// mail
		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_EMAIL);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getMailLink(tmpStr)).append("<br/>");
		return ConnectUiUtils.replaceAmpersand(builder.toString());
	}

	/**
	 * Simply gets a snippet to display clickable primary contacts
	 * 
	 * @param entity
	 * @return
	 */
	public static String getPrimaryContacts(PeopleService peopleService, Node entity) {
		StringBuilder builder = new StringBuilder();
		// phone
		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_PHONE);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getPhoneLink(tmpStr)).append("<br/>");

		// mail
		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity, PeopleTypes.PEOPLE_EMAIL);
		if (notEmpty(tmpStr))
			builder.append(ConnectUiSnippets.getMailLink(tmpStr)).append("<br/>");
		return ConnectUiUtils.replaceAmpersand(builder.toString());
	}
}
