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

	// Contact categories: maps corresponding node types with a label
	public static final Map<String, String> MAPS_CONTACT_TYPES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(PeopleTypes.PEOPLE_EMAIL, "Email");
		tmpMap.put(PeopleTypes.PEOPLE_PHONE, "Phone");
		tmpMap.put(PeopleTypes.PEOPLE_ADDRESS, "Address");
		tmpMap.put(PeopleTypes.PEOPLE_URL, "URL");
		tmpMap.put(PeopleTypes.PEOPLE_IMPP, "Instant Messaging");
		tmpMap.put(PeopleTypes.PEOPLE_CONTACT, "Other...");
		MAPS_CONTACT_TYPES = Collections.unmodifiableMap(tmpMap);
	}
	public static final String[] ARRAY_CONTACT_TYPES = { "Email", "Phone",
			"Address", "URL", "Instant Messaging", "Other..." };

	// Contact categories: maps corresponding node types with a label
	public static final String[] ARRAY_CONTACT_CATEGORIES = { "Work", "Home",
			"Other" };

	// Contact categories: maps corresponding node types with a label
	public static final String[] ARRAY_PHONE_TYPES = { "Mobile", "Fix",
			"Direct", "Fax", "Reception" };

	/**
	 * Provide the key for a given value. we assume it is a bijection each value
	 * is only linked to one key
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
