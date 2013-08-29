package org.argeo.connect.people.core;

import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/** Concrete access to people services */
public class PeopleServiceImpl implements PeopleService {

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Map<Integer, String> managedRoles;

	private Session adminSession = null;

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		// Do nothing
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		// Do nothing
	}

	/* Persons */

	/* USER MANAGEMENT */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(Integer userRole) {
		String role = managedRoles.get(userRole);
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		for (GrantedAuthority ga : authen.getAuthorities()) {
			if (ga.getAuthority().equals(role))
				return true;
		}
		return false;
		// return currentUserService.getCurrentUser().getRoles().contains(role);
	}

	/** returns the current user ID **/
	public String getCurrentUserId() {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		return authen.getName();
	}

	/** Returns a human readable display name using the user ID **/
	public String getUserDisplayName(String userId) {
		// FIXME Must use a commons utils
		return userId;
	}

	/** Expose injected repository */
	public Repository getRepository() {
		return repository;
	}

	/* DEPENDENCY INJECTION */
	public void setManagedRoles(Map<Integer, String> managedRoles) {
		this.managedRoles = managedRoles;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
