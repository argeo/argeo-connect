package org.argeo.connect.people.ui.extracts;

/**
 * Utility object to manage column in various tables and extracts displaying
 * data from JCR
 */
public class ColumnDefinition {
	private final static int DEFAULT_COLUMN_SIZE = 120;

	public final String selectorName;
	public final String propertyName;
	public final String headerLabel;
	public final int propertyType;
	public final int columnSize;

	/**
	 * new column using default width
	 * 
	 * @param selectorName
	 * @param propertyName
	 * @param propertyType
	 * @param headerLabel
	 */
	public ColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel) {
		this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.headerLabel = headerLabel;
		this.columnSize = DEFAULT_COLUMN_SIZE;
	}

	/**
	 * 
	 * @param selectorName
	 * @param propertyName
	 * @param propertyType
	 * @param headerLabel
	 * @param columnSize
	 */
	public ColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel, int columnSize) {
		this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.headerLabel = headerLabel;
		this.columnSize = columnSize;
	}
}
