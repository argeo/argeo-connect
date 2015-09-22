package org.argeo.connect.people.rap.exports.calc;

import java.util.List;

import org.argeo.connect.people.ui.PeopleColumnDefinition;

/**
 * Views and editors can implement this interface so that one of the row list
 * that is displayed in the part can be rebuild externally. Typically to
 * generate csv or calc extract.
 */
public interface IJcrTableViewer {
	/**
	 * Returns the list to display in the spread sheet
	 */
	public Object[] getElements(String extractId);

	/**
	 * Returns the column definition for passed ID
	 */
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId);
}
