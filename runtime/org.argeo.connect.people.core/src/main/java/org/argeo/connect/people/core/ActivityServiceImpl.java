package org.argeo.connect.people.core;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;

public class ActivityServiceImpl implements ActivityService {

	/* ACTIVITIES */
	@Override
	public String getActivityParentCanonicalPath(Session session) {
		String currentUser = session.getUserID();
		Calendar currentTime = GregorianCalendar.getInstance();
		String path = PeopleConstants.PEOPLE_ACTIVITIES_BASE_PATH + "/"
				+ JcrUtils.dateAsPath(currentTime, true) + currentUser;
		return path;
	}

	@Override
	public Node createActivity(Session session, String type, String title,
			String desc, List<Node> relatedTo) {
		try {
			Node parent = JcrUtils.mkdirs(session,
					getActivityParentCanonicalPath(session));
			Node activity = parent.addNode(type, PeopleTypes.PEOPLE_ACTIVITY);
			activity.addMixin(type);

			Node userProfile = UserJcrUtils.getUserProfile(session,
					session.getUserID());
			activity.setProperty(PeopleNames.PEOPLE_MANAGER, userProfile);

			// related to
			if (relatedTo != null && !relatedTo.isEmpty())
				CommonsJcrUtils.setMultipleReferences(activity,
						PeopleNames.PEOPLE_RELATED_TO, relatedTo);

			// Content
			activity.setProperty(Property.JCR_TITLE, title);
			activity.setProperty(Property.JCR_DESCRIPTION, desc);
			JcrUtils.updateLastModified(activity);
			return activity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create activity node", e);
		}
	}

	@Override
	public Calendar getActivityRelevantDate(Node activityNode) {
		try {
			Calendar dateToDisplay = null;
			if (activityNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
				if (activityNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE))
					dateToDisplay = activityNode.getProperty(
							PeopleNames.PEOPLE_CLOSE_DATE).getDate();
				else if (activityNode.hasProperty(PeopleNames.PEOPLE_DUE_DATE))
					dateToDisplay = activityNode.getProperty(
							PeopleNames.PEOPLE_DUE_DATE).getDate();
				else
					dateToDisplay = activityNode.getProperty(
							Property.JCR_CREATED).getDate();
			} else if (activityNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
				dateToDisplay = activityNode.getProperty(Property.JCR_CREATED)
						.getDate();
			}
			return dateToDisplay;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get relevant date for activity " + activityNode,
					re);
		}
	}

	// TODO refactor this in the UserManagementService
	private Node getMyPeopleProfile(Session session) throws RepositoryException {
		String currentUserId = session.getUserID();
		Node argeoProfile = UserJcrUtils.getUserProfile(session, currentUserId);
		if (argeoProfile == null)
			return null;

		Node parent = argeoProfile.getParent();
		if (parent.hasNode(PeopleTypes.PEOPLE_PROFILE))
			return parent.getNode(PeopleTypes.PEOPLE_PROFILE);
		else
			// FIXME should we create it ?
			return null;
	}

	// @ Override
	public List<Node> getMyTasks(Session session, boolean onlyOpenTasks) {
		try {
			Node myProfile = getMyPeopleProfile(session);
			return getTasksForUser(myProfile, onlyOpenTasks);
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get my tasks", re);
		}
	}

	public List<Node> getTasksForUser(Node peopleProfile, boolean onlyOpenTasks) {
		return null;
	}

	// TODO refactor this in the group / user management service
	// private Node getUserById(Session session, String userId)
	// throws RepositoryException {
	// String currentUser = session.getUserID();
	//
	// String path = "/argeo:system/argeo:people/"
	// + JcrUtils.firstCharsToPath(currentUser, 2) + "/" + currentUser
	// + "/argeo:profile";
	// Node userProfile = null;
	// userProfile = session.getNode(path);
	// return userProfile;
	// }

	/* TASKS */
	@Override
	public Node createTask(Session session, Node parentNode, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar wakeUpDate) {
		try {

			if (session == null && parentNode == null)
				throw new PeopleException(
						"Define either a session or a parent node. "
								+ "Both cannot be null at the same time.");

			if (session == null)
				session = parentNode.getSession();

			if (parentNode == null)
				parentNode = JcrUtils.mkdirs(session,
						getActivityParentCanonicalPath(session));

			Node taskNode = parentNode.addNode(PeopleTypes.PEOPLE_TASK,
					PeopleTypes.PEOPLE_TASK);

			if (CommonsJcrUtils.checkNotEmptyString(title))
				taskNode.setProperty(Property.JCR_TITLE, title);

			if (CommonsJcrUtils.checkNotEmptyString(description))
				taskNode.setProperty(Property.JCR_DESCRIPTION, description);

			Node userProfile = UserJcrUtils.getUserProfile(session,
					session.getUserID());
			taskNode.setProperty(PeopleNames.PEOPLE_MANAGER, userProfile);

			if (assignedTo != null)
				taskNode.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO, assignedTo);

			if (relatedTo != null && !relatedTo.isEmpty())
				CommonsJcrUtils.setMultipleReferences(taskNode,
						PeopleNames.PEOPLE_RELATED_TO, relatedTo);

			if (dueDate != null) {
				taskNode.setProperty(PeopleNames.PEOPLE_DUE_DATE, dueDate);
			}
			if (wakeUpDate != null) {
				taskNode.setProperty(PeopleNames.PEOPLE_WAKE_UP_DATE,
						wakeUpDate);
			}
			return taskNode;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create the new task " + title,
					e);
		}
	}

}
