package org.argeo.connect.tracker.internal.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Centralise label providers for Argeo tracker to keep packages simple */
public class TrackerLps {

	public class VersionDateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -7883411659009058536L;
		private String datePattern = "dd MMM yyyy";

		@Override
		public String getText(Object element) {
			Node version = (Node) element;
			String date = ConnectJcrUtils.getDateFormattedAsString(version, TrackerNames.TRACKER_RELEASE_DATE, datePattern);
			if (date == null)
				date = ConnectJcrUtils.getDateFormattedAsString(version, TrackerNames.TRACKER_TARGET_DATE, datePattern);

			if (date == null)
				date = " - ";
			return date;
		}
	}

	public class PriorityLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -6190366293616451065L;

		@Override
		public String getText(Object element) {
			Node issue = (Node) element;
			try {
				if (issue.hasProperty(TrackerNames.TRACKER_PRIORITY)) {
					long priorityL = issue.getProperty(TrackerNames.TRACKER_PRIORITY).getLong();
					String priorityStr = TrackerUtils.MAPS_ISSUE_PRIORITIES.get("" + priorityL);
					return priorityStr;
				}
			} catch (RepositoryException e) {
				throw new TrackerException("Unable to get isssue priority for " + issue, e);
			}
			return "";
		}
	}

	public class CommentNbLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -1685888796850965318L;

		@Override
		public String getText(Object element) {
			Node issue = (Node) element;
			try {
				// TODO enhance to also count children comments once this
				// concept has been implemented
				if (issue.hasNode(TrackerNames.TRACKER_COMMENTS)) {
					long commentNb = issue.getNode(TrackerNames.TRACKER_COMMENTS).getNodes().getSize();
					if (commentNb > 0)
						return commentNb + " comments";
				}
			} catch (RepositoryException e) {
				throw new TrackerException("Unable to get isssue priority for " + issue, e);
			}
			return "None yet";
		}
	}

	public class ImportanceLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 7920611707085451018L;

		@Override
		public String getText(Object element) {
			Node issue = (Node) element;
			try {
				if (issue.hasProperty(TrackerNames.TRACKER_IMPORTANCE)) {
					long importanceL = issue.getProperty(TrackerNames.TRACKER_IMPORTANCE).getLong();
					String importanceStr = "" + importanceL;
					return TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(importanceStr);
				}
			} catch (RepositoryException e) {
				throw new TrackerException("Unable to get isssue importance for " + issue, e);
			}
			return "";
		}
	}
}