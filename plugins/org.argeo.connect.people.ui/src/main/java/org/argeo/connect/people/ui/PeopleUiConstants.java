package org.argeo.connect.people.ui;

/** Defines some constants that are used all across the user interface **/
public interface PeopleUiConstants {

	/* Default messages */
	public final static String FILTER_HELP_MSG = "Enter filter criterion";

	/* IDs for the various panels */
	public final static String PANEL_CONTACT_DETAILS = "people:contactDetails";
	public final static String PANEL_JOBS = "people:jobs";
	public final static String PANEL_EMPLOYEES = "people:employees";
	public final static String PANEL_PRODUCTIONS = "people:productions";
	public final static String PANEL_MEMBERS = "people:members";
	public final static String PANEL_DESCRIPTION = "people:descriptions";

	/* CUSTOM STYLING */
	// a composite used to put titles in various lists
	public final static String PEOPLE_CSS_TITLE_COMPOSITE_FIRST = "peopleCss-titleCompositeFirst";
	public final static String PEOPLE_CSS_TITLE_COMPOSITE = "peopleCss-titleComposite";

	// for various meta lists subtitles
	// public final static String PEOPLE_CSS_LIST_SUBTITLE_FIRST =
	// "peopleCss-list-subtitle-first";
	public final static String PEOPLE_CSS_LIST_SUBTITLE = "peopleCss-list-subtitle";

	// for main items "header" with all main info
	public final static String PEOPLE_CSS_GENERALINFO_TITLE = "peopleCss-generalInfo-title";
	public final static String PEOPLE_CSS_GENERALINFO_SUBTITLE = "peopleCss-generalInfo-subtitle";
	public final static String PEOPLE_CSS_GENERALINFO_COMPOSITE = "peopleCss-generalInfo-composite";
	public final static String PEOPLE_CSS_GENERALINFO_TAGS = "peopleCss-generalInfo-tags";

	public final static String PEOPLE_CSS_URL_STYLE = "style='color:#383838; font-decoration:none;'";

	// custom image for each item : set size
	public final static String PEOPLE_CSS_ITEM_IMAGE = "peopleCss-itemImage";

	// a dummy style just to see where is a composite
	public final static String PEOPLE_CSS_SHOW_BORDER = "peopleCss-showBorder";
}
