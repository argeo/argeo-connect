package org.argeo.connect.resources;

import java.net.URI;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;

/** Default implementation of an AppMaintenanceService for the Resources app */
public class ResourcesMaintenanceService implements AppMaintenanceService {

	/** Returns true if something as been updated */
	public boolean prepareJcrTree(Session session) {
		try {
			boolean hasChanged = false;
			Node resourcesParent = JcrUtils.mkdirs(session, ResourcesNames.RESOURCES_BASE_NAME);
			if (session.hasPendingChanges()) {
				JcrUtils.mkdirs(resourcesParent, ResourcesNames.RESOURCES_TAG_LIKE);
				JcrUtils.mkdirs(resourcesParent, ResourcesNames.RESOURCES_TEMPLATES);
				session.save();
				hasChanged = true;
			}
			return hasChanged;
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot create base nodes for Resources app", e);
		}
	}

	public void configurePrivileges(Session session) {
		try {
			String resourcesPath = "/" + ResourcesNames.RESOURCES_BASE_NAME;
			JcrUtils.addPrivilege(session, resourcesPath, ResourcesRole.editor.dn(), Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, resourcesPath, ResourcesRole.reader.dn(), Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot configure JCR privileges for Resources app", e);
		}
	}

	public void importResources(Session session, Map<String, URI> resources) {

	}

	public void importData(Session session, URI uri, Map<String, URI> dataSources) {

	}

	public void doBackup(Session session, URI uri, Object resource) {
	}
}
