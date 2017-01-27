package org.argeo.connect.tracker.core;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.argeo.connect.ConnectConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.tracker.PeopleTrackerService;
import org.argeo.connect.tracker.TrackerConstants;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.JcrUiUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;

public class TrackerUtils {

	public static final Map<String, String> DEFAULT_COMPONENTS;
	static {
		Map<String, String> tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put("Model", "Specification, design and data model");
		tmpMap.put("Backend", "Core components");
		tmpMap.put("Demo", "A basic instance for this project that can be freely shown");
		tmpMap.put("UI", "The user interface");
		tmpMap.put("Documentation", "Reference documentation");
		tmpMap.put("Integration", "Import, export, external APIs");
		tmpMap.put("QA", "Build, deployment, testing, documentation");
		DEFAULT_COMPONENTS = Collections.unmodifiableMap(tmpMap);
	}

	// Define Issue status labels
	public static final Map<String, String> MAPS_ISSUE_PRIORITIES;
	static {
		Map<String, String> tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put(TrackerConstants.TRACKER_PRIORITY_LOWEST + "", "Lowest");
		tmpMap.put(TrackerConstants.TRACKER_PRIORITY_LOW + "", "Low");
		tmpMap.put(TrackerConstants.TRACKER_PRIORITY_NORMAL + "", "Normal");
		tmpMap.put(TrackerConstants.TRACKER_PRIORITY_HIGH + "", "High");
		tmpMap.put(TrackerConstants.TRACKER_PRIORITY_HIGHEST + "", "Highest");
		MAPS_ISSUE_PRIORITIES = Collections.unmodifiableMap(tmpMap);
	}

	public static final Map<String, String> MAPS_ISSUE_IMPORTANCES;
	static {
		Map<String, String> tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_ENHANCEMENT + "", "Enhancement");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_TRIVIAL + "", "Trivial");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_MINOR + "", "Minor");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_NORMAL + "", "Normal");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_MAJOR + "", "Major");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_CRITICAL + "", "Critical");
		tmpMap.put(TrackerConstants.TRACKER_IMPORTANCE_BLOCKER + "", "Blocker");
		MAPS_ISSUE_IMPORTANCES = Collections.unmodifiableMap(tmpMap);
	}

	public static String issuesRelPath() {
		return TrackerNames.TRACKER_ISSUES;
	}

	public static String componentsRelPath() {
		return TrackerNames.TRACKER_COMPONENTS;
	}

	public static String versionsRelPath() {
		return TrackerNames.TRACKER_VERSIONS;
	}

	public static String getRelevantPropName(Node category) {
		try {
			if (category.isNodeType(TrackerTypes.TRACKER_COMPONENT))
				return TrackerNames.TRACKER_COMPONENTS;
			else if (category.isNodeType(TrackerTypes.TRACKER_VERSION)) {
				// TODO enhance the choice between milestone and version
				if (category.hasProperty(TrackerNames.TRACKER_RELEASE_DATE))
					return TrackerNames.TRACKER_VERSION_IDS;
				else
					return TrackerNames.TRACKER_TARGET_ID;
			} else
				throw new TrackerException("Unsupported category node type " + category.getMixinNodeTypes().toString()
						+ " for " + category.getPath());
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot get relevant property name for category " + category, e);
		}
	}

	/** Retrieve all projects that are visible for this session */
	public static NodeIterator getProjects(Session session, String projectParentPath) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(projectParentPath));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_PROJECT).append(")");
			builder.append(" order by @").append(PeopleNames.JCR_TITLE);
			QueryManager qm = session.getWorkspace().getQueryManager();
			QueryResult result = qm.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException(
					"Unable to get projects under " + projectParentPath + " for session " + session.getUserID(), e);
		}
	}

	/**
	 * Simply returns the milestones defined for this project. Note that
	 * ordering by ID use lexical order that won't work for version, f.i. 2.1.23
	 * & 2.1.4, it is caller duty to use a correct version comparator if the
	 * order is important
	 */
	public static NodeIterator getMilestones(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			Node parent = project.getNode(versionsRelPath());
			builder.append(XPathUtils.descendantFrom(parent.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_VERSION).append(")");
			builder.append("[not(@").append(TrackerNames.TRACKER_RELEASE_DATE).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append(" and ").append(XPathUtils.getFreeTextConstraint(filter));
			builder.append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" descending");
			QueryManager qm = parent.getSession().getWorkspace().getQueryManager();
			QueryResult result = qm.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get milestones on " + project + " with filter:" + filter, e);
		}
	}

	public static List<String> getMilestoneIds(Node project, String filter) {
		NodeIterator nit = getMilestones(project, filter);
		List<String> milestoneIds = new ArrayList<String>();
		while (nit.hasNext()) {
			Node currNode = nit.nextNode();
			milestoneIds.add(JcrUiUtils.get(currNode, TrackerNames.TRACKER_ID));
		}
		return milestoneIds;
	}

	public static NodeIterator getVersions(Node project, String filter) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		Node parent = project.getNode(versionsRelPath());
		builder.append(XPathUtils.descendantFrom(parent.getPath()));
		builder.append("//element(*, ").append(TrackerTypes.TRACKER_VERSION).append(")");
		builder.append("[@").append(TrackerNames.TRACKER_RELEASE_DATE);
		if (EclipseUiUtils.notEmpty(filter))
			builder.append(" and ").append(XPathUtils.getFreeTextConstraint(filter));
		builder.append("]");
		builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" descending");
		QueryManager qm = parent.getSession().getWorkspace().getQueryManager();
		QueryResult result = qm.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
		return result.getNodes();
	}

	public static List<String> getVersionIds(Node project, String filter) {
		try {
			NodeIterator nit = getVersions(project, filter);
			List<String> versionIds = new ArrayList<String>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				versionIds.add(JcrUiUtils.get(currNode, TrackerNames.TRACKER_ID));
			}
			return versionIds;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get version ids on " + project + " with filter:" + filter, e);
		}
	}

	public static NodeIterator getIssues(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			Node parent = project.getNode(issuesRelPath());
			builder.append(XPathUtils.descendantFrom(parent.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_ISSUE).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID);
			// .append(" descending");
			QueryManager qm = project.getSession().getWorkspace().getQueryManager();
			QueryResult result = qm.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get issues for " + project + " with filter: " + filter, e);
		}
	}

	/**
	 * Simply requests all issues of a project that are referencing this
	 * category (a version, a milestone or a component)
	 */
	public static NodeIterator getIssues(Node project, String filter, String propName, String catId) {
		return getIssues(project, filter, propName, catId, false);
	}

	/** Simply checks if an issue is closed */
	public static boolean isIssueClosed(Node issue) {
		try {
			// TODO enhance definition of closed status
			return issue.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE);
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to check closed status of " + issue, e);
		}
	}

	public static NodeIterator getIssues(Node project, String filter, String propName, String catId,
			boolean onlyOpenTasks) {
		try {
			QueryManager queryManager = project.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			Node parent = project.getNode(issuesRelPath());
			builder.append(XPathUtils.descendantFrom(parent.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_ISSUE).append(")");

			StringBuilder tmpBuilder = new StringBuilder();

			String andStr = " and ";
			if (EclipseUiUtils.notEmpty(catId)) {
				tmpBuilder.append(XPathUtils.getPropertyEquals(propName, catId));
				tmpBuilder.append(andStr);
			}

			if (EclipseUiUtils.notEmpty(filter)) {
				tmpBuilder.append(XPathUtils.getFreeTextConstraint(filter));
				tmpBuilder.append(andStr);
			}

			if (onlyOpenTasks) {
				tmpBuilder.append(" not(@");
				tmpBuilder.append(PeopleNames.PEOPLE_CLOSE_DATE);
				tmpBuilder.append(")");
				tmpBuilder.append(andStr);
			}

			if (tmpBuilder.length() > 0)
				builder.append("[").append(tmpBuilder.substring(0, tmpBuilder.length() - andStr.length())).append("]");

			builder.append(" order by @" + TrackerNames.TRACKER_ID);
			Query xpathQuery = queryManager.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH);
			QueryResult result = xpathQuery.execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get issues for " + project + " with filter: " + filter, e);
		}
	}

	public static NodeIterator getAllVersions(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			Node parent = project.getNode(versionsRelPath());
			builder.append(XPathUtils.descendantFrom(parent.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_VERSION).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" descending");
			QueryManager qm = parent.getSession().getWorkspace().getQueryManager();
			QueryResult result = qm.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get version for " + project + " with filter: " + filter, e);
		}
	}

	public static NodeIterator getComponents(Node project, String filter) {
		try {
			String relPath = componentsRelPath();
			if (!project.hasNode(relPath))
				return null;
			Node parent = project.getNode(relPath);
			QueryManager queryManager = parent.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(parent.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_COMPONENT).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" ascending");
			QueryResult result = queryManager.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get components for " + project + " with filter: " + filter, e);
		}
	}

	public static Node getVersionById(Node project, String versionId) {
		try {
			Node parent = project.getNode(versionsRelPath());
			QueryManager queryManager = parent.getSession().getWorkspace().getQueryManager();
			String xpathQueryStr = XPathUtils.descendantFrom(parent.getPath());
			xpathQueryStr += "//element(*, " + TrackerTypes.TRACKER_VERSION + ")";
			xpathQueryStr += "[" + XPathUtils.getPropertyEquals(TrackerNames.TRACKER_ID, versionId) + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			NodeIterator results = xpathQuery.execute().getNodes();
			if (!results.hasNext())
				return null;
			else if (results.getSize() > 1)
				throw new TrackerException(
						"Found " + results.getSize() + " versions with Id " + versionId + " under " + project);
			else
				return results.nextNode();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get version " + versionId + " under " + project, e);
		}
	}

	public static Node getComponentById(Node project, String officeId) {
		try {
			Node parent = project.getNode(componentsRelPath());
			String xpathQueryStr = XPathUtils.descendantFrom(parent.getPath());
			xpathQueryStr += "//element(*, " + TrackerTypes.TRACKER_COMPONENT + ")";
			xpathQueryStr += "[" + XPathUtils.getPropertyEquals(TrackerNames.TRACKER_ID, officeId) + "]";
			QueryManager qm = parent.getSession().getWorkspace().getQueryManager();
			Query xpathQuery = qm.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			NodeIterator results = xpathQuery.execute().getNodes();
			if (!results.hasNext())
				return null;
			else if (results.getSize() > 1)
				throw new TrackerException(
						"Found " + results.getSize() + " versions with Id " + officeId + " under " + project);
			else
				return results.nextNode();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get component " + officeId + " under " + project, e);
		}
	}

	public static long getIssueNb(Node category, boolean onlyOpen) {
		Node project = getProjectFromChild(category);
		String relProp = getRelevantPropName(category);
		NodeIterator nit = getIssues(project, null, relProp, JcrUiUtils.get(category, TrackerNames.TRACKER_ID),
				onlyOpen);
		return nit.getSize();
	}

	public static Node getProjectFromChild(Node issue) {
		try {
			// Not very clean. Will fail in the case of a draft issue that is
			// still in the home of current user, among others
			Node parent = issue;
			while (!parent.isNodeType(TrackerTypes.TRACKER_PROJECT))
				parent = parent.getParent();
			return parent;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get project for " + issue, e);
		}
	}

	public static String getImportanceLabel(Node issue) {
		try {
			int importance = (int) issue.getProperty(TrackerNames.TRACKER_IMPORTANCE).getLong();
			Integer importanceI = new Integer(importance);
			return TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importanceI);
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get importance label for " + issue, e);
		}
	}

	public static String getPriorityLabel(Node issue) {
		try {
			int priority = (int) issue.getProperty(TrackerNames.TRACKER_PRIORITY).getLong();
			Integer priorityI = new Integer(priority);
			return TrackerUtils.MAPS_ISSUE_PRIORITIES.get(priorityI);
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get priority label for " + issue, e);
		}
	}

	public static String getCreationLabel(PeopleTrackerService aoService, Node issue) {
		try {
			String result = "";
			if (issue.hasProperty(Property.JCR_CREATED_BY)) {
				String userId = issue.getProperty(Property.JCR_CREATED_BY).getString();
				String displayName = aoService.getUserAdminService().getUserDisplayName(userId);
				if (EclipseUiUtils.notEmpty(displayName))
					result = displayName;
			}

			if (issue.hasProperty(Property.JCR_CREATED)) {
				result += " on " + JcrUiUtils.getDateFormattedAsString(issue, Property.JCR_CREATED,
						ConnectUiConstants.DEFAULT_DATE_TIME_FORMAT);
			}
			return result;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get creation label for " + issue, e);
		}
	}

	private static DateFormat dtFormat = new SimpleDateFormat(ConnectUiConstants.DEFAULT_DATE_TIME_FORMAT);

	public static String getStatusText(PeopleTrackerService aoService, Node issue) {
		try {
			StringBuilder builder = new StringBuilder();

			// status, importance, priority
			builder.append("<b> Status: </b>").append(JcrUiUtils.get(issue, PeopleNames.PEOPLE_TASK_STATUS))
					.append(" ");
			builder.append("[").append(TrackerUtils.getImportanceLabel(issue)).append("/")
					.append(TrackerUtils.getPriorityLabel(issue)).append("] - ");

			// milestone, version
			String targetId = JcrUiUtils.get(issue, TrackerNames.TRACKER_TARGET_ID);
			if (notEmpty(targetId))
				builder.append("<b>Target milestone: </b> ").append(targetId).append(" - ");
			String versionId = JcrUiUtils.getMultiAsString(issue, TrackerNames.TRACKER_VERSION_IDS, ", ");
			builder.append(" - ");
			if (notEmpty(versionId))
				builder.append("<b>Affected version: </b> ").append(versionId).append(" - ");

			// assigned to
			if (aoService.getActivityService().isTaskDone(issue)) {
				String closeBy = JcrUiUtils.get(issue, PeopleNames.PEOPLE_CLOSED_BY);
				Calendar closedDate = issue.getProperty(PeopleNames.PEOPLE_CLOSE_DATE).getDate();
				builder.append(" - Marked as closed by ").append(closeBy);
				builder.append(" on ").append(dtFormat.format(closedDate.getTime())).append(".");
			} else {
				String assignedToId = JcrUiUtils.get(issue, PeopleNames.PEOPLE_ASSIGNED_TO);
				String dName = aoService.getUserAdminService().getUserDisplayName(assignedToId);
				if (notEmpty(dName))
					builder.append("<b>Assigned to: </b>").append(dName);
			}
			return ConnectUiUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get status text for issue " + issue, e);
		}
	}

	// DEFAULT CONFIGURATIONS
	public static void createDefaultComponents(TrackerService issueService, Node parentProject)
			throws RepositoryException {
		for (String title : DEFAULT_COMPONENTS.keySet()) {
			issueService.createComponent(parentProject, title, title, DEFAULT_COMPONENTS.get(title));
		}
	}

	/**
	 * Provides the key for a given value. we assume it is a bijection each
	 * value is only linked to one key
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
