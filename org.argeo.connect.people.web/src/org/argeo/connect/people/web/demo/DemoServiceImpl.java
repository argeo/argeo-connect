package org.argeo.connect.people.web.demo;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.core.PeopleServiceImpl;
import org.argeo.connect.people.core.imports.CountriesCsvFileParser;
import org.argeo.connect.people.core.imports.LanguagesCsvFileParser;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrSecurityModel;
import org.springframework.core.io.Resource;

/** Exemplary implementation of the people specific Backend */
public class DemoServiceImpl extends PeopleServiceImpl implements
		PeopleService, PeopleConstants {
	private final static Log log = LogFactory.getLog(DemoServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName;
	private UserAdminService userAdminService;
	private JcrSecurityModel jcrSecurityModel;
	private Map<String, Resource> initResources = null;
	/* DEMO ONLY: injected business data */
	private Map<String, Resource> demoBusinessData = null;

	public void init() {
		Session adminSession = null;
		try {
			adminSession = repository.login(workspaceName);
			// Initialization of the model if needed
			initializeModel(adminSession);
			addModelResources(adminSession, initResources);

			// Demo specific Initializes the system with dummy data
			// if the repository is empty (no person defined)
			if (demoBusinessData != null) {
				Node personPar = adminSession
						.getNode(getBasePath(PeopleTypes.PEOPLE_PERSON));
				if (!personPar.hasNodes()) {
					DemoDataImport importer = new DemoDataImport();
					importer.doImport(this, userAdminService, jcrSecurityModel,
							adminSession, demoBusinessData);
				}
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot import demo data", e);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	// HELPERS
	/** Creates various usefull parent nodes if needed */
	protected void initializeModel(Session adminSession) throws Exception {
		JcrUtils.mkdirs(adminSession, getBasePath(null)); // Root business node
		JcrUtils.mkdirs(adminSession, getTmpPath()); // Root temporary node

		// Various business parents
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_PERSON));
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_ORG));
		JcrUtils.mkdirs(adminSession, getBasePath(PeopleTypes.PEOPLE_ACTIVITY));
		JcrUtils.mkdirs(adminSession, getResourceBasePath(null)); // Resources

		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Repository has been initialized " + "with People's model");
		}
	}

	// MODEL INITIALISATION
	// Import resources
	private void addModelResources(Session adminSession,
			Map<String, Resource> initResources) throws Exception {
		// Languages and countries

		Resource resource = initResources.get("Countries");
		if (!adminSession
				.nodeExists(getResourceBasePath(PeopleConstants.RESOURCE_COUNTRY))
				&& resource != null) {
			Node countries = JcrUtils.mkdirs(adminSession,
					getResourceBasePath(PeopleConstants.RESOURCE_COUNTRY));
			new CountriesCsvFileParser(adminSession, countries).parse(
					resource.getInputStream(), "UTF-8");
		}

		resource = initResources.get("Languages");
		if (!adminSession
				.nodeExists(getResourceBasePath(PeopleConstants.RESOURCE_LANG))
				&& resource != null) {
			Node languages = JcrUtils.mkdirs(adminSession,
					getResourceBasePath(PeopleConstants.RESOURCE_LANG));
			new LanguagesCsvFileParser(adminSession, languages).parse(
					resource.getInputStream(), "UTF-8");
		}

		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Resources have been added to People's model");
		}
	}

	/* DEPENDENCY INJECTION */
	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setJcrSecurityModel(JcrSecurityModel jcrSecurityModel) {
		this.jcrSecurityModel = jcrSecurityModel;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public void setDemoBusinessData(Map<String, Resource> demoBusinessData) {
		this.demoBusinessData = demoBusinessData;
	}

	public void setInitResources(Map<String, Resource> initResources) {
		this.initResources = initResources;
	}
}