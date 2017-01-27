package org.argeo.connect.people.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

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
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
				displayName = JcrUiUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				String lastName = JcrUiUtils.get(entity, PEOPLE_LAST_NAME);
				String firstName = JcrUiUtils.get(entity, PEOPLE_FIRST_NAME);
				if (EclipseUiUtils.notEmpty(firstName) || notEmpty(lastName)) {
					displayName = lastName;
					if (notEmpty(firstName) && notEmpty(lastName))
						displayName += ", ";
					displayName += firstName;
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				// Default display is simply the legal name
				displayName = JcrUiUtils.get(entity, PEOPLE_LEGAL_NAME);
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
	public Node saveEntity(Node entity, boolean commit) throws PeopleException,
			RepositoryException {
		if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
			return savePerson(entity, commit);
		else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
			return saveOrganisation(entity, commit);
		else
			throw new PeopleException("Cannot save " + entity
					+ ", Unknown entity type");
	}

	/**
	 * Business specific save of a business object of type person. Among other,
	 * it updates cache information. Extend to provide business specific rules
	 * before save and commit
	 */
	protected Node savePerson(Node person, boolean publish)
			throws PeopleException, RepositoryException {
		String lastName = JcrUiUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		String firstName = JcrUiUtils
				.get(person, PeopleNames.PEOPLE_FIRST_NAME);
		Boolean useDistinctDName = JcrUiUtils.getBooleanValue(person,
				PEOPLE_USE_DISTINCT_DISPLAY_NAME);
		String displayName = null;
		JcrUiUtils.saveAndPublish(person, false);

		// Update display name cache if needed
		if (useDistinctDName == null || !useDistinctDName) {
			displayName = getDisplayName(person);
			person.setProperty(Property.JCR_TITLE, displayName);
		} else
			displayName = JcrUiUtils.get(person, Property.JCR_TITLE);

		// Check validity of main info
		if (isEmpty(lastName) && isEmpty(firstName) && isEmpty(displayName)) {
			String msg = "Please note that you must define a first name, a "
					+ "last name or a display name to be able to create or "
					+ "update this person.";
			throw new PeopleException(msg);
		}

		person = peopleService.checkPathAndMoveIfNeeded(person,
				PeopleTypes.PEOPLE_PERSON);
		JcrUiUtils.saveAndPublish(person, false);

		// Update cache
		peopleService.updatePrimaryCache(person);
		JcrUiUtils.saveAndPublish(person, publish);
		return person;
	}

	/** Override to provide business specific rules before save and commit */
	protected Node saveOrganisation(Node org, boolean publish)
			throws PeopleException, RepositoryException {
		// Check validity of main info
		String legalName = JcrUiUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);

		Boolean defineDistinctDefaultDisplay = JcrUiUtils.getBooleanValue(org,
				PEOPLE_USE_DISTINCT_DISPLAY_NAME);
		String displayName;
		if (defineDistinctDefaultDisplay == null
				|| !defineDistinctDefaultDisplay) {
			displayName = getDisplayName(org);
			org.setProperty(Property.JCR_TITLE, legalName);
		} else
			displayName = JcrUiUtils.get(org, Property.JCR_TITLE);

		if (isEmpty(legalName) && isEmpty(displayName)) {
			String msg = "Please note that you must define a legal "
					+ " or a display name to be able to create or "
					+ "update this organisation.";
			throw new PeopleException(msg);
		}
		org = peopleService.checkPathAndMoveIfNeeded(org,
				PeopleTypes.PEOPLE_ORG);
		// Update cache
		peopleService.updatePrimaryCache(org);
		// real save
		JcrUiUtils.saveAndPublish(org, publish);
		return org;
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
					JcrUiUtils.checkCOStatusBeforeUpdate(oldPerson);
					oldJob.remove();
					// FIXME we should not save the session anymore
					oldPerson.getSession().save();
				} else
					newJob = oldJob;
			}

			// Define the job node new name
			String orgName = JcrUiUtils.get(organisation,
					PeopleNames.PEOPLE_LEGAL_NAME);
			String orgUid = JcrUiUtils
					.get(organisation, PeopleNames.PEOPLE_UID);
			String newNodeName = null;
			if (notEmpty(orgName)) {
				newNodeName = JcrUtils.replaceInvalidChars(orgName);
				// FIXME centralize this
				if (newNodeName.indexOf("\n") > -1)
					newNodeName = newNodeName.replaceAll("(?:\n)", "");
			} else
				newNodeName = orgUid;

			JcrUiUtils.checkCOStatusBeforeUpdate(person);
			// Create node if necessary
			if (newJob == null) {
				Node parentNode = JcrUtils.mkdirs(person,
						PeopleNames.PEOPLE_JOBS, NodeType.NT_UNSTRUCTURED);
				newJob = parentNode.addNode(newNodeName.trim(),
						PeopleTypes.PEOPLE_JOB);
			} else if (!newNodeName.equals(newJob.getName())) {
				Session session = newJob.getSession();
				String srcAbsPath = newJob.getPath();
				String destAbsPath = JcrUtils.parentPath(srcAbsPath) + "/"
						+ newNodeName.trim();
				session.move(srcAbsPath, destAbsPath);
			}

			// Update properties
			// Related org
			newJob.setProperty(PeopleNames.PEOPLE_REF_UID, orgUid);

			// position
			if (isEmpty(position)
					&& newJob.hasProperty(PeopleNames.PEOPLE_ROLE))
				newJob.getProperty(PeopleNames.PEOPLE_ROLE).remove();
			else
				newJob.setProperty(PeopleNames.PEOPLE_ROLE, position);

			// department
			if (isEmpty(department)
					&& newJob.hasProperty(PeopleNames.PEOPLE_DEPARTMENT))
				newJob.getProperty(PeopleNames.PEOPLE_DEPARTMENT).remove();
			else
				newJob.setProperty(PeopleNames.PEOPLE_DEPARTMENT, department);

			// primary flag
			if (isPrimary)
				PeopleJcrUtils.markAsPrimary(peopleService, person, newJob);
			else
				newJob.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, isPrimary);
			// FIXME we should not save the session anymore
			person.getSession().save();
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create or update job "
					+ oldJob + " for person " + person + " and org "
					+ organisation, re);
		}
		return null;
	}
}