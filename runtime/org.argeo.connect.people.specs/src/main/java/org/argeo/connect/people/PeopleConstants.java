package org.argeo.connect.people;

public interface PeopleConstants {
	public final static String PEOPLE_BASE_PATH = "/people:system";

	// People resources, typically language and country lists
	public final static String PEOPLE_RESOURCES_PATH = PEOPLE_BASE_PATH
			+ "/people:resources";

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

	/* CONTACT CATEGORIES */
	public final static String PEOPLE_CONTACT_CATEGORY_WORK = "Work";

}
