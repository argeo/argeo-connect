package org.argeo.connect.tracker.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.security.Privilege;

import org.argeo.cms.CmsTypes;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.core.ActivityServiceImpl;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.tracker.internal.ui.TrackerUiConstants;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

public class TrackerServiceImpl extends ActivityServiceImpl implements TrackerService {

	private PeopleService aoService;

	public TrackerServiceImpl(PeopleService peopleService) {
		super((PeopleService) peopleService);
		this.aoService = peopleService;
	}

	/** No check is done to see if a similar project already exists */
	@Override
	public Node createProject(Session session, String title, String description, String managerId,
			String counterpartyGroupId) {
		try {
			String parPath = aoService.getBasePath(TrackerTypes.TRACKER_PROJECT);
			Node projects = session.getNode(parPath);
			String name = cleanTitle(title);
			Node project = projects.addNode(name);
			project.addMixin(TrackerTypes.TRACKER_PROJECT);
			project.setProperty(TrackerNames.TRACKER_CP_GROUP_ID, counterpartyGroupId);
			project.setProperty(Property.JCR_TITLE, title);
			project.setProperty(Property.JCR_DESCRIPTION, description);
			JcrUtils.mkdirs(project, TrackerNames.TRACKER_DATA, NodeType.NT_FOLDER);
			JcrUtils.mkdirs(project, TrackerNames.TRACKER_SPEC, CmsTypes.CMS_TEXT);
			JcrUtils.mkdirs(project, TrackerNames.TRACKER_VERSIONS, NodeType.NT_UNSTRUCTURED);
			Node issuesParent = JcrUtils.mkdirs(project, TrackerNames.TRACKER_ISSUES, NodeType.NT_UNSTRUCTURED);

			// Nodes must be saved before the rights are assigned.
			if (project.getSession().hasPendingChanges())
				project.getSession().save();

			// TODO refine privileges for client group
			JcrUtils.addPrivilege(project.getSession(), project.getPath(), counterpartyGroupId, Privilege.JCR_READ);
			JcrUtils.addPrivilege(project.getSession(), issuesParent.getPath(), counterpartyGroupId, Privilege.JCR_ALL);

			session.save();

			return project;
		} catch (RepositoryException re) {
			throw new TrackerException("Cannot create project " + title, re);
		}

	}

	@Override
	public Node createIssue(Node parentIssue, String title, String description, String versionId, String targetId,
			int priority, int importance, String managerId) throws RepositoryException {
		Node issue = createTask(parentIssue, title, description, managerId);
		issue.addMixin(TrackerTypes.TRACKER_ISSUE);
		setTaskDefaultStatus(issue, TrackerTypes.TRACKER_ISSUE);
		issue.setProperty(Property.JCR_TITLE, title);
		issue.setProperty(TrackerNames.TRACKER_PRIORITY, priority);
		issue.setProperty(TrackerNames.TRACKER_IMPORTANCE, importance);
		if (EclipseUiUtils.notEmpty(description))
			issue.setProperty(Property.JCR_DESCRIPTION, description);
		if (EclipseUiUtils.notEmpty(targetId))
			issue.setProperty(TrackerNames.TRACKER_TARGET_ID, targetId);
		if (EclipseUiUtils.notEmpty(versionId)) {
			// if (versionId != null) {
			String[] versions = { versionId };
			issue.setProperty(TrackerNames.TRACKER_VERSION_IDS, versions);
		}
		String issueIdStr = createIssueIdIfNeeded(TrackerUtils.getProjectFromChild(parentIssue), issue) + "";
		String currName = issue.getName();
		if (!issueIdStr.equals(currName))
			issue.getSession().move(issue.getPath(), parentIssue.getPath() + "/" + issueIdStr);
		return issue;
	}

	private final static DateFormat isobdf = new SimpleDateFormat(TrackerUiConstants.isoDateBasicFormat);

	@Override
	public Node addComment(Node parentIssue, String description) throws RepositoryException {
		Node comments = JcrUtils.mkdirs(parentIssue, TrackerNames.TRACKER_COMMENTS);
		String currUid = UserAdminUtils.getUserLocalId(CurrentUser.getUsername());
		String timeStamp = isobdf.format(new Date());
		Node comment = comments.addNode(timeStamp + "_" + currUid);
		comment.addMixin(TrackerTypes.TRACKER_COMMENT);
		JcrUiUtils.setJcrProperty(comment, Property.JCR_DESCRIPTION, PropertyType.STRING, description);
		return comment;
	}

	@Override
	public boolean updateComment(Node comment, String newDescription) throws RepositoryException {
		boolean hasChanged = JcrUiUtils.setJcrProperty(comment, Property.JCR_DESCRIPTION, PropertyType.STRING,
				newDescription);
		if (hasChanged)
			JcrUtils.updateLastModified(comment);
		return hasChanged;
	}

	/** Encapsulate the activity service create task to enhance read-ability */
	private Node createTask(Node parentNode, String title, String description, String assignedTo) {
		return createTask(null, parentNode, PeopleTypes.PEOPLE_TASK, null, title, description, assignedTo, null, null,
				null, null);
	}

	@Override
	public Node createVersion(Node project, String versionId, String description, Calendar targetDate,
			Calendar releaseDate) throws RepositoryException {
		Node version = JcrUtils.mkdirs(project, TrackerUtils.versionsRelPath() + "/" + versionId,
				NodeType.NT_UNSTRUCTURED);
		version.addMixin(TrackerTypes.TRACKER_VERSION);
		version.setProperty(TrackerNames.TRACKER_ID, versionId);
		version.setProperty(Property.JCR_TITLE, versionId);
		if (EclipseUiUtils.notEmpty(description))
			version.setProperty(Property.JCR_DESCRIPTION, description);
		if (targetDate != null)
			version.setProperty(TrackerNames.TRACKER_TARGET_DATE, targetDate);
		if (releaseDate != null)
			version.setProperty(TrackerNames.TRACKER_RELEASE_DATE, releaseDate);
		return version;
	}

	@Override
	public Node createComponent(Node project, String officeId, String title, String description)
			throws RepositoryException {
		Node component = JcrUtils.mkdirs(project, TrackerUtils.componentsRelPath() + "/" + officeId,
				NodeType.NT_UNSTRUCTURED);
		component.addMixin(TrackerTypes.TRACKER_COMPONENT);
		component.setProperty(TrackerNames.TRACKER_ID, officeId);
		component.setProperty(Property.JCR_TITLE, title);
		if (EclipseUiUtils.notEmpty(description))
			component.setProperty(Property.JCR_DESCRIPTION, description);
		return component;
	}

	private static Node getProjectFromIssue(Node issue) throws RepositoryException {
		Node parent = issue;
		while (!parent.isNodeType(TrackerTypes.TRACKER_PROJECT)) {
			parent = parent.getParent();
		}
		return parent;
	}

	private static Node getIssueParent(Node project) throws RepositoryException {
		// Should always be there
		return project.getNode(TrackerUtils.issuesRelPath());
	}

	private static Node getVersionParent(Node project) throws RepositoryException {
		// Should always be there
		return project.getNode(TrackerUtils.versionsRelPath());
	}

	// FIXME harden to avoid discrepancy in numbering while having concurrent
	// access
	protected long createIssueIdIfNeeded(Node project, Node issue) throws RepositoryException {
		Long issueId = JcrUiUtils.getLongValue(issue, TrackerNames.TRACKER_ID);
		if (issueId == null) {
			Node issueParent = getIssueParent(project);
			String xpathQueryStr = XPathUtils.descendantFrom(issueParent.getPath()) + "//element(*, "
					+ TrackerTypes.TRACKER_ISSUE + ")";
			xpathQueryStr += " order by @" + TrackerNames.TRACKER_ID + " descending";
			QueryManager queryManager = project.getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(xpathQueryStr, PeopleConstants.QUERY_XPATH);
			query.setLimit(1);
			NodeIterator nit = query.execute().getNodes();
			issueId = 1l;
			if (nit.hasNext())
				issueId = JcrUiUtils.getLongValue(nit.nextNode(), TrackerNames.TRACKER_ID) + 1;
			issue.setProperty(TrackerNames.TRACKER_ID, issueId);
		}
		return issueId;
	}

	// Local helpers
	private static String cleanTitle(String title) {
		String name = title.replaceAll("[^a-zA-Z0-9]", "");
		return name;
	}
}
