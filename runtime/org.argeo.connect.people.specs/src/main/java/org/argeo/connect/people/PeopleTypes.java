package org.argeo.connect.people;

/** JCR node types managed by Connect People */
public interface PeopleTypes {

	// type used in MBaudier proof of concept
	public final static String PEOPLE_ANONYMOUS_PERSON = "people:anonymousPerson";

	
	/* COMMON CONCEPTS */
	// Parent base type
	public final static String PEOPLE_ENTITY = "people:entity";
	public final static String PEOPLE_TAGABLE = "people:tagable";
	public final static String PEOPLE_ORDERABLE = "people:orderable";
	public final static String PEOPLE_CONTACTABLE = "people:contactable";
	
	/* PERSONS */
	public final static String PEOPLE_PERSON = "people:person";

	/* ORGANIZATIONS */
	public final static String PEOPLE_ORGANIZATION = "people:org";

	/* MISCELLANEOUS */
	// Enable links between nodes
	public final static String PEOPLE_POSITION = "people:position";
	public final static String PEOPLE_JOB = "people:job";
	public final static String PEOPLE_MEMBER = "people:member";
	public final static String PEOPLE_BANK_ACCOUNT = "people:bankAccount";

	/* CONTACT */
	public final static String PEOPLE_CONTACT = "people:contact";
	public final static String PEOPLE_PHONE = "people:phone";
	public final static String PEOPLE_EMAIL = "people:email";
	public final static String PEOPLE_WEBSITE = "people:website";
	public final static String PEOPLE_ADDRESS = "people:address";
}