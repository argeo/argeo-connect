package org.argeo.connect.people;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Provides method interfaces to manage persons and organisations in a people
 * repository. Implementing applications should extend and/or override the
 * canonical implementation in order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface PersonService {

	/** Simply returns the display name of the given person or organisation */
	public String getDisplayName(Node person);
	
	/**
	 * Try to save and optionally commit a person or an organisationbusiness after applying
	 * context specific rules and special behaviours (typically cache updates).
	 * 
	 * @param entity
	 * @param commit
	 *            also commit the corresponding object
	 * @throws PeopleException
	 *             If one a the rule defined for this type is not respected. Use
	 *             getMessage to display to the user if needed
	 */
	public void saveEntity(Node entity, boolean commit) throws PeopleException, RepositoryException;
	
	/**
	 * 
	 * Creates or update a job of a person in an organisation
	 * 
	 * @param oldJob
	 *            null if creation
	 * @param person
	 *            cannot be null
	 * @param organisation
	 *            cannot be null
	 * @param position
	 *            can be null
	 * @param department
	 *            can be null
	 * @param isPrimary
	 *            pass false by default
	 * @return
	 */
	public Node createOrUpdateJob(Node oldJob, Node person, Node organisation,
			String position, String department, boolean isPrimary);

}
