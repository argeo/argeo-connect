package org.argeo.connect.people.web.demo;

import java.util.ArrayList;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.core.PeopleServiceImpl;
import org.argeo.connect.people.core.imports.EncodedTagCsvFileParser;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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
			initialiseModel(adminSession);
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
	@Override
	protected void initialiseModel(Session adminSession)
			throws RepositoryException {
		super.initialiseModel(adminSession);

		// The resources
		getResourceService().initialiseResources(adminSession);

		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Repository has been initialized " + "with People's model");
		}
	}

	// MODEL INITIALISATION
	// Import resources
	protected void addModelResources(Session adminSession,
			Map<String, Resource> initResources) throws Exception {

		// initialisation
		ResourceService resourceService = getResourceService();

		Resource resource = initResources.get("Countries");
		if (resourceService.getTagLikeResourceParent(adminSession,
				PeopleConstants.RESOURCE_COUNTRY) == null && resource != null) {
			resourceService.createTagLikeResourceParent(adminSession,
					PeopleConstants.RESOURCE_COUNTRY,
					PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE,
					PeopleNames.PEOPLE_CODE, getBasePath(null), CommonsJcrUtils
							.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED),
					new ArrayList<String>());
			String EN_SHORT_NAME = "English short name (upper-lower case)";
			String ISO_CODE = "Alpha-2 code";
			new EncodedTagCsvFileParser(resourceService, adminSession,
					PeopleConstants.RESOURCE_COUNTRY, ISO_CODE, EN_SHORT_NAME)
					.parse(resource.getInputStream(), "UTF-8");
		}

		resource = initResources.get("Languages");
		if (resourceService.getTagLikeResourceParent(adminSession,
				PeopleConstants.RESOURCE_LANG) == null && resource != null) {
			resourceService.createTagLikeResourceParent(adminSession,
					PeopleConstants.RESOURCE_LANG,
					PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE,
					PeopleNames.PEOPLE_CODE, getBasePath(null), CommonsJcrUtils
							.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED),
					new ArrayList<String>());
			String EN_SHORT_NAME = "Language name";
			String ISO_CODE = "639-1";
			new EncodedTagCsvFileParser(resourceService, adminSession,
					PeopleConstants.RESOURCE_LANG, ISO_CODE, EN_SHORT_NAME)
					.parse(resource.getInputStream(), "UTF-8");
		}

		// Create tag & mailing list parents
		if (resourceService.getTagLikeResourceParent(adminSession,
				PeopleConstants.RESOURCE_TAG) == null)
			resourceService.createTagLikeResourceParent(adminSession,
					PeopleConstants.RESOURCE_TAG,
					PeopleTypes.PEOPLE_TAG_INSTANCE, null, getBasePath(null),
					PeopleTypes.PEOPLE_ENTITY, PeopleNames.PEOPLE_TAGS);
		if (resourceService.getTagLikeResourceParent(adminSession,
				PeopleTypes.PEOPLE_MAILING_LIST) == null)
			resourceService
					.createTagLikeResourceParent(adminSession, null,
							PeopleTypes.PEOPLE_MAILING_LIST, null,
							getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
							PeopleNames.PEOPLE_MAILING_LISTS);

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