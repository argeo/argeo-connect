package org.argeo.connect.core;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemMaintenanceService;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;

/** Make the DJay-ing to provide a full running Suite platform */
public class DynamicSystemMaintenanceService implements SystemMaintenanceService {
	private final static Log log = LogFactory.getLog(DynamicSystemMaintenanceService.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private String workspaceName = "main";
	private List<AppMaintenanceService> maintenanceServices = Collections.synchronizedList(new ArrayList<>());

	public void init() {
		Session adminSession = openAdminSession();
		try {
			// adminSession = repository.login(workspaceName);
			if (prepareJcrTree(adminSession)) {
				configurePrivileges(adminSession);
			}
		} catch (Exception e) {
			throw new ConnectException("Cannot initialise model", e);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	protected Session openAdminSession() {
		try {
			LoginContext lc = new LoginContext(NodeConstants.LOGIN_CONTEXT_DATA_ADMIN);
			lc.login();
			Session session = Subject.doAs(lc.getSubject(), new PrivilegedExceptionAction<Session>() {

				@Override
				public Session run() throws Exception {
					return repository.login(workspaceName);
				}

			});
			return session;
		} catch (Exception e) {
			throw new ConnectException("Cannot login as data admin", e);
		}
	}

	// To be cleaned once first init and config mechanisms have been implemented
	// private final static String publicPath = "/public";
	// FIXME Users must have read access on the jcr:system/jcr:versionStorage
	// node under JackRabbit to be able to manage versions
	private final static String jackRabbitVersionSystemPath = "/jcr:system";

	@Override
	public boolean prepareJcrTree(Session session) {
		boolean hasCHanged = false;
		try {
			// JcrUtils.mkdirs(session, publicPath, NodeType.NT_UNSTRUCTURED);
			if (session.hasPendingChanges()) {
				session.save();
				hasCHanged = true;
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			hasCHanged |= service.prepareJcrTree(session);
		if (hasCHanged)
			log.info("Repository has been initialised with Argeo Suite model");
		return hasCHanged;
	}

	@Override
	public void configurePrivileges(Session session) {
		try {
			// Remove unused default JCR rights
			JcrUtils.clearAccessControList(session, "/", "everyone");

			JcrUtils.addPrivilege(session, jackRabbitVersionSystemPath, OfficeRole.coworker.dn(), Privilege.JCR_READ);
			// Default configuration of the workspace
			JcrUtils.addPrivilege(session, "/", NodeConstants.ROLE_ADMIN, Privilege.JCR_ALL);
			// JcrUtils.addPrivilege(session, publicPath,
			// NodeConstants.ROLE_USER, Privilege.JCR_READ);
			// JcrUtils.addPrivilege(session, publicPath, "anonymous",
			// Privilege.JCR_READ);
			// JcrUtils.addPrivilege(session, publicPath,
			// NodeConstants.ROLE_ANONYMOUS, Privilege.JCR_READ);

			session.save();
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot build model", e);
		}
		for (AppMaintenanceService service : maintenanceServices)
			service.configurePrivileges(session);
		log.info("Access control configured");
	}

	public void destroy() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public void addAppService(AppMaintenanceService appService, Map<String, String> properties) {
		maintenanceServices.add(appService);
		Session adminSession = openAdminSession();
		try {
			if (appService.prepareJcrTree(adminSession)) {
				appService.configurePrivileges(adminSession);
			}
			log.debug("Added service " + appService);
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	public void removeAppService(AppMaintenanceService appService, Map<String, String> properties) {
		maintenanceServices.remove(appService);
	}

}
