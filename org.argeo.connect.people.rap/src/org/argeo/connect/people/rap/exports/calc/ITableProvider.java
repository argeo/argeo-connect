package org.argeo.connect.people.rap.exports.calc;

import java.util.List;

import javax.jcr.query.Row;

import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;

/**
 * Views and editors can implement this interface so that one of the row list
 * that is displayed in the part can be rebuild externally. Typically to
 * generate csv or calc extract.
 */
public interface ITableProvider {
	/**
	 * Returns the list to display in the spread sheet
	 */
	public Row[] getRows(String extractId);

	/**
	 * Returns the column definition for passed ID
	 */
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId);
}
