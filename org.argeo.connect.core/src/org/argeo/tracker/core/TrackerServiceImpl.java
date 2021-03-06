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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.auth.UserAdminUtils;
import org.argeo.connect.CmsTypes;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.core.AbstractAppService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.connect.util.RemoteJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;

public class TrackerServiceImpl extends AbstractAppService implements TrackerService {
	private final static Log log = LogFactory.getLog(TrackerServiceImpl.class);

	private ActivitiesService activitiesService;

	@Override
	public synchronized Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		Node createdNode = null;
		if (TrackerTypes.TRACKER_ISSUE.equals(nodeType) || TrackerTypes.TRACKER_TASK.equals(nodeType)
				|| TrackerTypes.TRACKER_COMPONENT.equals(nodeType) || TrackerTypes.TRACKER_MILESTONE.equals(nodeType)
				|| TrackerTypes.TRACKER_VERSION.equals(nodeType)) {
			Session session = parent.getSession();
			Node project = getEntityByUid(session, null, get(srcNode, TRACKER_PROJECT_UID));
			if (TrackerTypes.TRACKER_ISSUE.equals(nodeType) || TrackerTypes.TRACKER_TASK.equals(nodeType))
				createIssueIdIfNeeded(project, srcNode);
			String relPath = getDefaultRelPath(srcNode);
			createdNode = JcrUtils.mkdirs(project, relPath);
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
				|| TrackerTypes.TRACKER_MILESTONE.equals(nodeType) || TrackerTypes.TRACKER_VERSION.equals(nodeType)
				|| TrackerTypes.TRACKER_COMPONENT.equals(nodeType) || TrackerTypes.TRACKER_ISSUE.equals(nodeType)
				|| TrackerTypes.TRACKER_TASK.equals(nodeType))
			return TrackerNames.TRACKER_PROJECTS;
		else
			return getAppBaseName();
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		if (entity.isNodeType(TrackerTypes.TRACKER_TASK)) {
			String issueIdStr = ConnectJcrUtils.get(entity, TrackerNames.TRACKER_ID);
			return TrackerNames.TRACKER_ISSUES + "/" + issueIdStr;
		} else if (entity.isNodeType(TrackerTypes.TRACKER_PROJECT)
				|| entity.isNodeType(TrackerTypes.TRACKER_IT_PROJECT)) {
			String title = entity.getProperty(Property.JCR_TITLE).getString();
			String name = cleanTitle(title);
			return name;
		} else if (entity.isNodeType(TrackerTypes.TRACKER_MILESTONE)
				|| entity.isNodeType(TrackerTypes.TRACKER_VERSION)) {
			String title = entity.getProperty(Property.JCR_TITLE).getString();
			String name = cleanTitle(title);
			return TrackerNames.TRACKER_MILESTONES + "/" + name;
		} else if (entity.isNodeType(TrackerTypes.TRACKER_COMPONENT)) {
			String title = entity.getProperty(Property.JCR_TITLE).getString();
			String name = cleanTitle(title);
			return TrackerNames.TRACKER_COMPONENTS + "/" + name;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodeType, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final String[] KNOWN_MIXIN = { TrackerTypes.TRACKER_PROJECT, TrackerTypes.TRACKER_IT_PROJECT,
			TrackerTypes.TRACKER_ISSUE, TrackerTypes.TRACKER_TASK, TrackerTypes.TRACKER_COMMENT,
			TrackerTypes.TRACKER_VERSION, TrackerTypes.TRACKER_MILESTONE, TrackerTypes.TRACKER_COMPONENT };

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

	@Override
	public boolean isKnownType(Node entity) {
		for (String mixin : KNOWN_MIXIN)
			if (ConnectJcrUtils.isNodeType(entity, mixin))
				return true;
		return false;
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

	/** No check is done to see if a similar project already exists */
	@Override
	public void configureItProject(Node itProject, String title, String description, String managerId,
			String counterpartyGroupId) throws RepositoryException {
		itProject.setProperty(TrackerNames.TRACKER_CP_GROUP_ID, counterpartyGroupId);
		itProject.setProperty(Property.JCR_TITLE, title);
		itProject.setProperty(Property.JCR_DESCRIPTION, description);
		JcrUtils.mkdirs(itProject, TrackerNames.TRACKER_DATA, NodeType.NT_FOLDER);
		JcrUtils.mkdirs(itProject, TrackerNames.TRACKER_SPEC, CmsTypes.CMS_TEXT);
		JcrUtils.mkdirs(itProject, TrackerNames.TRACKER_MILESTONES);
		JcrUtils.mkdirs(itProject, TrackerNames.TRACKER_ISSUES);

		if (itProject.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(itProject);
	}

	public void configureProject(Node project, String title, String description, String managerId)
			throws RepositoryException {
		ConnectJcrUtils.setJcrProperty(project, JCR_TITLE, STRING, title);
		ConnectJcrUtils.setJcrProperty(project, JCR_DESCRIPTION, STRING, description);
		// TODO check if users are really existing
		ConnectJcrUtils.setJcrProperty(project, TrackerNames.TRACKER_MANAGER, STRING, managerId);

		if (project.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(project);
	}

	@Override
	public void configureTask(Node task, Node project, Node milestone, String title, String description,
			String managerId) throws RepositoryException {
		activitiesService.configureTask(task, TrackerTypes.TRACKER_TASK, title, description, managerId);
		task.setProperty(TrackerNames.TRACKER_PROJECT_UID, project.getProperty(ConnectNames.CONNECT_UID).getString());
		if (milestone != null)
			task.setProperty(TrackerNames.TRACKER_MILESTONE_UID,
					milestone.getProperty(ConnectNames.CONNECT_UID).getString());
		else if (task.hasProperty(TrackerNames.TRACKER_MILESTONE_UID))
			task.getProperty(TrackerNames.TRACKER_MILESTONE_UID).remove();

		activitiesService.setTaskDefaultStatus(task, TrackerTypes.TRACKER_TASK);

		if (task.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(task);
	}

	@Override
	public void configureIssue(Node issue, Node project, Node milestone, String title, String description,
			List<String> versionIds, List<String> componentIds, int priority, int importance, String managerId)
			throws RepositoryException {
		activitiesService.configureTask(issue, TrackerTypes.TRACKER_ISSUE, title, description, managerId);
		// TODO Useless?
		issue.setProperty(TrackerNames.TRACKER_PROJECT_UID, project.getProperty(ConnectNames.CONNECT_UID).getString());
		issue.setProperty(TrackerNames.TRACKER_PRIORITY, priority);
		issue.setProperty(TrackerNames.TRACKER_IMPORTANCE, importance);
		if (milestone != null) {
			issue.setProperty(TrackerNames.TRACKER_MILESTONE_UID,
					milestone.getProperty(ConnectNames.CONNECT_UID).getString());
			// String targetId = ConnectJcrUtils.get(milestone,
			// TrackerNames.TRACKER_ID);
			// if (EclipseUiUtils.notEmpty(targetId))
			// issue.setProperty(TrackerNames.TRACKER_MILESTONE_ID, targetId);
		} else {
			if (issue.hasProperty(TrackerNames.TRACKER_MILESTONE_UID))
				issue.getProperty(TrackerNames.TRACKER_MILESTONE_UID).remove();
			// if (issue.hasProperty(TrackerNames.TRACKER_MILESTONE_ID))
			// issue.getProperty(TrackerNames.TRACKER_MILESTONE_ID).remove();
		}

		if (versionIds != null && !versionIds.isEmpty()) {
			issue.setProperty(TrackerNames.TRACKER_VERSION_IDS, versionIds.toArray(new String[0]));
		}
		if (componentIds != null && !componentIds.isEmpty())
			issue.setProperty(TrackerNames.TRACKER_COMPONENT_IDS, componentIds.toArray(new String[0]));

		if (issue.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(issue);
	}

	@Override
	public void configureMilestone(Node milestone, Node project, Node parentMilestone, String title, String description,
			String managerId, String defaultAssigneeId, Calendar targetDate) throws RepositoryException {
		ConnectJcrUtils.setJcrProperty(milestone, TRACKER_PROJECT_UID, STRING, get(project, CONNECT_UID));
		if (parentMilestone != null)
			ConnectJcrUtils.setJcrProperty(milestone, TRACKER_PARENT_UID, STRING, get(parentMilestone, CONNECT_UID));
		else if (milestone.hasProperty(TRACKER_PARENT_UID))
			milestone.getProperty(TRACKER_PARENT_UID).remove();
		ConnectJcrUtils.setJcrProperty(milestone, JCR_TITLE, STRING, title);
		ConnectJcrUtils.setJcrProperty(milestone, JCR_DESCRIPTION, STRING, description);
		// TODO check if users are really existing
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_MANAGER, STRING, managerId);
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_DEFAULT_ASSIGNEE, STRING, defaultAssigneeId);
		ConnectJcrUtils.setJcrProperty(milestone, TrackerNames.TRACKER_TARGET_DATE, DATE, targetDate);

		if (milestone.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(milestone);
	}

	@Override
	public void configureVersion(Node version, Node project, String id, String description, Calendar releaseDate)
			throws RepositoryException {
		ConnectJcrUtils.setJcrProperty(version, TRACKER_PROJECT_UID, STRING, get(project, CONNECT_UID));
		ConnectJcrUtils.setJcrProperty(version, TrackerNames.TRACKER_ID, STRING, id);
		if (!version.isNodeType(TrackerTypes.TRACKER_MILESTONE))
			ConnectJcrUtils.setJcrProperty(version, JCR_TITLE, STRING, id);
		ConnectJcrUtils.setJcrProperty(version, JCR_DESCRIPTION, STRING, description);
		ConnectJcrUtils.setJcrProperty(version, TrackerNames.TRACKER_RELEASE_DATE, DATE, releaseDate);

		if (version.getSession().hasPendingChanges())
			JcrUtils.updateLastModified(version);
	}

	@Override
	public NodeIterator getMyProjects(Session session, boolean onlyOpenProjects) {
		List<String> normalisedRoles = new ArrayList<>();
		for (String role : CurrentUser.roles())
			normalisedRoles.add(TrackerUtils.normalizeDn(role));
		String[] nrArr = normalisedRoles.toArray(new String[0]);
		return getProjectsForGroup(session, nrArr, onlyOpenProjects);
	}

	private NodeIterator getProjectsForGroup(Session session, String[] roles, boolean onlyOpenProjects) {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_PROJECT).append(")");

			// Assigned to
			StringBuilder tmpBuilder = new StringBuilder();
			for (String role : roles) {
				String attrQuery = XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MANAGER, role);
				if (ConnectUtils.notEmpty(attrQuery))
					tmpBuilder.append(attrQuery).append(" or ");
			}
			String groupCond = null;
			if (tmpBuilder.length() > 4)
				groupCond = "(" + tmpBuilder.substring(0, tmpBuilder.length() - 3) + ")";

			// Only open
			String notClosedCond = null;
			if (onlyOpenProjects)
				notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";

			String allCond = XPathUtils.localAnd(groupCond, notClosedCond);
			if (EclipseUiUtils.notEmpty(allCond))
				builder.append("[").append(allCond).append("]");

			builder.append(" order by @").append(Property.JCR_LAST_MODIFIED).append(" descending");
			if (log.isTraceEnabled())
				log.trace("Getting open project list for " + CurrentUser.getDisplayName() + " (DN: "
						+ CurrentUser.getUsername() + ") with query: " + builder.toString());
			Query query = XPathUtils.createQuery(session, builder.toString());
			return query.execute().getNodes();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get milestones for groups " + roles.toString());
		}
	}

	public NodeIterator getMyMilestones(Session session, boolean onlyOpenMilestones) {
		List<String> normalisedRoles = new ArrayList<>();
		for (String role : CurrentUser.roles())
			normalisedRoles.add(TrackerUtils.normalizeDn(role));
		String[] nrArr = normalisedRoles.toArray(new String[0]);
		return getMilestonesForGroup(session, nrArr, onlyOpenMilestones);
	}

	private NodeIterator getMilestonesForGroup(Session session, String[] roles, boolean onlyOpenMilestones) {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_MILESTONE).append(")");

			// Assigned to
			StringBuilder tmpBuilder = new StringBuilder();
			for (String role : roles) {
				String attrQuery = XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MANAGER, role);
//				if (StringUtils.notEmpty(attrQuery))
				tmpBuilder.append(attrQuery).append(" or ");
			}
			String groupCond = null;
			if (tmpBuilder.length() > 4)
				groupCond = "(" + tmpBuilder.substring(0, tmpBuilder.length() - 3) + ")";

			// Only open
			String notClosedCond = null;
			if (onlyOpenMilestones)
				notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";

			String allCond = XPathUtils.localAnd(groupCond, notClosedCond);
			if (EclipseUiUtils.notEmpty(allCond))
				builder.append("[").append(allCond).append("]");

			builder.append(" order by @").append(Property.JCR_LAST_MODIFIED).append(" descending");
			if (log.isTraceEnabled())
				log.trace("Getting open milestone list for " + CurrentUser.getDisplayName() + " (DN: "
						+ CurrentUser.getUsername() + ") with query: " + builder.toString());
			Query query = XPathUtils.createQuery(session, builder.toString());
			return query.execute().getNodes();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get milestones for groups " + roles.toString());
		}
	}

	private final static DateFormat isobdf = new SimpleDateFormat(TrackerConstants.ISO_BASIC_DATE_FORMAT);

	@Override
	public Node addComment(Node parentIssue, String description) throws RepositoryException {
		Node comments = JcrUtils.mkdirs(parentIssue, TrackerNames.TRACKER_COMMENTS);
		String reporterId = parentIssue.getSession().getUserID();
		String currUid = UserAdminUtils.getUserLocalId(reporterId);
		Calendar creationDate = new GregorianCalendar();
		String timeStamp = isobdf.format(creationDate.getTime());
		Node comment = comments.addNode(timeStamp + "_" + currUid);
		comment.addMixin(TrackerTypes.TRACKER_COMMENT);
		ConnectJcrUtils.setJcrProperty(comment, Property.JCR_DESCRIPTION, PropertyType.STRING, description);
		// We rather use these activity like properties to be robuster in case
		// of migration for instance
		comment.setProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE, creationDate);
		comment.setProperty(ActivitiesNames.ACTIVITIES_REPORTED_BY, reporterId);
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

	// private static Node getIssueParent(Node project) throws
	// RepositoryException {
	// // Should always be there
	// return project.getNode(TrackerUtils.issuesRelPath());
	// }

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
			String xpathQueryStr = XPathUtils.descendantFrom(project.getPath()) + "//element(*, "
					+ TrackerTypes.TRACKER_TASK + ")";
			xpathQueryStr += " order by @" + TrackerNames.TRACKER_ID + " descending";
			Query query = XPathUtils.createQuery(project.getSession(), xpathQueryStr);
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
