package org.argeo.connect.people.core;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PersonService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people {@link PersonService} */
public class PersonServiceImpl implements PersonService, PeopleNames {

	private PeopleService peopleService;

	public PersonServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public String getDisplayName(Node entity) {
		String displayName = null;
		try {
			boolean defineDistinct = false;
			if (entity.hasProperty(PEOPLE_USE_DISTINCT_DISPLAY_NAME))
				defineDistinct = entity.getProperty(
						PEOPLE_USE_DISTINCT_DISPLAY_NAME).getBoolean();
			if (defineDistinct)
				displayName = CommonsJcrUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				String lastName = CommonsJcrUtils.get(entity, PEOPLE_LAST_NAME);
				String firstName = CommonsJcrUtils.get(entity,
						PEOPLE_FIRST_NAME);
				if (CommonsJcrUtils.checkNotEmptyString(firstName)
						|| CommonsJcrUtils.checkNotEmptyString(lastName)) {
					displayName = lastName;
					if (CommonsJcrUtils.checkNotEmptyString(firstName)
							&& CommonsJcrUtils.checkNotEmptyString(lastName))
						displayName += ", ";
					displayName += firstName;
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				// Default display is simply the legal name
				displayName = CommonsJcrUtils.get(entity, PEOPLE_LEGAL_NAME);
			} else
				throw new PeopleException("Display name not defined for type "
						+ entity.getPrimaryNodeType().getName() + " - node: "
						+ entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get display name for node "
					+ entity, e);
		}
		return displayName;
	}

	@Override
	public void saveEntity(Node entity, boolean commit) throws PeopleException,
			RepositoryException {
		if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
			savePerson(entity, commit);
		else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
			saveOrganisation(entity, commit);

	}

	/**
	 * Business specific save of a business object of type person. Among other,
	 * it updates cache information. Extend to provide business specific rules
	 * before save and commit
	 */
	protected void savePerson(Node person, boolean commit)
			throws PeopleException, RepositoryException {
		String lastName = CommonsJcrUtils.get(person,
				PeopleNames.PEOPLE_LAST_NAME);
		String firstName = CommonsJcrUtils.get(person,
				PeopleNames.PEOPLE_FIRST_NAME);
		Boolean useDistinctDName = CommonsJcrUtils.getBooleanValue(person,
				PEOPLE_USE_DISTINCT_DISPLAY_NAME);
		String displayName = null;

		// Update display name cache if needed
		if (useDistinctDName == null || !useDistinctDName) {
			displayName = getDisplayName(person);
			person.setProperty(Property.JCR_TITLE, displayName);
		} else
			displayName = CommonsJcrUtils.get(person, Property.JCR_TITLE);

		// Check validity of main info
		if (CommonsJcrUtils.isEmptyString(lastName)
				&& CommonsJcrUtils.isEmptyString(firstName)
				&& CommonsJcrUtils.isEmptyString(displayName)) {
			String msg = "Please note that you must define a first name, a "
					+ "last name or a display name to be able to create or "
					+ "update this person.";
			throw new PeopleException(msg);
		}

		// Update cache
		peopleService.updatePrimaryCache(person);

		peopleService.checkPathAndMoveIfNeeded(person,
				PeopleTypes.PEOPLE_PERSON);

		if (commit)
			CommonsJcrUtils.saveAndCheckin(person);
		else
			person.getSession().save();
	}

	/** Override to provide business specific rules before save and commit */
	protected void saveOrganisation(Node org, boolean commit)
			throws PeopleException, RepositoryException {
		// Update cache
		peopleService.updatePrimaryCache(org);

		// Check validity of main info
		String legalName = CommonsJcrUtils.get(org,
				PeopleNames.PEOPLE_LEGAL_NAME);

		Boolean defineDistinctDefaultDisplay = CommonsJcrUtils.getBooleanValue(
				org, PEOPLE_USE_DISTINCT_DISPLAY_NAME);
		String displayName;
		if (defineDistinctDefaultDisplay == null
				|| !defineDistinctDefaultDisplay) {
			displayName = getDisplayName(org);
			org.setProperty(Property.JCR_TITLE, legalName);
		} else
			displayName = CommonsJcrUtils.get(org, Property.JCR_TITLE);

		if (CommonsJcrUtils.isEmptyString(legalName)
				&& CommonsJcrUtils.isEmptyString(displayName)) {
			String msg = "Please note that you must define a legal "
					+ " or a display name to be able to create or "
					+ "update this organisation.";
			throw new PeopleException(msg);
		}

		// Finalise save process.
		peopleService.checkPathAndMoveIfNeeded(org, PeopleTypes.PEOPLE_ORG);
		if (commit)
			CommonsJcrUtils.saveAndCheckin(org);
		else
			org.getSession().save();
	}

	@Override
	public Node createOrUpdateJob(Node oldJob, Node person, Node organisation,
			String position, String department, boolean isPrimary) {

		// The job on which to update various info
		Node newJob = null;

		try {
			// First check if we must remove the old job when linked person has
			// changed
			if (oldJob != null) {
				Node oldPerson = oldJob.getParent().getParent();
				String oldPath = oldPerson.getPath();
				String newPath = person.getPath();
				if (!newPath.equals(oldPath)) {
					// remove old
					boolean wasCO = CommonsJcrUtils
							.checkCOStatusBeforeUpdate(oldPerson);
					oldJob.remove();
					CommonsJcrUtils.checkCOStatusAfterUpdate(oldPerson, wasCO);
				} else
					newJob = oldJob;
			}

			// Define the job node new name
			String orgName = CommonsJcrUtils.get(organisation,
					PeopleNames.PEOPLE_LEGAL_NAME);
			String orgUid = CommonsJcrUtils.get(organisation,
					PeopleNames.PEOPLE_UID);
			String newNodeName = null;
			if (CommonsJcrUtils.checkNotEmptyString(orgName))
				newNodeName = JcrUtils.replaceInvalidChars(newNodeName);
			else
				newNodeName = orgUid;

			boolean wasCO = CommonsJcrUtils.checkCOStatusBeforeUpdate(person);
			// Create node if necessary
			if (newJob == null) {
				Node parentNode = JcrUtils.mkdirs(person,
						PeopleNames.PEOPLE_JOBS, NodeType.NT_UNSTRUCTURED);
				newJob = parentNode
						.addNode(newNodeName, PeopleTypes.PEOPLE_JOB);
			} else if (!newNodeName.equals(newJob.getName())) {
				Session session = newJob.getSession();
				String srcAbsPath = newJob.getPath();
				String destAbsPath = JcrUtils.parentPath(srcAbsPath) + "/"+ newNodeName;
				session.move(srcAbsPath, destAbsPath);
			}

			// Update properties
			// Related org
			newJob.setProperty(PeopleNames.PEOPLE_REF_UID, orgUid);

			// position
			if (CommonsJcrUtils.isEmptyString(position) && newJob.hasProperty(PeopleNames.PEOPLE_ROLE))
				newJob.getProperty(PeopleNames.PEOPLE_ROLE).remove();
			else 
				newJob.setProperty(PeopleNames.PEOPLE_ROLE, position);
			
			// department
			if (CommonsJcrUtils.isEmptyString(department) && newJob.hasProperty(PeopleNames.PEOPLE_DEPARTMENT))
				newJob.getProperty(PeopleNames.PEOPLE_DEPARTMENT).remove();
			else 
				newJob.setProperty(PeopleNames.PEOPLE_DEPARTMENT, department);

			// primary flag
			if (isPrimary)
				PeopleJcrUtils.markAsPrimary(peopleService, person, newJob);
			else
				newJob.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, isPrimary);

			CommonsJcrUtils.checkCOStatusAfterUpdate(person, wasCO);
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create or update job "
					+ oldJob + " for person " + person + " and org "
					+ organisation, re);
		}
		return null;
	}
}