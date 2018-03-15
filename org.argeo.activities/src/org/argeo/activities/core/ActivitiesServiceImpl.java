package org.argeo.activities.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ActivityValueCatalogs;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.core.AbstractAppService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/** Concrete access to Connect's {@link ActivitiesService} */
public class ActivitiesServiceImpl extends AbstractAppService implements ActivitiesService, ActivitiesNames {
	private final static Log log = LogFactory.getLog(ActivitiesServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private UserAdminService userAdminService;
	private ResourcesService resourcesService;

	/* API METHODS */
	@Override
	public String getAppBaseName() {
		return ActivitiesNames.ACTIVITIES_APP_BASE_NAME;
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		if (entity.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY)) {
			String currentUser = null;
			if (entity.hasProperty(ACTIVITIES_REPORTED_BY))
				currentUser = entity.getProperty(ACTIVITIES_REPORTED_BY).getString();
			else {
				currentUser = CurrentUser.getUsername();
				if (log.isDebugEnabled())
					log.warn("Activity at " + entity.getPath() + "has no reportedBy property");
			}
			String userId = UserAdminUtils.getUserLocalId(currentUser);

			Calendar currentTime = null;
			if (entity.hasProperty(ACTIVITIES_ACTIVITY_DATE))
				currentTime = entity.getProperty(ACTIVITIES_ACTIVITY_DATE).getDate();
			else {
				currentTime = entity.getProperty(Property.JCR_CREATED).getDate();
				if (log.isDebugEnabled())
					log.warn("Activity at " + entity.getPath() + "has no activity date ");
			}

			String nodeType = getMainNodeType(entity);

			return dateAsRelPath(currentTime) + "/" + userId + "/" + nodeType;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodeType, String id) {
		throw new ActivitiesException("This method should not be used anymore");
		// if (ActivitiesTypes.ACTIVITIES_ACTIVITY.equals(nodeType)) {
		// String userId = session.getUserID();
		// Calendar currentTime = GregorianCalendar.getInstance();
		// return dateAsRelPath(currentTime) + "/" + userId;
		// } else
		// return null;
	}

	// @Override
	// public Node publishEntity(Node parent, String nodeType, Node srcNode,
	// boolean removeSrcNode)
	// throws RepositoryException {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public Node configureActivity(Node activity, String type, String title, String desc, List<Node> relatedTo)
			throws RepositoryException {
		return configureActivity(activity, activity.getSession().getUserID(), type, title, desc, relatedTo,
				new GregorianCalendar());
	}

	@Override
	public Node configureActivity(Node activity, String reporterId, String type, String title, String desc,
			List<Node> relatedTo, Calendar date) throws RepositoryException {
		// try {
		// Node activityBase = session.getNode(getBasePath());
		// String localId = UserAdminUtils.getUserLocalId(reporterId);
		// Node activity = JcrUtils.mkdirs(activityBase,
		// getDefaultRelPath(session, ActivitiesTypes.ACTIVITIES_ACTIVITY,
		// localId));
		// activity.addMixin(type);

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
		// } catch (RepositoryException e) {
		// throw new ActivitiesException("Unable to create activity node", e);
		// }
	}

	@Override
	public boolean isKnownType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_TASK)
				|| ConnectJcrUtils.isNodeType(entity, ActivitiesTypes.ACTIVITIES_ACTIVITY))
			return true;
		else
			return false;
	}

	private static final String[] KNOWN_MIXIN = { ActivitiesTypes.ACTIVITIES_TASK, ActivitiesTypes.ACTIVITIES_NOTE,
			ActivitiesTypes.ACTIVITIES_MEETING, ActivitiesTypes.ACTIVITIES_SENT_EMAIL,
			ActivitiesTypes.ACTIVITIES_SENT_FAX, ActivitiesTypes.ACTIVITIES_SENT_EMAIL,
			ActivitiesTypes.ACTIVITIES_BLOG_POST, ActivitiesTypes.ACTIVITIES_CALL, ActivitiesTypes.ACTIVITIES_CHAT,
			ActivitiesTypes.ACTIVITIES_PAYMENT, ActivitiesTypes.ACTIVITIES_POLL, ActivitiesTypes.ACTIVITIES_RATE,
			ActivitiesTypes.ACTIVITIES_REVIEW, ActivitiesTypes.ACTIVITIES_TWEET, ActivitiesTypes.ACTIVITIES_ACTIVITY };

	@Override
	public String getMainNodeType(Node entity) {

		for (String mixin : KNOWN_MIXIN)
			if (ConnectJcrUtils.isNodeType(entity, mixin))
				return mixin;
		return null;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		for (String mixin : KNOWN_MIXIN)
			if (mixin.equals(nodeType))
				return true;
		return false;
	}

	/* ACTIVITIES APP SPECIFIC METHODS */

	private String dateAsRelPath(Calendar cal) {
		// StringBuffer buf = new StringBuffer(9);
		// buf.append('Y');
		// buf.append(cal.get(Calendar.YEAR));
		// buf.append('/');
		//
		// int woy = cal.get(Calendar.WEEK_OF_YEAR);
		// buf.append('W');
		// if (woy < 10)
		// buf.append(0);
		// buf.append(woy);
		// return buf.toString();
		//
		return JcrUtils.dateAsPath(cal, true);
	}

	/* ACTIVITIES */

	@Override
	public Calendar getActivityRelevantDate(Node activityNode) {
		try {
			Calendar relevantDate = null;
			if (activityNode.isNodeType(ActivitiesTypes.ACTIVITIES_TASK)) {
				if (activityNode.hasProperty(ConnectNames.CONNECT_CLOSE_DATE))
					relevantDate = activityNode.getProperty(ConnectNames.CONNECT_CLOSE_DATE).getDate();
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
		List<String> normalisedRoles = new ArrayList<>();
		for (String role : CurrentUser.roles())
			normalisedRoles.add(normalizeDn(role));
		String[] nrArr = normalisedRoles.toArray(new String[0]);
		return getTasksForGroup(session, nrArr, onlyOpenTasks);
		// return getTasksForUser(session, session.getUserID(), onlyOpenTasks);
	}

	private String normalizeDn(String dn) {
		// FIXME dirty workaround for the DN key case issue
		String lowerCased = dn.replaceAll("UID=", "uid=").replaceAll("CN=", "cn=").replaceAll("DC=", "dc=")
				.replaceAll("OU=", "ou=").replaceAll(", ", ",");
		return lowerCased;
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
				String attrQuery = XPathUtils.getPropertyEquals(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, role);
				if (ConnectUtils.notEmpty(attrQuery))
					tmpBuilder.append(attrQuery).append(" or ");
			}
			String groupCond = null;
			if (tmpBuilder.length() > 4)
				groupCond = "(" + tmpBuilder.substring(0, tmpBuilder.length() - 3) + ")";

			// Only opened tasks
			String notClosedCond = null;
			if (onlyOpenTasks)
				notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";

			String allCond = XPathUtils.localAnd(groupCond, notClosedCond);
			if (EclipseUiUtils.notEmpty(allCond))
				builder.append("[").append(allCond).append("]");

			builder.append(" order by @").append(ActivitiesNames.ACTIVITIES_DUE_DATE).append(", @")
					.append(Property.JCR_LAST_MODIFIED).append(" ascending");
			if (log.isDebugEnabled())
				log.debug("Getting todo list for " + CurrentUser.getDisplayName() + " (DN: " + CurrentUser.getUsername()
						+ ") with query: " + builder.toString());
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
					taskNode.setProperty(ConnectNames.CONNECT_CLOSE_DATE, new GregorianCalendar());
					taskNode.setProperty(ConnectNames.CONNECT_CLOSED_BY, session.getUserID());
					changed = true;
				}
			} else {
				if (!closingStatus.contains(oldStatus)) {
					// Already open, nothing to do
				} else {
					// Re-Open
					if (taskNode.hasProperty(ConnectNames.CONNECT_CLOSE_DATE))
						taskNode.getProperty(ConnectNames.CONNECT_CLOSE_DATE).remove();
					if (taskNode.hasProperty(ConnectNames.CONNECT_CLOSED_BY))
						taskNode.getProperty(ConnectNames.CONNECT_CLOSED_BY).remove();
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
			if (ConnectUtils.notEmpty(oldStatus) && oldStatus.equals(newStatus))
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
			return taskNode.hasProperty(ConnectNames.CONNECT_CLOSE_DATE);
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
	public Node configureTask(Node task, String taskNodeType, String title, String description, String assignedTo)
			throws RepositoryException {
		return configureTask(task, taskNodeType, task.getSession().getUserID(), title, description, assignedTo, null,
				new GregorianCalendar(), null, null);
	}
	//
	// @Override
	// public Node createDraftTask(Session session, String title, String
	// description, String assignedTo,
	// List<Node> relatedTo, Calendar dueDate, Calendar wakeUpDate) throws
	// RepositoryException {
	// return createDraftTask(session, CurrentUser.getUsername(), title,
	// description, assignedTo, relatedTo,
	// new GregorianCalendar(), dueDate, wakeUpDate);
	// }
	//
	// @Override
	// public Node createDraftTask(Session session, String reporterId, String
	// title, String description, String assignedTo,
	// List<Node> relatedTo, Calendar creationDate, Calendar dueDate, Calendar
	// wakeUpDate)
	// throws RepositoryException {
	// return createDraftTask(session, ActivitiesTypes.ACTIVITIES_TASK,
	// reporterId, title, description, assignedTo,
	// relatedTo, creationDate, dueDate, wakeUpDate);
	// }

	@Override
	public Node configureTask(Node draftTask, String taskNodeType, String reporterId, String title, String description,
			String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar dueDate, Calendar wakeUpDate)
			throws RepositoryException {

		// Node parentNode = getDraftParent(session);
		// // We use the mixin as node name
		// Node taskNode = parentNode.addNode(taskNodeType);
		// taskNode.addMixin(taskNodeType);

		// Node draftTask = createDraftEntity(session, taskNodeType);

		if (ConnectUtils.notEmpty(title))
			draftTask.setProperty(Property.JCR_TITLE, title);
		if (ConnectUtils.notEmpty(description))
			draftTask.setProperty(Property.JCR_DESCRIPTION, description);
		if (EclipseUiUtils.isEmpty(reporterId))
			reporterId = draftTask.getSession().getUserID();
		draftTask.setProperty(ActivitiesNames.ACTIVITIES_REPORTED_BY, reporterId);

		if (ConnectUtils.notEmpty(assignedTo))
			draftTask.setProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, assignedTo);

		if (relatedTo != null && !relatedTo.isEmpty())
			ConnectJcrUtils.setMultipleReferences(draftTask, ActivitiesNames.ACTIVITIES_RELATED_TO, relatedTo);

		if (creationDate == null)
			creationDate = new GregorianCalendar();
		draftTask.setProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE, creationDate);

		if (dueDate != null) {
			draftTask.setProperty(ActivitiesNames.ACTIVITIES_DUE_DATE, dueDate);
		}
		if (wakeUpDate != null) {
			draftTask.setProperty(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE, wakeUpDate);
		}
		setTaskDefaultStatus(draftTask, taskNodeType);
		return draftTask;
	}

	@Override
	public void setTaskDefaultStatus(Node taskNode, String taskNodeType) throws RepositoryException {
		// Default status management
		Node template = resourcesService.getNodeTemplate(taskNode.getSession(), taskNodeType);
		String defaultStatus = null;
		if (template != null)
			defaultStatus = ConnectJcrUtils.get(template, ActivitiesNames.ACTIVITIES_TASK_DEFAULT_STATUS);
		if (ConnectUtils.notEmpty(defaultStatus))
			taskNode.setProperty(ActivitiesNames.ACTIVITIES_TASK_STATUS, defaultStatus);
	}

	// @Override
	// public Node createPoll(Node parentNode, String reporterId, String
	// pollName, String title, String description,
	// String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar
	// dueDate, Calendar wakeUpDate)
	// throws RepositoryException {
	// Node poll = createDraftTask(null, ActivitiesTypes.ACTIVITIES_POLL,
	// reporterId, title, description, assignedTo,
	// relatedTo, creationDate, dueDate, wakeUpDate);
	//
	// String newPath = parentNode.getPath() + "/" +
	// JcrUtils.replaceInvalidChars(pollName);
	// poll.setProperty(ActivitiesNames.ACTIVITIES_POLL_NAME, pollName);
	// poll.addNode(ActivitiesNames.ACTIVITIES_RATES);
	//
	// // TODO clean this
	// Session session = parentNode.getSession();
	// session.move(poll.getPath(), newPath);
	// // } catch (RepositoryException e) {
	// // throw new ActivitiesException(
	// // "Unable to add poll specific info to task " + poll + " and move it to
	// // " + newPath, e);
	// // }
	// return poll;
	// }

	/* DEPENDENCY INJECTION */
	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}
}
