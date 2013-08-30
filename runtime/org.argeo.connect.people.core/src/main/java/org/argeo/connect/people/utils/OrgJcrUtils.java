package org.argeo.connect.people.utils;

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

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;

/**
 * static utilitary methods to manage CRM organization concepts in JCR. Rather
 * use these methods than direct Jcr queries in order to ease model evolution.
 */

public class OrgJcrUtils {

	public static Node getOrgWithWebSite(Session session, String website)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();
		final String typeSelector = "website";
		Selector source = factory.selector(PeopleTypes.PEOPLE_WEBSITE,
				typeSelector);
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
}