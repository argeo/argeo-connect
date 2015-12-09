package org.argeo.connect.people.util;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
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
 * Static utility methods to manage CRM organisation concepts in JCR. Rather use
 * these methods than direct JCR queries in order to ease model evolution.
 */
public class OrgJcrUtils implements PeopleNames {

	/**
	 * Mainly used during imports to provide a key to a given organisation. Do
	 * not rely on this for production purposes. *
	 */
	public static Node getOrgWithWebSite(Session session, String website)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();
		Selector source = factory.selector(PeopleTypes.PEOPLE_URL,
				PeopleTypes.PEOPLE_URL);
		DynamicOperand dynOp = factory.propertyValue(source.getSelectorName(),
				PeopleNames.PEOPLE_CONTACT_VALUE);
		StaticOperand statOp = factory.literal(session.getValueFactory()
				.createValue(website));
		Constraint defaultC = factory.comparison(factory.lowerCase(dynOp),
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
		QueryObjectModel query = factory.createQuery(source, defaultC, null,
				null);
		QueryResult result = query.execute();
		NodeIterator ni = result.getNodes();
		Node orga = null;
		if (ni.hasNext())
			orga = ni.nextNode().getParent().getParent();
		return orga;
	}

	/**
	 * Mainly used during imports to provide a key to a given organisation. Do
	 * not rely on this for production purposes.
	 */
	public static Node getOrgByName(Session session, String name)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();
		Selector source = factory.selector(PeopleTypes.PEOPLE_ORG,
				PeopleTypes.PEOPLE_ORG);

		DynamicOperand dynOp = factory.propertyValue(source.getSelectorName(),
				PeopleNames.PEOPLE_LEGAL_NAME);
		StaticOperand statOp = factory.literal(session.getValueFactory()
				.createValue(name));
		Constraint defaultC = factory.comparison(dynOp,
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);

		QueryObjectModel query = factory.createQuery(source, defaultC, null,
				null);
		QueryResult result = query.execute();
		NodeIterator ni = result.getNodes();

		long resultNb = ni.getSize();

		if (resultNb == 0)
			return null;
		else if (resultNb == 1)
			return ni.nextNode();
		else
			throw new PeopleException("More than 1 org with name " + name
					+ " has been found.");
	}

	/* PAYMENT MANAGEMENT */
	public static NodeIterator getPaymentAccounts(Node entity) {
		try {
			if (entity.hasNode(PEOPLE_PAYMENT_ACCOUNTS))
				return entity.getNode(PEOPLE_PAYMENT_ACCOUNTS).getNodes();
			else
				return null;
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting "
					+ "payment accounts for node " + entity, e);
		}
	}

	public static Node getPrimaryPaymentAccount(Node entity) {
		NodeIterator nit = getPaymentAccounts(entity);
		if (nit == null || nit.getSize() == 0)
			return null;
		else if (nit.getSize() > 1)
			throw new PeopleException(
					"Unable to get primary payment accounts for " + entity
							+ ". Multiple accounts is not implemented and "
							+ nit.getSize() + " accounts has been found.");
		else
			return nit.nextNode();
	}

	public static Node createPaymentAccount(Node entity, String nodeType,
			String name) {
		try {
			Node accounts = null;
			if (entity.hasNode(PEOPLE_PAYMENT_ACCOUNTS))
				accounts = entity.getNode(PEOPLE_PAYMENT_ACCOUNTS);
			else
				accounts = entity.addNode(PEOPLE_PAYMENT_ACCOUNTS);
			return accounts.addNode(name, nodeType);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Error while creating new payment account for node "
							+ entity, e);
		}
	}
}