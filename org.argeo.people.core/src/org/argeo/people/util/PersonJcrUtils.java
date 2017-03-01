package org.argeo.people.util;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.ConnectNames;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleTypes;

/**
 * Static utilitary methods to manage Person concepts in JCR. Rather use these
 * methods than direct JCR queries in order to ease model evolution.
 */
public class PersonJcrUtils implements PeopleNames {

	public static String getVariousNameInfo(Node person) {
		StringBuilder nameInfo = new StringBuilder();

		if (ConnectJcrUtils.get(person, PEOPLE_SALUTATION) != null) {
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_SALUTATION));
			nameInfo.append(" ");
		}
		if (ConnectJcrUtils.get(person, PEOPLE_HONORIFIC_TITLE) != null) {
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_HONORIFIC_TITLE));
			nameInfo.append(" ");
		}

		if (ConnectJcrUtils.get(person, PEOPLE_FIRST_NAME) != null) {
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_FIRST_NAME));
			nameInfo.append(" ");
		}

		if (ConnectJcrUtils.get(person, PEOPLE_NICKNAME) != null) {
			nameInfo.append("(");
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_NICKNAME));
			nameInfo.append(") ");
		}

		if (ConnectJcrUtils.get(person, PEOPLE_LAST_NAME) != null) {
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_LAST_NAME));
			nameInfo.append(" ");
		}

		if (ConnectJcrUtils.get(person, PEOPLE_NAME_SUFFIX) != null) {
			nameInfo.append(ConnectJcrUtils.get(person, PEOPLE_NAME_SUFFIX));
			nameInfo.append(" ");
		}
		return nameInfo.toString();
	}

	public static String getSecondaryName(Node person) {
		String secondaryName = null;
		String nickName = ConnectJcrUtils.get(person, PEOPLE_NICKNAME);

		if (EclipseUiUtils.notEmpty(nickName)) {
			secondaryName = "Nickname: " + nickName;
		}

		String maidenName = ConnectJcrUtils.get(person, PEOPLE_MAIDEN_NAME);
		if (EclipseUiUtils.notEmpty(maidenName)) {
			if (secondaryName != null)
				secondaryName += "   ";
			secondaryName += "Maiden name: " + maidenName;
		}
		return secondaryName == null ? "" : secondaryName;
	}

	/**
	 * Helper to retrieve a person given her first and last Name. Must be
	 * refined.
	 */
	public static Node getPersonWithLastAndFirstName(Session session, String lastName, String firstName)
			throws RepositoryException {
		QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
		final String typeSelector = "person";
		Selector source = factory.selector(PeopleTypes.PEOPLE_PERSON, typeSelector);
		DynamicOperand dynOp = factory.propertyValue(source.getSelectorName(), PEOPLE_LAST_NAME);
		DynamicOperand dynOp2 = factory.propertyValue(source.getSelectorName(), PEOPLE_FIRST_NAME);
		StaticOperand statOp = factory.literal(session.getValueFactory().createValue(lastName));
		StaticOperand statOp2 = factory.literal(session.getValueFactory().createValue(firstName));
		Constraint defaultC = factory.comparison(dynOp, QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
		Constraint defaultC2 = factory.comparison(dynOp2, QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp2);
		defaultC = factory.and(defaultC, defaultC2);

		QueryObjectModel query = factory.createQuery(source, defaultC, null, null);
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
	 *            instance "Actor" or "Engineer" - Not yet implemented
	 */
	public static Node addJob(Node person, Node org, String department, String role, String title, boolean isPrimary,
			Calendar dateBegin, Calendar dateEnd, Boolean isCurrent) throws RepositoryException {

		Node jobs = JcrUtils.mkdirs(person, PEOPLE_JOBS);
		Node job = jobs.addNode(org.getName());
		job.addMixin(PeopleTypes.PEOPLE_JOB);
		job.setProperty(PEOPLE_REF_UID, org.getProperty(ConnectNames.CONNECT_UID).getString());

		if (EclipseUiUtils.notEmpty(role))
			job.setProperty(PEOPLE_ROLE, role);
		if (EclipseUiUtils.notEmpty(department))
			job.setProperty(PEOPLE_DEPARTMENT, department);
		if (EclipseUiUtils.notEmpty(title))
			throw new PeopleException("Position Nature: Unimplemented property ");
		if (dateBegin != null)
			job.setProperty(PEOPLE_DATE_BEGIN, dateBegin);
		if (dateEnd != null)
			job.setProperty(PEOPLE_DATE_END, dateEnd);
		if (isCurrent != null)
			job.setProperty(PEOPLE_IS_CURRENT, isCurrent);
		// TODO manage primary concept
		return job;
	}

	/**
	 * Shortcut to add a job for a given person using default values
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 */
	public static Node addJob(Node person, Node org, String department, String role, boolean isPrimary)
			throws RepositoryException {
		return addJob(person, org, department, role, null, isPrimary, null, null, null);
	}

	/**
	 * Shortcut to add a job for a given person using default values
	 * 
	 * @param role
	 *            the role of the given entity in this group. Cannot be null
	 */
	public static Node addJob(Node person, Node org, String role, boolean isPrimary) throws RepositoryException {
		return addJob(person, org, null, role, null, isPrimary, null, null, null);
	}

	public static Node getPrimaryJob(Node person) throws RepositoryException {
		// Rather
		if (person.hasNode(PEOPLE_JOBS)) {
			NodeIterator ni = person.getNode(PEOPLE_JOBS).getNodes();
			while (ni.hasNext()) {
				Node pJob = ni.nextNode();
				if (pJob.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
						&& pJob.getProperty(PeopleNames.PEOPLE_IS_PRIMARY).getBoolean())
					return pJob;
			}
		}
		return null;
	}
}
