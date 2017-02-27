package org.argeo.connect.activities.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.activities.ActivitiesException;
import org.argeo.connect.activities.ActivitiesNames;
import org.argeo.connect.activities.ActivitiesTypes;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

/**
 * Draft methods that should be centralized in the activityService as soon as
 * stabilized
 */
public class ActivityUtils {

	// private final static Log log = LogFactory.getLog(ActivityUtils.class);
	private final static NumberFormat nbFormat = DecimalFormat.getInstance();

	/** Simply returns the poll name that is relevant for the given rate */
	public static String getPollName(Node rate) {
		try {
			Node poll = null;
			Node curr = rate;
			while (poll == null)
				if (curr.isNodeType(ActivitiesTypes.ACTIVITIES_POLL))
					poll = curr;
				else
					curr = curr.getParent();
			return ConnectJcrUtils.get(poll, ActivitiesNames.ACTIVITIES_POLL_NAME);
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get related " + "poll name for " + rate, re);
		}
	}

	public static NodeIterator getPolls(Node pollable, boolean onlyOpenPolls) {
		try {
			Session session = pollable.getSession();

			if (onlyOpenPolls)
				throw new ActivitiesException("Unimplemented feature");

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(pollable.getPath()));
			builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_POLL).append(")");
			Query query = session.getWorkspace().getQueryManager().createQuery(builder.toString(),
					ConnectConstants.QUERY_XPATH);

			// SQL2
			// String queryStr = "SELECT * FROM [" +
			// ActivitiesTypes.ACTIVITIES_POLL
			// + "] WHERE ISDESCENDANTNODE('" + pollable.getPath() + "')";
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get polls for " + pollable, re);
		}
	}

	public static NodeIterator getRates(Node pollable) {
		try {
			Session session = pollable.getSession();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(pollable.getPath()));
			builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_RATE).append(")");
			Query query = session.getWorkspace().getQueryManager().createQuery(builder.toString(),
					ConnectConstants.QUERY_XPATH);

			// // SQL2
			// String queryStr = "SELECT * FROM [" +
			// ActivitiesTypes.ACTIVITIES_RATE
			// + "] WHERE ISDESCENDANTNODE('" + pollable.getPath() + "')";
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get rates for " + pollable);
		}
	}

	public static Node getMyVote(Node poll) {
		try {
			Node myVote = null;
			if (poll.hasNode(ActivitiesNames.ACTIVITIES_RATES)) {
				Node allRates = poll.getNode(ActivitiesNames.ACTIVITIES_RATES);
				String userId = poll.getSession().getUserID();
				if (allRates.hasNode(userId))
					myVote = allRates.getNode(userId);
			}
			return myVote;
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to get polls for " + poll);
		}

	}

	public static Node createVote(Node poll) {
		return createOneRating(poll, null, null);
	}

	public static Node createVote(Node poll, String userID) {
		return createOneRating(poll, userID, null);
	}

	public static Node createOneRating(Node poll, String userID, Long rate) {
		try {
			Node parent = JcrUtils.mkdirs(poll, ActivitiesNames.ACTIVITIES_RATES, NodeType.NT_UNSTRUCTURED);

			String nodeName = EclipseUiUtils.isEmpty(userID) ? poll.getSession().getUserID() : userID;

			Node vote = parent.addNode(nodeName, ActivitiesTypes.ACTIVITIES_ACTIVITY);
			vote.addMixin(ActivitiesTypes.ACTIVITIES_RATE);
			vote.setProperty(ActivitiesNames.ACTIVITIES_REPORTED_BY, nodeName);

			// Activity Date
			vote.setProperty(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE, new GregorianCalendar());

			// related to
			ConnectJcrUtils.addRefToMultiValuedProp(vote, ActivitiesNames.ACTIVITIES_RELATED_TO, poll);

			JcrUtils.updateLastModified(vote);

			if (rate != null)
				vote.setProperty(ActivitiesNames.ACTIVITIES_RATE, rate);
			updateAvgRatingCache(poll);
			return vote;
		} catch (RepositoryException re) {
			throw new ActivitiesException("Unable to create vote on " + poll, re);
		}
	}

	public static void updateAvgRatingCache(Node poll) {
		try {
			double average = -1;
			long nb = 0;
			long total = 0;

			if (poll.hasNode(ActivitiesNames.ACTIVITIES_RATES)) {
				NodeIterator nit = poll.getNode(ActivitiesNames.ACTIVITIES_RATES).getNodes();
				while (nit.hasNext()) {
					Node node = nit.nextNode();
					if (node.hasProperty(ActivitiesNames.ACTIVITIES_RATE)) {
						total += node.getProperty(ActivitiesNames.ACTIVITIES_RATE).getLong();
						nb++;
					}
				}
			}
			if (nb > 0)
				average = (double) total / (double) nb;
			poll.setProperty(ActivitiesNames.ACTIVITIES_CACHE_AVG_RATE, average);
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to compute " + "average rating for " + poll, e);
		}
	}

	public static String getAvgRating(Node poll) {
		try {
			Double avg = -1d;
			if (poll.hasProperty(ActivitiesNames.ACTIVITIES_CACHE_AVG_RATE))
				avg = poll.getProperty(ActivitiesNames.ACTIVITIES_CACHE_AVG_RATE).getDouble();

			if (avg <= 0)
				return "(none yet)";
			else {
				// TODO enhance retrieval of vote count
				long nb = poll.getNode(ActivitiesNames.ACTIVITIES_RATES).getNodes().getSize();
				String result = nbFormat.format(avg) + " (" + nb + (nb == 1 ? " vote)" : " votes)");
				return result;
			}
		} catch (RepositoryException e) {
			throw new ActivitiesException("unable to compute " + "average rating for " + poll, e);
		}
	}

	// Prevents instantiation
	private ActivityUtils() {
	}
}
