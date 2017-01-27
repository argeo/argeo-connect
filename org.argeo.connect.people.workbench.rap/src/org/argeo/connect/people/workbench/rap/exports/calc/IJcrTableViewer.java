package org.argeo.connect.people.workbench.rap.exports.calc;

import java.util.List;

import org.argeo.connect.ui.ConnectColumnDefinition;

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
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId);
}
