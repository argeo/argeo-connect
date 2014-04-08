package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
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

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;

/**
 * Static utility methods to manage the resources for people concepts in JCR.
 * Rather use these methods than direct Jcr queries in order to ease model
 * evolution.
 */

public class ResourcesJcrUtils {

	/**
	 * Returns the ISO code given a language name in English or null if none is
	 * found.
	 * 
	 * @return
	 */
	public static String getLangIsoFromEnLabel(PeopleService peopleService,
			Session session, String enLabel) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ISO_LANGUAGE,
					PeopleTypes.PEOPLE_ISO_LANGUAGE);

			Constraint c1 = factory
					.descendantNode(
							source.getSelectorName(),
							peopleService
									.getResourcesBasePath(PeopleConstants.RESOURCE_LANGS));

			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(enLabel));
			Constraint c2 = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);

			Constraint defaultC = factory.and(c1, c2);

			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 1)
				return ni.nextNode().getProperty(PeopleNames.PEOPLE_ISO_CODE)
						.getString();
			else {
				return null;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get isocode for " + enLabel, e);
		}
	}

	public static String getLangEnLabelFromIso(PeopleService peopleService,
			Session session, String isoCode) {
		try {
			String path = peopleService
					.getResourcesBasePath(PeopleConstants.RESOURCE_LANGS)
					+ "/"
					+ isoCode.substring(0, 1) + "/" + isoCode;
			Node code = session.getNode(path);
			return CommonsJcrUtils.get(code, Property.JCR_TITLE);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get lable for " + isoCode, e);
		}
	}

	public static String getCountryIsoFromEnLabel(PeopleService peopleService,
			Session session, String enLabel) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ISO_COUNTRY,
					PeopleTypes.PEOPLE_ISO_COUNTRY);

			Constraint c1 = factory
					.descendantNode(
							source.getSelectorName(),
							peopleService
									.getResourcesBasePath(PeopleConstants.RESOURCE_COUNTRIES));

			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(enLabel));
			Constraint c2 = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);

			Constraint defaultC = factory.and(c1, c2);

			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 1)
				return ni.nextNode().getProperty(PeopleNames.PEOPLE_ISO_CODE)
						.getString();
			else {
				return null;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get isocode for " + enLabel, e);
		}
	}

	public static String getCountryEnLabelFromIso(PeopleService peopleService,
			Session session, String isoCode) {
		try {
			String path = peopleService
					.getResourcesBasePath(PeopleConstants.RESOURCE_COUNTRIES)
					+ "/" + isoCode.substring(0, 1) + "/" + isoCode;
			Node code = session.getNode(path);
			return CommonsJcrUtils.get(code, Property.JCR_TITLE);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get lable for " + isoCode, e);
		}
	}
}