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

import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.connect.util.XPathUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerTypes;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of known project */
public class ProjectDropDown extends ConnectAbstractDropDown {

	private final Session session;
	private final boolean onlyItProject;
	private final Map<String, Node> knownProjects = new LinkedHashMap<>();
	private Map<String, Node> filteredProjects;

	public ProjectDropDown(Session session, Text text, boolean onlyItProjects) {
		super(text);
		this.session = session;
		this.onlyItProject = onlyItProjects;
		populateProjectList();
		init();
	}

	protected void populateProjectList() {
		try {
			knownProjects.clear();
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ");
			if (onlyItProject)
				builder.append(TrackerTypes.TRACKER_IT_PROJECT);
			else
				builder.append(TrackerTypes.TRACKER_PROJECT);
			builder.append(")");
			builder.append(" order by @").append(Property.JCR_TITLE);
			QueryResult result = XPathUtils.createQuery(session, builder.toString()).execute();
			NodeIterator nIt = result.getNodes();
			while (nIt.hasNext()) {
				Node currProj = nIt.nextNode();
				knownProjects.put(currProj.getProperty(Property.JCR_TITLE).getString(), currProj);
			}
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get projects for session " + session.getUserID(), e);
		}
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		filteredProjects = knownProjects.entrySet().stream()
				.filter(p -> p.getKey().toLowerCase().indexOf(filter.toLowerCase()) >= 0)
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		List<String> res = new ArrayList<>();
		res.addAll(filteredProjects.keySet());
		return res;
	}

	public Node getChosenProject() {
		return knownProjects.get(getText());
	}
}
