package org.argeo.connect.people.web.demo;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.core.imports.GroupsCsvFileParser;
import org.argeo.connect.people.core.imports.OrgCsvFileParser;
import org.argeo.connect.people.core.imports.PersonCsvFileParser;
import org.argeo.connect.people.core.imports.UsersCsvFileParser;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.ArgeoTypes;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrSecurityModel;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.springframework.core.io.Resource;

/** Fills the repository in a demo context */
public class DemoDataImport implements PeopleConstants {
	private final static Log log = LogFactory.getLog(DemoDataImport.class);

	private PeopleService peopleService;

	/** Imports demo data and creates a dummy festival */
	public void doImport(PeopleService peopleService,
			UserAdminService userAdminService,
			JcrSecurityModel jcrSecurityModel, Session adminSession,
			Map<String, Resource> demoData) {
		try {
			this.peopleService = peopleService;

			// Images
			Resource dummyImageFolder = demoData.get("demoImages");

			Resource resource;

			// User & group management
			resource = demoData.get("userFile");
			// Force the import of org.argeo.util.CsvParserWithLinesAsMap
			CsvParserWithLinesAsMap parser = new UsersCsvFileParser(
					adminSession, peopleService, userAdminService,
					jcrSecurityModel);
			parser.parse(resource.getInputStream(), "UTF-8");

			createUserGroups(adminSession);

			resource = demoData.get("groupFile");
			new GroupsCsvFileParser(adminSession, peopleService).parse(
					resource.getInputStream(), "UTF-8");

			// Entities
			resource = demoData.get("orgFile");
			new OrgCsvFileParser(adminSession, peopleService, dummyImageFolder)
					.parse(resource.getInputStream(), "UTF-8");

			resource = demoData.get("personFile");
			new PersonCsvFileParser(adminSession, peopleService,
					dummyImageFolder).parse(resource.getInputStream(), "UTF-8");

			log.info("Demo data have been imported");

			ResourceService resourceService = peopleService
					.getResourceService();

			// Create tags
			Node tagParent = resourceService.createTagLikeResourceParent(
					adminSession, PeopleConstants.RESOURCE_TAG,
					PeopleTypes.PEOPLE_TAG_INSTANCE, null,
					peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
					PeopleNames.PEOPLE_TAGS);
			resourceService.refreshKnownTags(tagParent);

			// Create Mailing lists
			Node mlParent = resourceService.createTagLikeResourceParent(
					adminSession, null, PeopleTypes.PEOPLE_MAILING_LIST, null,
					peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
					PeopleNames.PEOPLE_MAILING_LISTS);
			resourceService.refreshKnownTags(mlParent);

		} catch (Exception e) {
			throw new ArgeoException("Cannot initialize backend", e);
		}
	}

	private void createUserGroups(Session adminSession)
			throws RepositoryException {

		QueryManager queryManager = adminSession.getWorkspace()
				.getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();
		Selector source = factory.selector(ArgeoTypes.ARGEO_USER_PROFILE,
				ArgeoTypes.ARGEO_USER_PROFILE);
		QueryObjectModel query = factory.createQuery(source, null, null, null);
		QueryResult result = query.execute();

		NodeIterator nit = result.getNodes();
		while (nit.hasNext()) {
			Node currProfile = nit.nextNode();
			String username = CommonsJcrUtils.get(currProfile,
					ArgeoNames.ARGEO_USER_ID);

			// TODO remove hard coded default users names
			if (!("root".equals(username) || "demo".equals(username)))
				peopleService.getUserManagementService()
						.createDefaultGroupForUser(adminSession, username);
		}
	}
}
