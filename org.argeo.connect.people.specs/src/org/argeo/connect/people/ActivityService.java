package org.argeo.connect.people;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Provides method interfaces to manage activity and tasks concept in a people
 * repository. Implementing applications should extend and/or override the
 * canonical implementation in order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface ActivityService {

	/* ACTIVITIES */
	/**
	 * Returns a canonical path for an activity parent only given a session. It
	 * will return something of the form:
	 * people:system/people:activities/Y2014/M01/D14/H12/myUser if myUser is the
	 * id of the user that is currently logged in the JCR Session.
	 * 
	 * **/
	public String getActivityParentCanonicalPath(Session session);

	/**
	 * Creates a new simple activity using the default path with default manager
	 * (the current logged in user) and default date (now)
	 */
	public Node createActivity(Session session, String type, String title,
			String desc, List<Node> relatedTo);

	/** Creates a new simple activity using the default path */
	public Node createActivity(Session session, Calendar date,
			String managerId, String type, String title, String desc,
			List<Node> relatedTo);

	/** Returns the default activity English Label if defined */
	public String getActivityLabel(Node activity);

	/**
	 * Try to retrieve a date to display depending on the node type.
	 * 
	 * @param activityNode
	 *            an activity or a task
	 * @return
	 */
	public Calendar getActivityRelevantDate(Node activityNode);

	/* TASKS */
	/**
	 * Creates a new default task given some information. If no parent node is
	 * provided, the task is created using the same path policy as all other
	 * activity types e.g. by instance:
	 * people:system/people:activities/Y2014/M01/D14/H12/root/Task
	 * 
	 * Either a valid session or a parent Node should be provided
	 * 
	 * Reporter is by default current logged in User
	 **/
	public Node createTask(Session session, Node parentNode, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar wakeUpDate);

	/**
	 * Creates a new default task given some information. If no parent node is
	 * provided, the task is created using the same path policy as all other
	 * activity types eg, by instance:
	 * people:system/people:activities/Y2014/M01/D14/H12/root/Task
	 * 
	 * Either a valid session or a parent Node should be provided
	 * 
	 **/
	public Node createTask(Session session, Node parentNode, String managerId,
			String title, String description, Node assignedTo,
			List<Node> relatedTo, Calendar creationDate, Calendar dueDate,
			Calendar wakeUpDate);

	/**
	 * Creates a new task of given type with some information. If no parent node
	 * is provided, the task is created using the same path policy as all other
	 * activity types eg, by instance:
	 * people:system/people:activities/Y2014/M01/D14/H12/root/Task
	 * 
	 * Either a valid session or a parent Node should be provided
	 * 
	 **/
	public Node createTask(Session session, Node parentNode,
			String taskNodeType, String managerId, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar creationDate, Calendar dueDate, Calendar wakeUpDate);

	/**
	 * Retrieves tasks assigned to one of the group that contain the username
	 * retrieved from the current session
	 */
	public List<Node> getMyTasks(Session session, boolean onlyOpenTasks);

	/** Retrieves tasks assigned to one of the group that contain this username */
	public List<Node> getTasksForUser(Session session, String username,
			boolean onlyOpenTasks);

	/**
	 * Determines wether a task has been done. Application should override or
	 * extends this to provide specific behaviour
	 */
	public boolean isTaskDone(Node taskNode);

	/**
	 * Retrieves valid possible status for a given task. if task == null,
	 * returns default possible values for a simple task.
	 */
	public String[] getStatusList(Node task);

	/* MISCELLANEOUS */
}
