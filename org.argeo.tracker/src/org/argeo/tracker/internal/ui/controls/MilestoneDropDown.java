package org.argeo.tracker.internal.ui.controls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerTypes;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of milestones for this project */
public class MilestoneDropDown extends ConnectAbstractDropDown {

	private final Session session;
	private Node project;
	private final boolean onlyOpenMilestons;
	private final Map<String, Node> relevantMilestones = new LinkedHashMap<>();
	private Map<String, Node> filteredMilestones;

	public MilestoneDropDown(Session session, Text text, boolean onlyOpenMilestons) {
		super(text);
		this.session = session;
		this.onlyOpenMilestons = onlyOpenMilestons;
		init();
	}

	public void setProject(Node project) {
		this.project = project;
		populateMilestoneList();
	}

	public void resetMilestone(Node milestone) {
		if (milestone != null)
			reset(ConnectJcrUtils.get(milestone, Property.JCR_TITLE));
	}

	protected void populateMilestoneList() {
		try {
			relevantMilestones.clear();
			if (project == null)
				return;

			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ");
			builder.append(TrackerTypes.TRACKER_MILESTONE);
			builder.append(")");

			if (onlyOpenMilestons)
				builder.append("[not(@").append(ConnectNames.CONNECT_CLOSE_DATE).append(")").append("]");

			builder.append(" order by @").append(Property.JCR_TITLE);
			QueryResult result = XPathUtils.createQuery(session, builder.toString()).execute();
			NodeIterator nIt = result.getNodes();
			while (nIt.hasNext()) {
				Node currProj = nIt.nextNode();
				relevantMilestones.put(currProj.getProperty(Property.JCR_TITLE).getString(), currProj);
			}
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get projects for session " + session.getUserID(), e);
		}
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		filteredMilestones = relevantMilestones.entrySet().stream()
				.filter(p -> p.getKey().toLowerCase().indexOf(filter.toLowerCase()) >= 0)
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		List<String> res = new ArrayList<>();
		res.addAll(filteredMilestones.keySet());
		return res;
	}

	public Node getChosenMilestone() {
		return relevantMilestones.get(getText());
	}
}
