package org.argeo.people.core.imports;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.springframework.core.io.Resource;

/** Base utility to load CSV data in a People repository **/
public abstract class AbstractPeopleCsvFileParser extends CsvParserWithLinesAsMap implements PeopleNames {
	private final static Log log = LogFactory.getLog(AbstractPeopleCsvFileParser.class);

	private final Session adminSession;
	private final PeopleService peopleService;
	private final ResourcesService resourceService;

	protected VersionManager vm;
	protected DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	// Enable importing images
	private Resource images = null;

	public AbstractPeopleCsvFileParser(Session adminSession, ResourcesService resourceService,
			PeopleService peopleService, Resource images) {
		super();
		this.adminSession = adminSession;
		this.resourceService = resourceService;
		this.peopleService = peopleService;
		this.images = images;
		try {
			vm = adminSession.getWorkspace().getVersionManager();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get version manager " + "while trying to import nodes", e);
		}
	}

	public AbstractPeopleCsvFileParser(Session adminSession, ResourcesService resourceService,
			PeopleService peopleService) {
		this(adminSession, resourceService, peopleService, null);
	}

	@Override
	protected abstract void processLine(Integer lineNumber, Map<String, String> line);

	// UTILS
	protected void setDateValueFromString(Node node, String propName, String value) {
		try {
			Calendar cal = new GregorianCalendar();
			cal.setTime(dateFormat.parse(value));
			node.setProperty(propName, cal);
		} catch (ParseException e) {
			throw new PeopleException("Unable to parse date for: " + value, e);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get version manager " + "while trying to import nodes", e);
		}

	}

	protected Resource getPicture(String fileName) throws IOException {
		OsgiBundleResource imgBundle = (OsgiBundleResource) images;
		String tmpPath = imgBundle.getURI().getPath();
		tmpPath = tmpPath.substring(0, tmpPath.length() - 1);
		tmpPath = JcrUtils.lastPathElement(tmpPath);
		tmpPath = tmpPath + "/" + fileName;
		if (log.isTraceEnabled())
			log.trace("Getting dummy image at path: " + tmpPath);
		return imgBundle.createRelative(tmpPath);
	}

	/* Exposes context */
	protected Session getSession() {
		return adminSession;
	}

	protected ResourcesService getResourcesService() {
		return resourceService;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}
}
