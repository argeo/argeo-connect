package org.argeo.tracker.core;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerRole;

/**
 * Default implementation of the AppMaintenanceService for the Tracker app
 */
public class TrackerMaintenanceService implements AppMaintenanceService {

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
