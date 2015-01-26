package org.argeo.connect.people;

import static java.util.Arrays.asList;

import java.util.List;

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
	public final static String PEOPLE_USER_GROUPS = "userGroups";
	public final static String PEOPLE_ACTIVITIES = "activities";

	// Known types
	public static final List<String> PEOPLE_KNOWN_PARENT_NAMES = asList(
			//
			PEOPLE_RESOURCES, PEOPLE_ORGS, PEOPLE_PERSONS, PEOPLE_PROJECTS,
			PEOPLE_USER_GROUPS, PEOPLE_ACTIVITIES);

	// public static final List<String> KNOWN_RESOURCE_NAMES = asList(
	// //
	// PeopleNames.PEOPLE_TAGS, PeopleNames.PEOPLE_MAILING_LISTS,
	// PeopleNames.PEOPLE_TASKS, PeopleNames.PEOPLE_COUNTRIES,
	// PeopleNames.PEOPLE_LANGS);

	// Corresponding subnodes for resources
	public final static String PEOPLE_RESOURCE_TEMPLATE = "templates";
	public final static String PEOPLE_RESOURCE_TAG_LIKE = "tags";

	// Configuration System Properties
	public final static String PEOPLE_PROP_PREVENT_TAG_ADDITION = "connect.people.user.preventTagAddition";
	// public final static String PEOPLE_PROP_PREVENT_ML_ADDITION =
	// "connect.people.user.preventMLAddition";

	// TODO check this.
	public final static Integer TARGET_LINK_PARENT = 0;
	public final static Integer TARGET_LINK_REF = 1;

	// default query limit
	public final static long QUERY_DEFAULT_LIMIT = 50;

	/*
	 * USER ROLES
	 */
	public final static String ROLE_GUEST = "ROLE_REGISTERED_GUEST";
	public final static String ROLE_MEMBER = "ROLE_REGISTERED_MEMBER";
	public final static String ROLE_BUSINESS_ADMIN = "ROLE_BUSINESS_ADMIN";
	public final static String ROLE_ADMIN = "ROLE_ADMIN";

	/* LANGUAGE CONSTANTS */
	public final static String LANG_EN = "en";
	public final static String LANG_DE = "de";
	public final static String LANG_FR = "fr";

	// String used in the various paths to replace an empty value
	// Typically /?/john
	public final static String UNKNOWN_NAME = "?";

	// Import Constants
	public static final String IMPORT_CATALOGUE_KEY_COL = "Field";
	public static final String IMPORT_CATALOGUE_VALUES_COL = "Values";
	public static final String IMPORT_CATALOGUE_VALUES_SEPARATOR = "; ";

}