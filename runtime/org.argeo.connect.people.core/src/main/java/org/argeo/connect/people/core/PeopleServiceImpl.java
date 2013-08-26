package org.argeo.connect.people.core;

import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;

/** Concrete access to the backend */
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
