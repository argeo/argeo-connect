package org.argeo.connect.activities;

/** Mixin supported by the Activities App */
public interface ActivitiesTypes {

	/* TASKS AND ACTIVITIES, from most to least specific */
	// known task types
	String ACTIVITIES_POLL = "activities:poll";
	String ACTIVITIES_TASK = "activities:task";
	// known activity types
	String ACTIVITIES_NOTE = "activities:note";
	String ACTIVITIES_SENT_EMAIL = "activities:sentEmail";
	String ACTIVITIES_CALL = "activities:call";
	String ACTIVITIES_MEETING = "activities:meeting";
	String ACTIVITIES_SENT_LETTER = "activities:sentLetter";
	String ACTIVITIES_SENT_FAX = "activities:sentFax";
	String ACTIVITIES_PAYMENT = "activities:payment";
	String ACTIVITIES_REVIEW = "activities:review";
	String ACTIVITIES_CHAT = "activities:chat";
	String ACTIVITIES_TWEET = "activities:tweet";
	String ACTIVITIES_BLOG_POST = "activities:blogPost";
	String ACTIVITIES_RATE = "activities:rate";
	// base mixin
	String ACTIVITIES_ACTIVITY = "activities:activity";
}
