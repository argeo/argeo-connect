package org.argeo.tracker.internal.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.core.TrackerUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Centralise label providers for Argeo tracker to keep packages simple */
public class TrackerLps {

	public class VersionDateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -7883411659009058536L;
		private String datePattern = "dd MMM yyyy";

		@Override
		public String getText(Object element) {
			Node version = (Node) element;
			String date = ConnectJcrUtils.getDateFormattedAsString(version, TrackerNames.TRACKER_RELEASE_DATE,
					datePattern);
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

	public class IssueCommentOverviewLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 3580821684409015205L;

		private DateFormat df = new SimpleDateFormat(TrackerUiConstants.simpleDateTimeFormat);

		@Override
		public String getText(Object element) {
			Node comment = (Node) element;
			String createdBy = ConnectJcrUtils.get(comment, Property.JCR_CREATED_BY);
			Calendar createdOn = ConnectJcrUtils.getDateValue(comment, Property.JCR_CREATED);
			String lastUpdatedBy = ConnectJcrUtils.get(comment, Property.JCR_LAST_MODIFIED_BY);
			Calendar lastUpdatedOn = ConnectJcrUtils.getDateValue(comment, Property.JCR_LAST_MODIFIED);

			if (createdBy.equalsIgnoreCase(lastUpdatedBy))
				lastUpdatedBy = null;
			long t1Millis = createdOn.getTimeInMillis() + 300000;
			long t2Millis = createdOn.getTimeInMillis();
			if (t2Millis < t1Millis)
				lastUpdatedOn = null;

			String createdOnStr = df.format(createdOn.getTime());
			ConnectJcrUtils.getDateFormattedAsString(comment, Property.JCR_CREATED,
					TrackerUiConstants.simpleDateTimeFormat);
			StringBuilder builder = new StringBuilder();
			builder.append(UserAdminUtils.getUserLocalId(createdBy)).append(" on ").append(createdOnStr);
			if (lastUpdatedOn != null) {
				builder.append(" (last edited ");
				if (EclipseUiUtils.notEmpty(lastUpdatedBy))
					builder.append("by ").append(UserAdminUtils.getUserLocalId(lastUpdatedBy)).append(" ");
				builder.append("on ").append(df.format(lastUpdatedOn.getTime())).append(")");
			}
			return builder.toString();
		}
	}
}