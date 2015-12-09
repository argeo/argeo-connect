package org.argeo.connect.people.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;

/**
 * Some helper methods to generate html snippets TODO refactor this once the
 * internationalization has been implemented.
 */
public class PeopleUiSnippets {

	/**
	 * Simply formats a couple href / label to display a link in a markup
	 * enabled tree / table / label that will trigger a corresponding RWT
	 * specific listener. Such a listener must be able to understand the
	 * specific format of the value of this href attribute
	 */
	public static String getRWTLink(String href, String value) {
		return "<a href=\"" + href + "\" target=\"_rwt\">" + value + "</a>";
	}

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
		String maidenName = JcrUiUtils
				.get(node, PeopleNames.PEOPLE_MAIDEN_NAME);
		String middleName = JcrUiUtils
				.get(node, PeopleNames.PEOPLE_MIDDLE_NAME);

		if (notEmpty(salutation) || notEmpty(title) || notEmpty(suffix)
				|| notEmpty(nickName) || notEmpty(maidenName)
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
			return PeopleUiUtils.replaceAmpersand(builder.toString());
		} // useless otherwise
		else
			return "";
	}

	/** creates the display ReadOnly HTML snippet for various contacts */
	public static String getContactDisplaySnippet(PeopleService peopleService,
			Node node) {
		try {
			StringBuilder builder = new StringBuilder();

			if (node.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
				builder.append(getAddressDisplayValue(peopleService, node));
			} else {
				String value = JcrUiUtils.get(node,
						PeopleNames.PEOPLE_CONTACT_VALUE);
				if (node.isNodeType(PeopleTypes.PEOPLE_URL)
						|| node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA))
					builder.append(getUrlLink(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_EMAIL))
					builder.append(getMailLink(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_PHONE))
					builder.append(getPhoneLink(value));
				else
					// if (node.isNodeType(PeopleTypes.PEOPLE_IMPP))
					builder.append(value);
			}
			builder.append(getContactMetaData(node));
			return PeopleUiUtils.replaceAmpersand(builder.toString());

		} catch (RepositoryException re) {
			throw new PeopleException("Error while generating contact "
					+ "display HTML snippet ", re);
		}
	}

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddress(PeopleService peopleService,
			Node contactNode, Node referencedEntity) {
		StringBuilder builder = new StringBuilder();
		// the referenced org
		if (referencedEntity != null)
			builder.append(JcrUiUtils.get(referencedEntity, Property.JCR_TITLE));
		// current contact meta data
		builder.append(getContactMetaData(contactNode));
		// Referenced org primary address
		if (referencedEntity != null) {
			Node primaryAddress = PeopleJcrUtils.getPrimaryContact(
					referencedEntity, PeopleTypes.PEOPLE_ADDRESS);
			if (primaryAddress != null) {
				builder.append("<br />");
				builder.append(getAddressDisplayValue(peopleService,
						primaryAddress));
			}
		}
		return PeopleUiUtils.replaceAmpersand(builder.toString());
	}

	/** creates the display ReadOnly HTML snippet for various contact metadata */
	public static String getContactMetaData(Node node) {
		StringBuilder builder = new StringBuilder();

		String nature = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_NATURE);
		String category = JcrUiUtils.get(node,
				PeopleNames.PEOPLE_CONTACT_CATEGORY);
		String label = JcrUiUtils.get(node, PeopleNames.PEOPLE_CONTACT_LABEL);

		if (notEmpty(nature) || notEmpty(category) || notEmpty(label)) {
			builder.append(PeopleUiConstants.NB_DOUBLE_SPACE + "[");

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
		return PeopleUiUtils.replaceAmpersand(builder.toString());
	}

	/** creates an address Display value */
	public static String getAddressDisplayValue(PeopleService peopleService,
			Node node) {
		String street = JcrUiUtils.get(node, PeopleNames.PEOPLE_STREET);
		String street2 = JcrUiUtils.get(node,
				PeopleNames.PEOPLE_STREET_COMPLEMENT);
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
			country = peopleService.getResourceService().getEncodedTagValue(
					JcrUiUtils.getSession(node),
					PeopleConstants.RESOURCE_COUNTRY, country);

			if (builder.length() > 0)
				builder.append(", ");
			builder.append(country.toUpperCase());
		}
		return builder.toString();
	}

	/** creates the localisation snippet */
	public static String getLocalisationInfo(PeopleService peopleService,
			Node entity) {
		String town = PeopleJcrUtils.getTownFromItem(peopleService, entity);
		String country = PeopleJcrUtils.getCountryFromItem(peopleService,
				entity);
		if (notEmpty(town) || notEmpty(country)) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			if (notEmpty(town)) {
				builder.append(town);
				if (notEmpty(country))
					builder.append(", ");
			}
			if (notEmpty(country)) {
				country = peopleService.getResourceService()
						.getEncodedTagValue(JcrUiUtils.getSession(entity),
								PeopleConstants.RESOURCE_COUNTRY, country);
				builder.append(country);
			}
			builder.append("]");
			return builder.toString();
		} else
			return "";
	}

	/** Returns primary contacts (phone, mail, website) as links if they exist */
	public static String getPrimaryContacts(Node entity) {
		StringBuilder builder = new StringBuilder();

		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_PHONE);
		if (notEmpty(tmpStr)) {
			builder.append(getPhoneLink(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);
		}

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_EMAIL);
		if (notEmpty(tmpStr))
			builder.append(getMailLink(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_URL);
		if (notEmpty(tmpStr))
			builder.append(getUrlLink(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);

		String result = builder.toString();
		if (result.lastIndexOf(PeopleUiConstants.NB_DOUBLE_SPACE) > 0)
			result = result.substring(0,
					result.lastIndexOf(PeopleUiConstants.NB_DOUBLE_SPACE));
		return PeopleUiUtils.replaceAmpersand(result);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able phone number
	 */
	public static String getPhoneLink(String value) {
		return getPhoneLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able phone number
	 * 
	 * @param value
	 * @param label
	 *            a potentially distinct label
	 * @return
	 */
	public static String getPhoneLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		builder.append("<a href=\"tel:");
		builder.append(value).append("\" target=\"_blank\" >").append(label)
				.append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able mail
	 */
	public static String getMailLink(String value) {
		return getMailLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able mail
	 * 
	 * @param value
	 * @param label
	 *            a potentially distinct label
	 * @return
	 */
	public static String getMailLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = PeopleUiUtils.replaceAmpersand(value);
		builder.append("<a href=\"mailto:");
		builder.append(value).append("\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLink(String value) {
		return getUrlLink(value, value);
	}

	// /**
	// * Creates the read-only HTML snippet to display in a label with styling
	// * enabled in order to provide a click-able link. It makes extra check to
	// be
	// * sure the URL would be rendering correctly
	// */
	// public static String getUrlLinkSafely(String value) {
	// return getUrlLink(value, value);
	// }

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = PeopleUiUtils.replaceAmpersand(value);
		label = PeopleUiUtils.replaceAmpersand(label);
		if (!(value.startsWith("http://") || value.startsWith("https://")))
			value = "http://" + value;
		builder.append("<a href=\"");
		builder.append(value + "\" target=\"_blank\" >" + label + "</a>");
		return builder.toString();
	}

	/**
	 * 
	 * @param entity
	 * @param label
	 *            an optional label to be displayed first
	 * @return
	 */
	public static String getEntityContact(PeopleService peopleService,
			Node entity, String label) {
		try {
			// local cache
			Node person = null, org = null;

			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				person = entity;
				Node currContact = PeopleJcrUtils.getPrimaryContact(person,
						PeopleTypes.PEOPLE_ADDRESS);
				if (!(currContact == null || !currContact
						.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))) {
					org = peopleService.getEntityByUid(JcrUiUtils
							.getSession(currContact), JcrUiUtils.get(
							currContact, PeopleNames.PEOPLE_REF_UID));
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				org = entity;

			StringBuilder builder = new StringBuilder();

			builder.append("<b>");
			if (notEmpty(label))
				builder.append(label);
			if (org != null)
				builder.append(JcrUiUtils.get(org, Property.JCR_TITLE)).append(
						"<br/>");
			builder.append("</b>");
			if (person != null)
				builder.append(peopleService.getDisplayName(person)).append(
						"<br/>");

			// phone
			String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
					PeopleTypes.PEOPLE_PHONE);
			if (notEmpty(tmpStr))
				builder.append(getPhoneLink(tmpStr)).append("<br/>");

			// mail
			tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
					PeopleTypes.PEOPLE_EMAIL);
			if (notEmpty(tmpStr))
				builder.append(getMailLink(tmpStr)).append("<br/>");

			return PeopleUiUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to create contact snippet for node " + entity, re);
		}
	}

	/**
	 * Simply gets a snippet to display clickable primary contacts
	 * 
	 * @param entity
	 * @return
	 */
	public static String getPrimaryContacts(PeopleService peopleService,
			Node entity) {
		StringBuilder builder = new StringBuilder();
		// phone
		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_PHONE);
		if (notEmpty(tmpStr))
			builder.append(getPhoneLink(tmpStr)).append("<br/>");

		// mail
		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_EMAIL);
		if (notEmpty(tmpStr))
			builder.append(getMailLink(tmpStr)).append("<br/>");
		return PeopleUiUtils.replaceAmpersand(builder.toString());
	}

	/** shortcut to set form data while dealing with switching panel */
	private final static DateFormat df = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("<i>").append("Last updated on ");
				builder.append(df.format(entity
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");
				builder.append(entity
						.getProperty(Property.JCR_LAST_MODIFIED_BY).getString());
				builder.append(". </i>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}