package org.argeo.connect.people;

/** JCR node types managed by Connect People */
public interface PeopleTypes {

	// type used in MBaudier proof of concept
	public final static String PEOPLE_ANONYMOUS_PERSON = "people:anonymousPerson";

	/* PERSONS */
	public final static String PEOPLE_PERSON = "people:person";
	public final static String PEOPLE_PERSON_NAME = "people:personName";

	/* ORGANIZATIONS */
	public final static String PEOPLE_ORGANIZATION = "people:organization";

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