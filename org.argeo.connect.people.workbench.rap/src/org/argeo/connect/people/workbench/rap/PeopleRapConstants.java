package org.argeo.connect.people.workbench.rap;

/** Defines the constants that are specific for People Rap Workbench UI **/
public interface PeopleRapConstants {

	// Default messages
	String FILTER_HELP_MSG = "Enter filter criterion";
	// Duration in ms before a search is launched
	int SEARCH_TEXT_DELAY = 800;
	String KEY_PEOPLE_SERVICE = "PeopleService";

	// Various types for list label providers
	int LIST_TYPE_OVERVIEW_TITLE = 0;
	// public final static int LIST_TYPE_OVERVIEW_DETAIL = 1;
	int LIST_TYPE_SMALL = 2;
	int LIST_TYPE_MEDIUM = 3;

	// Map<String, String> PEOPLE_TYPE_LABELS = new HashMap<String, String>() {
	// private static final long serialVersionUID = 1L;
	// {
	// put(PeopleTypes.PEOPLE_PERSON, "Person");
	// put(PeopleTypes.PEOPLE_ORG, "Organisation");
	// put(PeopleTypes.PEOPLE_MAILING_LIST, "Mailing list");
	// put(PeopleTypes.PEOPLE_GROUP, "Group");
	// put(PeopleTypes.PEOPLE_TASK, "Task");
	// }
	// };

	// Various dialogs IDs
	String DIALOG_ADD_ML_MEMBERS = "dialog:addMLMembers";
	String DIALOG_ADD_ML_MEMBERSHIP = "dialog:addMLMembership";

	// CTabFolder tabs Ids
	String CTAB_CONTACT_DETAILS = "people:contactDetails";
	String CTAB_ACTIVITY_LOG = "people:activityLog";
	String CTAB_JOBS = "people:jobs";
	String CTAB_LEGAL_INFO = "people:legalInfo";
	String CTAB_EMPLOYEES = "people:employees";
	String CTAB_MEMBERS = "people:members";
	String CTAB_HISTORY = "people:history";
	String CTAB_EDIT_CATALOGUE = "people:editCatalogue";

	// Custom styling within table cell that are markup enabled:
	// we cannot use the CSS class parameter inside of a custom variant control.
	String PEOPLE_STYLE_ENTITY_HEADER = "style='font-size:14px;'";
	String PEOPLE_STYLE_LINK = " style='color:#383838; text-decoration:none;' ";
}
