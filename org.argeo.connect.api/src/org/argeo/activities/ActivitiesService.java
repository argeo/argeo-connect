package org.argeo.activities;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;

/**
 * Manage activity and tasks concepts in a Connect repository. Implementing
 * applications should extend and/or override the canonical implementation in
 * order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 */
public interface ActivitiesService extends AppService {

	/* ACTIVITIES */

	/**
	 * Simply configures a simple activity using default manager (the current
	 * logged in user) and default date (now)
	 */
	public Node configureActivity(Node activity, String type, String title, String desc, List<Node> relatedTo)
			throws RepositoryException;

	/**
	 * Creates a new simple activity using the default path
	 * 
	 * We use a distinct manager and activity date rather than JCR_CREATED and
	 * JCR_CREATED_BY fields because we cannot force these fields and this is
	 * problematic for instance when importing old activities
	 */
	public Node configureActivity(Node activity, String reporterId, String type, String title, String desc,
			List<Node> relatedTo, Calendar date) throws RepositoryException;

	/**
	 * Returns the default activity English Label if defined.
	 * 
	 * It relies on the mixin of this node. If an activity Node is having more
	 * than one "activity type" mixin, the main type as returned by the
	 * {@link AppService#getMainNodeType(Node)} methods will be used.
	 */
	public String getActivityLabel(Node activity);

	/**
	 * Retrieves a date to display depending on the node type.
	 * 
	 * @param activityNode
	 *            an activity or a task
	 * @return
	 */
	public Calendar getActivityRelevantDate(Node activityNode);

	/* TASKS */
	/**
	 * Creates a new draft task with the minimum information
	 * 
	 * @param taskNodeType
	 */
	public Node configureTask(Node task, String taskNodeType, String title, String description, String assignedTo)
			throws RepositoryException;

	/**
	 * Creates a new draft task with the given information.
	 * 
	 * @param session
	 * @param taskNodeType
	 * @param reporterId
	 * @param title
	 * @param description
	 * @param assignedTo
	 * @param relatedTo
	 * @param creationDate
	 * @param dueDate
	 * @param wakeUpDate
	 * @return
	 */
	public Node configureTask(Node task, String taskNodeType, String reporterId, String title, String description,
			String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar dueDate, Calendar wakeUpDate)
			throws RepositoryException;

	public void setTaskDefaultStatus(Node taskNode, String taskNodeType) throws RepositoryException;

	// public Node createPoll(Node parentNode, String reporterId, String
	// pollName, String title, String description,
	// String assignedTo, List<Node> relatedTo, Calendar creationDate, Calendar
	// dueDate, Calendar wakeUpDate)
	// throws RepositoryException;

	/**
	 * Retrieves tasks assigned to one of the group that contain the username
	 * retrieved from the current session
	 */
	public NodeIterator getMyTasks(Session session, boolean onlyOpenTasks);

	/**
	 * Retrieves tasks assigned to one of the group that contain this username
	 */
	public NodeIterator getTasksForUser(Session session, String username, boolean onlyOpenTasks);

	/** Gets the display name of the assigned to group for this task */
	public String getAssignedToDisplayName(Node taskNode);

	/**
	 * Updates the status of this task to the new passed status. It is also in
	 * charge of updating other task properties, typically after determining if
	 * the closed status of the task has changed.
	 * 
	 * It also enable to keep a cache of modified node if the update triggered
	 * modification or update on other nodes.
	 * 
	 * It will return true if anything has changed but *DOES NOT SAVE* the
	 * session
	 */
	public boolean updateStatus(String templateId, Node taskNode, String newStatus, List<String> modifiedPaths)
			throws RepositoryException;

	/**
	 * Determines whether a task has been done. Application should override or
	 * extends this to provide specific behaviour
	 */
	public boolean isTaskDone(Node taskNode);

	/**
	 * Determines whether is sleeping. Application should override or extends
	 * this to provide specific behaviour
	 */
	public boolean isTaskSleeping(Node taskNode);

	/* MISCELLANEOUS */
}
