package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;

/**
 * static utils methods to manage CRM concepts in JCR. Rather use these methods
 * than direct Jcr queries in order to ease model evolution.
 */
public class PersonJcrUtils implements PeopleNames {

	/**
	 * 
	 * Get the display name
	 * 
	 */
	public static String getDisplayName(Node person) {
		if (CommonsJcrUtils.getStringValue(person, PEOPLE_DISPLAY_NAME) != null)
			return CommonsJcrUtils.getStringValue(person, PEOPLE_DISPLAY_NAME);
		else {
			String displayName = CommonsJcrUtils.getStringValue(person,
					PEOPLE_LAST_NAME);
			if (CommonsJcrUtils.getStringValue(person, PEOPLE_FIRST_NAME) != null)
				displayName += ", "
						+ CommonsJcrUtils.getStringValue(person,
								PEOPLE_FIRST_NAME);
			return displayName;
		}
	}

	public static String getVariousNameInfo(Node person) {
		StringBuilder nameInfo = new StringBuilder();

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_SALUTATION) != null) {
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_SALUTATION));
			nameInfo.append(" ");
		}
		if (CommonsJcrUtils.getStringValue(person, PEOPLE_PERSON_TITLE) != null) {
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_PERSON_TITLE));
			nameInfo.append(" ");
		}

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_FIRST_NAME) != null) {
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_FIRST_NAME));
			nameInfo.append(" ");
		}

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_NICKNAME) != null) {
			nameInfo.append("(");
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_NICKNAME));
			nameInfo.append(") ");
		}

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_LAST_NAME) != null) {
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_LAST_NAME));
			nameInfo.append(" ");
		}

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_NAME_SUFFIX) != null) {
			nameInfo.append(CommonsJcrUtils.getStringValue(person,
					PEOPLE_NAME_SUFFIX));
			nameInfo.append(" ");
		}
		return nameInfo.toString();
	}

	public static String getSecondaryName(Node person) {
		String secondaryName = null;

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_PSEUDONYM) != null) {
			secondaryName = "Pseudonym: "
					+ CommonsJcrUtils.getStringValue(person, PEOPLE_PSEUDONYM);
		}

		if (CommonsJcrUtils.getStringValue(person, PEOPLE_MAIDEN_NAME) != null) {
			if (secondaryName != null)
				secondaryName += "   ";
			secondaryName += "Maiden name: "
					+ CommonsJcrUtils
							.getStringValue(person, PEOPLE_MAIDEN_NAME);
		}
		return secondaryName == null ? "" : secondaryName;
	}

	public static String getTags(Node person) {
		try {
			StringBuilder tags = new StringBuilder();
			if (person.hasProperty(PEOPLE_TAGS)) {
				for (Value value : person.getProperty(PEOPLE_TAGS).getValues())
					tags.append("#").append(value.getString()).append(" ");
			}
			return tags.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for node "
					+ person, e);
		}
	}
}