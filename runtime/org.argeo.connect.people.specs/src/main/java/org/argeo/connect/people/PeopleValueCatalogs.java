package org.argeo.connect.people;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Workaround class to define used maps before we implement a more clean
 * management of such an issue
 */

public class PeopleValueCatalogs {

	// Labels
	public final static String CONTACT_OTHER = "Other";

	/* CONTACT NATURE */
	public final static String CONTACT_NATURE_PRO = "Professional";
	public final static String CONTACT_NATURE_PRIVATE = "Private";

	/* CONTACT TYPE */
	public final static String CONTACT_TYPE_EMAIL = "Email";
	public final static String CONTACT_TYPE_PHONE = "Phone";
	public final static String CONTACT_TYPE_ADDRESS = "Address";
	public final static String CONTACT_TYPE_URL = "URL";
	public final static String CONTACT_TYPE_SOCIAL_MEDIA = "Social Media";
	public final static String CONTACT_TYPE_IMPP = "Instant Messaging";

	/* CONTACT CATEGORIES */
	// Person & Org phones
	public final static String CONTACT_CAT_FAX = "Fax";
	public final static String CONTACT_CAT_PRO_RECEPTION = "Reception";

	// Person phones
	public final static String CONTACT_CAT_MOBILE = "Mobile";
	public final static String CONTACT_CAT_HOME = "Home";
	public final static String CONTACT_CAT_PRO_DIRECT = "Direct";

	// Person addresses
	public final static String CONTACT_CAT_MAIN = "Main";
	public final static String CONTACT_CAT_OFFICE = "Office";
	public final static String CONTACT_CAT_SECONDARY = "Secondary";
	public final static String CONTACT_CAT_DELIVERY = "Delivery";

	// Org addresses
	public final static String CONTACT_CAT_HEADOFFICE = "Head Office";
	public final static String CONTACT_CAT_SECOFFICE = "Secondary Office";
	public final static String CONTACT_CAT_BILLING = "Billing Adress";
	public final static String CONTACT_CAT_PUBLIC_ENTRANCE = "Public Entrance";

	// Social Media
	public final static String CONTACT_CAT_GOOGLEPLUS = "Google+";
	public final static String CONTACT_CAT_FACEBOOK = "Facebook";
	public final static String CONTACT_CAT_TWITTER = "Twitter";
	public final static String CONTACT_CAT_XING = "Xing";
	public final static String CONTACT_CAT_LINKEDIN = "LinkedIn";

	// IMPP
	public final static String CONTACT_CAT_SKYPE = "Skype";
	public final static String CONTACT_CAT_SIP = "SIP";
	public final static String CONTACT_CAT_ICQ = "ICQ";

	public static String[] getCategoryList(String entityType,
			String contactType, String nature) {
		if (PeopleTypes.PEOPLE_PHONE.equals(contactType)) {
			if (entityType.equals(PeopleTypes.PEOPLE_PERSON)) {
				if (CONTACT_NATURE_PRO.equals(nature))
					return ARRAY_PERSON_PRO_PHONES;
				else
					return ARRAY_PERSON_PRIVATE_PHONES;
			} else if (entityType.equals(PeopleTypes.PEOPLE_ORGANIZATION))
				return ARRAY_ORG_PHONES;
		}
		// NO category for MAIL
		// else if (PeopleTypes.PEOPLE_MAil.equals(contactType))
		else if (PeopleTypes.PEOPLE_ADDRESS.equals(contactType)) {
			if (entityType.equals(PeopleTypes.PEOPLE_PERSON))
				return ARRAY_PERSON_ADDRESSES;
			else if (entityType.equals(PeopleTypes.PEOPLE_ORGANIZATION))
				return ARRAY_ORG_ADDRESSES;
		}
		// NO category for URL
		// else if (PeopleTypes.PEOPLE_URL.equals(contactType))
		else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactType))
			return ARRAY_SOCIAL_MEDIA;
		else if (PeopleTypes.PEOPLE_IMPP.equals(contactType))
			return ARRAY_IMPP;
		return new String[0];
	}

	/* CONTACT TYPE */
	// Contact categories: maps corresponding node types with a label
	public static final Map<String, String> MAPS_CONTACT_TYPES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(PeopleTypes.PEOPLE_EMAIL, CONTACT_TYPE_EMAIL);
		tmpMap.put(PeopleTypes.PEOPLE_PHONE, CONTACT_TYPE_PHONE);
		tmpMap.put(PeopleTypes.PEOPLE_ADDRESS, CONTACT_TYPE_ADDRESS);
		tmpMap.put(PeopleTypes.PEOPLE_URL, CONTACT_TYPE_URL);
		tmpMap.put(PeopleTypes.PEOPLE_SOCIAL_MEDIA, CONTACT_TYPE_SOCIAL_MEDIA);
		tmpMap.put(PeopleTypes.PEOPLE_IMPP, CONTACT_TYPE_IMPP);
		MAPS_CONTACT_TYPES = Collections.unmodifiableMap(tmpMap);
	}

	// corresponding array for various lists
	public static final String[] ARRAY_CONTACT_NATURES = { CONTACT_NATURE_PRO,
			CONTACT_NATURE_PRIVATE };

	public static final String[] ARRAY_CONTACT_TYPES = { CONTACT_TYPE_EMAIL,
			CONTACT_TYPE_PHONE, CONTACT_TYPE_ADDRESS,
			CONTACT_TYPE_SOCIAL_MEDIA, CONTACT_TYPE_URL, CONTACT_TYPE_IMPP };

	// person private phones
	public static final String[] ARRAY_PERSON_PRIVATE_PHONES = {
			CONTACT_CAT_MOBILE, CONTACT_CAT_HOME, CONTACT_CAT_FAX,
			CONTACT_OTHER };

	// person professional phones
	public static final String[] ARRAY_PERSON_PRO_PHONES = {
			CONTACT_CAT_MOBILE, CONTACT_CAT_PRO_DIRECT,
			CONTACT_CAT_PRO_RECEPTION, CONTACT_CAT_FAX, CONTACT_OTHER };

	// Org phones
	public static final String[] ARRAY_ORG_PHONES = {
			CONTACT_CAT_PRO_RECEPTION, CONTACT_CAT_FAX, CONTACT_OTHER };

	// Person adresses
	public static final String[] ARRAY_PERSON_ADDRESSES = { CONTACT_CAT_MAIN,
			CONTACT_CAT_SECONDARY, CONTACT_CAT_OFFICE, CONTACT_CAT_DELIVERY,
			CONTACT_OTHER };

	// Org adresses
	public static final String[] ARRAY_ORG_ADDRESSES = {
			CONTACT_CAT_HEADOFFICE, CONTACT_CAT_SECOFFICE, CONTACT_CAT_BILLING,
			CONTACT_CAT_DELIVERY, CONTACT_CAT_PUBLIC_ENTRANCE, CONTACT_OTHER };

	// Social Media
	public static final String[] ARRAY_SOCIAL_MEDIA = { CONTACT_CAT_GOOGLEPLUS,
			CONTACT_CAT_FACEBOOK, CONTACT_CAT_TWITTER, CONTACT_CAT_XING,
			CONTACT_CAT_LINKEDIN, CONTACT_OTHER };

	// IMPP
	public static final String[] ARRAY_IMPP = { CONTACT_CAT_SKYPE,
			CONTACT_CAT_SIP, CONTACT_CAT_ICQ, CONTACT_OTHER };

	/**
	 * Provides the key for a given value. we assume it is a bijection each
	 * value is only linked to one key
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
