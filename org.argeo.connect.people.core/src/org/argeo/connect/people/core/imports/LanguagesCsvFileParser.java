package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/**
 * Base utility to load the iso language list in an empty repository
 * 
 * @deprecated
 * **/
public class LanguagesCsvFileParser extends CsvParserWithLinesAsMap {
	private final static String EN_SHORT_NAME = "Language name";
	private final static String ISO_CODE = "639-1";

	private final Session adminSession;
	private final Node langs;

	public LanguagesCsvFileParser(Session adminSession, Node langs) {
		super();
		this.adminSession = adminSession;
		this.langs = langs;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String enName = line.get(EN_SHORT_NAME);
			String isoCode = line.get(ISO_CODE);
			String relPath = isoCode.substring(0, 1) + "/" + isoCode;
			if (!adminSession.nodeExists(langs.getPath() + "/" + relPath)) {
				Node lang = JcrUtils.mkdirs(langs, relPath,
						PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE,
						NodeType.NT_UNSTRUCTURED);
				lang.setProperty(PeopleNames.PEOPLE_CODE, isoCode);
				lang.setProperty(Property.JCR_TITLE, enName);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}