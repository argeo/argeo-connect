package org.argeo.activities.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.LabelProvider;

/** Provide a single column label provider for person lists */
public class ActivityListLabelProvider extends LabelProvider {
	private static final long serialVersionUID = -8261171417328758985L;

	private final ActivitiesService activitiesService;

	public ActivityListLabelProvider(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(ConnectJcrUtils.get(node, Property.JCR_TITLE));
		builder.append("</b>");
		String dv = ConnectJcrUtils.getDateFormattedAsString(node, ActivitiesNames.ACTIVITIES_ACTIVITY_DATE,
				ConnectUiConstants.DEFAULT_DATE_FORMAT);
		if (EclipseUiUtils.notEmpty(dv))
			builder.append(" on ").append(dv);

		List<String> relatedToUid = ConnectJcrUtils.getMultiAsList(node, ActivitiesNames.ACTIVITIES_RELATED_TO);
		List<String> relatedToNames = new ArrayList<>();
		Session session = ConnectJcrUtils.getSession(node);
		for (String uid : relatedToUid) {
			String currName = getRelatedToName(session, uid);
			if (EclipseUiUtils.notEmpty(currName))
				relatedToNames.add(currName);
		}

		if (relatedToNames.size() == 1) {
			builder.append(" with ").append(relatedToNames.get(0));
		} else if (relatedToNames.size() > 1) {
			builder.append(" with ");
			int i;
			for (i = 0; i < relatedToNames.size() - 1; i++) {
				builder.append(relatedToNames.get(i));

			}
			builder.append(" and ").append(relatedToNames.get(i + 1));
		}

		String result = ConnectUiUtils.replaceAmpersand(builder.toString());
		return result;
	}

	private String getRelatedToName(Session session, String connectUid) {
		Node entity = activitiesService.getEntityByUid(session, "/", connectUid);
		if (entity != null)
			return ConnectJcrUtils.get(entity, Property.JCR_TITLE);
		else
			return null;
	}
}
