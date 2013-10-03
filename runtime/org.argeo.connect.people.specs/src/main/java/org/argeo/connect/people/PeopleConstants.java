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
	public final static String CONTACT_CATEGORY_WORK = "Work";
	public final static String CONTACT_CATEGORY_PRIVATE = "Private";
	public final static String CONTACT_CATEGORY_OTHER = "Other";

	// public final static String CONTACT_LABEL_PRIVATE = "Private";
	public final static String CONTACT_LABEL_OTHER = "Other";

	public final static String CONTACT_LABEL_FAX = "Fax";
	public final static String CONTACT_LABEL_MOBILE = "Mobile";
	public final static String CONTACT_LABEL_FIX = "Fix";
	public final static String CONTACT_LABEL_DIRECT = "Direct";
	public final static String CONTACT_LABEL_RECEPTION = "Reception";

	public final static String CONTACT_CATEGORY_WEBSITE = "Website";
	public final static String CONTACT_CATEGORY_SOCIALMEDIA = "Social Media";

	public final static String CONTACT_LABEL_GOOGLEPLUS = "Google+";
	public final static String CONTACT_LABEL_FACEBOOK = "Facebook";
	public final static String CONTACT_LABEL_TWITTER = "Twitter";
	public final static String CONTACT_LABEL_XING = "Xing";

	/* LANGUAGE CONSTANTS */
	public final static String LANG_EN = "en";
	public final static String LANG_DE = "de";
	public final static String LANG_FR = "fr";

}
