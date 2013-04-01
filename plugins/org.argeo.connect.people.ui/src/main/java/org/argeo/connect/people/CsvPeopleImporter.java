package org.argeo.connect.people;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.ArgeoMonitor;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;

/** Imports a list of people from a CSV file */
public class CsvPeopleImporter implements Runnable, PeopleNames, ArgeoNames {
	private final static Log log = LogFactory.getLog(CsvPeopleImporter.class);
	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private String url;
	private String base = PeopleConstants.PEOPLE_BASE_PATH;
	private Repository repository;
	private ArgeoMonitor monitor;

	@Override
	public void run() {
		Session session = null;
		InputStream in = null;
		try {
			session = repository.login();
			PeopleParser peopleParser = new PeopleParser(session);
			in = new URL(url).openStream();
			peopleParser.parse(in);
		} catch (Exception e) {
			throw new ArgeoException("Cannot import " + url, e);
		} finally {
			JcrUtils.logoutQuietly(session);
			IOUtils.closeQuietly(in);
		}

	}

	protected void addPerson(Session session, String lastName,
			String firstName, Calendar dateOfBirth, Map<String, String> values)
			throws RepositoryException, UnsupportedEncodingException {
		// Distinguished name upon which the person UUID is based
		String dn = "sn=" + lastName.toUpperCase() + ",givenName="
				+ firstName.toUpperCase() + ",dateOfBirth="
				+ dateFormat.format(dateOfBirth.getTime());
		// Class 3 UUID based on Distinguished name in UTF-8
		UUID personUuid = UUID.nameUUIDFromBytes(dn.getBytes(UTF8_CHARSET));

		// Path
		String country = values.get("PAYS");
		String votingBooth = values.get("LIB_BUREAU_DE_VOTE").replace('/', ' ');
		String vbPath = base + "/" + country + "/" + votingBooth;
		Integer birthYear = dateOfBirth.get(Calendar.YEAR);
		Integer birthDecade = birthYear - (birthYear % 10);
		String dbPath = birthDecade.toString();
		// String dbPath = JcrUtils.dateAsPath(dateOfBirth);
		Node baseNode = JcrUtils.mkdirs(session, vbPath + "/" + dbPath);

		String personUuidStr = personUuid.toString();
		// TODO better deal with update and merge
		if (baseNode.hasNode(personUuidStr))
			return;
		Node personNode = baseNode.addNode(personUuidStr,
				PeopleTypes.PEOPLE_PERSON);
		personNode.setProperty(PEOPLE_PERSON_UUID, personUuidStr);
		personNode.setProperty(PEOPLE_DATE_OF_BIRTH, dateOfBirth);
		// personNode.setProperty(PEOPLE_DN, dn);
		personNode.setProperty(ARGEO_LAST_NAME, lastName);
		personNode.setProperty(ARGEO_FIRST_NAME, firstName);
		personNode.setProperty(Property.JCR_TITLE,
				firstName + " " + lastName.toUpperCase());

		// count
		incrementPeopleCount(personNode.getParent());

		session.save();
	}

	/** Recursively update people counts */
	protected void incrementPeopleCount(Node node) throws RepositoryException {
		Long count = 0l;
		if (node.hasProperty(PEOPLE_COUNT))
			count = node.getProperty(PEOPLE_COUNT).getLong();
		node.setProperty(PEOPLE_COUNT, count + 1);
		try {
			incrementPeopleCount(node.getParent());
		} catch (ItemNotFoundException e) {// root
			// silent
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	private class PeopleParser extends CsvParserWithLinesAsMap {
		private final Session session;

		public PeopleParser(Session session) {
			this.session = session;
		}

		@Override
		protected void processLine(Integer lineNumber, Map<String, String> line) {
			try {
				String lastName = line.get("NOMFAM");
				String firstName = line.get("PRENOM_USUEL");
				Calendar dateOfBirth = new GregorianCalendar();
				dateOfBirth.setTime(dateFormat.parse(line.get("DATNAI")));

				addPerson(session, lastName, firstName, dateOfBirth, line);

				if ((lineNumber % 100) == 0) {
					if (log.isDebugEnabled())
						log.debug("Processed line " + lineNumber);
					if (monitor != null)
						monitor.subTask("Processed line " + lineNumber);
				}
			} catch (Exception e) {
				log.error("Line " + lineNumber + ": " + e.getMessage());
				if (log.isDebugEnabled())
					log.debug("Stack trace", e);
			}
		}

	}
}
