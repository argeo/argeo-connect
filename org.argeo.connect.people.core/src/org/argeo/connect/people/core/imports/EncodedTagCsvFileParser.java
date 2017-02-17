package org.argeo.connect.people.core.imports;

import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.resources.ResourceService;
import org.argeo.util.CsvParserWithLinesAsMap;

/** Base utility to load an encoded tag like resource catalogue in a repository **/
public class EncodedTagCsvFileParser extends CsvParserWithLinesAsMap {

	private final ResourceService resourceService;
	private final Session session;
	private final String tagId;
	private final String codeHeaderName;
	private final String valueHeaderName;

	public EncodedTagCsvFileParser(ResourceService resourceService,
			Session session, String tagId, String codeHeaderName,
			String valueHeaderName) {
		super();
		this.session = session;
		this.resourceService = resourceService;
		this.tagId = tagId;
		this.codeHeaderName = codeHeaderName;
		this.valueHeaderName = valueHeaderName;
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			String tagCode = line.get(codeHeaderName);
			String tagValue = line.get(valueHeaderName);
			resourceService.registerTag(session, tagId, tagCode, tagValue);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}