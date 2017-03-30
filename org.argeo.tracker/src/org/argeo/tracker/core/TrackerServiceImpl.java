package org.argeo.tracker.core;

import static javax.jcr.Property.JCR_DESCRIPTION;
import static javax.jcr.Property.JCR_TITLE;
import static javax.jcr.PropertyType.DATE;
import static javax.jcr.PropertyType.STRING;
import static org.argeo.connect.ConnectNames.CONNECT_UID;
import static org.argeo.connect.util.ConnectJcrUtils.get;
import static org.argeo.tracker.TrackerNames.TRACKER_PARENT_UID;
import static org.argeo.tracker.TrackerNames.TRACKER_PROJECT_UID;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import org.argeo.activities.ActivitiesService;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.RemoteJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.ui.TrackerUiConstants;

public class TrackerServiceImpl implements TrackerService {

	private ActivitiesService activitiesService;

	@Override
	public synchronized Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		Node createdNode = null;
		if (TrackerTypes.TRACKER_ISSUE.equals(nodeType)) {
			Session session = parent.getSession();
			Node project = getEntityByUid(session, parent.getPath(),
					ConnectJcrUtils.get(srcNode, TrackerNames.TRACKER_PROJECT_UID));
			createIssueIdIfNeeded(project, srcNode);
			String relPath = getDefaultRelPath(srcNode);
			createdNode = JcrUtils.mkdirs(parent, relPath);
			RemoteJcrUtils.copy(srcNode, createdNode, true);
			createdNode.addMixin(nodeType);
			JcrUtils.updateLastModified(createdNode);
			if (removeSrcNode)
				srcNode.remove();
		} else if (TrackerTypes.TRACKER_PROJECT.equals(nodeType) || TrackerTypes.TRACKER_IT_PROJECT.equals(nodeType)) {
			String relPath = getDefaultRelPath(srcNode);
			createdNode = JcrUtils.mkdirs(parent, relPath);
			RemoteJcrUtils.copy(srcNode, createdNode, true);
			createdNode.addMixin(nodeType);
			JcrUtils.updateLastModified(createdNode);
			if (removeSrcNode)
				srcNode.remove();
		}
		return createdNode;
	}

	@Override
	public String getAppBaseName() {
		return TrackerConstants.TRACKER_APP_BASE_NAME;
	}

	@Override
	public String getBaseRelPath(String nodeType) {
		if (TrackerTypes.TRACKER_PROJECT.equals(nodeType) || TrackerTypes.TRACKER_IT_PROJECT.equals(nodeType)
				|| TrackerTypes.TRACKER_ISSUE.equals(nodeType))
			return TrackerNames.TRACKER_PROJECTS;
		else
			return getAppBaseName();
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		if (entity.isNodeType(TrackerTypes.TRACKER_ISSUE)) {
			Session session = entity.getSession();
			Node project = getEntityByUid(session, null, ConnectJcrUtils.get(entity, TrackerNames.TRACKER_PROJECT_UID));
			String issueIdStr = ConnectJcrUtils.get(entity, TrackerNames.TRACKER_ID);
			return getDefaultRelPath(project) + "/" + TrackerNames.TRACKER_ISSUES + "/" + issueIdStr;
		} else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT)
				|| entity.isNodeType(TrackerTypes.TRACKER_IT_PROJECT)) {
			String title = entity.getProperty(Property.JCR_TITLE).getString();
			String name = cleanTitle(title);
			return name;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodeType, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isKnownType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_PROJECT)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_IT_PROJECT)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_ISSUE)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_COMMENT)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_VERSION)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_MILESTONE)
				|| ConnectJcrUtils.isNodeType(entity, TrackerTypes.TRACKER_COMPONENT))
			return true;
		else
			return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		if (TrackerTypes.TRACKER_PROJECT.equals(nodeType) || TrackerTypes.TRACKER_IT_PROJECT.equals(nodeType)
				|| TrackerTypes.TRACKER_ISSUE.equals(nodeType) || TrackerTypes.TRACKER_COMMENT.equals(nodeType)
				|| TrackerTypes.TRACKER_VERSION.equals(nodeType) || TrackerTypes.TRACKER_MILESTONE.equals(nodeType)
				|| TrackerTypes.TRACKER_COMPONENT.equals(nodeType))
			return true;
		else
			return false;
	}

	// /**
	// * Centralises the management of known types to provide corresponding base
	// * path
	// */
	// private String getBasePath(String entityType) {
	// if (TrackerTypes.TRACKER_PROJECT.equals(entityType))
	// return "/projects";
	// else
	// throw new TrackerException("Unvalid entity type");
	// }

	/** No check is done to see if a similar project already exists */
	@Override
	public Node configureItProject(Node draftProject, String title, String description, String managerId,
			String counterpartyGroupId) throws RepositoryException {
		draftProject.setProperty(TrackerNames.TRACKER_CP_GROUP_ID, counterpartyGroupId);
		draftProject.setProperty(Property.JCR_TITLE, title);
		draftProject.setProperty(Property.JCR_DESCRIPTION, description);
		JcrUtils.mkdirs(draftProject, TrackerNames.TRACKER_DATA, NodeType.NT_FOLDER);
		JcrUtils.mkdirs(draftProject, TrackerNames.TRACKER_SPEC, CmsTypes.CMS_TEXT);
		JcrUtils.mkdirs(draftProject, TrackerNames.TRACKER_VERSIONS);
		JcrUtils.mkdirs(draftProject, TrackerNames.TRACKER_ISSUES);

		// // Nodes must be saved before the rights are assigned.
		// if (project.getSession().hasPendingChanges())
		// project.getSession().save();
		//
		// // TODO refine privileges for client group
		// JcrUtils.addPrivilege(project.getSession(), project.getPath(),
		// counterpartyGroupId, Privilege.JCR_READ);
		// JcrUtils.addPrivilege(project.getSession(), issuesParent.getPath(),
		// counterpartyGroupId, Privilege.JCR_ALL);
		// session.save();
		return draftProject;
	}

	@Override
	public void configureCustomACL(Node node) {
		try {
			if (node.isNodeType(TrackerTypes.TRACKER_PROJECT) || node.isNodeType(TrackerTypes.TRACKER_IT_PROJECT)) {
				Session session = node.getSession();
				// TODO refine privileges for client group
				String basePath = node.getPath();
				String counterpartyGroupId = node.getProperty(TrackerNames.TRACKER_CP_GROUP_ID).getString();
				JcrUtils.addPrivilege(session, basePath, counterpartyGroupId, Privilege.JCR_READ);
				JcrUtils.addPrivilege(session, basePath + "/" + TrackerNames.TRACKER_ISSUES, counterpartyGroupId,
						Privilege.JCR_ALL);
				session.save();
			}
		} catch (RepositoryException re) {
			throw new TrackerException("Cannot onfigure ACL on" + node, re);
		}
	}

	@Override
	public Node configureIssue(Node issue, Node project, String title, String description, String targetId,
			List<String> versionIds, List<String> componentIds, int priority, int importance, String managerId)
			throws RepositoryException {
		activitiesService.configureTask(issue, TrackerTypes.TRACKER_ISSUE, title, description, managerId);
		// TODO Useless?
		// activitiesService.setTaskDefaultStatus(issue,
		// TrackerTypes.TRACKER_ISSUE);
		issue.setProperty(TrackerNames.TRACKER_PROJECT_UID, project.getProperty(ConnectNames.CONNECT_UID).getString());
		issue.setProperty(TrackerNames.TRACKER_PRIORITY, priority);
		issue.setProperty(TrackerNames.TRACKER_IMPORTANCE, importance);
		if (EclipseUiUtils.notEmpty(targetId))
			issue.setProperty(TrackerNames.TRACKER_TARGET_ID, targetId);
		if (versionIds != null && !versionIds.isEmpty()) {
			issue.setProperty(TrackerNames.TRACKER_VERSION_IDS, versionIds.toArray(new String[0]));
		}
		if (componentIds != null && !componentIds.isEmpty())
			issue.setProperty(TrackerNames.TRACKER_COMPONENT_IDS, componentIds.toArray(new String[0]));

		// String issueIdStr = createIssueIdIfNeeded(project, issue) + "";
		// String currName = issue.getName();
		// if (!issueIdStr.equals(currName))
		// issue.getSession().move(issue.getPath(), parentIssue.getPath() + "/"
		// + issueIdStr);
		return issue;
	}

	@Override
	public void configureMilestone(Node milestone, Node project, Node parentMilestone, String title, String description,
			String managerId, String defaultAssigneeId, Calendar targetDate) throws RepositoryException {
		ConnectJcrUtils.setJcrProperty(milestone, TRACKER_PROJECT_UID, STRING, get(project, CONNECT_UID));
		ConnectJcrUtils.setJcrProperty(milestone, TRACKER_PARENT_UID, STRING, get(parentMilestone, CONNECT_UID));
		ConnectJcrUtils.setJcrProperty(milestone, JCR_TITLE, STRING, title);
		ConnectJcrUtils.setJcrProperty(milestone, JCR_DESCRIPTION, STRING, description);
		// TODO check if users are really existing
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_MANAGER, STRING, managerId);
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_DEFAULT_ASSIGNEE, STRING, defaultAssigneeId);
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_TARGET_DATE, DATE, targetDate);
	}

	public void configureProject(Node project, String title, String description, String managerId)
			throws RepositoryException {
		ConnectJcrUtils.setJcrProperty(project, JCR_TITLE, STRING, title);
		ConnectJcrUtils.setJcrProperty(project, JCR_DESCRIPTION, STRING, description);
		// TODO check if users are really existing
		ConnectJcrUtils.setJcrProperty(project, TrackerNames.TRACKER_MANAGER, STRING, managerId);
	}

	private final static DateFormat isobdf = new SimpleDateFormat(TrackerUiConstants.isoDateBasicFormat);

	@Override
	public Node addComment(Node parentIssue, String description) throws RepositoryException {
		Node comments = JcrUtils.mkdirs(parentIssue, TrackerNames.TRACKER_COMMENTS);
		String currUid = UserAdminUtils.getUserLocalId(CurrentUser.getUsername());
		String timeStamp = isobdf.format(new Date());
		Node comment = comments.addNode(timeStamp + "_" + currUid);
		comment.addMixin(TrackerTypes.TRACKER_COMMENT);
		ConnectJcrUtils.setJcrProperty(comment, Property.JCR_DESCRIPTION, PropertyType.STRING, description);
		return comment;
	}

	@Override
	public boolean updateComment(Node comment, String newDescription) throws RepositoryException {
		boolean hasChanged = ConnectJcrUtils.setJcrProperty(comment, Property.JCR_DESCRIPTION, PropertyType.STRING,
				newDescription);
		if (hasChanged)
			JcrUtils.updateLastModified(comment);
		return hasChanged;
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

	// private static Node getProjectFromIssue(Node issue) throws
	// RepositoryException {
	// Node parent = issue;
	// while (!parent.isNodeType(TrackerTypes.TRACKER_PROJECT)) {
	// parent = parent.getParent();
	// }
	// return parent;
	// }

	private static Node getIssueParent(Node project) throws RepositoryException {
		// Should always be there
		return project.getNode(TrackerUtils.issuesRelPath());
	}

	// private static Node getVersionParent(Node project) throws
	// RepositoryException {
	// // Should always be there
	// return project.getNode(TrackerUtils.versionsRelPath());
	// }

	// FIXME harden to avoid discrepancy in numbering while having concurrent
	// access
	protected long createIssueIdIfNeeded(Node project, Node issue) throws RepositoryException {
		Long issueId = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_ID);
		if (issueId == null) {
			Node issueParent = getIssueParent(project);
			String xpathQueryStr = XPathUtils.descendantFrom(issueParent.getPath()) + "//element(*, "
					+ TrackerTypes.TRACKER_ISSUE + ")";
			xpathQueryStr += " order by @" + TrackerNames.TRACKER_ID + " descending";
			QueryManager queryManager = project.getSession().getWorkspace().getQueryManager();
			Query query = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			query.setLimit(1);
			NodeIterator nit = query.execute().getNodes();
			issueId = 1l;
			if (nit.hasNext())
				issueId = ConnectJcrUtils.getLongValue(nit.nextNode(), TrackerNames.TRACKER_ID) + 1;
			issue.setProperty(TrackerNames.TRACKER_ID, issueId);
		}
		return issueId;
	}

	// Local helpers
	private static String cleanTitle(String title) {
		String name = title.replaceAll("[^a-zA-Z0-9]", "");
		return name;
	}

	/* DEPENDENCY INJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
