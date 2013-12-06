package org.argeo.connect.people.ui;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleTypes;

/** Defines some constants that are used all across the user interface **/
public interface PeopleUiConstants {

	/* Default formats */
	public final static String DEFAULT_DATE_FORMAT = "EEE, dd MMM yyyy";
	public final static String DEFAULT_DATE_TIME_FORMAT = "EEE, dd MMM yyyy 'at' HH:mm";
	public final static String DEFAULT_NUMBER_FORMAT = "#,##0.0";

	// Default column size for various tables and extracts
	public final static int DEFAULT_COLUMN_SIZE = 120;

	/* Default messages */
	public final static String FILTER_HELP_MSG = "Enter filter criterion";

	/* IDs for the various dialogs */
	public final static String DIALOG_ADD_ML_MEMBERS = "dialog:addMLMembers";
	public final static String DIALOG_ADD_ML_MEMBERSHIP = "dialog:addMLMembership";

	/* IDs for the various panels */
	public final static String PANEL_CONTACT_DETAILS = "people:contactDetails";
	public final static String PANEL_JOBS = "people:jobs";
	public final static String PANEL_LEGAL_INFO = "people:legalInfo";
	public final static String PANEL_EMPLOYEES = "people:employees";
	public final static String PANEL_PRODUCTIONS = "people:productions";
	public final static String PANEL_MEMBERS = "people:members";
	public final static String PANEL_DESCRIPTION = "people:descriptions";
	public final static String PANEL_SYNOPSES = "people:synopses";
	public final static String PANEL_FILM_INFO = "people:filmInfo";
	public final static String PANEL_HISTORY = "people:history";

	// Various types for list label providers
	public final static int LIST_TYPE_OVERVIEW_TITLE = 0;
	public final static int LIST_TYPE_OVERVIEW_DETAIL = 1;
	public final static int LIST_TYPE_SMALL = 2;
	public final static int LIST_TYPE_MEDIUM = 3;

	public final static Map<String, String> PEOPLE_TYPE_LABELS = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(PeopleTypes.PEOPLE_PERSON, "Person");
			put(PeopleTypes.PEOPLE_ORGANIZATION, "Organisation");
			put(FilmTypes.FILM, "Film");
			put(PeopleTypes.PEOPLE_MAILING_LIST, "Mailing list");
			put(PeopleTypes.PEOPLE_GROUP, "Group");
		}
	};

	/* CUSTOM STYLING */
	// a composite used to put titles in various lists
	public final static String CSS_FLAT_IMG_BUTTON = "peopleCss-flatImgBtn";

	// FIXME workaround to work on fixing RAP/RCP single sourcing for people
	// Caches name of RWT.MARKUP_ENABLED property
	public final static String MARKUP_ENABLED = "org.eclipse.rap.rwt.markupEnabled";
	// Caches name of RWT.CUSTOM_ITEM_HEIGHT property
	public final static String CUSTOM_ITEM_HEIGHT = "org.eclipse.rap.rwt.customItemHeight";
	// Caches name of RWT.CUSTOM_VARIANT property
	public static final String CUSTOM_VARIANT = "org.eclipse.rap.rwt.customVariant";
	
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
	public final static String CSS_STYLE_UNIQUE_CELL_TABLE = "uniqueCellTable";

	// custom image for each item : set size
	public final static String PEOPLE_CSS_ITEM_IMAGE = "peopleCss-itemImage";

	// a dummy style just to see where is a composite
	public final static String PEOPLE_CSS_SHOW_BORDER = "peopleCss-showBorder";
}
