package org.argeo.connect.people;

/** JCR names managed by Connect People. */
public interface PeopleNames {
	/** Distinguished names */
	// public static String PEOPLE_DN = "people:dn";
	/** Class 3 UUID of the distinguished name in UTF-8 */
	public static String PEOPLE_PERSON_UUID = "people:personUuid";
	/** Date of birth */
	public static String PEOPLE_DATE_OF_BIRTH = "people:dateOfBirth";
	/** A number of people */
	public static String PEOPLE_COUNT = "people:count";

	/* Parent node for all persons */
	public final static String PEOPLE_PERSONS = "people:persons";
	/* Parent node for all organisations */
	public final static String PEOPLE_ORGS = "people:organizations";
	/* Parent node for all films */
	public final static String PEOPLE_FILMS = "people:films";
	/* Parent node for all projects */
	public final static String PEOPLE_PROJECTS = "people:projects";

	// path to parent node for various sub concepts
	public final static String PEOPLE_BANK_ACCOUNTS = "people:bankAccounts";
	public final static String PEOPLE_CONTACTS = "people:contacts";
	public final static String PEOPLE_CREW = "people:crew";
	public final static String PEOPLE_EMPLOYEE_OF = "people:employeeOf";
	public final static String PEOPLE_FILM_DONE = "people:filmDone";
	public final static String PEOPLE_SYNOPSES = "people:synopses";
	public final static String PEOPLE_TITLES = "people:titles";

	public final static String PEOPLE_WORKFLOWS = "people:workflows";
	// public final static String PEOPLE_FILM_SELECTION_WFS =
	// "people:filmSelectionWorkflows";

	/* Common concept */
	public final static String PEOPLE_PICTURE = "people:picture";
	// public final static String PEOPLE_DISPLAY_NAME = "people:displayName";
	// an implementation specific UID, might be a JCR node Identifier but it is
	// not compulsory
	public final static String PEOPLE_UID = "people:uid";
	// Reference an other entity using the implementation specific UID
	public final static String PEOPLE_REF_UID = "people:refUid";

	// an integer to enable ordering, 1 is the most prefered
	public final static String PEOPLE_PREF = "people:pref";

	/* PERSONS */
	public final static String PEOPLE_FIRST_NAME = "people:firstName";
	public final static String PEOPLE_MIDDLE_NAME = "people:middleName";
	public final static String PEOPLE_LAST_NAME = "people:lastName";
	public final static String PEOPLE_PRIMARY_EMAIL = "people:primaryEmail";
	public final static String PEOPLE_BIRTH_DATE = "people:birthDate";
	public final static String PEOPLE_SALUTATION = "people:salutation";
	public final static String PEOPLE_GENDER = "people:gender";
	public final static String PEOPLE_PERSON_TITLE = "people:personTitle";
	public final static String PEOPLE_NAME_SUFFIX = "people:nameSuffix";
	public final static String PEOPLE_NICKNAME = "people:nickname";
	public final static String PEOPLE_MAIDEN_NAME = "people:maidenName";
	public final static String PEOPLE_USE_DEFAULT_DISPLAY_NAME = "people:useDefaultDisplayName";

	// public final static String PEOPLE_PSEUDONYM = "people:pseudonym";

	/* ORGANIZATIONS */
	public final static String PEOPLE_LEGAL_NAME = "people:legalName";
	public final static String PEOPLE_ORG_BRANCHE = "people:orgBranche";
	public final static String PEOPLE_LEGAL_STATUS = "people:legalStatus";
	public final static String PEOPLE_VAT_ID_NB = "people:vatIdNb";

	/* WORKFLOWS */
	public final static String PEOPLE_DATE_BEGIN = "people:dateBegin";
	public final static String PEOPLE_DATE_END = "people:dateEnd";
	public final static String PEOPLE_WF_VERSION = "people:wfVersion";
	public final static String PEOPLE_WF_VERSION_ID = "people:wfId";
	public final static String PEOPLE_WF_STATUS = "people:wfStatus";

	/* CONTACTS */
	// base properties for all contact type nodes
	public final static String PEOPLE_CONTACT_VALUE = "people:contactValue";
	public final static String PEOPLE_CONTACT_CATEGORY = "people:contactCategory";
	public final static String PEOPLE_CONTACT_LABEL = "people:contactLabel";
	// if contactCategory == work
	// Rather use PEOPLE_REF_UID
	// public final static String PEOPLE_REL_ORG_ID = "people:relatedOrgId";
	public final static String PEOPLE_CONTACT_URI = "people:contactUri";

	// tel
	// public final static String PEOPLE_PHONE_NUMBER = "people:phoneNumber";
	// to display current time for this timezone
	public final static String PEOPLE_TIME_ZONE = "people:timeZone";

	// email
	// public final static String PEOPLE_EMAIL_ADDRESS = "people:emailAddress";

	// a site URL
	// public final static String PEOPLE_WEBSITE_URL = "people:websiteUrl";

	// post mail
	public final static String PEOPLE_STREET = "people:street";
	public final static String PEOPLE_STREET_COMPLEMENT = "people:streetComplement";
	public final static String PEOPLE_ZIP_CODE = "people:zipCode";
	public final static String PEOPLE_CITY = "people:city";
	public final static String PEOPLE_STATE = "people:state";
	public final static String PEOPLE_COUNTRY = "people:country";
	// A shortcut to store the displayed address
	// public final static String PEOPLE_DISPLAY_ADDRESS =
	// "people:displayAddress";

	/* MISCENELLANEOUS */

	public final static String PEOPLE_LATIN_PHONETIC_SPELLING = "people:latinPhoneticSpelling";
	public final static String PEOPLE_TAGS = "people:tags";

	public final static String PEOPLE_LANG = "people:lang";
	// public final static String PEOPLE_ALT_TITLES = "people:altTitles";
	public final static String PEOPLE_ALT_LANGS = "people:altLangs";
	// public final static String PEOPLE_ALT_DESCS = "people:altDescs";

	// the iso code of a given resource
	public final static String PEOPLE_ISO_CODE = "people:isoCode";

	// Flag a node as primary node in a set
	// public final static String PEOPLE_IS_PRIMARY = "people:isPrimary";

	/* GROUP AND JOBS MANAGEMENT */
	// parent node relpath
	public final static String PEOPLE_MEMBERS = "people:members";
	public final static String PEOPLE_JOBS = "people:jobs";

	// public final static String PEOPLE_MEMBER_ID = "people:memberId";
	// public final static String PEOPLE_ORG_ID = "people:orgId";
	// An optional department within the org corresponding to the current
	// position
	public final static String PEOPLE_DEPARTMENT = "people:department";

	// nature of the participation of the given entity in a group
	public final static String PEOPLE_ROLE = "people:role";
	public final static String PEOPLE_TITLE = "people:title";
	public final static String PEOPLE_POSITION = "people:position";
	public final static String PEOPLE_IS_CURRENT = "people:isCurrent";

	// public final static String PEOPLE_LINKED_ITEM_REF =
	// "people:linkedItemRef";
	// public final static String PEOPLE_LINKED_ITEM_WEAKREF =
	// "people:linkedItemWeakRef";
	// public final static String PEOPLE_LINKED_ITEM_PATH =
	// "people:linkedItemPath";

	// Bank account
	public final static String PEOPLE_BANK_NAME = "people:bankName";
	public final static String PEOPLE_CURRENCY = "people:currency";
	// To be used while passing transaction orders
	public final static String PEOPLE_ACCOUNT_OWNER_LABEL = "people:accountOwnerLbl";
	public final static String PEOPLE_ACCOUNT_NB = "people:accountNb";
	public final static String PEOPLE_BANK_CODE = "people:bankCode";
	public final static String PEOPLE_IBAN = "people:iban";
	public final static String PEOPLE_BIC = "people:bic";
}