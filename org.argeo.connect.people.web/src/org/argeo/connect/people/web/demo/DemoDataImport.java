package org.argeo.connect.people.web.demo;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.core.imports.OrgCsvFileParser;
import org.argeo.connect.people.core.imports.PersonCsvFileParser;
import org.argeo.jcr.JcrUtils;
import org.springframework.core.io.Resource;

/** Fill the repository in a demo context */
public class DemoDataImport implements PeopleConstants {
	private final static Log log = LogFactory.getLog(DemoDataImport.class);

	/** Imports demo data */
	public void doImport(PeopleService peopleService, Session adminSession,
			Map<String, Resource> demoData) {
		try {
			// this.peopleService = peopleService;

			// Images
			Resource dummyImageFolder = demoData.get("demoImages");

			Resource resource;

			// // User & group management
			// resource = demoData.get("userFile");
			// // Force the import of org.argeo.util.CsvParserWithLinesAsMap
			// CsvParserWithLinesAsMap parser = new UsersCsvFileParser(
			// adminSession, peopleService, userAdminService);
			// parser.parse(resource.getInputStream(), "UTF-8");
			//
			// createUserGroups(adminSession);
			//
			// resource = demoData.get("groupFile");
			// new GroupsCsvFileParser(adminSession, peopleService).parse(
			// resource.getInputStream(), "UTF-8");

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

			// Initialises queries
			createSearch(adminSession);

		} catch (Exception e) {
			throw new PeopleException("Cannot initialize backend", e);
		}
	}

	/** Saves the underlying passed session **/
	private void createSearch(Session adminSession) throws RepositoryException {
		String SEARCH_BASE_PATH = "/search";
		Node queryParentNode = JcrUtils.mkdirs(adminSession, SEARCH_BASE_PATH);

		createQueryNodeForType(queryParentNode, PeopleTypes.PEOPLE_ENTITY,
				"all");
		createQueryNodeForType(queryParentNode, PeopleTypes.PEOPLE_PERSON,
				"persons");
		createQueryNodeForType(queryParentNode, PeopleTypes.PEOPLE_ORG, "orgs");
		// save queries
		adminSession.save();
	}

	private void createQueryNodeForType(Node queryParentNode, String NodeType,
			String nodeName) throws RepositoryException {
		Query query = queryParentNode
				.getSession()
				.getWorkspace()
				.getQueryManager()
				.createQuery("SELECT * FROM [" + NodeType + "]", Query.JCR_SQL2);
		query.storeAsNode(queryParentNode.getPath() + "/" + nodeName);

	}

	// private void createUserGroups(Session adminSession)
	// throws RepositoryException {
	//
	// QueryManager queryManager = adminSession.getWorkspace()
	// .getQueryManager();
	// QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// Selector source = factory.selector(ArgeoTypes.ARGEO_USER_PROFILE,
	// ArgeoTypes.ARGEO_USER_PROFILE);
	// QueryObjectModel query = factory.createQuery(source, null, null, null);
	// QueryResult result = query.execute();
	//
	// NodeIterator nit = result.getNodes();
	// while (nit.hasNext()) {
	// Node currProfile = nit.nextNode();
	// String username = CommonsJcrUtils.get(currProfile,
	// ArgeoNames.ARGEO_USER_ID);
	//
	// // TODO remove hard coded default users names
	// if (!("root".equals(username) || "demo".equals(username)))
	// peopleService.getUserManagementService()
	// .createDefaultGroupForUser(adminSession, username);
	// }
	// }
}
