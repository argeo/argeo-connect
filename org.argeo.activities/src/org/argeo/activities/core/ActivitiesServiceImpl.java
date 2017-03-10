package org.argeo.activities.core;

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

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ActivityValueCatalogs;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to Connect's {@link ActivitiesService} */
public class ActivitiesServiceImpl implements ActivitiesService, ActivitiesNames {
	// private final static Log log =
	// LogFactory.getLog(ActivityServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private UserAdminService userAdminService;
	private ResourcesService resourcesService;

	/* API METHODS */
	@Override
	public String getAppBaseName() {
		return ActivitiesNames.ACTIVITIES_APP_BASE_NAME;
	}

	@Override
	public String getActivityParentCanonicalPath(Session session) {
		String currentUser = session.getUserID();
		Calendar currentTime = GregorianCalendar.getInstance();
		String path = "/" + getAppBaseName() + "/" + dateAsRelPath(currentTime) + currentUser;
		return path;
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		if (entity.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY)) {
			String currentUser = ConnectJcrUtils.getSession(entity).getUserID();
			Calendar currentTime = entity.getProperty(Property.JCR_CREATED).getDate();
			return dateAsRelPath(currentTime) + "/" + currentUser;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(String nodeType, String userId) {
		if (ActivitiesTypes.ACTIVITIES_ACTIVITY.equals(nodeType)) {
			Calendar currentTime = GregorianCalendar.getInstance();
			return dateAsRelPath(currentTime) + "/" + userId;
		} else
			return null;
	}

	public String getActivityParentRelPath(Session session, Calendar date, String managerId) {
		String path = JcrUtils.dateAsPath(date, true) + managerId;
		return path;
	}

	@Override
	public Node createEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createActivity(Session session, String type, String title, String desc, List<Node> relatedTo) {
		return createActivity(session, session.getUserID(), type, title, desc, relatedTo, new GregorianCalendar());
	}

	@Override
	public Node createActivity(Session session, String reporterId, String type, String title, String desc,
			List<Node> relatedTo, Calendar date) {
		try {
			Node activityBase = session.getNode(getBasePath());
			String localId = UserAdminUtils.getUserLocalId(reporterId);
			Node parent = JcrUtils.mkdirs(activityBase, getActivityParentRelPath(session, date, localId),
					NodeType.NT_UNSTRUCTURED);
			Node activity = parent.addNode(type, NodeType.NT_UNSTRUCTURED);
			activity.addMixin(type);
			activity.setProperty(ActivitiesNames.ACTIVITIES_REPORTED_BY, reporterId);

			// Activity Date
			if (date != null)
				activity.setProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE, date);

			// related to
			if (relatedTo != null && !relatedTo.isEmpty())
				ConnectJcrUtils.setMultipleReferences(activity, ActivitiesNames.ACTIVITIES_RELATED_TO, relatedTo);

			// Content
			activity.setProperty(Property.JCR_TITLE, title);
			activity.setProperty(Property.JCR_DESCRIPTION, desc);
			JcrUtils.updateLastModified(activity);
			return activity;
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create activity node", e);
		}
	}

	private String getBasePath() {
		return "/" + getAppBaseName();
	}

	@Override
	public boolean isKnownType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK)
				|| ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return true;
		else
			return false;
	}

	@Override
	public String getMainNodeType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK))
			return ActivitiesTypes.ACTIVITIES_TASK;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_NOTE))
			return ActivitiesTypes.ACTIVITIES_NOTE;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_MEETING))
			return ActivitiesTypes.ACTIVITIES_MEETING;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_SENT_EMAIL))
			return ActivitiesTypes.ACTIVITIES_SENT_EMAIL;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_SENT_FAX))
			return ActivitiesTypes.ACTIVITIES_SENT_FAX;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_SENT_EMAIL))
			return ActivitiesTypes.ACTIVITIES_SENT_EMAIL;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_BLOG_POST))
			return ActivitiesTypes.ACTIVITIES_BLOG_POST;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_CALL))
			return ActivitiesTypes.ACTIVITIES_CALL;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_CHAT))
			return ActivitiesTypes.ACTIVITIES_CHAT;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_PAYMENT))
			return ActivitiesTypes.ACTIVITIES_PAYMENT;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_POLL))
			return ActivitiesTypes.ACTIVITIES_POLL;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_RATE))
			return ActivitiesTypes.ACTIVITIES_RATE;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_REVIEW))
			return ActivitiesTypes.ACTIVITIES_REVIEW;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TWEET))
			return ActivitiesTypes.ACTIVITIES_TWEET;
		else if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return ActivitiesTypes.ACTIVITIES_ACTIVITY;
		else
			return null;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		if (ActivitiesTypes.ACTIVITIES_TASK.equals(nodeType) || ActivitiesTypes.ACTIVITIES_ACTIVITY.equals(nodeType))
			return true;
		else
			return false;
	}

	/* ACTIVITIES APP SPECIFIC METHODS */

	private String dateAsRelPath(Calendar cal) {
		StringBuffer buf = new StringBuffer(9);
		buf.append('Y');
		buf.append(cal.get(Calendar.YEAR));
		buf.append('/');

		int woy = cal.get(Calendar.WEEK_OF_YEAR);
		buf.append('W');
		if (woy < 10)
			buf.append(0);
		buf.append(woy);
		return buf.toString();
	}

	/* ACTIVITIES */

	@Override
	public Calendar getActivityRelevantDate(Node activityNode) {
		try {
			Calendar relevantDate = null;
			if (activityNode.isNodeType(ActivitiesTypes.ACTIVITIES_TASK)) {
				if (activityNode.hasProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE))
					relevantDate = activityNode.getProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE).getDate();
				else if (activityNode.hasProperty(ActivitiesNames.ACTIVITIES_DUE_DATE))
					relevantDate = activityNode.getProperty(ActivitiesNames.ACTIVITIES_DUE_DATE).getDate();
				else if (activityNode.hasProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE))
					relevantDate = activityNode.getProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE).getDate();
				else if (activityNode.hasProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE))
					relevantDate = activityNode.getProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE).getDate();
				else if (activityNode.hasProperty(Property.JCR_LAST_MODIFIED))
					relevantDate = activityNode.getProperty(Property.JCR_LAST_MODIFIED).getDate();
				else if (activityNode.hasProperty(Property.JCR_CREATED))
					relevantDate = activityNode.getProperty(Property.JCR_CREATED).getDate();
			} else if (activityNode.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY)) {
				if (activityNode.hasProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE))
					relevantDate = activityNode.getProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE).getDate();
				else if (activityNode.hasProperty(Property.JCR_LAST_MODIFIED))
					relevantDate = activityNode.getProperty(Property.JCR_LAST_MODIFIED).getDate();
				else if (activityNode.hasProperty(Property.JCR_CREATED))
					relevantDate = activityNode.getProperty(Property.JCR_CREATED).getDate();
			}
			return relevantDate;
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get relevant date " + "for activity " + activityNode, re);
		}
	}

	@Override
	public String getActivityLabel(Node activity) {
		try {
			for (String type : ActivityValueCatalogs.MAPS_ACTIVITY_TYPES.keySet()) {
				if (activity.isNodeType(type))
					return ActivityValueCatalogs.MAPS_ACTIVITY_TYPES.get(type);
			}
			throw new ActivitiesException("Undefined type for activity: " + activity);
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get type for activity " + activity, e);
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
	public NodeIterator getTasksForUser(Session session, String username, boolean onlyOpenTasks) {
		return getTasksForGroup(session, userAdminService.getUserRoles(username), onlyOpenTasks);
	}

	public NodeIterator getTasksForGroup(Session session, String[] roles, boolean onlyOpenTasks) {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_TASK).append(")");

			// Assigned to
			StringBuilder tmpBuilder = new StringBuilder();
			for (String role : roles) {
				// TODO clean this: IPA roles have upper case key tokens
				String attrQuery = XPathUtils.getPropertyEquals(ActivitiesNames.ACTIVITIES_ASSIGNED_TO,
						role.toLowerCase());
				if (notEmpty(attrQuery))
					tmpBuilder.append(attrQuery).append(" or ");
			}
			String groupCond = null;
			if (tmpBuilder.length() > 4)
				groupCond = "(" + tmpBuilder.substring(0, tmpBuilder.length() - 3) + ")";

			// Only opened tasks
			String notClosedCond = null;
			if (onlyOpenTasks)
				notClosedCond = "not(@" + ActivitiesNames.ACTIVITIES_CLOSE_DATE + ")";

			String allCond = XPathUtils.localAnd(groupCond, notClosedCond);
			if (EclipseUiUtils.notEmpty(allCond))
				builder.append("[").append(allCond).append("]");

			builder.append(" order by @").append(Property.JCR_LAST_MODIFIED).append(" descending");
			Query query = XPathUtils.createQuery(session, builder.toString());
			return query.execute().getNodes();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get tasks for groups " + roles.toString());
		}
	}

	protected boolean manageClosedState(String templateId, Node taskNode, String oldStatus, String newStatus,
			List<String> modifiedPaths) throws RepositoryException {
		try {
			Session session = taskNode.getSession();
			List<String> closingStatus = resourcesService.getTemplateCatalogue(session, templateId,
					ActivitiesNames.ACTIVITIES_TASK_CLOSING_STATUSES, null);

			boolean changed = false;

			if (closingStatus.contains(newStatus)) {
				if (closingStatus.contains(oldStatus)) {
					// Already closed, nothing to do
				} else {
					// Close
					taskNode.setProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE, new GregorianCalendar());
					taskNode.setProperty(ActivitiesNames.ACTIVITIES_CLOSED_BY, session.getUserID());
					changed = true;
				}
			} else {
				if (!closingStatus.contains(oldStatus)) {
					// Already open, nothing to do
				} else {
					// Re-Open
					if (taskNode.hasProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE))
						taskNode.getProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE).remove();
					if (taskNode.hasProperty(ActivitiesNames.ACTIVITIES_CLOSED_BY))
						taskNode.getProperty(ActivitiesNames.ACTIVITIES_CLOSED_BY).remove();
					changed = true;
				}
			}
			return changed;
		} catch (RepositoryException re) {
			throw new RepositoryException("Unable to manage closed state for " + newStatus + " status for task "
					+ taskNode + " of template ID " + templateId, re);
		}
	}

	public boolean updateStatus(String templateId, Node taskNode, String newStatus, List<String> modifiedPaths)
			throws RepositoryException {
		try {
			String oldStatus = ConnectJcrUtils.get(taskNode, ActivitiesNames.ACTIVITIES_TASK_STATUS);
			if (notEmpty(oldStatus) && oldStatus.equals(newStatus))
				return false;
			else {
				taskNode.setProperty(ActivitiesNames.ACTIVITIES_TASK_STATUS, newStatus);
				manageClosedState(templateId, taskNode, oldStatus, newStatus, modifiedPaths);
				return true;
			}
		} catch (RepositoryException re) {
			throw new RepositoryException("Unable to set new status " + newStatus + " status for task " + taskNode
					+ " of template ID " + templateId, re);
		}
	}

	@Override
	public boolean isTaskDone(Node taskNode) {
		try {
			// Only rely on the non-nullity of the closed date
			return taskNode.hasProperty(ActivitiesNames.ACTIVITIES_CLOSE_DATE);
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get done status for task " + taskNode, re);
		}
	}

	@Override
	public boolean isTaskSleeping(Node taskNode) {
		try {
			if (taskNode.hasProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE)) {
				Calendar wuDate = taskNode.getProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE).getDate();
				Calendar now = new GregorianCalendar();
				// Add a day: the task is awake as from 00:01AM on the given day
				now.add(Calendar.DAY_OF_YEAR, 1);
				return wuDate.after(now);
			} else
				return false;
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get sleeping status for task " + taskNode, re);
		}
	}

	/** Get the display name of the assigned to group for this task */
	@Override
	public String getAssignedToDisplayName(Node taskNode) {
		try {
			if (taskNode.hasProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO)) {
				String groupId = taskNode.getProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO).getString();
				return userAdminService.getUserDisplayName(groupId);
			}
			return "";
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get name of group assigned to " + taskNode, e);
		}
	}

	@Override
	public Node createTask(Session session, Node parentNode, String title, String description, String assignedTo,
			List<Node> relatedTo, Calendar dueDate, Calendar wakeUpDate) throws RepositoryException {
		return createTask(session, parentNode, CurrentUser.getUsername(), title, description, assignedTo, relatedTo,
				new GregorianCalendar(), dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode, String reporterId, String title, String description,
			String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar dueDate, Calendar wakeUpDate)
			throws RepositoryException {
		return createTask(session, parentNode, ActivitiesTypes.ACTIVITIES_TASK, reporterId, title, description,
				assignedTo, relatedTo, creationDate, dueDate, wakeUpDate);
	}

	@Override
	public Node createTask(Session session, Node parentNode, String taskNodeType, String reporterId, String title,
			String description, String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate) throws RepositoryException {
		if (session == null && parentNode == null)
			throw new ActivitiesException(
					"Define either a session or a parent node. Both cannot be null at the same time.");

		if (session == null)
			session = parentNode.getSession();

		if (parentNode == null) {
			Node activityBase = session.getNode(getBasePath());
			String localId = UserAdminUtils.getUserLocalId(reporterId);
			parentNode = JcrUtils.mkdirs(activityBase, getActivityParentRelPath(session, creationDate, localId),
					NodeType.NT_UNSTRUCTURED);
		}

		Node taskNode = parentNode.addNode(taskNodeType);
		taskNode.addMixin(taskNodeType);

		if (notEmpty(title))
			taskNode.setProperty(Property.JCR_TITLE, title);
		if (notEmpty(description))
			taskNode.setProperty(Property.JCR_DESCRIPTION, description);
		if (EclipseUiUtils.isEmpty(reporterId))
			reporterId = session.getUserID();
		taskNode.setProperty(ActivitiesNames.ACTIVITIES_REPORTED_BY, reporterId);

		if (notEmpty(assignedTo))
			taskNode.setProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, assignedTo);

		if (relatedTo != null && !relatedTo.isEmpty())
			ConnectJcrUtils.setMultipleReferences(taskNode, ActivitiesNames.ACTIVITIES_RELATED_TO, relatedTo);

		if (creationDate == null)
			creationDate = new GregorianCalendar();
		taskNode.setProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE, creationDate);

		if (dueDate != null) {
			taskNode.setProperty(ActivitiesNames.ACTIVITIES_DUE_DATE, dueDate);
		}
		if (wakeUpDate != null) {
			taskNode.setProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE, wakeUpDate);
		}
		setTaskDefaultStatus(taskNode, taskNodeType);
		return taskNode;
	}

	@Override
	public void setTaskDefaultStatus(Node taskNode, String taskNodeType) throws RepositoryException {
		// Default status management
		Node template = resourcesService.getNodeTemplate(taskNode.getSession(), taskNodeType);
		String defaultStatus = null;
		if (template != null)
			defaultStatus = ConnectJcrUtils.get(template, ActivitiesNames.ACTIVITIES_TASK_DEFAULT_STATUS);
		if (notEmpty(defaultStatus))
			taskNode.setProperty(ActivitiesNames.ACTIVITIES_TASK_STATUS, defaultStatus);
	}

	@Override
	public Node createPoll(Node parentNode, String reporterId, String pollName, String title, String description,
			String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar dueDate, Calendar wakeUpDate)
			throws RepositoryException {
		Node poll = createTask(null, parentNode, ActivitiesTypes.ACTIVITIES_POLL, reporterId, title, description,
				assignedTo, relatedTo, creationDate, dueDate, wakeUpDate);

		String newPath = parentNode.getPath() + "/" + JcrUtils.replaceInvalidChars(pollName);
		poll.setProperty(ActivitiesNames.ACTIVITIES_POLL_NAME, pollName);
		poll.addNode(ActivitiesNames.ACTIVITIES_RATES);

		// TODO clean this
		Session session = parentNode.getSession();
		session.move(poll.getPath(), newPath);
		// } catch (RepositoryException e) {
		// throw new ActivitiesException(
		// "Unable to add poll specific info to task " + poll + " and move it to
		// " + newPath, e);
		// }
		return poll;
	}

	/* DEPENDENCY INJECTION */
	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

}