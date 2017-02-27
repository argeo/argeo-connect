package org.argeo.connect.people.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.ConnectNames;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PersonService;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to people {@link PersonService} */
public class PersonServiceImpl implements PersonService, PeopleNames {

	private final PeopleService peopleService;
	private final ResourcesService resourceService;

	public PersonServiceImpl(PeopleService peopleService, ResourcesService resourceService) {
		this.peopleService = peopleService;
		this.resourceService = resourceService;
	}

	@Override
	public String getDisplayName(Node entity) {
		String displayName = null;
		try {
			if (entity.hasProperty(PEOPLE_DISPLAY_NAME))
				displayName = entity.getProperty(PEOPLE_DISPLAY_NAME).getString();
			else if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				String lastName = ConnectJcrUtils.get(entity, PEOPLE_LAST_NAME);
				String firstName = ConnectJcrUtils.get(entity, PEOPLE_FIRST_NAME);
				if (EclipseUiUtils.notEmpty(firstName) || notEmpty(lastName)) {
					displayName = lastName;
					if (notEmpty(firstName) && notEmpty(lastName))
						displayName += ", ";
					displayName += firstName;
				}
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				// Default display is simply the legal name
				displayName = ConnectJcrUtils.get(entity, PEOPLE_LEGAL_NAME);
			} else
				throw new PeopleException("Display name not defined for type " + entity.getPrimaryNodeType().getName()
						+ " - node: " + entity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get display name for node " + entity, e);
		}
		return displayName;
	}

	@Override
	public Node saveEntity(Node entity, boolean commit) throws PeopleException, RepositoryException {
		if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
			return savePerson(entity, commit);
		else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
			return saveOrganisation(entity, commit);
		else
			throw new PeopleException("Cannot save " + entity + ", Unknown entity type");
	}

	/**
	 * Business specific save of a business object of type person. Among other,
	 * it updates cache information. Extend to provide business specific rules
	 * before save and commit
	 */
	protected Node savePerson(Node person, boolean publish) throws PeopleException, RepositoryException {
		String lastName = ConnectJcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		String firstName = ConnectJcrUtils.get(person, PeopleNames.PEOPLE_FIRST_NAME);
		String displayName = ConnectJcrUtils.get(person, PEOPLE_DISPLAY_NAME);

		// Check validity of main info
		if (isEmpty(lastName) && isEmpty(firstName) && isEmpty(displayName)) {
			String msg = "Please note that you must define a first name, a "
					+ "last name or a display name to be able to create or " + "update this person.";
			throw new PeopleException(msg);
		}

		// Update display name cache if needed
		if (EclipseUiUtils.isEmpty(displayName))
			displayName = getDisplayName(person);

		person.setProperty(Property.JCR_TITLE, displayName);

		// person = peopleService.checkPathAndMoveIfNeeded(person,
		// PeopleTypes.PEOPLE_PERSON);
		// ConnectJcrUtils.saveAndPublish(person, false);

		// Update cache
		peopleService.updatePrimaryCache(person);
		ConnectJcrUtils.saveAndPublish(person, publish);
		return person;
	}

	/** Override to provide business specific rules before save and commit */
	protected Node saveOrganisation(Node org, boolean publish) throws PeopleException, RepositoryException {
		// Check validity of main info
		String legalName = ConnectJcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		String displayName = ConnectJcrUtils.get(org, PEOPLE_DISPLAY_NAME);

		// Check validity of main info
		if (isEmpty(legalName) && isEmpty(displayName)) {
			String msg = "Please note that you must define a legal or a display name "
					+ "to be able to create or update this organisation.";
			throw new PeopleException(msg);
		}

		// Update display name cache if needed
		if (EclipseUiUtils.isEmpty(displayName))
			displayName = getDisplayName(org);

		org.setProperty(Property.JCR_TITLE, displayName);

		// org = peopleService.checkPathAndMoveIfNeeded(org,
		// PeopleTypes.PEOPLE_ORG);
		// Update cache
		peopleService.updatePrimaryCache(org);
		// real save
		ConnectJcrUtils.saveAndPublish(org, publish);
		return org;
	}

	@Override
	public Node createOrUpdateJob(Node oldJob, Node person, Node organisation, String position, String department,
			boolean isPrimary) {

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
					ConnectJcrUtils.checkCOStatusBeforeUpdate(oldPerson);
					oldJob.remove();
					// FIXME we should not save the session anymore
					oldPerson.getSession().save();
				} else
					newJob = oldJob;
			}

			// Define the job node new name
			String orgName = ConnectJcrUtils.get(organisation, PeopleNames.PEOPLE_LEGAL_NAME);
			String orgUid = ConnectJcrUtils.get(organisation, ConnectNames.CONNECT_UID);
			String newNodeName = null;
			if (notEmpty(orgName)) {
				newNodeName = JcrUtils.replaceInvalidChars(orgName);
				// FIXME centralize this
				if (newNodeName.indexOf("\n") > -1)
					newNodeName = newNodeName.replaceAll("(?:\n)", "");
			} else
				newNodeName = orgUid;

			ConnectJcrUtils.checkCOStatusBeforeUpdate(person);
			// Create node if necessary
			if (newJob == null) {
				Node parentNode = JcrUtils.mkdirs(person, PeopleNames.PEOPLE_JOBS, NodeType.NT_UNSTRUCTURED);
				newJob = parentNode.addNode(newNodeName.trim());
				newJob.addMixin(PeopleTypes.PEOPLE_JOB);
			} else if (!newNodeName.equals(newJob.getName())) {
				Session session = newJob.getSession();
				String srcAbsPath = newJob.getPath();
				String destAbsPath = JcrUtils.parentPath(srcAbsPath) + "/" + newNodeName.trim();
				session.move(srcAbsPath, destAbsPath);
			}

			// Update properties
			// Related org
			newJob.setProperty(PeopleNames.PEOPLE_REF_UID, orgUid);

			// position
			if (isEmpty(position) && newJob.hasProperty(PeopleNames.PEOPLE_ROLE))
				newJob.getProperty(PeopleNames.PEOPLE_ROLE).remove();
			else
				newJob.setProperty(PeopleNames.PEOPLE_ROLE, position);

			// department
			if (isEmpty(department) && newJob.hasProperty(PeopleNames.PEOPLE_DEPARTMENT))
				newJob.getProperty(PeopleNames.PEOPLE_DEPARTMENT).remove();
			else
				newJob.setProperty(PeopleNames.PEOPLE_DEPARTMENT, department);

			// primary flag
			if (isPrimary)
				PeopleJcrUtils.markAsPrimary(resourceService, peopleService, person, newJob);
			else
				newJob.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, isPrimary);
			// FIXME we should not save the session anymore
			person.getSession().save();
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to create or update job " + oldJob + " for person " + person + " and org " + organisation,
					re);
		}
		return null;
	}
}