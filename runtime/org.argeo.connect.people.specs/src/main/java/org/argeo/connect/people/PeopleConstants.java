package org.argeo.connect.people;

import static java.util.Arrays.asList;

import java.util.List;

public interface PeopleConstants {

	// Namespace
	public final static String PEOPLE_PREFIX = "people";

	// Base path
	public final static String PEOPLE_BASE_PATH = "/people:system";

	// Types that do not correspond to a Jcr type
	public final static String PEOPLE_RESOURCE = "people:resource";
	public final static String RESOURCE_TAG = "people:tag";
	public final static String RESOURCE_COUNTRY = "people:country";
	public final static String RESOURCE_LANG = "people:language";

	// Known types
	public static final List<String> PEOPLE_KNOWN_PARENT_NAMES = asList(
			//
			PeopleNames.PEOPLE_PERSONS, PeopleNames.PEOPLE_ORGS,
			PeopleNames.PEOPLE_FILMS, PeopleNames.PEOPLE_PROJECTS,
			PeopleNames.PEOPLE_ACTIVITIES, "/people:tasks",
			"/people:userGroups");

	public static final List<String> KNOWN_RESOURCE_NAMES = asList(
			//
			PeopleNames.PEOPLE_TAGS, PeopleNames.PEOPLE_ML_INSTANCES,
			PeopleNames.PEOPLE_TASKS, PeopleNames.PEOPLE_COUNTRIES,
			PeopleNames.PEOPLE_LANGS);

	// public final static String PEOPLE_MAILING_LISTS_BASE_PATH =
	// PEOPLE_BASE_PATH
	// + '/' + PeopleNames.PEOPLE_MAILING_LISTS;
	//
	// public final static String PEOPLE_ACTIVITIES_BASE_PATH = PEOPLE_BASE_PATH
	// + "/people:activities";
	// public final static String PEOPLE_TASKS_BASE_PATH = PEOPLE_BASE_PATH
	// + "/people:tasks";
	// public final static String PEOPLE_USER_GROUPS_BASE_PATH =
	// PEOPLE_BASE_PATH
	// + "/people:userGroups";

	// // People resources, typically language and country lists
	// public final static String PEOPLE_RESOURCES_BASE_PATH = PEOPLE_BASE_PATH
	// + "/people:resources";

	// Defined resources
	// public final static String RESOURCE_COUNTRIES = "people:countries";
	// public final static String RESOURCE_LANGS = "people:languages";

	@Deprecated
	// public final static String PEOPLE_COUNTRIES_BASE_PATH =
	// PEOPLE_RESOURCES_BASE_PATH
	// + "/" + RESOURCE_COUNTRIES;
	// @Deprecated
	// public final static String PEOPLE_LANGS_BASE_PATH =
	// PEOPLE_RESOURCES_BASE_PATH
	// + "/" + RESOURCE_LANGS;
	//
	// @Deprecated
	// public final static String PEOPLE_TAGS_BASE_PATH =
	// PEOPLE_RESOURCES_BASE_PATH
	// + "/" + PeopleNames.PEOPLE_TAGS;
	// Helper to decide wether we speak about the parent object of a link node
	// or about the ref it points to
	public final static Integer TARGET_LINK_PARENT = 0;
	public final static Integer TARGET_LINK_REF = 1;

	// default query limit
	public final static long QUERY_DEFAULT_LIMIT = 30;

	/*
	 * USER ROLES
	 */
	public final static String ROLE_GUEST = "ROLE_REGISTERED_GUEST";
	public final static String ROLE_MEMBER = "ROLE_REGISTERED_MEMBER";

	/* LANGUAGE CONSTANTS */
	public final static String LANG_EN = "en";
	public final static String LANG_DE = "de";
	public final static String LANG_FR = "fr";

	// String used in the various pathes to replace an empty value
	// Typically /?/john
	public final static String UNKNOWN_NAME = "?";

}