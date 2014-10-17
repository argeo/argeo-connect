package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/**
 * Base utility to load a catalogue of property values for a given business type
 * in a repository.
 * 
 * Expected file format is a Csv with 2 columns, the first with the property
 * names and the second with a '; ' separated list of String values.
 * 
 * Found values are stored in a multi-String property with this name
 **/
public class TemplateCatalogueCsvFileParser extends CsvParserWithLinesAsMap {

	private final Node node;

	public TemplateCatalogueCsvFileParser(Node node) {
		super();
		this.node = node;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String propName = line.get(PeopleConstants.IMPORT_CATALOGUE_KEY_COL);
			String valuesStr = line.get(PeopleConstants.IMPORT_CATALOGUE_VALUES_COL);
			String[] values = CommonsJcrUtils.parseAndClean(valuesStr,
					PeopleConstants.IMPORT_CATALOGUE_VALUES_SEPARATOR, true);
			node.setProperty(propName, values);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}