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
	public final static String PEOPLE_UID = "people:uid";

	// A default Node at the root of the business path to store information
	// about current instance of the People repository
	public final static String PEOPLE_CONF = "conf";

	// Sub concepts parent node names
	public final static String PEOPLE_TAGS = "people:tags";
	public final static String PEOPLE_MAILING_LISTS = "people:mailingLists";

	public final static String PEOPLE_PAYMENT_ACCOUNTS = "people:paymentAccounts";
	public final static String PEOPLE_CONTACTS = "people:contacts";
	public final static String PEOPLE_TITLES = "people:titles";
	// For groups, job and mailing lists.
	public final static String PEOPLE_MEMBERS = "people:members";
	public final static String PEOPLE_JOBS = "people:jobs";
	// Enable synchronisation with third party systems
	public final static String PEOPLE_EXTERNAL_IDS = "people:externalIds";

	// Widely used property names
	public final static String PEOPLE_LANG = "people:lang";
	public final static String PEOPLE_PICTURE = "people:picture";
	// Reference an other entity using the business specific UID
	public final static String PEOPLE_REF_UID = "people:refUid";
	// Primary flag
	public final static String PEOPLE_IS_PRIMARY = "people:isPrimary";

	/* EXTERNAL IDS */
	public final static String PEOPLE_SOURCE_ID = "people:sourceId";
	public final static String PEOPLE_EXTERNAL_UID = "people:externalUid";

	/* PERSONS */
	public final static String PEOPLE_FIRST_NAME = "people:firstName";
	public final static String PEOPLE_MIDDLE_NAME = "people:middleName";
	public final static String PEOPLE_LAST_NAME = "people:lastName";
	public final static String PEOPLE_PRIMARY_EMAIL = "people:primaryEmail";
	public final static String PEOPLE_BIRTH_DATE = "people:birthDate";
	public final static String PEOPLE_SALUTATION = "people:salutation";
	public final static String PEOPLE_GENDER = "people:gender";
	public final static String PEOPLE_HONORIFIC_TITLE = "people:honorificTitle";
	public final static String PEOPLE_NAME_SUFFIX = "people:nameSuffix";
	public final static String PEOPLE_NICKNAME = "people:nickname";
	public final static String PEOPLE_MAIDEN_NAME = "people:maidenName";
	public final static String PEOPLE_USE_DISTINCT_DISPLAY_NAME = "people:useDistinctDisplayName";// (BOOLEAN)
	public final static String PEOPLE_USE_POLITE_FORM = "people:usePoliteForm"; // (BOOLEAN)
	public final static String PEOPLE_SPOKEN_LANGUAGES = "people:spokenLanguages"; // STRING*

	/* ORGANIZATIONS */
	public final static String PEOPLE_LEGAL_NAME = "people:legalName";
	public final static String PEOPLE_LEGAL_FORM = "people:legalForm";
	public final static String PEOPLE_VAT_ID_NB = "people:vatIdNb";

	/* USER MANAGEMENT */
	// Lists groups for current user
	public final static String PEOPLE_USER_GROUPS = "people:userGroups"; // REFERENCE*
	public final static String PEOPLE_GROUP_ID = "people:groupId";
	public final static String PEOPLE_IS_SINGLE_USER_GROUP = "people:isSingleUserGroup";

	/* RESOURCES */
	// Generally the corresponding node type. Might be something else.
	public final static String PEOPLE_TEMPLATE_ID = "people:templateId"; // (STRING)
	public final static String PEOPLE_TAG_ID = "people:tagId"; // (STRING)
	public final static String PEOPLE_TAG_INSTANCE_TYPE = "people:tagInstanceType"; // (STRING)
	public final static String PEOPLE_TAG_CODE_PROP_NAME = "people:codePropName"; // (STRING)
	public final static String PEOPLE_TAGGABLE_PARENT_PATH = "people:taggableParentPath"; // (STRING)
	public final static String PEOPLE_TAGGABLE_NODE_TYPE = "people:taggableNodeType"; // (STRING)
	public final static String PEOPLE_TAGGABLE_PROP_NAME = "people:taggablePropNames"; // (STRING)
																						// *
	public final static String PEOPLE_CODE = "people:code"; // (STRING)

	/* ACTIVITIES AND TASKS */
	public final static String PEOPLE_MANAGER = "people:manager";
	public final static String PEOPLE_REPORTED_BY = "people:reportedBy";
	public final static String PEOPLE_RELATED_TO = "people:relatedTo";
	public final static String PEOPLE_BOUND_ACTIVITIES = "people:boundActivities";
	public final static String PEOPLE_ATTACHEMENTS = "people:attachments";
	public final static String PEOPLE_ACTIVITY_DATE = "people:activityDate";

	// Tasks
	public final static String PEOPLE_TASK_STATUS = "people:taskStatus";
	// The corresponding groupID - we cannot use references because some groups
	// might be assigned to a lot of tasks
	public final static String PEOPLE_ASSIGNED_TO = "people:assignedTo"; // STRING
	public final static String PEOPLE_DUE_DATE = "people:dueDate";
	public final static String PEOPLE_CLOSE_DATE = "people:closeDate";
	public final static String PEOPLE_CLOSED_BY = "people:closedBy";
	public final static String PEOPLE_WAKE_UP_DATE = "people:wakeUpDate";
	public final static String PEOPLE_DEPENDS_ON = "people:dependsOn";
	public final static String PEOPLE_TASKS = "people:tasks";

	// Management of user rating
	public final static String PEOPLE_POLL_NAME = "people:pollName"; // (STRING)
	public final static String PEOPLE_CACHE_AVG_RATE = "people:cacheAvgRate"; // (STRING)
	public final static String PEOPLE_RATES = "people:rates"; // (nt:unstructured)
	// a single rate for one of the children people:rate activities
	public final static String PEOPLE_RATE = "people:rate"; // (LONG)

	// definition of the task template
	public final static String PEOPLE_TASK_CLOSING_STATUSES = "people:closingStatuses";
	public final static String PEOPLE_TASK_DEFAULT_STATUS = "people:defaultStatus";

	// Workflow specific
	public final static String PEOPLE_DATE_BEGIN = "people:dateBegin";
	public final static String PEOPLE_DATE_END = "people:dateEnd";
	public final static String PEOPLE_WF_VERSION = "people:wfVersion";
	public final static String PEOPLE_WF_VERSION_ID = "people:wfId";
	public final static String PEOPLE_WF_STATUS = "people:wfStatus";

	/* CONTACTS */
	public final static String PEOPLE_CONTACT_VALUE = "people:contactValue";
	// Pro or private
	public final static String PEOPLE_CONTACT_NATURE = "people:contactNature";
	public final static String PEOPLE_CONTACT_CATEGORY = "people:contactCategory";
	public final static String PEOPLE_CONTACT_LABEL = "people:contactLabel";
	public final static String PEOPLE_CONTACT_URI = "people:contactUri";

	// Phone: enable display of current time for this time zone
	public final static String PEOPLE_TIME_ZONE = "people:timeZone";

	// Physical Address
	public final static String PEOPLE_STREET = "people:street";
	public final static String PEOPLE_STREET_COMPLEMENT = "people:streetComplement";
	public final static String PEOPLE_ZIP_CODE = "people:zipCode";
	public final static String PEOPLE_CITY = "people:city";
	public final static String PEOPLE_STATE = "people:state";
	public final static String PEOPLE_COUNTRY = "people:country";
	public final static String PEOPLE_GEOPOINT = "people:geoPoint";

	/* CACHE */
	// following properties are all "on parent version" ignore and are used to
	// store primary information to ease fulltextsearch
	public final static String PEOPLE_CACHE_PCITY = "people:cachePCity";
	public final static String PEOPLE_CACHE_PCOUNTRY = "people:cachePCountry";
	public final static String PEOPLE_CACHE_PORG = "people:cachePOrg";
	public final static String PEOPLE_CACHE_PPHONE = "people:cachePPhone";
	public final static String PEOPLE_CACHE_PMAIL = "people:cachePMail";
	public final static String PEOPLE_CACHE_PURL = "people:cachePWeb";

	/* MISCENELLANEOUS */
	public final static String PEOPLE_ALT_LANGS = "people:altLangs";

	public final static String PEOPLE_LATIN_PHONETIC_SPELLING = "people:latinPhoneticSpelling";

	/* GROUP AND JOBS MANAGEMENT */
	// An optional department within the org corresponding to the current
	// position
	public final static String PEOPLE_DEPARTMENT = "people:department";
	// nature of the participation of the given entity in a group
	public final static String PEOPLE_ROLE = "people:role";
	// public final static String PEOPLE_TITLE = "jcr:title";
	public final static String PEOPLE_POSITION = "people:position";
	public final static String PEOPLE_IS_CURRENT = "people:isCurrent";
	// An additional reference to an organisation when needed
	// For instance for contacts or mailing list items
	public final static String PEOPLE_ORG_REF_UID = "people:orgRefUid";

	// Bank account
	public final static String PEOPLE_BANK_NAME = "people:bankName";
	public final static String PEOPLE_CURRENCY = "people:currency";
	// To be used while passing transaction orders
	public final static String PEOPLE_ACCOUNT_HOLDER = "people:accountHolder";
	public final static String PEOPLE_ACCOUNT_NB = "people:accountNb";
	public final static String PEOPLE_BANK_NB = "people:bankNb";
	public final static String PEOPLE_IBAN = "people:iban";
	public final static String PEOPLE_BIC = "people:bic";

	// Dirty workaround to avoid plain strings in xpath.request: the jcr
	// references have the complete unique namespace URL as prefix
	// TODO find a cleaner way to replace the URL by the local declared prefix

	public final static String JCR_TITLE = "jcr:title";
	public final static String JCR_DESCRIPTION = "jcr:description";
	public final static String JCR_PRIMARY_TYPE = "jcr:primaryType";
	public final static String JCR_CREATED = "jcr:created";
}