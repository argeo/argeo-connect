package org.argeo.people.core;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleConstants;
import org.argeo.people.PeopleRole;

/**
 * Default implementation of the AppMaintenanceService for the People App
 */
public class PeopleMaintenanceService implements AppMaintenanceService {

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
			JcrUtils.addPrivilege(session, getDefaultBasePath(), PeopleRole.editor.dn(), Privilege.JCR_ALL);
			JcrUtils.addPrivilege(session, getDefaultBasePath(), PeopleRole.reader.dn(), Privilege.JCR_READ);
			session.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ConnectException("Cannot configure JCR privileges for Resources app", e);
		}
	}

	private String getDefaultBasePath() {
		return "/" + PeopleConstants.PEOPLE_APP_BASE_NAME;
	}

	// legacy. kept for the pattern
	// private String pathToRepository = System.getProperty("user.dir");
	//
	// private String getMonitoringLogFolderPath(){
	// return pathToRepository + "/log/monitoring";
	// }

	// protected InputStream getStreamFromUrl(String url) throws IOException {
	// InputStream inputStream = null;
	// if (url.startsWith("classpath:")) {
	// url = url.substring("classpath:".length());
	// Resource resultbasepath = new ClassPathResource(url);
	// if (resultbasepath.exists())
	// inputStream = resultbasepath.getInputStream();
	// } else if (url.startsWith("file:")) {
	// url = url.substring("file:".length());
	// File file = new File(url);
	// // String tmpPath = file.getAbsolutePath();
	// if (file.exists())
	// inputStream = new FileInputStream(url);
	// }
	// return inputStream;
	// }

	// public long publishAll(Session session, JcrMonitor monitor) {
	// Query query;
	// long nodeNb = 0;
	// try {
	// query = session.getWorkspace().getQueryManager().createQuery("SELECT *
	// FROM [" + NodeType.MIX_VERSIONABLE
	// + "] ORDER BY [" + Property.JCR_LAST_MODIFIED + "] DESC ",
	// Query.JCR_SQL2);
	// if (monitor != null && !monitor.isCanceled())
	// monitor.beginTask("Gathering versionnable items", -1);
	// NodeIterator nit = query.execute().getNodes();
	//
	// if (nit.hasNext() && monitor != null && !monitor.isCanceled()) {
	// nodeNb = nit.getSize();
	// int shortNb = (int) nodeNb / 100;
	// monitor.beginTask("Committing " + nodeNb + " nodes", shortNb);
	//
	// }
	// long i = 0;
	// VersionManager vm = session.getWorkspace().getVersionManager();
	// while (nit.hasNext()) {
	// String currPath = nit.nextNode().getPath();
	// vm.checkpoint(currPath);
	// if (i % 100 == 0 && monitor != null && !monitor.isCanceled())
	// monitor.worked(1);
	// i++;
	// }
	// return nodeNb;
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to publish the workspace for " +
	// session, e);
	// }
	//
	// }
}
