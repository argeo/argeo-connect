package org.argeo.connect.people.utils;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;

/**
 * static utils methods to manage Person concepts in JCR. Rather use these
 * methods than direct Jcr queries in order to ease model evolution.
 */
public class PersonJcrUtils implements PeopleNames {

	/**
	 * Get the display name
	 */
	public static String getPersonDisplayName(Node person) {
		String displayName = null;
		try {
			if (person.hasProperty(PEOPLE_USE_DEFAULT_DISPLAY_NAME)
					&& person.getProperty(PEOPLE_USE_DEFAULT_DISPLAY_NAME)
							.getBoolean()) {
				String lastName = CommonsJcrUtils.get(person, PEOPLE_LAST_NAME);
				String firstName = CommonsJcrUtils.get(person,
						PEOPLE_FIRST_NAME);
				if (CommonsJcrUtils.checkNotEmptyString(firstName)
						|| CommonsJcrUtils.checkNotEmptyString(lastName)) {
					displayName = lastName;
					if (CommonsJcrUtils.checkNotEmptyString(firstName)
							&& CommonsJcrUtils.checkNotEmptyString(lastName))
						displayName += ", ";
					displayName += firstName;

				}
			} else
				displayName = CommonsJcrUtils.get(person, Property.JCR_TITLE);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get Person display name", e);
		}
		return displayName;
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
		String nickName = CommonsJcrUtils.get(person, PEOPLE_NICKNAME);

		if (CommonsJcrUtils.checkNotEmptyString(nickName)) {
			secondaryName = "Nickname: " + nickName;
		}

		String maidenName = CommonsJcrUtils.get(person, PEOPLE_MAIDEN_NAME);
		if (CommonsJcrUtils.checkNotEmptyString(maidenName)) {
			if (secondaryName != null)
				secondaryName += "   ";
			secondaryName += "Maiden name: " + maidenName;
		}
		return secondaryName == null ? "" : secondaryName;
	}

	// @Deprecated
	// public static String getTags(Node person) {
	// try {
	// StringBuilder tags = new StringBuilder();
	// if (person.hasProperty(PEOPLE_TAGS)) {
	// for (Value value : person.getProperty(PEOPLE_TAGS).getValues())
	// tags.append("#").append(value.getString()).append(" ");
	// }
	// return tags.toString();
	// } catch (RepositoryException e) {
	// throw new PeopleException("Error while getting tags for node "
	// + person, e);
	// }
	// }

	/**
	 * Helper to retrieve a person given her first and last Name. Must be
	 * refined.
	 */
	public static Node getPersonWithLastAndFirstName(Session session,
			String lastName, String firstName) throws RepositoryException {
		QueryObjectModelFactory factory = session.getWorkspace()
				.getQueryManager().getQOMFactory();
		final String typeSelector = "person";
		Selector source = factory.selector(PeopleTypes.PEOPLE_PERSON,
				typeSelector);
		DynamicOperand dynOp = factory.propertyValue(source.getSelectorName(),
				PEOPLE_LAST_NAME);
		DynamicOperand dynOp2 = factory.propertyValue(source.getSelectorName(),
				PEOPLE_FIRST_NAME);
		StaticOperand statOp = factory.literal(session.getValueFactory()
				.createValue(lastName));
		StaticOperand statOp2 = factory.literal(session.getValueFactory()
				.createValue(firstName));
		Constraint defaultC = factory.comparison(dynOp,
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
		Constraint defaultC2 = factory.comparison(dynOp2,
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp2);
		defaultC = factory.and(defaultC, defaultC2);

		QueryObjectModel query = factory.createQuery(source, defaultC, null,
				null);
		QueryResult result = query.execute();
		NodeIterator ni = result.getNodes();
		// TODO clean this to handle multiple result
		if (ni.hasNext())
			return ni.nextNode();
		return null;
	}

	/**
	 * Add a job for a given person
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 * @param title
	 *            OPTIONAL: the nature of the subject in this relation, for
	 *            instance "Actor" or "Engineer"
	 * */
	public static Node addJob(Node person, Node org, String role, String title,
			Calendar dateBegin, Calendar dateEnd, Boolean isCurrent)
			throws RepositoryException {

		Node jobs = CommonsJcrUtils.getOrCreateDirNode(person, PEOPLE_JOBS);
		Node job = jobs.addNode(org.getName(), PeopleTypes.PEOPLE_JOB);
		job.setProperty(PEOPLE_REF_UID, org.getProperty(PEOPLE_UID).getString());
		job.setProperty(PEOPLE_ROLE, role);
		if (CommonsJcrUtils.checkNotEmptyString(title))
			job.setProperty(PEOPLE_TITLE, title);
		if (dateBegin != null)
			job.setProperty(PEOPLE_DATE_BEGIN, dateBegin);
		if (dateEnd != null)
			job.setProperty(PEOPLE_DATE_END, dateEnd);
		if (isCurrent != null)
			job.setProperty(PEOPLE_IS_CURRENT, isCurrent);
		return job;
	}

	/**
	 * Shortcut to add a job for a given person using default values
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 * */
	public static Node addJob(Node person, Node org, String role)
			throws RepositoryException {
		return addJob(person, org, role, null, null, null, null);
	}

}