package org.argeo.connect.core;

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeConstants;
import org.argeo.cms.CmsUserManager;
import org.argeo.connect.AppMaintenanceService;
import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;
import org.argeo.naming.Distinguished;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

/** base class for the app maintenance services. */
public abstract class AbstractMaintenanceService implements AppMaintenanceService {
	private final static Log log = LogFactory.getLog(AbstractMaintenanceService.class);

	private Repository repository;
	private CmsUserManager cmsUserManager;

	public void init() {
		makeSureRolesExists(EnumSet.allOf(OfficeRole.class));
		addManagersToGroup(OfficeRole.coworker.dn());
		makeSureRolesExists(getRequiredRoles());
		addOfficeGroups();

		Session adminSession = openAdminSession();
		try {
			if (prepareJcrTree(adminSession)) {
				configurePrivileges(adminSession);
			}
		} finally {
			JcrUtils.logoutQuietly(adminSession);
		}
	}

	protected void addOfficeGroups() {

	}

	public void destroy() {

	}

	private Session openAdminSession() {
		try {
			LoginContext lc = new LoginContext(NodeConstants.LOGIN_CONTEXT_DATA_ADMIN);
			lc.login();
			Session session = Subject.doAs(lc.getSubject(), new PrivilegedExceptionAction<Session>() {

				@Override
				public Session run() throws Exception {
					return repository.login();
				}

			});
			return session;
		} catch (Exception e) {
			throw new ConnectException("Cannot login as data admin", e);
		}
	}

	protected static List<String> enumToDns(EnumSet<? extends Distinguished> enumSet) {
		List<String> res = new ArrayList<>();
		for (Enum<? extends Distinguished> enm : enumSet) {
			res.add(((Distinguished) enm).dn());
		}
		return res;
	}

	protected void makeSureRolesExists(EnumSet<? extends Distinguished> enumSet) {
		makeSureRolesExists(enumToDns(enumSet));
	}

	@SuppressWarnings("deprecation")
	protected void makeSureRolesExists(List<String> requiredRoles) {
		if (requiredRoles == null)
			return;
		if (cmsUserManager == null) {
			log.warn("No user admin service available, cannot make sure that role exists");
			return;
		}
		for (String role : requiredRoles) {
			Role systemRole = cmsUserManager.getUserAdmin().getRole(role);
			if (systemRole == null) {
				try {
					cmsUserManager.getUserTransaction().begin();
					cmsUserManager.getUserAdmin().createRole(role, Role.GROUP);
					cmsUserManager.getUserTransaction().commit();
					log.info("Created role " + role);
				} catch (Exception e) {
					try {
						cmsUserManager.getUserTransaction().rollback();
					} catch (Exception e1) {
						// silent
					}
					throw new ConnectException("Cannot create role " + role, e);
				}
			}
		}
	}

	protected void addManagersToGroup(String groupDn) {
		addToGroup(OfficeRole.manager.dn(), groupDn);
	}

	protected void addCoworkersToGroup(String groupDn) {
		addToGroup(OfficeRole.coworker.dn(), groupDn);
	}

	@SuppressWarnings("deprecation")
	private void addToGroup(String officeGroup, String groupDn) {
		if (cmsUserManager == null) {
			log.warn("No user admin service available, cannot add group " + officeGroup + " to " + groupDn);
			return;
		}
		Group managerGroup = (Group) cmsUserManager.getUserAdmin().getRole(officeGroup);
		Group group = (Group) cmsUserManager.getUserAdmin().getRole(groupDn);
		if (group == null)
			throw new ConnectException("Group " + groupDn + " not found");
		try {
			cmsUserManager.getUserTransaction().begin();
			if (group.addMember(managerGroup))
				log.info("Added " + officeGroup + " to " + group);
			cmsUserManager.getUserTransaction().commit();
		} catch (Exception e) {
			try {
				cmsUserManager.getUserTransaction().rollback();
			} catch (Exception e1) {
				// silent
			}
			throw new ConnectException("Cannot add " + managerGroup + " to " + group);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setCmsUserManager(CmsUserManager cmsUserManager) {
		this.cmsUserManager = cmsUserManager;
	}

}
