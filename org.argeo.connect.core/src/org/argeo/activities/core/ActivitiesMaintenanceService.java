package org.argeo.activities.core;

import java.util.EnumSet;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesRole;
import org.argeo.connect.ConnectException;
import org.argeo.connect.core.AbstractMaintenanceService;
import org.argeo.jcr.JcrUtils;

/**
 * Default implementation of the AppMaintenanceService for the Activities app
 */
public class ActivitiesMaintenanceService extends AbstractMaintenanceService {

	@Override
	public List<String> getRequiredRoles() {
		return enumToDns(EnumSet.allOf(ActivitiesRole.class));
	}

	@Override
	protected void addOfficeGroups() {
		addManagersToGroup(ActivitiesRole.editor.dn());
		addCoworkersToGroup(ActivitiesRole.reader.dn());
	}

	@Override
	public boolean prepareJcrTree(Session session) {
		try {
			boolean hasChanged = false;
			JcrUtils.mkdirs(session, getDefaultBasePath());
			if (session.hasPendingChanges()) {
				session.save();
				hasChanged = true;
			}
			return hasChanged;
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot create base nodes for Activities app", e);
		}
	}

	@Override
	public void configurePrivileges(Session session) {
		try {
			JcrUtils.addPrivilege(session, getDefaultBasePath(), ActivitiesRole.editor.dn(), Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, getDefaultBasePath(), ActivitiesRole.reader.dn(), Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot configure JCR privileges for Resources app", e);
		}
	}

	public String getDefaultBasePath() {
		return "/" + ActivitiesNames.ACTIVITIES_APP_BASE_NAME;
	}
}
