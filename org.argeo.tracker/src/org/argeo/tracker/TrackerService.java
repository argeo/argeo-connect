package org.argeo.tracker;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;

/**
 * Manage Issue tracking concepts based on the Activities management processes
 */
public interface TrackerService extends AppService {

	/** Simply configure an IT Project node */
	public Node configureItProject(Node project, String title, String description, String managerId,
			String counterpartyGroupId) throws RepositoryException;

	public void configureCustomACL(Node node);

	/**
	 * 
	 * @param title
	 * @param description
	 * @param targetId
	 * @param priority
	 * @param importance
	 * @param managerId
	 * @param versionId
	 * @return
	 * @throws RepositoryException
	 */
	public void configureIssue(Node issue, Node project, Node milestone, String title, String description,
			List<String> versionIds, List<String> componentIds, int priority, int importance, String managerId)
			throws RepositoryException;

	public void configureTask(Node task, Node project, Node milestone, String title, String description,
			String managerId) throws RepositoryException;

	public void configureMilestone(Node milestone, Node project, Node parentMilestone, String title, String description,
			String managerId, String defaultAssigneeId, Calendar targetDate) throws RepositoryException;

	public void configureVersion(Node version, Node project, String id, String description, Calendar releaseDate)
			throws RepositoryException;

	public void configureProject(Node project, String title, String description, String managerId)
			throws RepositoryException;

	public Node addComment(Node parentIssue, String description) throws RepositoryException;

	public boolean updateComment(Node comment, String newDescription) throws RepositoryException;

	/**
	 * 
	 * @param project
	 * @param id
	 * @param description
	 * @param targetDate
	 * @param releaseDate
	 * @return
	 * @throws RepositoryException
	 */
	public Node createVersion(Node project, String id, String description, Calendar targetDate, Calendar releaseDate)
			throws RepositoryException;

	/**
	 * 
	 * @param project
	 * @param title
	 * @param description
	 * @return
	 * @throws RepositoryException
	 */
	public Node createComponent(Node project, String officeId, String title, String description)
			throws RepositoryException;

	/* Remarkable queries */
	public NodeIterator getMyProjects(Session session, boolean onlyOpenProjects);

	public NodeIterator getMyMilestones(Session session, boolean onlyOpenTasks);

}
