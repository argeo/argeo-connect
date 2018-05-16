package org.argeo.connect.resources.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.core.AbstractMaintenanceService;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesRole;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.core.imports.EncodedTagCsvFileParser;

/** Default implementation of an AppMaintenanceService for the Resources app */
public class ResourcesMaintenanceService extends AbstractMaintenanceService {
	private ResourcesService resourcesService;

	@Override
	public List<String> getRequiredRoles() {
		return enumToDns(EnumSet.allOf(ResourcesRole.class));
	}


	@Override
	public boolean prepareJcrTree(Session session) {
		try {
			boolean hasChanged = false;
			Node resourcesParent = JcrUtils.mkdirs(session, getDefaultBasePath());
			JcrUtils.mkdirs(resourcesParent, ResourcesNames.RESOURCES_TAG_LIKE);
			JcrUtils.mkdirs(resourcesParent, ResourcesNames.RESOURCES_TEMPLATES);
			addModelResources(session);
			if (session.hasPendingChanges()) {
				session.save();
				hasChanged = true;
			}
			return hasChanged;
		} catch (RepositoryException | IOException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot create base nodes for Resources app", e);
		}
	}

	// Import resources
	protected void addModelResources(Session adminSession) throws IOException {

		if (resourcesService.getTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_COUNTRY) == null) {
			String path = "/" + getClass().getPackage().getName().replace('.', '/') + "/ISO3166-1-countries.csv";
			URL url = getClass().getResource(path);
			try (InputStream in = url.openStream()) {
				resourcesService.createTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_COUNTRY,
						ResourcesTypes.RESOURCES_ENCODED_TAG, ResourcesNames.RESOURCES_TAG_CODE, "/",
						ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new ArrayList<String>());
				String EN_SHORT_NAME = "English short name (upper-lower case)";
				String ISO_CODE = "Alpha-2 code";
				new EncodedTagCsvFileParser(resourcesService, adminSession, ConnectConstants.RESOURCE_COUNTRY, ISO_CODE,
						EN_SHORT_NAME).parse(in, "UTF-8");
			}
		}

		if (resourcesService.getTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_LANG) == null) {
			String path = "/" + getClass().getPackage().getName().replace('.', '/') + "/ISO639-1-languages.csv";
			URL url = getClass().getResource(path);
			try (InputStream in = url.openStream()) {
				resourcesService.createTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_LANG,
						ResourcesTypes.RESOURCES_ENCODED_TAG, ResourcesNames.RESOURCES_TAG_CODE, "/",
						ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_UNSTRUCTURED), new ArrayList<String>());
				String EN_SHORT_NAME = "Language name";
				String ISO_CODE = "639-1";
				new EncodedTagCsvFileParser(resourcesService, adminSession, ConnectConstants.RESOURCE_LANG, ISO_CODE,
						EN_SHORT_NAME).parse(in, "UTF-8");
			}
		}

		// Create tag & mailing list parents
		if (resourcesService.getTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_TAG) == null)
			resourcesService.createTagLikeResourceParent(adminSession, ConnectConstants.RESOURCE_TAG,
					ResourcesTypes.RESOURCES_TAG, null, "/", ConnectTypes.CONNECT_TAGGABLE,
					ResourcesNames.CONNECT_TAGS);
		// if (resourcesService.getTagLikeResourceParent(adminSession,
		// PeopleTypes.PEOPLE_MAILING_LIST) == null)
		// resourcesService.createTagLikeResourceParent(adminSession, null,
		// PeopleTypes.PEOPLE_MAILING_LIST, null,
		// "/" + peopleService.getBaseRelPath(PeopleTypes.PEOPLE_ENTITY),
		// ConnectTypes.CONNECT_ENTITY,
		// PeopleNames.PEOPLE_MAILING_LISTS);
	}

	@Override
	public void configurePrivileges(Session session) {
		try {
			JcrUtils.addPrivilege(session, getDefaultBasePath(), ResourcesRole.editor.dn(), Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, getDefaultBasePath(), ResourcesRole.reader.dn(), Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot configure JCR privileges for Resources app", e);
		}
	}

	private String getDefaultBasePath() {
		return "/" + ResourcesNames.RESOURCES_BASE_NAME;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

}
