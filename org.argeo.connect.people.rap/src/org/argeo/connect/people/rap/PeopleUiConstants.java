package org.argeo.connect.people.rap;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.PeopleTypes;

/** Defines some constants that are used all across the user interface **/
public interface PeopleUiConstants {

	/* Default formats */
	public final static String DEFAULT_SHORT_DATE_FORMAT = "dd/MM/yyyy";
	public final static String DEFAULT_DATE_FORMAT = "MMM, dd yyyy";
	public final static String DEFAULT_DATE_TIME_FORMAT = "MMM, dd yyyy 'at' HH:mm";
	public final static String DEFAULT_NUMBER_FORMAT = "#,##0.0";

	// Default column size for various tables and extracts
	public final static int DEFAULT_COLUMN_SIZE = 120;

	/* Default messages */
	public final static String FILTER_HELP_MSG = "Enter filter criterion";

	/* Ids for the various exports */
	public final static String DEFAULT_CALC_EXPORT = "defaultCalcExport";

	/* IDs for the various dialogs */
	public final static String DIALOG_ADD_ML_MEMBERS = "dialog:addMLMembers";
	public final static String DIALOG_ADD_ML_MEMBERSHIP = "dialog:addMLMembership";

	/* IDs for the various panels */
	public final static String PANEL_CONTACT_DETAILS = "people:contactDetails";
	public final static String PANEL_ACTIVITY_LOG = "people:activityLog";
	public final static String PANEL_JOBS = "people:jobs";
	public final static String PANEL_LEGAL_INFO = "people:legalInfo";
	public final static String PANEL_EMPLOYEES = "people:employees";
	public final static String PANEL_MEMBERS = "people:members";
	public final static String PANEL_HISTORY = "people:history";

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

	// CRUD ACTION TYPE IDS
	public final static String CRUD_CREATE = "Create";
	public final static String CRUD_EDIT = "Edit";
	public final static String CRUD_DELETE = "Delete";

	/* CUSTOM STYLING */

	// It is still some wortk in progress.
	// TODO workaround to work on fixing RAP/RCP single sourcing for people
	// Caches name of RWT.MARKUP_ENABLED property
	public final static String MARKUP_ENABLED = "org.eclipse.rap.rwt.markupEnabled";
	// Caches name of RWT.CUSTOM_ITEM_HEIGHT property
	public final static String CUSTOM_ITEM_HEIGHT = "org.eclipse.rap.rwt.customItemHeight";
	// Caches name of RWT.CUSTOM_VARIANT property
	public static final String CUSTOM_VARIANT = "org.eclipse.rap.rwt.customVariant";

	// Specific CSS classes
	public final static String PEOPLE_CLASS_ENTITY_HEADER = "people_entity_header";
	public final static String PEOPLE_CLASS_GADGET = "people_gadget";
	public final static String PEOPLE_CLASS_GADGET_HEADER = "people_gadget_header";
	public final static String PEOPLE_CLASS_FLAT_BTN = "people_flat_btn";
	// Overwrite normal behaviour to show the border even when the text is
	// disabled.
	public final static String PEOPLE_CLASS_FORCE_BORDER = "people_force_border";
	// custom image for each item : set size
	// public final static String PEOPLE_CSS_ITEM_IMAGE = "peopleCss-itemImage";

	// Specific CSS styling: we cannot use the CSS class parameter inside of a
	// Custom variant widget.
	// Typically to style links inside of a table row
	public final static String PEOPLE_STYLE_ENTITY_HEADER = "style='font-size:14px;'";
	public final static String PEOPLE_STYLE_LINK = " style='color:#383838; text-decoration:none;' ";
}