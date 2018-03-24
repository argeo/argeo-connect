package org.argeo.tracker.ui;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.eclipse.jface.viewers.LabelProvider;

/** Provide a single column label provider for person lists */
public class TrackerSingleColLP extends LabelProvider {
	private static final long serialVersionUID = -2197098090529153597L;
	private final AppService appService;

	public TrackerSingleColLP(AppService appService) {
		this.appService = appService;
	}

	@Override
	public String getText(Object element) {
		Node person = (Node) element;
		Session session = ConnectJcrUtils.getSession(person);
		StringBuilder builder = new StringBuilder();

		String trackerID = ConnectJcrUtils.get(person, TrackerNames.TRACKER_ID);
		if (EclipseUiUtils.notEmpty(trackerID))
			if (ConnectJcrUtils.isNodeType(person, TrackerTypes.TRACKER_TASK))
				builder.append("#").append(trackerID).append(" ");
			else if (ConnectJcrUtils.isNodeType(person, TrackerTypes.TRACKER_VERSION))
				builder.append("v").append(trackerID).append(" ");

		builder.append("<b>");
		builder.append(ConnectJcrUtils.get(person, Property.JCR_TITLE));
		builder.append("</b>");
		// String dv = ConnectJcrUtils.getDateFormattedAsString(person,
		// ActivitiesNames.ACTIVITIES_ACTIVITY_DATE,
		// ConnectUiConstants.DEFAULT_DATE_FORMAT);

		String pid = ConnectJcrUtils.get(person, TrackerNames.TRACKER_PROJECT_UID);
		if (EclipseUiUtils.notEmpty(pid)) {
			Node project = appService.getEntityByUid(session, null, pid);
			if (project != null) {
				String pt = ConnectJcrUtils.get(project, Property.JCR_TITLE);
				if (EclipseUiUtils.notEmpty(pt))
					builder.append(" [").append(pt).append("] ");
			}
		}

		// TODO add manager and due date
		String result = ConnectUtils.replaceAmpersand(builder.toString());
		return result;
	}

}
