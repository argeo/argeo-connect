package org.argeo.activities.workbench.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.argeo.activities.ActivitiesService;
import org.argeo.connect.ui.util.NodeViewerComparator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;

/* This table specific comparator */
public class ActivityViewerComparator extends NodeViewerComparator {
	private static final long serialVersionUID = 1L;

	public final static String RELEVANT_DATE = "RELEVANT_DATE";

	private final Map<String, ColumnLabelProvider> labelProviderMap = new HashMap<String, ColumnLabelProvider>();
	private ActivitiesService activityService;

	public ActivityViewerComparator(ActivitiesService activityService,
			Map<String, ColumnLabelProvider> labelProviderMap) {
		this.activityService = activityService;
		if (labelProviderMap != null)
			this.labelProviderMap.putAll(labelProviderMap);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc = 0;

		if (propertyName.equals(RELEVANT_DATE)) {
			rc = compareRelevantDates(e1, e2);
		} else if (labelProviderMap.containsKey(propertyName)) {
			rc = compareWithLp(labelProviderMap.get(propertyName), e1, e2);
		} else
			return super.compare(viewer, e1, e2);

		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	protected int compareWithLp(ColumnLabelProvider labelProvider, Object e1, Object e2) {
		String s1 = labelProvider.getText(e1);
		String s2 = labelProvider.getText(e2);
		return s1.compareTo(s2);
	}

	protected int compareRelevantDates(Object e1, Object e2) {
		Calendar cal1 = activityService.getActivityRelevantDate((Node) e1);
		Calendar cal2 = activityService.getActivityRelevantDate((Node) e2);
		int rc = preCompareWithNull(cal1, cal2);
		if (rc != 99)
			return rc;
		else
			return cal1.compareTo(cal2);
	}

	protected int preCompareWithNull(Object v1, Object v2) {
		if (v2 == null && v1 == null)
			return 0;
		else if (v2 == null)
			return -1;
		else if (v1 == null)
			return 1;
		return 99;
	}

}
