package org.argeo.tracker.internal.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.core.TrackerUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Centralise label providers for Argeo tracker to keep packages simple */
public class TrackerLps {

	// FIXME provide a better management of date patterns
	private final static String SIMPLE_DATE_PATTERN = "dd MMM yyyy";

	public class DnLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -848512622910895692L;
		private final UserAdminService userAdminService;
		private final String propName;

		public DnLabelProvider(UserAdminService userAdminService, String propName) {
			this.userAdminService = userAdminService;
			this.propName = propName;
		}

		@Override
		public String getText(Object element) {
			Node entity = (Node) element;
			String dn = ConnectJcrUtils.get(entity, propName);
			if (EclipseUiUtils.notEmpty(dn))
				return userAdminService.getUserDisplayName(dn);
			else
				return "";
		}
	}

	public class VersionDateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -7883411659009058536L;

		@Override
		public String getText(Object element) {
			Node version = (Node) element;
			String date = ConnectJcrUtils.getDateFormattedAsString(version, TrackerNames.TRACKER_RELEASE_DATE,
					SIMPLE_DATE_PATTERN);
			if (date == null)
				date = ConnectJcrUtils.getDateFormattedAsString(version, TrackerNames.TRACKER_TARGET_DATE,
						SIMPLE_DATE_PATTERN);

			if (date == null)
				date = " - ";
			return date;
		}
	}

	public class MilestoneDateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 8003688506253830216L;

		@Override
		public String getText(Object element) {
			Node milestone = (Node) element;
			String dateStr = "";
			try {
				if (milestone.hasProperty(ConnectNames.CONNECT_CLOSE_DATE))
					// "Closed on " +
					dateStr = ConnectJcrUtils.getDateFormattedAsString(milestone, ConnectNames.CONNECT_CLOSE_DATE,
							SIMPLE_DATE_PATTERN);
				else if (milestone.hasProperty(TrackerNames.TRACKER_TARGET_DATE))
					// "Due to " +
					dateStr = ConnectJcrUtils.getDateFormattedAsString(milestone, TrackerNames.TRACKER_TARGET_DATE,
							SIMPLE_DATE_PATTERN);
				else
					dateStr = " - ";
			} catch (RepositoryException e) {
				throw new TrackerException("Cannot retrieve milstone relevant date for " + milestone, e);
			}
			return dateStr;
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

	public class MilestoneLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -2359291146337869742L;
		private AppService appService;

		public MilestoneLabelProvider(AppService appService) {
			this.appService = appService;
		}

		// TODO make it clickable
		@Override
		public String getText(Object element) {
			Node issue = (Node) element;
			try {
				String muid = ConnectJcrUtils.get(issue, TrackerNames.TRACKER_MILESTONE_UID);
				if (EclipseUiUtils.notEmpty(muid)) {
					Node m = appService.getEntityByUid(issue.getSession(), null, muid);
					if (m != null)
						return ConnectJcrUtils.get(m, Property.JCR_TITLE);
				}
			} catch (RepositoryException e) {
				throw new TrackerException("Unable to get milestone name for " + issue, e);
			}
			return "";
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