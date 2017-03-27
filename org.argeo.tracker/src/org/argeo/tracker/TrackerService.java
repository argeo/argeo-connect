package org.argeo.tracker;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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
	public Node configureIssue(Node issue, Node project, String title, String description, String targetId,
			List<String> versionIds, List<String> componentIds, int priority, int importance, String managerId)
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
}
