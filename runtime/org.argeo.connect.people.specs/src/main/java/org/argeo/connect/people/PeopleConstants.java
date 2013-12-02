package org.argeo.connect.people;

public interface PeopleConstants {
	public final static String PEOPLE_BASE_PATH = "/people:system";

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
	public final static Integer ROLE_CONSULTANT = 0;
	public final static Integer ROLE_MANAGER = 1;
	public final static Integer ROLE_ADMIN = 2;

	/* LANGUAGE CONSTANTS */
	public final static String LANG_EN = "en";
	public final static String LANG_DE = "de";
	public final static String LANG_FR = "fr";

}
