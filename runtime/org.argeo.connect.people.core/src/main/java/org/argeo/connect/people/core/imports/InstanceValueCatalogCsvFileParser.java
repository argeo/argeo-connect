package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/**
 * Base utility to load a catalog of property values for a given business type
 * in a repository.
 * 
 * Expected file format is a Csv with 2 columns, the first with the property
 * names and the second with a '; ' separated list of String values.
 * 
 * Found values are stored in a multi-String property with this name
 **/
public class InstanceValueCatalogCsvFileParser extends CsvParserWithLinesAsMap {

	private final String KEY_COL = "Field";
	private final String VALUES_COL = "Values";
	private final String SEPARATOR = "; ";
	private final Node node;

	public InstanceValueCatalogCsvFileParser(Node node) {
		super();
		this.node = node;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String propName = line.get(KEY_COL);
			String valuesStr = line.get(VALUES_COL);
			String[] values = CommonsJcrUtils.parseAndClean(valuesStr,
					SEPARATOR, true);
			node.setProperty(propName, values);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}