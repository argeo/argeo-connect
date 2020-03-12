package org.argeo.connect.ui;

/**
 * Centralizes management of UI constants for the various Connect UIs
 * 
 * TODO handle this cleanly.
 */
public interface ConnectUiConstants {
	// Nb of millisecond between 2 requests in the delayed filtered text
	int SEARCH_TEXT_DELAY = 800;

	// Characters that must be replaced by their codes for RWT
	String NB_SPACE = "&#160;";
	String NB_DOUBLE_SPACE = "&#160;&#160;";
	String AMPERSAND = "&#38;";

	// Crud ID & Default label to manage list items among others.
	String CRUD_CREATE = "Create";
	String CRUD_VIEW = "View";
	String CRUD_EDIT = "Edit";
	String CRUD_DELETE = "Delete";

	/* UI WIDGETS DATA KEYS */
	// We often need to store an ordered list of arbitrary objects that goes
	// together with the array of String that is displayed to the end user. Thus
	// the selected object will be retrieved using this:
	String COMBO_BUSINESS_OBJECTS = "comboBusinessList";

	// Default column size for various tables and extracts
	int DEFAULT_COLUMN_SIZE = 120;

	// Exports IDs
	String DEFAULT_JXL_EXPORT = "defaultJxlExport";

	// the separator used in the various href local values to provide internal
	// browsing using links in table / label / trees
	String HREF_SEPARATOR = "/";

	int SEARCH_DEFAULT_LIMIT = 100;
	
	// Various types for list label providers
	int LIST_TYPE_OVERVIEW_TITLE = 0;
	// public final static int LIST_TYPE_OVERVIEW_DETAIL = 1;
	int LIST_TYPE_SMALL = 2;
	int LIST_TYPE_MEDIUM = 3;

	// Custom styling within table cell that are markup enabled:
	// we cannot use the CSS class parameter inside of a custom variant control.
	String ENTITY_HEADER_INNER_CSS_STYLE = "style='font-size:14px;'";

	// CENTRALISE dependency to RWT. TODO manage this cleanly in CmsUiUtils
	//int MARKUP_VIEWER_HYPERLINK = RWT.HYPERLINK;
	int MARKUP_VIEWER_HYPERLINK = 1 << 26;
}
