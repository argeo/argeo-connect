package org.argeo.activities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Workaround class to define maps for activities before we implement a more
 * clean management of such an issue
 */
public class ActivityValueCatalogs {

	/* ACTIVITY TYPES */
	public final static String ACT_LBL_TASK = "Task";
	public final static String ACT_LBL_NOTE = "Note";
	public final static String ACT_LBL_SENT_MAIL = "Sent mail";
	public final static String ACT_LBL_CALL = "Call";
	public final static String ACT_LBL_SENT_FAX = "Sent fax";
	public final static String ACT_LBL_MEETING = "Meeting";
	public final static String ACT_LBL_POST_MAIL = "Post mail";
	public final static String ACT_LBL_PAYMENT = "Payment";
	public final static String ACT_LBL_REVIEW = "Review";
	public final static String ACT_LBL_CHAT = "Chat";
	public final static String ACT_LBL_TWEET = "Tweet";
	public final static String ACT_LBL_BLOG = "Blog post";
	public final static String ACT_LBL_RATE = "Rate";

	// maps corresponding node types with a label
	public static final Map<String, String> MAPS_ACTIVITY_TYPES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(ActivitiesTypes.ACTIVITIES_TASK, ACT_LBL_TASK);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_NOTE, ACT_LBL_NOTE);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_SENT_EMAIL, ACT_LBL_SENT_MAIL);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_CALL, ACT_LBL_CALL);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_SENT_FAX, ACT_LBL_SENT_FAX);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_MEETING, ACT_LBL_MEETING);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_SENT_LETTER, ACT_LBL_POST_MAIL);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_PAYMENT, ACT_LBL_PAYMENT);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_REVIEW, ACT_LBL_REVIEW);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_CHAT, ACT_LBL_CHAT);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_TWEET, ACT_LBL_TWEET);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_BLOG_POST, ACT_LBL_BLOG);
		tmpMap.put(ActivitiesTypes.ACTIVITIES_RATE, ACT_LBL_RATE);
		MAPS_ACTIVITY_TYPES = Collections.unmodifiableMap(tmpMap);
	}

	public static String[] getActivityTypeLabels() {
		return new String[] { ACT_LBL_NOTE, ACT_LBL_SENT_MAIL, ACT_LBL_CALL, ACT_LBL_MEETING, ACT_LBL_SENT_FAX,
				ACT_LBL_POST_MAIL, ACT_LBL_PAYMENT, ACT_LBL_REVIEW, ACT_LBL_CHAT, ACT_LBL_TWEET, ACT_LBL_BLOG };
	}

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
