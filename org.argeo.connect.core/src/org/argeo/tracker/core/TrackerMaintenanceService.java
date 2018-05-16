package org.argeo.tracker.core;

import java.util.EnumSet;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.activities.ActivitiesRole;
import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.core.AbstractMaintenanceService;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerRole;

/**
 * Default implementation of the AppMaintenanceService for the Tracker app
 */
public class TrackerMaintenanceService extends AbstractMaintenanceService {
	@Override
	public List<String> getRequiredRoles() {
		return enumToDns(EnumSet.allOf(TrackerRole.class));
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
			JcrUtils.addPrivilege(session, getDefaultBasePath(), TrackerRole.editor.dn(), Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, getDefaultBasePath(), TrackerRole.reader.dn(), Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot configure JCR privileges for Resources app", e);
		}
	}

	private String getDefaultBasePath() {
		return "/" + TrackerNames.TRACKER_PROJECTS;
	}
}
