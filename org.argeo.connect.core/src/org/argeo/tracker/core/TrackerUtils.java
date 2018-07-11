package org.argeo.tracker.core;

import static org.argeo.activities.ActivitiesNames.ACTIVITIES_DUE_DATE;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;

/** Centralise methods to ease implementation of the Tracker App */
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
		return TrackerNames.TRACKER_MILESTONES;
	}

	public static String getRelevantPropName(Node category) {
		try {
			if (category.isNodeType(TrackerTypes.TRACKER_COMPONENT))
				return TrackerNames.TRACKER_COMPONENT_IDS;
			else if (category.isNodeType(TrackerTypes.TRACKER_MILESTONE))
				return TrackerNames.TRACKER_MILESTONE_UID;
			else if (category.isNodeType(TrackerTypes.TRACKER_VERSION)) {
				// TODO enhance the choice between milestone and version
				if (category.hasProperty(TrackerNames.TRACKER_RELEASE_DATE))
					return TrackerNames.TRACKER_VERSION_IDS;
				else
					return TrackerNames.TRACKER_MILESTONE_UID;
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
			if (EclipseUiUtils.notEmpty(projectParentPath))
				builder.append(XPathUtils.descendantFrom(projectParentPath));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_PROJECT).append(")");
			builder.append(" order by @").append(Property.JCR_TITLE);
			QueryResult result = XPathUtils.createQuery(session, builder.toString()).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException(
					"Unable to get projects under " + projectParentPath + " for session " + session.getUserID(), e);
		}
	}

	public static NodeIterator getOpenMilestones(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_MILESTONE).append(")");
			builder.append("[not(@").append(ConnectNames.CONNECT_CLOSE_DATE).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append(" and ").append(XPathUtils.getFreeTextConstraint(filter));
			builder.append("]");
			builder.append(" order by @").append(Property.JCR_TITLE).append(" ascending");
			QueryResult result = XPathUtils.createQuery(project.getSession(), builder.toString()).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get milestones on " + project + " with filter:" + filter, e);
		}
	}

	/**
	 * Simply returns the milestones defined for this project. Note that ordering by
	 * ID use lexical order that won't work for version, f.i. 2.1.23 & 2.1.4, it is
	 * caller duty to use a correct version comparator if the order is important
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
			QueryResult result = XPathUtils.createQuery(parent.getSession(), builder.toString()).execute();
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
			milestoneIds.add(ConnectJcrUtils.get(currNode, TrackerNames.TRACKER_ID));
		}
		return milestoneIds;
	}

	public static NodeIterator getVersions(Node project, String filter) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		// Node parent = project.getNode(versionsRelPath());
		builder.append(XPathUtils.descendantFrom(project.getPath()));
		builder.append("//element(*, ").append(TrackerTypes.TRACKER_VERSION).append(")");
		builder.append("[@").append(TrackerNames.TRACKER_RELEASE_DATE);
		if (EclipseUiUtils.notEmpty(filter))
			builder.append(" and ").append(XPathUtils.getFreeTextConstraint(filter));
		builder.append("]");
		builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" descending");
		QueryResult result = XPathUtils.createQuery(project.getSession(), builder.toString()).execute();
		return result.getNodes();
	}

	public static List<String> getVersionIds(Node project, String filter) {
		try {
			NodeIterator nit = getVersions(project, filter);
			List<String> versionIds = new ArrayList<String>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				versionIds.add(ConnectJcrUtils.get(currNode, TrackerNames.TRACKER_ID));
			}
			return versionIds;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get version ids on " + project + " with filter:" + filter, e);
		}
	}

	public static NodeIterator getTasks(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_TASK).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID);
			QueryResult result = XPathUtils.createQuery(project.getSession(), builder.toString()).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get issues for " + project + " with filter: " + filter, e);
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
	 * Simply requests all issues of a project that are referencing this category (a
	 * version, a milestone or a component)
	 */
	public static NodeIterator getIssues(Node project, String filter, String propName, String catId) {
		return getIssues(project, filter, propName, catId, false);
	}

	/** Simply checks if an issue is closed */
	public static boolean isIssueClosed(Node issue) {
		try {
			// TODO enhance definition of closed status
			return issue.hasProperty(ConnectNames.CONNECT_CLOSE_DATE);
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to check closed status of " + issue, e);
		}
	}

	public static NodeIterator getIssues(Node project, String filter, String propName, String catId,
			boolean onlyOpenTasks) {
		try {
			StringBuilder builder = new StringBuilder();
			// Node parent = project.getNode(issuesRelPath());
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_TASK).append(")");

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
				tmpBuilder.append(ConnectNames.CONNECT_CLOSE_DATE);
				tmpBuilder.append(")");
				tmpBuilder.append(andStr);
			}

			if (tmpBuilder.length() > 0)
				builder.append("[").append(tmpBuilder.substring(0, tmpBuilder.length() - andStr.length())).append("]");

			builder.append(" order by @" + TrackerNames.TRACKER_ID);
			Query xpathQuery = XPathUtils.createQuery(project.getSession(), builder.toString());
			QueryResult result = xpathQuery.execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get issues for " + project + " with filter: " + filter, e);
		}
	}

	private final static String UNKNOWN = "Unknown";
	private final static String OTHERS = "Others";

	public static Map<String, String> getOpenTasksByAssignee(UserAdminService uas, Node project, String milestoneUid,
			int maxSize) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_TASK).append(")");
			builder.append("[");
			if (EclipseUiUtils.notEmpty(milestoneUid)) {
				builder.append(XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MILESTONE_UID, milestoneUid));
				builder.append(" and ");
			}
			builder.append("( not(@").append(ConnectNames.CONNECT_CLOSE_DATE).append("))");
			builder.append("]");
			QueryResult result = XPathUtils.createQuery(project.getSession(), builder.toString()).execute();

			Map<String, Long> nodeCount = countAndLimit(result.getNodes(), ActivitiesNames.ACTIVITIES_ASSIGNED_TO,
					maxSize);
			Map<String, String> openTasks = new LinkedHashMap<String, String>();
			for (String key : nodeCount.keySet()) {
				String dName;
				if (OTHERS.equals(key) || UNKNOWN.equals(key))
					dName = key;
				else
					dName = uas.getUserDisplayName(key);
				openTasks.put(dName, nodeCount.get(key).toString());
			}
			return openTasks;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get issues for " + project + " with filter: ", e);
		}
	}

	private static Map<String, Long> countAndLimit(NodeIterator nodes, String propName, int limit) {
		// First: count occurrences:
		Map<String, Long> nodeCount = new HashMap<>();
		while (nodes.hasNext()) {
			Node currNode = nodes.nextNode();
			String id = ConnectJcrUtils.get(currNode, propName);
			if (EclipseUiUtils.isEmpty(id))
				id = UNKNOWN;
			if (nodeCount.containsKey(id))
				nodeCount.replace(id, nodeCount.get(id) + 1);
			else
				nodeCount.put(id, 1l);
		}

		if (nodeCount.size() < limit)
			return nodeCount;

		Map<String, Long> orderedCount = sortByValue(nodeCount);
		Map<String, Long> limitedCount = new LinkedHashMap<String, Long>();

		int i = 0;
		long otherCount = 0;
		for (String key : orderedCount.keySet()) {
			if (i < limit)
				limitedCount.put(key, orderedCount.get(key));
			else
				otherCount += orderedCount.get(key);
			i++;
		}
		limitedCount.put(OTHERS, otherCount);
		return limitedCount;
	}

	private static Map<String, Long> sortByValue(Map<String, Long> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
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

	public static List<String> getComponentIds(Node project, String filter) {
		NodeIterator nit = getComponents(project, filter);
		List<String> componentIds = new ArrayList<String>();
		while (nit.hasNext()) {
			Node currNode = nit.nextNode();
			componentIds.add(ConnectJcrUtils.get(currNode, TrackerNames.TRACKER_ID));
		}
		return componentIds;
	}

	public static NodeIterator getComponents(Node project, String filter) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_COMPONENT).append(")");
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(TrackerNames.TRACKER_ID).append(" ascending");
			QueryResult result = XPathUtils.createQuery(project.getSession(), builder.toString()).execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get components for " + project + " with filter: " + filter, e);
		}
	}

	public static Node getVersionById(Node project, String versionId) {
		try {
			// Node parent = project.getNode(versionsRelPath());
			String xpathQueryStr = XPathUtils.descendantFrom(project.getPath());
			xpathQueryStr += "//element(*, " + TrackerTypes.TRACKER_VERSION + ")";
			xpathQueryStr += "[" + XPathUtils.getPropertyEquals(TrackerNames.TRACKER_ID, versionId) + "]";
			Query xpathQuery = XPathUtils.createQuery(project.getSession(), xpathQueryStr);
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
		NodeIterator nit = getIssues(project, null, relProp, ConnectJcrUtils.get(category, TrackerNames.TRACKER_ID),
				onlyOpen);
		return nit.getSize();
	}

	public static Node getRelatedProject(AppService appService, Node node) {
		String refUid = ConnectJcrUtils.get(node, TrackerNames.TRACKER_PROJECT_UID);
		if (EclipseUiUtils.notEmpty(refUid))
			return appService.getEntityByUid(ConnectJcrUtils.getSession(node), null, refUid);
		else
			return null;
	}

	public static Node getMilestone(AppService appService, Node task) {
		String muid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_MILESTONE_UID);
		if (EclipseUiUtils.notEmpty(muid))
			return appService.getEntityByUid(ConnectJcrUtils.getSession(task), null, muid);
		return null;
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
		Long importance = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_IMPORTANCE);
		if (importance != null)
			return TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importance.toString());
		else
			return "";
		// try {
		// int importance = (int)
		// issue.getProperty(TrackerNames.TRACKER_IMPORTANCE).getLong();
		// Integer importanceI = new Integer(importance);
		// return TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importanceI);
		// } catch (RepositoryException e) {
		// throw new TrackerException("Unable to get importance label for " +
		// issue, e);
		// }
	}

	public static String getPriorityLabel(Node issue) {
		Long priority = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_PRIORITY);
		if (priority != null)
			return TrackerUtils.MAPS_ISSUE_PRIORITIES.get(priority.toString());
		else
			return "";
		// try {
		// int priority = (int)
		// issue.getProperty(TrackerNames.TRACKER_PRIORITY).getLong();
		// Integer priorityI = new Integer(priority);
		// return TrackerUtils.MAPS_ISSUE_PRIORITIES.get(priorityI);
		// } catch (RepositoryException e) {
		// throw new TrackerException("Unable to get priority label for " +
		// issue, e);
		// }
	}

	public static String getCreationLabel(UserAdminService userAdminService, Node issue) {
		try {
			String result = "";
			if (issue.hasProperty(Property.JCR_CREATED_BY)) {
				String userId = issue.getProperty(Property.JCR_CREATED_BY).getString();
				String displayName = userAdminService.getUserDisplayName(userId);
				if (EclipseUiUtils.notEmpty(displayName))
					result = displayName;
			}

			if (issue.hasProperty(Property.JCR_CREATED)) {
				result += " on " + ConnectJcrUtils.getDateFormattedAsString(issue, Property.JCR_CREATED,
						ConnectConstants.DEFAULT_DATE_TIME_FORMAT);
			}
			return result;
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get creation label for " + issue, e);
		}
	}

	private static DateFormat dtFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

	public static String getStatusText(UserAdminService userAdminService, ActivitiesService activityService,
			Node issue) {
		try {
			StringBuilder builder = new StringBuilder();

			// status, importance, priority
			builder.append("<b> Status: </b>")
					.append(ConnectJcrUtils.get(issue, ActivitiesNames.ACTIVITIES_TASK_STATUS)).append(" ");
			builder.append("[").append(TrackerUtils.getImportanceLabel(issue)).append("/")
					.append(TrackerUtils.getPriorityLabel(issue)).append("] - ");

			// milestone, version
			Node milestone = TrackerUtils.getMilestone(activityService, issue);
			if (milestone != null)
				builder.append("<b>Target milestone: </b> ").append(ConnectJcrUtils.get(milestone, Property.JCR_TITLE))
						.append(" - ");
			String versionId = ConnectJcrUtils.getMultiAsString(issue, TrackerNames.TRACKER_VERSION_IDS, ", ");
			builder.append(" - ");
			if (notEmpty(versionId))
				builder.append("<b>Affect version: </b> ").append(versionId).append(" - ");

			// assigned to
			if (activityService.isTaskDone(issue)) {
				String closeBy = ConnectJcrUtils.get(issue, ConnectNames.CONNECT_CLOSED_BY);
				Calendar closedDate = issue.getProperty(ConnectNames.CONNECT_CLOSE_DATE).getDate();
				builder.append(" - Marked as closed by ").append(closeBy);
				builder.append(" on ").append(dtFormat.format(closedDate.getTime())).append(".");
			} else {
				String assignedToId = ConnectJcrUtils.get(issue, ActivitiesNames.ACTIVITIES_ASSIGNED_TO);
				String dName = userAdminService.getUserDisplayName(assignedToId);
				if (notEmpty(dName))
					builder.append("<b>Assigned to: </b>").append(dName);
			}
			return ConnectUtils.replaceAmpersand(builder.toString());
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
	 * Provides the key for a given value. we assume it is a bijection each value is
	 * only linked to one key
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static String normalizeDn(String dn) {
		// FIXME dirty workaround for the DN key case issue
		String lowerCased = dn.replaceAll("UID=", "uid=").replaceAll("CN=", "cn=").replaceAll("DC=", "dc=")
				.replaceAll("OU=", "ou=").replaceAll(", ", ",");
		return lowerCased;
	}

	public static long getProjectOverdueTasksNumber(Node project) {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_TASK).append(")");
			// Past due date
			Calendar now = GregorianCalendar.getInstance();
			String overdueCond = XPathUtils.getPropertyDateComparaison(ACTIVITIES_DUE_DATE, now, "<");
			// Only opened tasks
			String notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";
			builder.append("[").append(XPathUtils.localAnd(overdueCond, notClosedCond)).append("]");
			Query query = XPathUtils.createQuery(project.getSession(), builder.toString());
			return query.execute().getNodes().getSize();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to get overdue tasks number for " + project);
		}
	}
}
