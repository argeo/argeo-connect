package org.argeo.connect.people.web;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.TagService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleWebUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;

/**
 * Some helper methods to generate html snippets TODO refactor this once the
 * internationalization has been implemented.
 */
public class PeopleLabelsUtils {

	/** creates a full monty name snippet for overview panels */
	public static String getFullMontyName(Node node) {
		String salutation = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_SALUTATION);
		String firstName = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_FIRST_NAME);
		String lastName = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_LAST_NAME);
		String title = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_HONORIFIC_TITLE);
		String suffix = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_NAME_SUFFIX);
		String nickName = CommonsJcrUtils
				.get(node, PeopleNames.PEOPLE_NICKNAME);
		String maidenName = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_MAIDEN_NAME);
		String middleName = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_MIDDLE_NAME);

		if (CommonsJcrUtils.checkNotEmptyString(salutation)
				|| CommonsJcrUtils.checkNotEmptyString(title)
				|| CommonsJcrUtils.checkNotEmptyString(suffix)
				|| CommonsJcrUtils.checkNotEmptyString(nickName)
				|| CommonsJcrUtils.checkNotEmptyString(maidenName)
				|| CommonsJcrUtils.checkNotEmptyString(middleName)) {
			StringBuilder builder = new StringBuilder();
			// builder.append("<i>");
			builder.append(salutation);
			if (CommonsJcrUtils.checkNotEmptyString(title))
				builder.append(" ").append(title);
			builder.append(" ").append(firstName);
			if (CommonsJcrUtils.checkNotEmptyString(middleName))
				builder.append(" \"").append(middleName).append("\" ");
			builder.append(" ").append(lastName);
			builder.append(" ").append(suffix);

			if (CommonsJcrUtils.checkNotEmptyString(maidenName)
					|| CommonsJcrUtils.checkNotEmptyString(nickName)) {
				builder.append(" <i>(");
				if (CommonsJcrUtils.checkNotEmptyString(maidenName))
					builder.append("Born: ").append(maidenName);

				if (CommonsJcrUtils.checkNotEmptyString(maidenName)
						&& CommonsJcrUtils.checkNotEmptyString(nickName))
					builder.append(", ");
				if (CommonsJcrUtils.checkNotEmptyString(nickName))
					builder.append("aka: ").append(nickName);
				builder.append(")</i>");
			}
			return PeopleWebUtils.replaceAmpersand(builder.toString());
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
				String value = CommonsJcrUtils.get(node,
						PeopleNames.PEOPLE_CONTACT_VALUE);
				if (node.isNodeType(PeopleTypes.PEOPLE_URL)
						|| node.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA))
					builder.append(getUrlLinkSnippet(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_EMAIL))
					builder.append(getMailLinkSnippet(value));
				else if (node.isNodeType(PeopleTypes.PEOPLE_PHONE))
					builder.append(getPhoneLinkSnippet(value));
				else
					// if (node.isNodeType(PeopleTypes.PEOPLE_IMPP))
					builder.append(value);
			}
			builder.append(getContactMetaData(node));
			return PeopleWebUtils.replaceAmpersand(builder.toString());

		} catch (RepositoryException re) {
			throw new PeopleException("Error while generating contact "
					+ "display HTML snippet ", re);
		}
	}

	/** creates the display ReadOnly HTML snippet for a work address */
	public static String getWorkAddressDisplaySnippet(
			PeopleService peopleService, Node contactNode, Node referencedEntity) {
		StringBuilder builder = new StringBuilder();
		// the referenced org
		if (referencedEntity != null)
			builder.append(CommonsJcrUtils.get(referencedEntity,
					Property.JCR_TITLE));
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
		return PeopleWebUtils.replaceAmpersand(builder.toString());
	}

	/** creates the display ReadOnly HTML snippet for various contact metadata */
	public static String getContactMetaData(Node node) {
		StringBuilder builder = new StringBuilder();

		String nature = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_CONTACT_NATURE);
		String category = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_CONTACT_CATEGORY);
		String label = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_CONTACT_LABEL);

		if (CommonsJcrUtils.checkNotEmptyString(nature)
				|| CommonsJcrUtils.checkNotEmptyString(category)
				|| CommonsJcrUtils.checkNotEmptyString(label)) {
			builder.append(PeopleUiConstants.NB_DOUBLE_SPACE + "[");

			if (CommonsJcrUtils.checkNotEmptyString(nature)) {
				builder.append(nature).append(
						CommonsJcrUtils.checkNotEmptyString(category) ? " "
								: "");
			}
			if (CommonsJcrUtils.checkNotEmptyString(category)) {
				builder.append(category);
			}

			if (CommonsJcrUtils.checkNotEmptyString(label)) {
				if (CommonsJcrUtils.checkNotEmptyString(nature)
						|| CommonsJcrUtils.checkNotEmptyString(category))
					builder.append(", ");
				builder.append(label);
			}
			builder.append("]");
		}
		return PeopleWebUtils.replaceAmpersand(builder.toString());
	}

	/** creates an address Display value */
	public static String getAddressDisplayValue(PeopleService peopleService,
			Node node) {
		String street = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STREET);
		String street2 = CommonsJcrUtils.get(node,
				PeopleNames.PEOPLE_STREET_COMPLEMENT);
		String zip = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_ZIP_CODE);
		String city = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_CITY);
		String state = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_STATE);
		String country = CommonsJcrUtils.get(node, PeopleNames.PEOPLE_COUNTRY);

		StringBuilder builder = new StringBuilder();

		if (CommonsJcrUtils.checkNotEmptyString(street))
			builder.append(street);

		if (CommonsJcrUtils.checkNotEmptyString(street2)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(street2);
		}

		if (CommonsJcrUtils.checkNotEmptyString(zip)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(zip);
		}

		if (CommonsJcrUtils.checkNotEmptyString(city)) {
			if (builder.length() > 0)
				if (CommonsJcrUtils.checkNotEmptyString(zip))
					builder.append(" ");
				else
					builder.append(", ");
			builder.append(city);
		}

		if (CommonsJcrUtils.checkNotEmptyString(state)) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(state);
		}

		if (CommonsJcrUtils.checkNotEmptyString(country)) {
			country = ResourcesJcrUtils.getCountryEnLabelFromIso(peopleService,
					CommonsJcrUtils.getSession(node), country);

			if (builder.length() > 0)
				builder.append(", ");
			builder.append(country.toUpperCase());
		}
		return builder.toString();
	}

	/** creates the localisation snippet */
	public static String getLocalisationInfo(PeopleService peopleService,
			Node entity) {
		String town = PeopleJcrUtils.getTownFromItem(entity);
		String country = PeopleJcrUtils.getCountryFromItem(entity);
		if (CommonsJcrUtils.checkNotEmptyString(town)
				|| CommonsJcrUtils.checkNotEmptyString(country)) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			if (CommonsJcrUtils.checkNotEmptyString(town)) {
				builder.append(town);
				if (!CommonsJcrUtils.isEmptyString(country))
					builder.append(", ");
			}
			if (!CommonsJcrUtils.isEmptyString(country)) {
				country = ResourcesJcrUtils.getCountryEnLabelFromIso(
						peopleService, CommonsJcrUtils.getSession(entity),
						country);
				builder.append(country);
			}
			builder.append("]");
			return builder.toString();
		} else
			return "";
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTagLikeValues(PeopleService peopleService,
			Node entity, String propertyName, String prefix) {
		StringBuilder builder = new StringBuilder();

		TagService tagService = peopleService.getTagService();
		try {
			Session session = entity.getSession();
			String tagParentPath = null;
			// TODO fix this
			if (PeopleNames.PEOPLE_MAILING_LISTS.equals(propertyName))
				tagParentPath = peopleService
						.getResourceBasePath(PeopleTypes.PEOPLE_MAILING_LIST);
			else
				tagParentPath = peopleService
						.getResourceBasePath(PeopleTypes.PEOPLE_TAG);
			if (entity.hasProperty(propertyName)) {
				for (Value value : entity.getProperty((propertyName))
						.getValues()) {
					String valueStr = value.getString();
					Node tag = tagService.getRegisteredTag(session,
							tagParentPath, valueStr);
					builder.append(prefix);
					builder.append("<a href=\"#" + tag.getPath() + "\">")
							.append(valueStr).append("</a> ");
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Error while tag like property "
					+ propertyName + " values for node " + entity, e);
		}
		return PeopleWebUtils.replaceAmpersand(builder.toString().trim());
	}

	/** Returns primary contacts (phone, mail, website) as links if they exist */
	public static String getPrimaryContactsSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();

		String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_PHONE);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
			builder.append(getPhoneLinkSnippet(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);
		}

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_EMAIL);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			builder.append(getMailLinkSnippet(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);

		tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
				PeopleTypes.PEOPLE_URL);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			builder.append(getUrlLinkSnippet(tmpStr)).append(
					PeopleUiConstants.NB_DOUBLE_SPACE);

		String result = builder.toString();
		if (result.lastIndexOf(PeopleUiConstants.NB_DOUBLE_SPACE) > 0)
			result = result.substring(0,
					result.lastIndexOf(PeopleUiConstants.NB_DOUBLE_SPACE));
		return PeopleWebUtils.replaceAmpersand(result);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able phone number
	 */
	public static String getPhoneLinkSnippet(String value) {
		return getPhoneLinkSnippet(value, value);
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
	public static String getPhoneLinkSnippet(String value, String label) {
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
	public static String getMailLinkSnippet(String value) {
		return getMailLinkSnippet(value, value);
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
	public static String getMailLinkSnippet(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = PeopleWebUtils.replaceAmpersand(value);
		builder.append("<a href=\"mailto:");
		builder.append(value).append("\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLinkSnippet(String value) {
		return getUrlLinkSnippet(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling
	 * enabled in order to provide a click-able link
	 */
	public static String getUrlLinkSnippet(String value, String label) {
		StringBuilder builder = new StringBuilder();

		value = PeopleWebUtils.replaceAmpersand(value);
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
	public static String getEntityContactSnippet(PeopleService peopleService,
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
					org = peopleService.getEntityByUid(CommonsJcrUtils
							.getSession(currContact), CommonsJcrUtils.get(
							currContact, PeopleNames.PEOPLE_REF_UID));
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				org = entity;

			StringBuilder builder = new StringBuilder();
			// builder.append("<span>");

			builder.append("<b>");
			if (CommonsJcrUtils.checkNotEmptyString(label))
				builder.append(label);
			if (org != null)
				builder.append(CommonsJcrUtils.get(org, Property.JCR_TITLE))
						.append("<br/>");
			builder.append("</b>");
			if (person != null)
				builder.append(peopleService.getDisplayName(person)).append(
						"<br/>");

			// phone
			String tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
					PeopleTypes.PEOPLE_PHONE);
			if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
				builder.append(tmpStr);
				builder.append("<br/>");
			}

			// mail
			tmpStr = PeopleJcrUtils.getPrimaryContactValue(entity,
					PeopleTypes.PEOPLE_EMAIL);
			if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
				builder.append("<a  href=\"mailto:");
				builder.append(tmpStr).append("\">");
				builder.append(tmpStr).append("</a><br/>");
			}

			return PeopleWebUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to create contact snippet for node " + entity, re);
		}
	}
}