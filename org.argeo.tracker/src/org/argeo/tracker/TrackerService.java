package org.argeo.tracker;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;

/**
 * Manage Issue tracking concepts based on the Activities management processes
 */
public interface TrackerService extends AppService {

	/** Create a new project. Session is saved so that we can configure ACL */
	public Node createProject(Session session, String title, String description, String managerId,
			String counterpartyGroupId);

	/**
	 * 
	 * @param parentIssue
	 * @param title
	 * @param description
	 * @param versionId
	 * @param targetId
	 * @param priority
	 * @param importance
	 * @param managerId
	 * @return
	 * @throws RepositoryException
	 */
	public Node createIssue(Node parentIssue, String title, String description, String versionId, String targetId,
			int priority, int importance, String managerId) throws RepositoryException;

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
}
