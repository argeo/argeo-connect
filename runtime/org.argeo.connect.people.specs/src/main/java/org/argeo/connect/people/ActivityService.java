package org.argeo.connect.people;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Provides method interfaces to activity and tasks concept in a people
 * repository Implementing applications should extend and/or override the
 * canonical implementation in order to provide business specific behavioours.
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

	/* TASKS */

	/**
	 * Creates a new task given some information. If no parent node is provided,
	 * the task is created using the same path policy as all other activity
	 * types eg, by instance:
	 * people:system/people:activities/Y2014/M01/D14/H12/root/Task
	 * 
	 * Corresponding node is not saved. Either a valid session or a parent Node
	 * should be provided
	 **/
	public Node createTask(Session session, Node parentNode, String title,
			String description, Node assignedTo, List<Node> relatedTo,
			Calendar dueDate, Calendar awakeDate);

	/* MISCELLANEOUS */
}
