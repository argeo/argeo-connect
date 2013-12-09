package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/** Base utility to load the iso country list in an empty repository **/
public class CountriesCsvFileParser extends CsvParserWithLinesAsMap {

	private final static String EN_SHORT_NAME = "English short name (upper-lower case)";
	private final static String ISO_CODE = "Alpha-2 code";

	private Session adminSession;

	public CountriesCsvFileParser(Session adminSession) {
		super();
		this.adminSession = adminSession;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String enName = line.get(EN_SHORT_NAME);
			String isoCode = line.get(ISO_CODE);

			Node countries = adminSession
					.getNode(PeopleConstants.PEOPLE_COUNTRIES_BASE_PATH);
			Node country;
			String relPath = isoCode.substring(0, 1) + "/" + isoCode;
			if (!adminSession.nodeExists(countries.getPath() + "/" + relPath)) {
				country = JcrUtils.mkdirs(countries, relPath,
						PeopleTypes.PEOPLE_ISO_COUNTRY,
						NodeType.NT_UNSTRUCTURED);
				country.setProperty(PeopleNames.PEOPLE_ISO_CODE, isoCode);
				country.setProperty(Property.JCR_TITLE, enName);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}