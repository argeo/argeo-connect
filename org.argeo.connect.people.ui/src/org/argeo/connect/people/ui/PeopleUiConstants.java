package org.argeo.connect.people.ui;

/** Centralizes management of constants for the various People UIs */
public interface PeopleUiConstants {
	// Characters that must be replaced by their codes for RWT
	public static String NB_SPACE = "&#160;";
	public static String NB_DOUBLE_SPACE = "&#160;&#160;";
	public static String AMPERSAND = "&#38;";

	// Crud ID & Default label to manage list items among others.
	public final static String CRUD_CREATE = "Create";
	public final static String CRUD_EDIT = "Edit";
	public final static String CRUD_DELETE = "Delete";

	// Default English date and numbers formats
	public final static String DEFAULT_SHORT_DATE_FORMAT = "dd/MM/yyyy";
	public final static String DEFAULT_DATE_FORMAT = "MMM, dd yyyy";
	public final static String DEFAULT_DATE_TIME_FORMAT = "MMM, dd yyyy 'at' HH:mm";
	public final static String DEFAULT_NUMBER_FORMAT = "#,##0.0";

	// Default column size for various tables and extracts
	public final static int DEFAULT_COLUMN_SIZE = 120;
}
