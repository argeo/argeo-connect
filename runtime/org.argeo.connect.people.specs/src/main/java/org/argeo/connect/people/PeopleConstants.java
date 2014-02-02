package org.argeo.connect.people;

public interface PeopleConstants {

	// Base path
	public final static String PEOPLE_BASE_PATH = "/people:system";

	public final static String PEOPLE_ACTIVITIES_BASE_PATH = PEOPLE_BASE_PATH
			+ "/people:activities";
	public final static String PEOPLE_TASKS_BASE_PATH = PEOPLE_BASE_PATH
			+ "/people:tasks";

	public final static String PEOPLE_USER_GROUPS_BASE_PATH = PEOPLE_BASE_PATH
			+ "/people:userGroups";

	// People resources, typically language and country lists
	public final static String PEOPLE_RESOURCES_PATH = PEOPLE_BASE_PATH
			+ "/people:resources";
	public final static String PEOPLE_COUNTRIES_BASE_PATH = PEOPLE_RESOURCES_PATH
			+ "/people:countries";
	public final static String PEOPLE_LANGS_BASE_PATH = PEOPLE_RESOURCES_PATH
			+ "/people:languages";

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

}
