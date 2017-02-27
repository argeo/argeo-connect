package org.argeo.connect.activities;

/** Property names for the activities App */
public interface ActivitiesNames {

	String ACTIVITIES_APP_BASE_NAME = "activities";
	
	/* ACTIVITIES AND TASKS */
	String ACTIVITIES_MANAGER = "activities:manager";
	String ACTIVITIES_REPORTED_BY = "activities:reportedBy";
	String ACTIVITIES_RELATED_TO = "activities:relatedTo";
	String ACTIVITIES_BOUND_ACTIVITIES = "activities:boundActivities";
	String ACTIVITIES_ATTACHEMENTS = "activities:attachments";
	String ACTIVITIES_ACTIVITY_DATE = "activities:activityDate";

	// Tasks
	String ACTIVITIES_TASK_STATUS = "activities:taskStatus";
	String ACTIVITIES_ASSIGNED_TO = "activities:assignedTo"; // user or group dn
	String ACTIVITIES_DUE_DATE = "activities:dueDate";
	String ACTIVITIES_CLOSE_DATE = "activities:closeDate"; // This is the marker to check if a task has been done.
	String ACTIVITIES_CLOSED_BY = "activities:closedBy";
	String ACTIVITIES_WAKE_UP_DATE = "activities:wakeUpDate";
	String ACTIVITIES_DEPENDS_ON = "activities:dependsOn";
	String ACTIVITIES_TASKS = "activities:tasks";

	// Definition of the task template
	String ACTIVITIES_TASK_CLOSING_STATUSES = "activities:closingStatuses";
	String ACTIVITIES_TASK_DEFAULT_STATUS = "activities:defaultStatus";

	
	// Management of user rating
	String ACTIVITIES_POLL_NAME = "activities:pollName";
	String ACTIVITIES_CACHE_AVG_RATE = "activities:cacheAvgRate";
	String ACTIVITIES_RATES = "activities:rates";
	String ACTIVITIES_RATE = "activities:rate"; // (LONG)
	
}
