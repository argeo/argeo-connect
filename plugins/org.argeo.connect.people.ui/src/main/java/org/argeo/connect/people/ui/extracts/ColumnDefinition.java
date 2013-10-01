package org.argeo.connect.people.ui.extracts;

public class ColumnDefinition {

	public final String selectorName;
	public final String propertyName;
	public final String headerLabel;
	public final int propertyType;

	public ColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel) {
		this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.headerLabel = headerLabel;
	}
}
