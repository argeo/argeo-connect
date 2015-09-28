package org.argeo.connect.people;

import static java.util.Arrays.asList;

import java.util.List;

import javax.jcr.query.Query;

/** People constants */
public interface PeopleConstants {

	// Namespace
	public final static String PEOPLE_PREFIX = "people";

	// Base path
	public final static String PEOPLE_BASE_PATH = "/people";
	public final static String PEOPLE_TMP_PATH = "/tmp";

	// Types that do not correspond to a Jcr type
	public final static String PEOPLE_PROJECT = "people:project";
	public final static String PEOPLE_RESOURCE = "people:resource";
	public final static String RESOURCE_TAG = "people:tag";
	public final static String RESOURCE_COUNTRY = "people:country";
	public final static String RESOURCE_LANG = "people:language";

	// Known resources IDs
	public final static String RESOURCE_TYPE_ID_TEMPLATE = "people.resourceId.template";
	public final static String RESOURCE_TYPE_ID_TAG_LIKE = "people.resourceId.tagLike";

	// Main concepts parent node names
	public final static String PEOPLE_RESOURCES = "resources";
	public final static String PEOPLE_ORGS = "orgs";
	public final static String PEOPLE_PERSONS = "persons";
	public final static String PEOPLE_PROJECTS = "projects";
	public final static String PEOPLE_EDITIONS = "editions";
	public final static String PEOPLE_USER_GROUPS = "userGroups";
	public final static String PEOPLE_ACTIVITIES = "activities";

	// Known types
	public static final List<String> PEOPLE_KNOWN_PARENT_NAMES = asList(
			//
			PEOPLE_RESOURCES, PEOPLE_ORGS, PEOPLE_PERSONS, PEOPLE_PROJECTS,
			PEOPLE_USER_GROUPS, PEOPLE_ACTIVITIES);

	// Corresponding subnodes for resources
	public final static String PEOPLE_RESOURCE_TEMPLATE = "templates";
	public final static String PEOPLE_RESOURCE_TAG_LIKE = "tags";

	// Configuration System Properties
	public final static String PEOPLE_DEFAULT_DOMAIN_NAME = "connect.people.defaultDomainName";
	public final static String PEOPLE_PROP_PREVENT_TAG_ADDITION = "connect.people.user.preventTagAddition";
	// public final static String PEOPLE_PROP_PREVENT_ML_ADDITION =
	// "connect.people.user.preventMLAddition";

	// default query limit
	public final static long QUERY_DEFAULT_LIMIT = 50;

	// System roles exposed by people
	public final static String ROLE_GUEST = "org.argeo.connect.people.guest";
	public final static String ROLE_MEMBER = "org.argeo.connect.people.member";
	public final static String ROLE_BUSINESS_ADMIN = "org.argeo.connect.people.admin";
	// public final static String ROLE_ADMIN = "admin";

	// LANGUAGE CONSTANTS
	public final static String LANG_EN = "en";
	public final static String LANG_DE = "de";
	public final static String LANG_FR = "fr";

	// String used in the various paths to replace an empty value
	// Typically /?/john
	public final static String UNKNOWN_NAME = "?";

	// Import Constants
	public final static String IMPORT_REF_SUFFIX = "_puid";
	public static final String IMPORT_CATALOGUE_KEY_COL = "Field";
	public static final String IMPORT_CATALOGUE_VALUES_COL = "Values";
	public static final String IMPORT_CATALOGUE_VALUES_SEPARATOR = "; ";
	
	// Workaround the JCR deprecation of XPath
	@SuppressWarnings("deprecation")
	public final static String QUERY_XPATH = Query.XPATH;
}