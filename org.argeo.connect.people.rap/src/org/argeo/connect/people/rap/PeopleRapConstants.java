package org.argeo.connect.people.rap;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.PeopleTypes;

/** Defines the constants that are specific for People Rap Workbench UI **/
public interface PeopleRapConstants {

	// Default messages
	public final static String FILTER_HELP_MSG = "Enter filter criterion";

	// Various types for list label providers
	public final static int LIST_TYPE_OVERVIEW_TITLE = 0;
	// public final static int LIST_TYPE_OVERVIEW_DETAIL = 1;
	public final static int LIST_TYPE_SMALL = 2;
	public final static int LIST_TYPE_MEDIUM = 3;

	public final static Map<String, String> PEOPLE_TYPE_LABELS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(PeopleTypes.PEOPLE_PERSON, "Person");
			put(PeopleTypes.PEOPLE_ORG, "Organisation");
			put(PeopleTypes.PEOPLE_MAILING_LIST, "Mailing list");
			put(PeopleTypes.PEOPLE_GROUP, "Group");
			put(PeopleTypes.PEOPLE_TASK, "Task");
		}
	};

	// IDs for the various exports
	public final static String DEFAULT_CALC_EXPORT = "defaultCalcExport";

	// IDs for the various dialogs
	public final static String DIALOG_ADD_ML_MEMBERS = "dialog:addMLMembers";
	public final static String DIALOG_ADD_ML_MEMBERSHIP = "dialog:addMLMembership";

	// IDs for the various CTabFolder tabs
	public final static String CTAB_CONTACT_DETAILS = "people:contactDetails";
	public final static String CTAB_ACTIVITY_LOG = "people:activityLog";
	public final static String CTAB_JOBS = "people:jobs";
	public final static String CTAB_LEGAL_INFO = "people:legalInfo";
	public final static String CTAB_EMPLOYEES = "people:employees";
	public final static String CTAB_MEMBERS = "people:members";
	public final static String CTAB_HISTORY = "people:history";

	/* CUSTOM STYLING */

	// Specific CSS classes
	public final static String PEOPLE_CLASS_ENTITY_HEADER = "people_entity_header";
	public final static String PEOPLE_CLASS_GADGET = "people_gadget";
	public final static String PEOPLE_CLASS_GADGET_HEADER = "people_gadget_header";
	public final static String PEOPLE_CLASS_FLAT_BTN = "people_flat_btn";
	// Overwrite normal behaviour:
	// show the border of a text even when the text is disabled.
	public final static String PEOPLE_CLASS_FORCE_BORDER = "people_force_border";

	// Specific CSS styling: we cannot use the CSS class parameter inside of a
	// Custom variant widget.
	// TODO rather set a custom variant on the corresponding label
	public final static String PEOPLE_STYLE_ENTITY_HEADER = "style='font-size:14px;'";
	// TODO rather use the general URL Styling via the application
	public final static String PEOPLE_STYLE_LINK = " style='color:#383838; text-decoration:none;' ";
}