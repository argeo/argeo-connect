package org.argeo.connect.people.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.argeo.cms.util.useradmin.UserAdminUtils;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.ActivityValueCatalogs;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to People's {@link ActivityService} */
public class ActivityServiceImpl implements ActivityService, PeopleNames {
	// private final static Log log =
	// LogFactory.getLog(ActivityServiceImpl.class);

	// Keeps a local reference to the parent people service,
	// Among other to rely on its base path policies.
	private final PeopleService peopleService;

	/**
	 * Default constructor, caller must then inject a relevant
	 * {@link userManagementService}
	 */
	public ActivityServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
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

	public String getActivityParentRelPath(Session session, Calendar date,
			String managerId) {
		String path = JcrUtils.dateAsPath(date, true) + managerId;
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
			Node activityBase = session.getNode(peopleService
					.getBasePath(PeopleTypes.PEOPLE_ACTIVITY));
			String localId = UserAdminUtils.getUserUid(reporterId);
			Node parent = JcrUtils.mkdirs(activityBase,
					getActivityParentRelPath(session, date, localId),
					NodeType.NT_UNSTRUCTURED);
			Node activity = parent.addNode(type, PeopleTypes.PEOPLE_ACTIVITY);
			activity.addMixin(type);
			activity.setProperty(PeopleNames.PEOPLE_REPORTED_BY, reporterId);

			// Activity Date
			if (date != null)
				activity.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE, date);

			// related to
			if (relatedTo != null && !relatedTo.isEmpty())
				JcrUiUtils.setMultipleReferences(activity,
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
			throw new PeopleException("Unable to get relevant date "
					+ "for activity " + activityNode, re);
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

	/* TASKS */
	@Override
	public NodeIterator getMyTasks(Session session, boolean onlyOpenTasks) {
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
	public NodeIterator getTasksForUser(Session session, String username,
			boolean onlyOpenTasks) {
		UserAdminService usm = peopleService.getUserAdminService();
		return getTasksForGroup(session, usm.getUserRoles(username),
				onlyOpenTasks);
	}

	public NodeIterator getTasksForGroup(Session session, String[] roles,
			boolean onlyOpenTasks) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(PeopleTypes.PEOPLE_TASK)
					.append(")");

			StringBuilder tmpBuilder = new StringBuilder();
			for (String role : roles) {
				String attrQuery = XPathUtils.getPropertyEquals(
						PeopleNames.PEOPLE_ASSIGNED_TO, role);
				if (notEmpty(attrQuery))
					tmpBuilder.append(attrQuery).append(" or ");
			}

			if (tmpBuilder.length() > 4)
				builder.append("[")
						.append(tmpBuilder.substring(0, tmpBuilder.length() - 3))
						.append("]");

			// Implement the sleeping task query
			if (onlyOpenTasks)
				throw new PeopleException(
						"Unimplemented feature: search only open tasks");

			builder.append(" order by @").append(PeopleNames.JCR_LAST_MODIFIED)
					.append(" descending");

			Query query = queryManager.createQuery(builder.toString(),
					PeopleConstants.QUERY_XPATH);

			return query.execute().getNodes();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get tasks for groups "
					+ roles.toString());
		}
	}

	protected boolean manageClosedState(String templateId, Node taskNode,
			String oldStatus, String newStatus, List<String> modifiedPaths) {
		try {
			Session session = taskNode.getSession();
			ResourceService resourceService = peopleService
					.getResourceService();
			List<String> closingStatus = resourceService.getTemplateCatalogue(
					session, templateId,
					PeopleNames.PEOPLE_TASK_CLOSING_STATUSES, null);

			boolean changed = false;

			if (closingStatus.contains(newStatus)) {
				if (closingStatus.contains(oldStatus)) {
					// Already closed, nothing to do
				} else {
					// Close
					taskNode.setProperty(PeopleNames.PEOPLE_CLOSE_DATE,
							new GregorianCalendar());
					taskNode.setProperty(PeopleNames.PEOPLE_CLOSED_BY,
							session.getUserID());
					changed = true;
				}
			} else {
				if (!closingStatus.contains(oldStatus)) {
					// Already open, nothing to do
				} else {
					// Re-Open
					if (taskNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE))
						taskNode.getProperty(PeopleNames.PEOPLE_CLOSE_DATE)
								.remove();
					if (taskNode.hasProperty(PeopleNames.PEOPLE_CLOSED_BY))
						taskNode.getProperty(PeopleNames.PEOPLE_CLOSED_BY)
								.remove();
					changed = true;
				}
			}
			return changed;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to manage closed state for "
					+ newStatus + " status for task " + taskNode
					+ " of template ID " + templateId, re);
		}
	}

	public boolean updateStatus(String templateId, Node taskNode,
			String newStatus, List<String> modifiedPaths) {
		try {
			String oldStatus = JcrUiUtils.get(taskNode,
					PeopleNames.PEOPLE_TASK_STATUS);
			if (notEmpty(oldStatus) && oldStatus.equals(newStatus))
				return false;
			else {
				taskNode.setProperty(PeopleNames.PEOPLE_TASK_STATUS, newStatus);
				manageClosedState(templateId, taskNode, oldStatus, newStatus,
						modifiedPaths);
				return true;
			}
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
				String groupId = taskNode.getProperty(
						PeopleNames.PEOPLE_ASSIGNED_TO).getString();
				return peopleService.getUserAdminService().getUserDisplayName(
						groupId);
			}
			return "";
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to get name of group assigned to " + taskNode, e);
		}
	}

	@Override
	public Node createTask(Session session, Node parentNode, String title,
			String description, String assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar wakeUpDate) {
		return createTask(session, parentNode, session.getUserID(), title,
				description, assignedTo, relatedTo, new GregorianCalendar(),
				dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode, String reporterId,
			String title, String description, String assignedTo,
			List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate) {
		return createTask(session, parentNode, PeopleTypes.PEOPLE_TASK,
				reporterId, title, description, assignedTo, relatedTo,
				creationDate, dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode,
			String taskNodeType, String reporterId, String title,
			String description, String assignedTo, List<Node> relatedTo,
			Calendar creationDate, Calendar dueDate, Calendar wakeUpDate) {
		try {
			if (session == null && parentNode == null)
				throw new PeopleException(
						"Define either a session or a parent node. "
								+ "Both cannot be null at the same time.");

			if (session == null)
				session = parentNode.getSession();

			if (parentNode == null) {
				Node activityBase = session.getNode(peopleService
						.getBasePath(PeopleTypes.PEOPLE_ACTIVITY));
				String localId = UserAdminUtils.getUserUid(reporterId);
				parentNode = JcrUtils
						.mkdirs(activityBase,
								getActivityParentRelPath(session, creationDate,
										localId), NodeType.NT_UNSTRUCTURED);
			}

			Node taskNode = parentNode.addNode(taskNodeType, taskNodeType);

			if (notEmpty(title))
				taskNode.setProperty(Property.JCR_TITLE, title);

			if (notEmpty(description))
				taskNode.setProperty(Property.JCR_DESCRIPTION, description);

			taskNode.setProperty(PeopleNames.PEOPLE_REPORTED_BY, reporterId);

			if (notEmpty(assignedTo)) {
				// String atdn = peopleService.getUserAdminService()
				// .getDistinguishedName(assignedTo, Role.GROUP);
				taskNode.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO, assignedTo);
			}

			if (relatedTo != null && !relatedTo.isEmpty())
				JcrUiUtils.setMultipleReferences(taskNode,
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
			setTaskDefaultStatus(taskNode, taskNodeType);
			return taskNode;
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to create task of type " + taskNodeType + " named "
							+ title + " under " + parentNode, e);
		}
	}

	protected void setTaskDefaultStatus(Node taskNode, String taskNodeType)
			throws RepositoryException {
		// Default status management
		ResourceService resourceService = peopleService.getResourceService();
		Node template = resourceService.getNodeTemplate(taskNode.getSession(),
				taskNodeType);
		String defaultStatus = null;
		if (template != null)
			defaultStatus = JcrUiUtils
					.get(template, PEOPLE_TASK_DEFAULT_STATUS);
		if (notEmpty(defaultStatus))
			taskNode.setProperty(PEOPLE_TASK_STATUS, defaultStatus);
	}

	@Override
	public Node createPoll(Node parentNode, String reporterId, String pollName,
			String title, String description, String assignedTo,
			List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate) {
		Node poll = createTask(null, parentNode, PeopleTypes.PEOPLE_POLL,
				reporterId, title, description, assignedTo, relatedTo,
				creationDate, dueDate, wakeUpDate);

		String newPath = null;
		try {
			newPath = parentNode.getPath() + "/"
					+ JcrUtils.replaceInvalidChars(pollName);
			poll.setProperty(PEOPLE_POLL_NAME, pollName);
			poll.addNode(PeopleNames.PEOPLE_RATES);

			// TODO clean this
			// Enhance task naming
			// FIXME use the genereric move strategy
			Session session = parentNode.getSession();
			session.move(poll.getPath(), newPath);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to add poll specific info to task " + poll
							+ " and move it to " + newPath, e);
		}
		return poll;
	}
}