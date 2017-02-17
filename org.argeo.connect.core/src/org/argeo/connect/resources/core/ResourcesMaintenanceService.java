package org.argeo.connect.resources.core;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesRole;
import org.argeo.jcr.JcrUtils;

/** Default implementation of an AppMaintenanceService for the Resources app */
public class ResourcesMaintenanceService implements AppMaintenanceService {

	@Override
	public boolean prepareJcrTree(Session session) {
		try {
			boolean hasChanged = false;
			Node resourcesParent = JcrUtils.mkdirs(session, getDefaultBasePath());
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
}
