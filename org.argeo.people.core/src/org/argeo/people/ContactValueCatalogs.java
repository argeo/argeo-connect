package org.argeo.people;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Workaround class to define used maps before we implement a cleaner management
 * of such an issue
 */

public class ContactValueCatalogs {

	// Labels
	public final static String CONTACT_NO_VAL = "-";
	public final static String CONTACT_OTHER = "Other";

	/* CONTACT NATURE */
	public final static String CONTACT_NATURE_PRO = "Work";
	public final static String CONTACT_NATURE_PRIVATE = "Home";

	/* CONTACT TYPE */
	public final static String CONTACT_TYPE_EMAIL = "Email";
	public final static String CONTACT_TYPE_PHONE = "Phone";
	public final static String CONTACT_TYPE_ADDRESS = "Address";
	public final static String CONTACT_TYPE_URL = "URL";
	public final static String CONTACT_TYPE_SOCIAL_MEDIA = "Social Media";
	public final static String CONTACT_TYPE_IMPP = "Instant Messenger";

	/* CONTACT CATEGORIES */
	// Person & Org phones

	// Person phones
	public final static String CONTACT_CAT_MOBILE = "Mobile";
	public final static String CONTACT_CAT_DIRECT = "Direct";
	public final static String CONTACT_CAT_MAIN = "Main";
	public final static String CONTACT_CAT_VOIP = "VoIP";
	public final static String CONTACT_CAT_FAX = "Fax";

	// Person addresses
	public final static String CONTACT_CAT_OFFICE = "Office";
	public final static String CONTACT_CAT_SECONDARY = "Secondary";
	public final static String CONTACT_CAT_DELIVERY = "Delivery";
	public final static String CONTACT_CAT_INVOICE = "Invoice";

	// Org addresses
	public final static String CONTACT_CAT_HEADOFFICE = "Head Office";
	public final static String CONTACT_CAT_SECOFFICE = "Secondary Office";
	public final static String CONTACT_CAT_BILLING = "Billing Adress";
	public final static String CONTACT_CAT_PUBLIC_ENTRANCE = "Public Entrance";

	// Social Media
	public final static String CONTACT_CAT_FACEBOOK = "Facebook";
	public final static String CONTACT_CAT_GOOGLEPLUS = "Google+";
	public final static String CONTACT_CAT_LINKEDIN = "LinkedIn";
	public final static String CONTACT_CAT_TWITTER = "Twitter";
	public final static String CONTACT_CAT_XING = "Xing";
	public final static String CONTACT_CAT_YOUTUBE = "Youtube";
	public final static String CONTACT_CAT_VIMEO = "Vimeo";
	public final static String CONTACT_CAT_FLICKR = "Flickr";
	public final static String CONTACT_CAT_FOURSQUARE = "Foursquare";
	public final static String CONTACT_CAT_INSTAGRAM = "Instagram";

	// IMPP
	public final static String CONTACT_CAT_SKYPE = "Skype";
	public final static String CONTACT_CAT_MSN = "MSN";
	public final static String CONTACT_CAT_ICQ = "ICQ";
	public final static String CONTACT_CAT_AIM = "Aim";
	public final static String CONTACT_CAT_YAHOO = "Yahoo";
	public final static String CONTACT_CAT_GOOGLE_TALK = "Google Talk";

	/* CONTACT TYPE */
	// Contact categories: maps corresponding node types with a label
	public static final Map<String, String> MAPS_CONTACT_TYPES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(PeopleTypes.PEOPLE_MAIL, CONTACT_TYPE_EMAIL);
		tmpMap.put(PeopleTypes.PEOPLE_PHONE, CONTACT_TYPE_PHONE);
		tmpMap.put(PeopleTypes.PEOPLE_POSTAL_ADDRESS, CONTACT_TYPE_ADDRESS);
		tmpMap.put(PeopleTypes.PEOPLE_URL, CONTACT_TYPE_URL);
		tmpMap.put(PeopleTypes.PEOPLE_SOCIAL_MEDIA, CONTACT_TYPE_SOCIAL_MEDIA);
		tmpMap.put(PeopleTypes.PEOPLE_IMPP, CONTACT_TYPE_IMPP);
		MAPS_CONTACT_TYPES = Collections.unmodifiableMap(tmpMap);
	}

	// Corresponding array for various lists
	public static final String[] ARRAY_CONTACT_NATURES = { CONTACT_NATURE_PRO,
			CONTACT_NATURE_PRIVATE, CONTACT_OTHER };

	public static final String[] ARRAY_CONTACT_TYPES = { CONTACT_TYPE_EMAIL,
			CONTACT_TYPE_PHONE, CONTACT_TYPE_ADDRESS,
			CONTACT_TYPE_SOCIAL_MEDIA, CONTACT_TYPE_URL, CONTACT_TYPE_IMPP };

	// Person private phones
	public static final String[] ARRAY_PERSON_PRIVATE_PHONES = {
			CONTACT_NO_VAL, CONTACT_CAT_MOBILE, CONTACT_CAT_DIRECT,
			CONTACT_CAT_VOIP, CONTACT_CAT_FAX, CONTACT_OTHER };

	// Person professional phones
	public static final String[] ARRAY_PERSON_PRO_PHONES = { CONTACT_NO_VAL,
			CONTACT_CAT_MOBILE, CONTACT_CAT_DIRECT, CONTACT_CAT_MAIN,
			CONTACT_CAT_VOIP, CONTACT_CAT_FAX, CONTACT_OTHER };

	// Org phones
	public static final String[] ARRAY_ORG_PHONES = { CONTACT_NO_VAL,
			CONTACT_CAT_MAIN, CONTACT_CAT_VOIP, CONTACT_CAT_FAX, CONTACT_OTHER };

	// Person addresses
	public static final String[] ARRAY_PERSON_WORK_ADDRESSES = {
			CONTACT_NO_VAL, CONTACT_CAT_HEADOFFICE, CONTACT_CAT_SECOFFICE,
			CONTACT_CAT_BILLING, CONTACT_CAT_DELIVERY,
			CONTACT_CAT_PUBLIC_ENTRANCE, CONTACT_OTHER };

	public static final String[] ARRAY_PERSON_HOME_ADDRESSES = {
			CONTACT_NO_VAL, CONTACT_CAT_MAIN, CONTACT_CAT_SECONDARY,
			CONTACT_CAT_DELIVERY, CONTACT_CAT_INVOICE, CONTACT_OTHER };

	// Org adresses
	public static final String[] ARRAY_ORG_ADDRESSES = { CONTACT_NO_VAL,
			CONTACT_CAT_HEADOFFICE, CONTACT_CAT_SECOFFICE, CONTACT_CAT_BILLING,
			CONTACT_CAT_DELIVERY, CONTACT_CAT_PUBLIC_ENTRANCE, CONTACT_OTHER };

	// Social Media
	public static final String[] ARRAY_SOCIAL_MEDIA = { CONTACT_NO_VAL,
			CONTACT_CAT_FACEBOOK, CONTACT_CAT_GOOGLEPLUS, CONTACT_CAT_LINKEDIN,
			CONTACT_CAT_TWITTER, CONTACT_CAT_XING, CONTACT_CAT_YOUTUBE,
			CONTACT_CAT_VIMEO, CONTACT_CAT_FLICKR, CONTACT_CAT_FOURSQUARE,
			CONTACT_CAT_INSTAGRAM, CONTACT_OTHER };

	// IMPP
	public static final String[] ARRAY_IMPP = { CONTACT_NO_VAL,
			CONTACT_CAT_SKYPE, CONTACT_CAT_MSN, CONTACT_CAT_ICQ,
			CONTACT_CAT_AIM, CONTACT_CAT_YAHOO, CONTACT_CAT_GOOGLE_TALK,
			CONTACT_OTHER };

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