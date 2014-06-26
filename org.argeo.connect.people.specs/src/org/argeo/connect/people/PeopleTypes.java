package org.argeo.connect.people;

/** JCR node types managed by Connect People */
public interface PeopleTypes {

	// type used in MBaudier proof of concept
	public final static String PEOPLE_ANONYMOUS_PERSON = "people:anonymousPerson";

	/* RESOURCES */
	public final static String PEOPLE_ISO_COUNTRY = "people:isoCountry";
	public final static String PEOPLE_ISO_LANGUAGE = "people:isoLanguage";

	/* COMMON CONCEPTS */
	// Parent base type
	public final static String PEOPLE_BASE = "people:base";
	public final static String PEOPLE_ENTITY = "people:entity";
	public final static String PEOPLE_EXTERNAL_ID = "people:externalId";
	
	// Mixins
	public final static String PEOPLE_TAGABLE = "people:tagable";
	public final static String PEOPLE_ORDERABLE = "people:orderable";
	public final static String PEOPLE_CONTACTABLE = "people:contactable";
	
	/* PERSONS */
	public final static String PEOPLE_PERSON = "people:person";

	/* ORGANIZATIONS */
	public final static String PEOPLE_ORGANIZATION = "people:org";

	/* GROUPS */
	public final static String PEOPLE_GROUP = "people:group";
	@Deprecated
	public final static String PEOPLE_MAILING_LIST = "people:mailingList";

	/* GROUP MEMBERS */
	@Deprecated
	public final static String PEOPLE_MAILING_LIST_ITEM = "people:mailingListItem";
	public final static String PEOPLE_POSITION = "people:position";
	public final static String PEOPLE_JOB = "people:job";
	public final static String PEOPLE_MEMBER = "people:member";
	public final static String PEOPLE_BANK_ACCOUNT = "people:bankAccount";

	/* USER MANAGEMENT */
	public final static String PEOPLE_PROFILE = "people:profile";
	public final static String PEOPLE_USER_GROUP = "people:userGroup";

	/* TASKS AND ACTIVITIES */
	public final static String PEOPLE_ACTIVITY = "people:activity";
	public final static String PEOPLE_TASK = "people:task";

	public final static String PEOPLE_NOTE = "people:note";
	public final static String PEOPLE_SENT_EMAIL = "people:sentEmail";
	public final static String PEOPLE_CALL = "people:call";
	public final static String PEOPLE_MEETING = "people:meeting";
	public final static String PEOPLE_POST_MAIL = "people:sentPostMail";
	public final static String PEOPLE_SENT_FAX = "people:sentFax";
	public final static String PEOPLE_PAYMENT = "people:payment";
	public final static String PEOPLE_REVIEW = "people:review";
	public final static String PEOPLE_CHAT = "people:chat";
	public final static String PEOPLE_TWEET = "people:tweet";
	public final static String PEOPLE_BLOG = "people:blog";

	/* CONTACT */
	public final static String PEOPLE_CONTACT = "people:contact";
	public final static String PEOPLE_PHONE = "people:phone";
	public final static String PEOPLE_EMAIL = "people:email";
	public final static String PEOPLE_IMPP = "people:impp";
	public final static String PEOPLE_URL = "people:url";
	public final static String PEOPLE_SOCIAL_MEDIA = "people:socialMedia";
	public final static String PEOPLE_ADDRESS = "people:address";

	public final static String PEOPLE_ML_INSTANCE = "people:mailingListInstance";

	// contact with this mixin will use value(s) of the primary contact of the
	// given type
	// of the referenced entity if such a contact exists.
	// Used among other for persons professional addresses
	public final static String PEOPLE_CONTACT_REF = "people:contactRef";
}