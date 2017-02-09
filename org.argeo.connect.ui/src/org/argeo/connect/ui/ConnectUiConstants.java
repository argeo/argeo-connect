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

	// Default English date and numbers formats
	String DEFAULT_SHORT_DATE_FORMAT = "dd/MM/yyyy";
	String DEFAULT_DATE_FORMAT = "MMM dd, yyyy";
	String DEFAULT_DATE_TIME_FORMAT = "MMM dd, yyyy 'at' HH:mm";
	String DEFAULT_NUMBER_FORMAT = "#,##0.0";

	// Default column size for various tables and extracts
	int DEFAULT_COLUMN_SIZE = 120;

	// Exports IDs
	String DEFAULT_JXL_EXPORT = "defaultJxlExport";
}
