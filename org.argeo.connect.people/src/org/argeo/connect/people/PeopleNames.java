package org.argeo.connect.people;

/** People specific JCR names. */
public interface PeopleNames {

	/**
	 * An implementation specific UID, might be a JCR node Identifier but it is
	 * not compulsory We personally use the type 4 (pseudo randomly generated)
	 * UUID - we retrieve them simply in java with this method
	 * <code>UUID.randomUUID().toString()</code> Class 3 UUID of the
	 * distinguished name in UTF-8
	 */
	String PEOPLE_UID = "people:uid";

	// A default Node at the root of the business path to store information
	// about current instance of the People repository
	// TODO replace by config management
	String PEOPLE_CONF = "conf";

	// Sub concepts parent node names
	String PEOPLE_TAGS = "people:tags";
	String PEOPLE_MAILING_LISTS = "people:mailingLists";
	String PEOPLE_PAYMENT_ACCOUNTS = "people:paymentAccounts";
	String PEOPLE_CONTACTS = "people:contacts";
	String PEOPLE_TITLES = "people:titles";
	// For groups, job and mailing lists.
	String PEOPLE_MEMBERS = "people:members";
	String PEOPLE_JOBS = "people:jobs";
	// Enable synchronisation with third party systems
	String PEOPLE_EXTERNAL_IDS = "people:externalIds";

	// Widely used property names
	String PEOPLE_LANG = "people:lang";
	String PEOPLE_PICTURE = "people:picture";
	// Reference an other entity using the business specific UID
	String PEOPLE_REF_UID = "people:refUid";
	// Primary flag
	String PEOPLE_IS_PRIMARY = "people:isPrimary";

	/* EXTERNAL IDS */
	String PEOPLE_SOURCE_ID = "people:sourceId";
	String PEOPLE_EXTERNAL_UID = "people:externalUid";

	/* PERSONS */
	String PEOPLE_FIRST_NAME = "people:firstName";
	String PEOPLE_MIDDLE_NAME = "people:middleName";
	String PEOPLE_LAST_NAME = "people:lastName";
	String PEOPLE_PRIMARY_EMAIL = "people:primaryEmail";
	String PEOPLE_BIRTH_DATE = "people:birthDate";
	String PEOPLE_SALUTATION = "people:salutation";
	String PEOPLE_GENDER = "people:gender";
	String PEOPLE_HONORIFIC_TITLE = "people:honorificTitle";
	String PEOPLE_NAME_SUFFIX = "people:nameSuffix";
	String PEOPLE_NICKNAME = "people:nickname";
	String PEOPLE_MAIDEN_NAME = "people:maidenName";
	String PEOPLE_USE_DISTINCT_DISPLAY_NAME = "people:useDistinctDisplayName";// (BOOLEAN)
	String PEOPLE_USE_POLITE_FORM = "people:usePoliteForm"; // (BOOLEAN)
	String PEOPLE_SPOKEN_LANGUAGES = "people:spokenLanguages"; // STRING*

	/* ORGANIZATIONS */
	String PEOPLE_LEGAL_NAME = "people:legalName";
	String PEOPLE_LEGAL_FORM = "people:legalForm";
	String PEOPLE_VAT_ID_NB = "people:vatIdNb";

	/* USER MANAGEMENT */
	// Lists groups for current user
	// String PEOPLE_USER_GROUPS = "people:userGroups"; // REFERENCE*
	// String PEOPLE_GROUP_ID = "people:groupId";
	// String PEOPLE_IS_SINGLE_USER_GROUP = "people:isSingleUserGroup";

	/* ACTIVITIES AND TASKS */
	// String PEOPLE_MANAGER = "people:manager";
	// String PEOPLE_REPORTED_BY = "people:reportedBy";
	// String PEOPLE_RELATED_TO = "people:relatedTo";
	// String PEOPLE_BOUND_ACTIVITIES = "people:boundActivities";
	// String PEOPLE_ATTACHEMENTS = "people:attachments";
	// String PEOPLE_ACTIVITY_DATE = "people:activityDate";
	//
	// // Tasks
	// String PEOPLE_TASK_STATUS = "people:taskStatus";
	// // The corresponding groupID - we cannot use references because some
	// groups
	// // might be assigned to a lot of tasks
	// String PEOPLE_ASSIGNED_TO = "people:assignedTo"; // STRING
	// String PEOPLE_DUE_DATE = "people:dueDate";
	// String PEOPLE_CLOSE_DATE = "people:closeDate";
	// String PEOPLE_CLOSED_BY = "people:closedBy";
	// String PEOPLE_WAKE_UP_DATE = "people:wakeUpDate";
	// String PEOPLE_DEPENDS_ON = "people:dependsOn";
	// String PEOPLE_TASKS = "people:tasks";
	//
	// // Management of user rating
	// String PEOPLE_POLL_NAME = "people:pollName"; // (STRING)
	// String PEOPLE_CACHE_AVG_RATE = "people:cacheAvgRate"; // (STRING)
	// String PEOPLE_RATES = "people:rates"; // (nt:unstructured)
	// // a single rate for one of the children people:rate activities
	// String PEOPLE_RATE = "people:rate"; // (LONG)
	//
	// // definition of the task template
	// String PEOPLE_TASK_CLOSING_STATUSES = "people:closingStatuses";
	// String PEOPLE_TASK_DEFAULT_STATUS = "people:defaultStatus";

	// Workflow specific
	String PEOPLE_DATE_BEGIN = "people:dateBegin";
	String PEOPLE_DATE_END = "people:dateEnd";
	String PEOPLE_WF_VERSION = "people:wfVersion";
	String PEOPLE_WF_VERSION_ID = "people:wfId";
	String PEOPLE_WF_STATUS = "people:wfStatus";

	/* CONTACTS */
	String PEOPLE_CONTACT_VALUE = "people:contactValue";
	// Pro or private
	String PEOPLE_CONTACT_NATURE = "people:contactNature";
	String PEOPLE_CONTACT_CATEGORY = "people:contactCategory";
	String PEOPLE_CONTACT_LABEL = "people:contactLabel";
	String PEOPLE_CONTACT_URI = "people:contactUri";

	// Phone: enable display of current time for this time zone
	String PEOPLE_TIME_ZONE = "people:timeZone";

	// Physical Address
	String PEOPLE_STREET = "people:street";
	String PEOPLE_STREET_COMPLEMENT = "people:streetComplement";
	String PEOPLE_ZIP_CODE = "people:zipCode";
	String PEOPLE_CITY = "people:city";
	String PEOPLE_STATE = "people:state";
	String PEOPLE_COUNTRY = "people:country";
	String PEOPLE_GEOPOINT = "people:geoPoint";

	/* CACHE */
	// following properties are all "on parent version" ignore and are used to
	// store primary information to ease fulltextsearch
	String PEOPLE_CACHE_PCITY = "people:cachePCity";
	String PEOPLE_CACHE_PCOUNTRY = "people:cachePCountry";
	String PEOPLE_CACHE_PORG = "people:cachePOrg";
	String PEOPLE_CACHE_PPHONE = "people:cachePPhone";
	String PEOPLE_CACHE_PMAIL = "people:cachePMail";
	String PEOPLE_CACHE_PURL = "people:cachePWeb";

	/* MISCENELLANEOUS */
	String PEOPLE_ALT_LANGS = "people:altLangs";

	String PEOPLE_LATIN_PHONETIC_SPELLING = "people:latinPhoneticSpelling";

	/* GROUP AND JOBS MANAGEMENT */
	// An optional department within the org corresponding to the current
	// position
	String PEOPLE_DEPARTMENT = "people:department";
	// nature of the participation of the given entity in a group
	String PEOPLE_ROLE = "people:role";
	// String PEOPLE_TITLE = "jcr:title";
	String PEOPLE_POSITION = "people:position";
	String PEOPLE_IS_CURRENT = "people:isCurrent";
	// An additional reference to an organisation when needed
	// For instance for contacts or mailing list items
	String PEOPLE_ORG_REF_UID = "people:orgRefUid";

	// Bank account
	String PEOPLE_BANK_NAME = "people:bankName";
	String PEOPLE_CURRENCY = "people:currency";
	// To be used while passing transaction orders
	String PEOPLE_ACCOUNT_HOLDER = "people:accountHolder";
	String PEOPLE_ACCOUNT_NB = "people:accountNb";
	String PEOPLE_BANK_NB = "people:bankNb";
	String PEOPLE_IBAN = "people:iban";
	String PEOPLE_BIC = "people:bic";
}
