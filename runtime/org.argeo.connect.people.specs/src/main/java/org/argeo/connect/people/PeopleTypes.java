package org.argeo.connect.people;

/** JCR node types managed by Connect People */
public interface PeopleTypes {
	/* PERSONS */
	public final static String PEOPLE_PERSON = "people:person";
	public final static String PEOPLE_PERSON_NAME = "people:personName";

	/* ORGANIZATIONS */
	public final static String PEOPLE_ORGANIZATION = "people:organization";

	/* FILMS */
	public final static String PEOPLE_FILM = "people:film";
	public final static String PEOPLE_SYNOPSIS = "people:synopsis";
	public final static String PEOPLE_FILM_TITLE = "people:filmTitle";

	/* WORKFLOWS */
	public final static String PEOPLE_PROJECT = "people:project";
	public final static String PEOPLE_EDITION = "people:edition";

	public final static String PEOPLE_WORKFLOW = "people:workflow";
	public final static String PEOPLE_WF_FILM_SELECTION = "people:wfFilmSelection";
	public final static String PEOPLE_FILM_CATEGORY = "people:filmCategory";

	/* MISCELLANEOUS */
	// Enable links between nodes
	public final static String PEOPLE_LINKED_UNIT = "people:linkedUnit";
	public final static String PEOPLE_BANK_ACCOUNT = "people:bankAccount";

	/* CONTACT */
	public final static String PEOPLE_CONTACT = "people:contact";
	public final static String PEOPLE_PHONE = "people:phone";
	public final static String PEOPLE_EMAIL = "people:email";
	public final static String PEOPLE_WEBSITE = "people:website";
	public final static String PEOPLE_ADDRESS = "people:address";
}
