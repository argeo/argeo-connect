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

public class ActivityServiceImpl implements ActivityService {

	/* ACTIVITIES */
	@Override
	public String getActivityParentCanonicalPath(Session session) {
		String currentUser = session.getUserID();
		Calendar currentTime = GregorianCalendar.getInstance();
		String path = PeopleConstants.PEOPLE_TASKS_BASE_PATH + "/"
				+ JcrUtils.dateAsPath(currentTime, true) + currentUser;
		return path;
	}

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

			if (assignedTo != null)
				taskNode.setProperty(PeopleNames.PEOPLE_ASSIGNED_TO, assignedTo);

			if (relatedTo != null && !relatedTo.isEmpty()) {
				// TODO enhence this
				int size = relatedTo.size();
				String[] nodeIds = new String[size];
				int i = 0;
				for (Node node : relatedTo) {
					nodeIds[i++] = node.getIdentifier();
				}
				taskNode.setProperty(PeopleNames.PEOPLE_RELATED_TO, nodeIds);
			}

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
