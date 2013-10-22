package org.argeo.connect.people.ui.extracts;

import java.util.List;

import javax.jcr.query.RowIterator;

/**
 * Views and editors can implement this interface so that a call to
 * getCalcExtract command can generate an Openoffice Calc or MS Excel extract
 * corresponding to the displayed table
 */
public interface ITableProvider {
	/**
	 * Returns the list to display in the spread sheet
	 */
	public RowIterator getRowIterator(String extractId);

	/**
	 * Returns the column definition for passed ID
	 */
	public List<ColumnDefinition> getColumnDefinition(String extractId);
}
