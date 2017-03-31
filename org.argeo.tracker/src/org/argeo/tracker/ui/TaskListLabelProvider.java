package org.argeo.tracker.ui;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.eclipse.jface.viewers.LabelProvider;

/** Provide a single column label provider for person lists */
public class TaskListLabelProvider extends LabelProvider {
	private static final long serialVersionUID = 8524164843887702899L;

	// private ActivitiesService activitiesService;
	private TrackerService trackerService;
	private int descLength = 160;

	private Node project;
	private Node milestone;

	public TaskListLabelProvider(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	public void setProject(Node project) {
		this.project = project;
	}

	public void setMilestone(Node milestone) {
		this.milestone = milestone;
	}

	@Override
	public String getText(Object element) {
		try {
			return getText((Node) element);
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot get text for " + element, e);
		}

	}

	public String getText(Node task) throws RepositoryException {
		Session session = task.getSession();
		StringBuilder builder = new StringBuilder();

		// Milestone & Project
		String currMSStr = null;
		if (milestone == null) {
			String muid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_MILESTONE_UID);
			if (EclipseUiUtils.notEmpty(muid)) {
				Node currMilestone = trackerService.getEntityByUid(session, "/", muid);
				if (currMilestone != null)
					currMSStr = ConnectJcrUtils.get(currMilestone, Property.JCR_TITLE);
			}
		}
		String currProjStr = null;
		if (project == null) {
			String puid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_PROJECT_UID);
			if (EclipseUiUtils.notEmpty(puid)) {
				Node currProject = trackerService.getEntityByUid(session, "/", puid);
				if (currProject != null)
					currProjStr = ConnectJcrUtils.get(currProject, Property.JCR_TITLE);
			}
		}
		String tmpStr = ConnectJcrUtils.concatIfNotEmpty(currMSStr, currProjStr, ", ");
		if (EclipseUiUtils.notEmpty(tmpStr))
			builder.append("[").append(tmpStr).append("]  ");

		builder.append("<b>");
		builder.append(ConnectJcrUtils.get(task, Property.JCR_TITLE));
		builder.append(" </b>");

		String desc = ConnectJcrUtils.get(task, Property.JCR_DESCRIPTION).replaceAll("\\n", " ");

		if (desc.length() > descLength)
			desc = desc.substring(0, descLength - 1) + "... ";
		builder.append(desc);

		return wrapThis(builder.toString());
	}

	private final String LIST_WRAP_STYLE = "style='float:left;padding:0px;white-space:pre-wrap;'";

	private String wrapThis(String value) {
		String wrapped = "<span " + LIST_WRAP_STYLE + " >" + ConnectUiUtils.replaceAmpersand(value) + "</span>";
		return wrapped;
	}

}
