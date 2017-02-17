package org.argeo.connect.people;

/** JCR node types managed by Connect People */
public interface PeopleTypes {

	// Specific tag like resources instance types.
	String PEOPLE_MAILING_LIST = "people:mailingList";

	/* COMMON CONCEPTS */
	// Parent base type
	String PEOPLE_BASE = "people:base";
	String PEOPLE_ENTITY = "people:entity";
	String PEOPLE_EXTERNAL_ID = "people:externalId";

	// Mixins
	String PEOPLE_TAGGABLE = "people:taggable";
	String PEOPLE_ORDERABLE = "people:orderable";
	String PEOPLE_CONTACTABLE = "people:contactable";

	/* PERSONS */
	String PEOPLE_PERSON = "people:person";

	/* ORGANIZATIONS */
	String PEOPLE_ORG = "people:org";

	/* GROUPS */
	String PEOPLE_GROUP = "people:group";

	/* GROUP MEMBERS */
	String PEOPLE_POSITION = "people:position";
	String PEOPLE_JOB = "people:job";
	String PEOPLE_MEMBER = "people:member";
	String PEOPLE_BANK_ACCOUNT = "people:bankAccount";

	/* CONTACT */
	String PEOPLE_CONTACT = "people:contact";
	String PEOPLE_PHONE = "people:phone";
	String PEOPLE_EMAIL = "people:email";
	String PEOPLE_IMPP = "people:impp";
	String PEOPLE_URL = "people:url";
	String PEOPLE_SOCIAL_MEDIA = "people:socialMedia";
	String PEOPLE_ADDRESS = "people:address";

	// A array with the known types that might be defined as primary
	String[] KNOWN_CONTACT_TYPES = { PEOPLE_PHONE, PEOPLE_EMAIL, PEOPLE_ADDRESS, PEOPLE_URL, PEOPLE_IMPP,
			PEOPLE_SOCIAL_MEDIA };

	// contact with this mixin will use value(s) of the primary contact of the
	// given type of the referenced entity if such a contact exists.
	// Used among other for persons professional addresses
	String PEOPLE_CONTACT_REF = "people:contactRef";

	// Legacy, TODO remove (used in the proof of concept)
	String PEOPLE_ANONYMOUS_PERSON = "people:anonymousPerson";
}
