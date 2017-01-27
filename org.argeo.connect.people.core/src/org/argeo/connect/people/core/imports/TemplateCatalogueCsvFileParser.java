package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.util.JcrUiUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/**
 * Base utility to load a catalogue of property values for a given business type
 * in a repository.
 * 
 * Expected file format is a .CSV with 2 columns, the first with the property
 * names and the second with a '; ' separated list of String values.
 * 
 * By default, found values are stored in a multi-String property with this
 * name.
 * 
 * If the property name has been prefixed by "single:", found value is stored as
 * a regular STRING Property
 **/
public class TemplateCatalogueCsvFileParser extends CsvParserWithLinesAsMap {

	private final Node node;
	private final static String SINGLE_VALUE_PROP_PREFIX = "single:";

	public TemplateCatalogueCsvFileParser(Node node) {
		super();
		this.node = node;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String propName = line
					.get(PeopleConstants.IMPORT_CATALOGUE_KEY_COL);
			String valuesStr = line
					.get(PeopleConstants.IMPORT_CATALOGUE_VALUES_COL);

			if (propName.startsWith(SINGLE_VALUE_PROP_PREFIX)) {
				node.setProperty(
						propName.substring(SINGLE_VALUE_PROP_PREFIX.length()),
						valuesStr);
			} else {
				String[] values = JcrUiUtils
						.parseAndClean(
								valuesStr,
								PeopleConstants.IMPORT_CATALOGUE_VALUES_SEPARATOR,
								true);
				node.setProperty(propName, values);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}