package org.argeo.connect.people.ui.utils;

import org.argeo.connect.people.ui.PeopleUiConstants;

/** Utility object to manage column in various tables and extracts*/
public class ColumnDefinition {

	public final String selectorName;
	public final String propertyName;
	public final String headerLabel;
	public final int propertyType;
	public final int columnSize;

	public ColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel) {
		this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.headerLabel = headerLabel;
		this.columnSize = PeopleUiConstants.DEFAULT_COLUMN_SIZE;
	}

	public ColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel, int columnSize) {
		this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.headerLabel = headerLabel;
		this.columnSize = columnSize;
	}
}
