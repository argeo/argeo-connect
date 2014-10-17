package org.argeo.connect.people.utils;


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
	// public static String getLangIsoFromEnLabel(PeopleService peopleService,
	// Session session, String enLabel) {
	// try {
	// QueryManager queryManager = session.getWorkspace()
	// .getQueryManager();
	// QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// Selector source = factory.selector(PeopleTypes.PEOPLE_ISO_LANGUAGE,
	// PeopleTypes.PEOPLE_ISO_LANGUAGE);
	//
	// Constraint c1 = factory
	// .descendantNode(source.getSelectorName(), peopleService
	// .getResourceBasePath(PeopleConstants.RESOURCE_LANG));
	//
	// DynamicOperand dynOp = factory.propertyValue(
	// source.getSelectorName(), Property.JCR_TITLE);
	// StaticOperand statOp = factory.literal(session.getValueFactory()
	// .createValue(enLabel));
	// Constraint c2 = factory.comparison(dynOp,
	// QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
	//
	// Constraint defaultC = factory.and(c1, c2);
	//
	// QueryObjectModel query = factory.createQuery(source, defaultC,
	// null, null);
	// QueryResult queryResult = query.execute();
	// NodeIterator ni = queryResult.getNodes();
	//
	// if (ni.getSize() == 1)
	// return ni.nextNode().getProperty(PeopleNames.PEOPLE_ISO_CODE)
	// .getString();
	// else {
	// return null;
	// }
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to get isocode for " + enLabel, e);
	// }
	// }
	//
	// public static String getLangEnLabelFromIso(PeopleService peopleService,
	// Session session, String isoCode) {
	// try {
	// String path = peopleService
	// .getResourceBasePath(PeopleConstants.RESOURCE_LANG)
	// + "/"
	// + isoCode.substring(0, 1) + "/" + isoCode;
	// Node code = session.getNode(path);
	// return CommonsJcrUtils.get(code, Property.JCR_TITLE);
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to get lable for " + isoCode, e);
	// }
	// }
	//
	// public static String getCountryIsoFromEnLabel(PeopleService
	// peopleService,
	// Session session, String enLabel) {
	// try {
	// QueryManager queryManager = session.getWorkspace()
	// .getQueryManager();
	// QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// Selector source = factory.selector(PeopleTypes.PEOPLE_ISO_COUNTRY,
	// PeopleTypes.PEOPLE_ISO_COUNTRY);
	//
	// Constraint c1 = factory
	// .descendantNode(
	// source.getSelectorName(),
	// peopleService
	// .getResourceBasePath(PeopleConstants.RESOURCE_COUNTRY));
	//
	// DynamicOperand dynOp = factory.propertyValue(
	// source.getSelectorName(), Property.JCR_TITLE);
	// StaticOperand statOp = factory.literal(session.getValueFactory()
	// .createValue(enLabel));
	// Constraint c2 = factory.comparison(dynOp,
	// QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
	//
	// Constraint defaultC = factory.and(c1, c2);
	//
	// QueryObjectModel query = factory.createQuery(source, defaultC,
	// null, null);
	// QueryResult queryResult = query.execute();
	// NodeIterator ni = queryResult.getNodes();
	//
	// if (ni.getSize() == 1)
	// return ni.nextNode().getProperty(PeopleNames.PEOPLE_ISO_CODE)
	// .getString();
	// else {
	// return null;
	// }
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to get isocode for " + enLabel, e);
	// }
	// }
	//
	// public static String getCountryEnLabelFromIso(PeopleService
	// peopleService,
	// Session session, String isoCode) {
	// try {
	// if (isoCode == null)
	// return "";
	// else if (isoCode.length() < 2)
	// return isoCode;
	// else {
	// String path = peopleService
	// .getResourceBasePath(PeopleConstants.RESOURCE_COUNTRY)
	// + "/" + isoCode.substring(0, 1) + "/" + isoCode;
	// if (session.nodeExists(path))
	// return CommonsJcrUtils.get(session.getNode(path),
	// Property.JCR_TITLE);
	// else
	// return isoCode;
	// }
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to get label for " + isoCode, e);
	// }
	// }
	//
	// public static Node getTagNodeFromValue(Session session, String basePath,
	// String tagValue) {
	// try {
	// QueryObjectModelFactory factory = session.getWorkspace()
	// .getQueryManager().getQOMFactory();
	// Selector source = factory.selector(NodeType.MIX_TITLE,
	// NodeType.MIX_TITLE);
	// Constraint c1 = factory.descendantNode(source.getSelectorName(),
	// basePath);
	// DynamicOperand dynOp = factory.propertyValue(
	// source.getSelectorName(), Property.JCR_TITLE);
	// StaticOperand statOp = factory.literal(session.getValueFactory()
	// .createValue(tagValue));
	// Constraint c2 = factory.comparison(dynOp,
	// QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
	// Constraint defaultC = factory.and(c1, c2);
	// NodeIterator ni = factory.createQuery(source, defaultC, null, null)
	// .execute().getNodes();
	// return ni.getSize() == 1 ? ni.nextNode() : null;
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to get cached node for tag "
	// + tagValue, e);
	// }
	// }
}