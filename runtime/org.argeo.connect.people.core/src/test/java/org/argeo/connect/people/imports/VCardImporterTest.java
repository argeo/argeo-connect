package org.argeo.connect.people.imports;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class VCardImporterTest extends AbstractModelTestCase {
	private final static Log log = LogFactory.getLog(VCardImporterTest.class);

	protected String pathToRepository = System.getProperty("user.dir")
			+ "/target/jackrabbit-" + System.getProperty("user.name");
	@SuppressWarnings("unused")
	private Session session;

	private final static String PATH_TO_FILES = "org/argeo/connect/people/imports/";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// uncomment: this line to do real testing
		initializeMe();
		if (log.isTraceEnabled())
			log.trace("Set up done");
	}

	public void testIt() throws Exception {
		log.trace("Test IT");
		// uncomment to do real testing
		importFiles();
		verifyResults();
	}

	private void initializeMe() throws Exception {
		// Initialise the PeopleService with import.
		session = getBeanWithName(Session.class, "jcrSession");
	}

	private void verifyResults() {

	}

	@SuppressWarnings("unused")
	private void importFiles() throws Exception {
		Resource vCardFile = new ClassPathResource(PATH_TO_FILES
				+ "contacts.vcf");
		// import using ez-Vcard
	}

	@Override
	protected boolean deleteRepoOnStartup() {
		return true;
	}

	@Override
	protected boolean deleteRepoOnShutDown() {
		return true;
	}
}