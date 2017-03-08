package org.argeo.people;

/** JCR node types managed by People */
public interface PeopleTypes {

	// Specific tag like resources instance types.
	String PEOPLE_MAILING_LIST = "people:mailingList";

	/* COMMON CONCEPTS */

	String PEOPLE_ENTITY = "people:entity";
	String PEOPLE_EXTERNAL_ID = "people:externalId";

	String PEOPLE_CONTACTABLE = "people:contactable";
	String PEOPLE_USER = "people:user";
	String PEOPLE_PERSON = "people:person";
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
	String PEOPLE_CONTACT_REF = "people:contactRef";

	String PEOPLE_PHONE = "people:phone";// abstract generic phone
	String PEOPLE_MOBILE = "people:mobile";
	String PEOPLE_TELEPHONE_NUMBER = "people:telephoneNumber";
	String PEOPLE_FAX = "people:facsimileTelephoneNumber";
	String PEOPLE_MAIL = "people:mail";
	String PEOPLE_POSTAL_ADDRESS = "people:postalAddress";
	String PEOPLE_URL = "people:url";
	String PEOPLE_IMPP = "people:impp";
	String PEOPLE_SOCIAL_MEDIA = "people:socialMedia";

	// A array with the known main mixin types
	String[] KNOWN_CONTACT_TYPES = { PEOPLE_MAIL, PEOPLE_MOBILE, PEOPLE_TELEPHONE_NUMBER, PEOPLE_URL,
			PEOPLE_SOCIAL_MEDIA, PEOPLE_POSTAL_ADDRESS, PEOPLE_IMPP, PEOPLE_FAX };

}
