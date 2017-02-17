package org.argeo.connect.people;

import static java.util.Arrays.asList;

import java.util.List;

/** People constants */
public interface PeopleConstants {

	// Namespace
	String PEOPLE_PREFIX = "people";
	String PEOPLE_APP_BASE_NAME = "people";

	// Base path
	String PEOPLE_BASE_PATH = "/people";
	String PEOPLE_PUBLIC_PATH = "/public";
	String PEOPLE_TMP_PATH = "/tmp";

	// Types that do not correspond to a Jcr type
	String PEOPLE_PROJECT = "people:project";
	String PEOPLE_RESOURCE = "people:resource";

	// Main concepts parent node names
	String PEOPLE_RESOURCES = "resources";
	String PEOPLE_ORGS = "orgs";
	String PEOPLE_PERSONS = "persons";
	String PEOPLE_PROJECTS = "projects";
	String PEOPLE_EDITIONS = "editions";
	// String PEOPLE_USER_GROUPS = "userGroups";
	String PEOPLE_ACTIVITIES = "activities";
	String PEOPLE_DRAFT = "draft";

	// Known types
	List<String> PEOPLE_KNOWN_PARENT_NAMES = asList(
			//
			PEOPLE_RESOURCES, PEOPLE_ORGS, PEOPLE_PERSONS, PEOPLE_PROJECTS, PEOPLE_ACTIVITIES);

	// // Corresponding subnodes for resources
	// String PEOPLE_RESOURCE_TEMPLATE = "templates";
	// String PEOPLE_RESOURCE_TAG_LIKE = "tags";

	// Configuration System Properties
	String PEOPLE_DEFAULT_DOMAIN_NAME = "connect.people.defaultDomainName";
	String MIGRATION_USER_LOGIN = "connect.migration.user.login";
	String MIGRATION_USER_PWD = "connect.migration.user.pwd";

	// String PEOPLE_PROP_PREVENT_ML_ADDITION =
	// "connect.people.user.preventMLAddition";

	// default query limit
	long QUERY_DEFAULT_LIMIT = 50;

	// System roles exposed by people
	String ROLE_GUEST = "org.argeo.connect.people.guest";
	String ROLE_MEMBER = "org.argeo.connect.people.member";
	String ROLE_BUSINESS_ADMIN = "cn=businessAdmin,ou=roles,ou=node";
	// Give access to people specific perspectives
	// String ROLE_MANAGER =
	// "org.argeo.connect.people.manager";

	// LANGUAGE CONSTANTS
	String LANG_EN = "en";
	String LANG_DE = "de";
	String LANG_FR = "fr";

	// String used in the various paths to replace an empty value
	// Typically /?/john
	String UNKNOWN_NAME = "?";

	// Import Constants
	String IMPORT_REF_SUFFIX = "_puid";
	String IMPORT_CATALOGUE_KEY_COL = "Field";
	String IMPORT_CATALOGUE_VALUES_COL = "Values";
	String IMPORT_CATALOGUE_VALUES_SEPARATOR = "; ";
}
