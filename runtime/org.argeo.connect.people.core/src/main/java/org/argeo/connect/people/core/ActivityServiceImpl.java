package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;

public class ActivityServiceImpl implements ActivityService, PeopleNames {
	// private final static Log log =
	// LogFactory.getLog(ActivityServiceImpl.class);

	// Keeps a local reference to the parent people service,
	// Among other to rely on its base path policies.
	private final PeopleService peopleService;

	/* DEPENDENCY INJECTION */
	private UserManagementService userManagementService;

	/**
	 * Default constructor, caller must then inject a relevant
	 * {@link userManagementService}
	 */
	public ActivityServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	/**
	 * Shortcut to create an activity service directly with a user management
	 * service
	 * 
	 * @param userManagementService
	 */
	public ActivityServiceImpl(PeopleService peopleService,
			UserManagementService userManagementService) {
		this.peopleService = peopleService;
		setUserManagementService(userManagementService);
	}

	/* ACTIVITIES */
	@Override
	public String getActivityParentCanonicalPath(Session session) {
		String currentUser = session.getUserID();
		Calendar currentTime = GregorianCalendar.getInstance();
		String path = peopleService
				.getBasePathForType(PeopleTypes.PEOPLE_ACTIVITY) + "/"
				+ JcrUtils.dateAsPath(currentTime, true) + currentUser;
		return path;
	}

	public String getActivityParentPath(Session session, Calendar date,
			String managerId) {
		String path = peopleService
				.getBasePathForType(PeopleTypes.PEOPLE_ACTIVITY)
				+ "/"
				+ JcrUtils.dateAsPath(date, true) + managerId;
		return path;
	}

	@Override
	public Node createActivity(Session session, String type, String title,
			String desc, List<Node> relatedTo) {
		return createActivity(session, new GregorianCalendar(),
				session.getUserID(), type, title, desc, relatedTo);
	}

	@Override
	public Node createActivity(Session session, Calendar date,
			String managerId, String type, String title, String desc,
			List<Node> relatedTo) {

		try {
			Node parent = JcrUtils.mkdirs(session,
					getActivityParentPath(session, date, managerId));
			Node activity = parent.addNode(type, PeopleTypes.PEOPLE_ACTIVITY);
			activity.addMixin(type);
			Node userProfile = UserJcrUtils.getUserProfile(session, managerId);
			activity.setProperty(PeopleNames.PEOPLE_MANAGER, userProfile);

			// Activity Date
			activity.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE, date);

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
							PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
			} else if (activityNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
				dateToDisplay = activityNode.getProperty(
						PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
			}
			return dateToDisplay;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get relevant date for activity " + activityNode,
					re);
		}
	}

	@Override
	public List<Node> getMyTasks(Session session, boolean onlyOpenTasks) {
		return getTasksForUser(session, session.getUserID(), onlyOpenTasks);
	}

	/**
	 * 
	 * @param session
	 * @param username
	 * @param onlyOpenTasks
	 * @return an empty list if none were found
	 */
	@Override
	public List<Node> getTasksForUser(Session session, String username,
			boolean onlyOpenTasks) {
		try {
			List<Node> groups = userManagementService.getUserGroups(session,
					username);
			List<Node> tasks = new ArrayList<Node>();
			for (Node group : groups) {
				PropertyIterator pit = group
						.getReferences(PeopleNames.PEOPLE_ASSIGNED_TO);
				while (pit.hasNext()) {
					Property currProp = pit.nextProperty();
					Node currNode = currProp.getParent();
					if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
						if (onlyOpenTasks) {
							if (!isTaskDone(currNode))
								tasks.add(currNode);
						} else
							tasks.add(currNode);
					}
				}
			}
			return tasks;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get tasks for user "
					+ username);
		}
	}

	@Override
	public boolean isTaskDone(Node taskNode) {
		try {
			// TODO enhence this
			return taskNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get done status for task "
					+ taskNode, re);
		}
	}

	/* TASKS */
	@Override
	public Node createTask(Session session, Node parentNode, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar wakeUpDate) {
		return createTask(session, parentNode, session.getUserID(), title,
				description, assignedTo, relatedTo, new GregorianCalendar(),
				dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode, String managerId,
			String title, String description, Node assignedTo,
			List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate) {

		try {
			if (session == null && parentNode == null)
				throw new PeopleException(
						"Define either a session or a parent node. "
								+ "Both cannot be null at the same time.");

			if (session == null)
				session = parentNode.getSession();

			if (parentNode == null)
				parentNode = JcrUtils
						.mkdirs(session,
								getActivityParentPath(session, creationDate,
										managerId));

			Node taskNode = parentNode.addNode(PeopleTypes.PEOPLE_TASK,
					PeopleTypes.PEOPLE_TASK);

			if (CommonsJcrUtils.checkNotEmptyString(title))
				taskNode.setProperty(Property.JCR_TITLE, title);

			if (CommonsJcrUtils.checkNotEmptyString(description))
				taskNode.setProperty(Property.JCR_DESCRIPTION, description);

			Node userProfile = UserJcrUtils.getUserProfile(session, managerId);
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

			// FIXME check this, not very satisfaying.
			// Activity Date
			taskNode.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE, creationDate);

			CommonsJcrUtils.saveAndCheckin(taskNode);
			return taskNode;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create the new task " + title,
					e);
		}
	}

	// TODO implement this cleanly
	private static final String[] ARRAY_TASK_STATUS = { "New", "Done",
			"Sleeping", "Canceled" };

	@Override
	public String[] getStatusList(Node task) {
		return ARRAY_TASK_STATUS;
	}

	/* DEPENDENCY INJECTION */
	public void setUserManagementService(
			UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

}
