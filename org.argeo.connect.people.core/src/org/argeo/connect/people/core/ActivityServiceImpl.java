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
import org.argeo.connect.people.ActivityValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;

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
		String path = peopleService.getBasePath(PeopleTypes.PEOPLE_ACTIVITY)
				+ "/" + JcrUtils.dateAsPath(currentTime, true) + currentUser;
		return path;
	}

	public String getActivityParentPath(Session session, Calendar date,
			String managerId) {
		String path = peopleService.getBasePath(PeopleTypes.PEOPLE_ACTIVITY)
				+ "/" + JcrUtils.dateAsPath(date, true) + managerId;
		return path;
	}

	@Override
	public Node createActivity(Session session, String type, String title,
			String desc, List<Node> relatedTo) {
		return createActivity(session, session.getUserID(), type, title, desc,
				relatedTo, new GregorianCalendar());
	}

	@Override
	public Node createActivity(Session session, String reporterId, String type,
			String title, String desc, List<Node> relatedTo, Calendar date) {

		try {
			Node parent = JcrUtils.mkdirs(session,
					getActivityParentPath(session, date, reporterId));
			Node activity = parent.addNode(type, PeopleTypes.PEOPLE_ACTIVITY);
			activity.addMixin(type);
			activity.setProperty(PeopleNames.PEOPLE_REPORTED_BY, reporterId);

			// Activity Date
			if (date != null)
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
			Calendar relevantDate = null;
			if (activityNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
				if (activityNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE))
					relevantDate = activityNode.getProperty(
							PeopleNames.PEOPLE_CLOSE_DATE).getDate();
				else if (activityNode.hasProperty(PeopleNames.PEOPLE_DUE_DATE))
					relevantDate = activityNode.getProperty(
							PeopleNames.PEOPLE_DUE_DATE).getDate();
				else if (activityNode
						.hasProperty(PeopleNames.PEOPLE_WAKE_UP_DATE))
					relevantDate = activityNode.getProperty(
							PeopleNames.PEOPLE_WAKE_UP_DATE).getDate();
				else if (activityNode
						.hasProperty(PeopleNames.PEOPLE_ACTIVITY_DATE))
					relevantDate = activityNode.getProperty(
							PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
				else if (activityNode.hasProperty(Property.JCR_LAST_MODIFIED))
					relevantDate = activityNode.getProperty(
							Property.JCR_LAST_MODIFIED).getDate();
				else if (activityNode.hasProperty(Property.JCR_CREATED))
					relevantDate = activityNode.getProperty(
							Property.JCR_CREATED).getDate();
			} else if (activityNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
				if (activityNode.hasProperty(PeopleNames.PEOPLE_ACTIVITY_DATE))
					relevantDate = activityNode.getProperty(
							PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
				else if (activityNode.hasProperty(Property.JCR_LAST_MODIFIED))
					relevantDate = activityNode.getProperty(
							Property.JCR_LAST_MODIFIED).getDate();
				else if (activityNode.hasProperty(Property.JCR_CREATED))
					relevantDate = activityNode.getProperty(
							Property.JCR_CREATED).getDate();
			}
			return relevantDate;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get relevant date for activity " + activityNode,
					re);
		}
	}

	@Override
	public String getActivityLabel(Node activity) {
		try {
			for (String type : ActivityValueCatalogs.MAPS_ACTIVITY_TYPES
					.keySet()) {
				if (activity.isNodeType(type))
					return ActivityValueCatalogs.MAPS_ACTIVITY_TYPES.get(type);
			}
			throw new PeopleException("Undefined type for activity: "
					+ activity);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get type for activity "
					+ activity, e);
		}
	}

	/** Get the display name for the manager of an activity. */
	public String getActivityManagerDisplayName(Node activityNode) {
		// TODO return display name rather than ID
		String manager = CommonsJcrUtils.get(activityNode,
				PeopleNames.PEOPLE_REPORTED_BY);

		if (CommonsJcrUtils.isEmptyString(manager)) {
			// TODO workaround to try to return a manager name in case we are in
			// a legacy context
			try {
				if (activityNode.hasProperty(PeopleNames.PEOPLE_MANAGER)) {
					Node referencedManager = activityNode.getProperty(
							PeopleNames.PEOPLE_MANAGER).getNode();
					manager = referencedManager.getParent().getName();
				}
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to legacy get "
						+ "manager name for activity " + activityNode, e);
			}
		}
		return manager;
	}

	/* TASKS */
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
							if (!isTaskDone(currNode) && !isTaskSleeping(currNode) )
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
	public boolean updateStatus(String templateId, Node taskNode,
			String newStatus) {
		try {
			Session session = taskNode.getSession();

			String oldStatus = CommonsJcrUtils.get(taskNode,
					PeopleNames.PEOPLE_TASK_STATUS);

			if (CommonsJcrUtils.checkNotEmptyString(oldStatus)
					&& oldStatus.equals(newStatus))
				return false;

			taskNode.setProperty(PeopleNames.PEOPLE_TASK_STATUS, newStatus);

			ResourceService resourceService = peopleService
					.getResourceService();
			List<String> closingStatus = resourceService.getTemplateCatalogue(
					session, templateId,
					PeopleNames.PEOPLE_TASK_CLOSING_STATUSES, null);
			if (closingStatus.contains(newStatus)) {
				if (closingStatus.contains(oldStatus)) {
					// Already closed, nothing to do
				} else {
					taskNode.setProperty(PeopleNames.PEOPLE_CLOSE_DATE,
							new GregorianCalendar());
					taskNode.setProperty(PeopleNames.PEOPLE_CLOSED_BY,
							session.getUserID());
				}
			} else {
				if (!closingStatus.contains(oldStatus)) {
					// Already open, nothing to do
				} else {
					if (taskNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE))
						taskNode.getProperty(PeopleNames.PEOPLE_CLOSE_DATE)
								.remove();
					if (taskNode.hasProperty(PeopleNames.PEOPLE_CLOSED_BY))
						taskNode.getProperty(PeopleNames.PEOPLE_CLOSED_BY)
								.remove();
				}
			}
			return true;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to set new status " + newStatus
					+ " status for task " + taskNode + " of template ID "
					+ templateId, re);
		}

	}

	@Override
	public boolean isTaskDone(Node taskNode) {
		try {
			// We only rely on the non-nullity of the closed date for the time
			// being.
			return taskNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get done status for task "
					+ taskNode, re);
		}
	}

	@Override
	public boolean isTaskSleeping(Node taskNode) {
		try {
			if (taskNode.hasProperty(PeopleNames.PEOPLE_WAKE_UP_DATE)) {
				Calendar wuDate = taskNode.getProperty(
						PeopleNames.PEOPLE_WAKE_UP_DATE).getDate();
				Calendar now = new GregorianCalendar();
				// Add a day: the task is awake as from 00:01AM on the given day
				now.add(Calendar.DAY_OF_YEAR, 1);
				return wuDate.after(now);
			} else
				return false;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get sleeping status for task "
					+ taskNode, re);
		}
	}

	/** Get the display name of the assigned to group for this task */
	@Override
	public String getAssignedToDisplayName(Node taskNode) {
		try {
			if (taskNode.hasProperty(PeopleNames.PEOPLE_ASSIGNED_TO)) {
				Node referencedManager = taskNode.getProperty(
						PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
				return CommonsJcrUtils.get(referencedManager,
						Property.JCR_TITLE);
			} else
				return "";
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to get name of group assigned to " + taskNode, e);
		}
	}

	@Override
	public Node createTask(Session session, Node parentNode, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar wakeUpDate) {
		return createTask(session, parentNode, session.getUserID(), title,
				description, assignedTo, relatedTo, new GregorianCalendar(),
				dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode, String reporterId,
			String title, String description, Node assignedTo,
			List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate) {
		return createTask(session, parentNode, PeopleTypes.PEOPLE_TASK,
				reporterId, title, description, assignedTo, relatedTo,
				creationDate, dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode,
			String taskNodeType, String reporterId, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar creationDate, Calendar dueDate, Calendar wakeUpDate) {

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
										reporterId));

			Node taskNode = parentNode.addNode(taskNodeType, taskNodeType);

			if (CommonsJcrUtils.checkNotEmptyString(title))
				taskNode.setProperty(Property.JCR_TITLE, title);

			if (CommonsJcrUtils.checkNotEmptyString(description))
				taskNode.setProperty(Property.JCR_DESCRIPTION, description);

			taskNode.setProperty(PeopleNames.PEOPLE_REPORTED_BY, reporterId);

			if (assignedTo != null)
				taskNode.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO, assignedTo);

			if (relatedTo != null && !relatedTo.isEmpty())
				CommonsJcrUtils.setMultipleReferences(taskNode,
						PeopleNames.PEOPLE_RELATED_TO, relatedTo);

			if (creationDate == null)
				creationDate = new GregorianCalendar();
			taskNode.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE, creationDate);

			if (dueDate != null) {
				taskNode.setProperty(PeopleNames.PEOPLE_DUE_DATE, dueDate);
			}
			if (wakeUpDate != null) {
				taskNode.setProperty(PeopleNames.PEOPLE_WAKE_UP_DATE,
						wakeUpDate);
			}

			// Default status management
			ResourceService resourceService = peopleService
					.getResourceService();
			Node template = resourceService.getNodeTemplate(session,
					taskNodeType);
			String defaultStatus = CommonsJcrUtils.get(template,
					PEOPLE_TASK_DEFAULT_STATUS);
			if (CommonsJcrUtils.checkNotEmptyString(defaultStatus))
				taskNode.setProperty(PEOPLE_TASK_STATUS, defaultStatus);

			return taskNode;
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to create task of type " + taskNodeType + " named "
							+ title + " under " + parentNode, e);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setUserManagementService(
			UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}
}