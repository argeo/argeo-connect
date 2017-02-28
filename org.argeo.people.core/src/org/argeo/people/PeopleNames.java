package org.argeo.people;

import org.argeo.naming.LdapAttrs;

/** People specific JCR names. */
public interface PeopleNames {

	String PEOPLE_PAYMENT_ACCOUNTS = "paymentAccounts";
	String PEOPLE_CONTACTS = "contacts";
	// For groups, job and mailing lists.
	String PEOPLE_MEMBERS = "members";
	String PEOPLE_JOBS = "positions";

	// Reference an other entity using the business specific UID
	String PEOPLE_REF_UID = "people:refUid";
	String PEOPLE_REF_TITLE = "people:refTitle";

	// Primary flag
	String PEOPLE_IS_PRIMARY = "people:isPrimary";

	// Contactable
	String PEOPLE_MAILING_LISTS = "people:mailingLists";
	String PEOPLE_UNSUBSCRIBED_MAILING_LISTS = "people:unsubscribedMailingLists";

	// Persons
	String PEOPLE_FIRST_NAME = LdapAttrs.givenName.property();
	String PEOPLE_LAST_NAME = LdapAttrs.sn.property();
	String PEOPLE_PRIMARY_EMAIL = LdapAttrs.mail.property();
	String PEOPLE_HONORIFIC_TITLE = LdapAttrs.title.property();
	String PEOPLE_NAME_SUFFIX = LdapAttrs.generationQualifier.property();
	String PEOPLE_DISPLAY_NAME = LdapAttrs.displayName.property();

	String PEOPLE_MIDDLE_NAME = "people:middleName";
	String PEOPLE_BIRTH_DATE = "people:birthDate";
	String PEOPLE_SALUTATION = "people:salutation";
	String PEOPLE_GENDER = "people:gender";
	String PEOPLE_NICKNAME = "people:nickname";
	String PEOPLE_MAIDEN_NAME = "people:maidenName";
	String PEOPLE_LATIN_PHONETIC_SPELLING = "people:latinPhoneticSpelling";
	String PEOPLE_USE_POLITE_FORM = "people:usePoliteForm"; // (BOOLEAN)
	String PEOPLE_SPOKEN_LANGUAGES = "people:preferredLanguages"; // *

	/* ORGANIZATIONS */
	String PEOPLE_LEGAL_NAME = LdapAttrs.cn.property();
	String PEOPLE_LEGAL_FORM = "people:legalForm";
	String PEOPLE_VAT_ID_NB = "people:vatIdNb";

	// In people we use following properties to store primary contact info and
	// ease querying
	String PEOPLE_PPOSTAL_ADDRESS = LdapAttrs.postalAddress.property();
	String PEOPLE_PMOBILE = LdapAttrs.mobile.property();
	String PEOPLE_PTELEPHONE_NUMBER = LdapAttrs.telephoneNumber.property();
	String PEOPLE_PMAIL = LdapAttrs.mail.property();
	String PEOPLE_PURL = LdapAttrs.associatedDomain.property();

	String PEOPLE_CACHE_PORG = "people:cachePOrg";

	/* CONTACTS */
	String PEOPLE_CONTACT_VALUE = "people:contactValue";
	String PEOPLE_CONTACT_NATURE = "people:contactNature"; // Pro or private
	String PEOPLE_CONTACT_CATEGORY = "people:contactCategory";
	String PEOPLE_CONTACT_LABEL = "people:contactLabel";
	// String PEOPLE_CONTACT_URI = "people:contactUri";

	// Phone: enable display of current time for this time zone
	String PEOPLE_TIME_ZONE = "people:timeZone";

	// Postal Address
	String PEOPLE_STREET = LdapAttrs.street.property();
	String PEOPLE_ZIP_CODE = LdapAttrs.postalCode.property();
	String PEOPLE_CITY = LdapAttrs.l.property();
	String PEOPLE_STATE = LdapAttrs.st.property();
	String PEOPLE_COUNTRY = LdapAttrs.c.property();
	String PEOPLE_STREET_COMPLEMENT = "people:streetComplement";
	String PEOPLE_GEOPOINT = "people:geoPoint";

	// Groups and jobs
	// An optional department within the org for the current position
	String PEOPLE_DEPARTMENT = "people:department";
	// Nature of the participation of the given entity in a group
	String PEOPLE_ROLE = "people:role";
	// String PEOPLE_POSITION = "people:position";
	String PEOPLE_IS_CURRENT = "people:isCurrent";
	String PEOPLE_DATE_BEGIN = "people:dateBegin";
	String PEOPLE_DATE_END = "people:dateEnd";

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

	/* MISCENELLANEOUS */
	String PEOPLE_ALT_LANGS = "people:altLangs";

	// LEGACY: TODO remove

	// String PEOPLE_TITLES = "people:titles";
	// String PEOPLE_LANG = "people:lang";
	// String PEOPLE_PICTURE = "people:picture";

	/**
	 * An implementation specific UID, might be a JCR node Identifier but it is
	 * not compulsory We personally use the type 4 (pseudo randomly generated)
	 * UUID - we retrieve them simply in java with this method
	 * <code>UUID.randomUUID().toString()</code> Class 3 UUID of the
	 * distinguished name in UTF-8
	 */
	// String PEOPLE_UID = "people:uid";

	// A default Node at the root of the business path to store information
	// about current instance of the People repository
	// TODO replace by config management
	// String PEOPLE_CONF = "conf";

	// Enable synchronisation with third party systems
	// String PEOPLE_EXTERNAL_IDS = "people:externalIds";

}
