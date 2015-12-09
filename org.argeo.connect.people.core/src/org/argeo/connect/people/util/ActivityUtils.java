package org.argeo.connect.people.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
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
				if (curr.isNodeType(PeopleTypes.PEOPLE_POLL))
					poll = curr;
				else
					curr = curr.getParent();
			return JcrUiUtils.get(poll, PeopleNames.PEOPLE_POLL_NAME);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get related "
					+ "poll name for " + rate, re);
		}
	}

	public static NodeIterator getPolls(Node pollable, boolean onlyOpenPolls) {
		try {
			Session session = pollable.getSession();

			if (onlyOpenPolls)
				throw new ArgeoException("Unimplemented feature");

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(pollable.getPath()));
			builder.append("//element(*, ").append(PeopleTypes.PEOPLE_POLL)
					.append(")");
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(builder.toString(),
							PeopleConstants.QUERY_XPATH);

			// SQL2
			// String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_POLL
			// + "] WHERE  ISDESCENDANTNODE('" + pollable.getPath() + "')";
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get polls for " + pollable, re);
		}
	}

	public static NodeIterator getRates(Node pollable) {
		try {
			Session session = pollable.getSession();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(pollable.getPath()));
			builder.append("//element(*, ").append(PeopleTypes.PEOPLE_RATE)
					.append(")");
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(builder.toString(),
							PeopleConstants.QUERY_XPATH);

			// // SQL2
			// String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
			// + "] WHERE  ISDESCENDANTNODE('" + pollable.getPath() + "')";
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get rates for " + pollable);
		}
	}

	public static Node getMyVote(Node poll) {
		try {
			Node myVote = null;
			if (poll.hasNode(PeopleNames.PEOPLE_RATES)) {
				Node allRates = poll.getNode(PeopleNames.PEOPLE_RATES);
				String userId = poll.getSession().getUserID();
				if (allRates.hasNode(userId))
					myVote = allRates.getNode(userId);
			}
			return myVote;
			// Session session = poll.getSession();
			// String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
			// + "] WHERE  ISDESCENDANTNODE('" + poll.getPath() + "') ";
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			// NodeIterator nit = query.execute().getNodes();
			// long size = nit.getSize();
			// if (size == 0)
			// return null;
			// else if (size == 1)
			// return nit.nextNode();
			//
			// else {
			// log.warn("Found " + size + " votes for " + poll + " with user "
			// + ;
			// return nit.nextNode();
			// }
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get polls for " + poll);
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
			Node parent = JcrUtils.mkdirs(poll, PeopleNames.PEOPLE_RATES,
					NodeType.NT_UNSTRUCTURED);

			String nodeName = EclipseUiUtils.isEmpty(userID) ? poll
					.getSession().getUserID() : userID;

			Node vote = parent.addNode(nodeName, PeopleTypes.PEOPLE_ACTIVITY);
			vote.addMixin(PeopleTypes.PEOPLE_RATE);
			vote.setProperty(PeopleNames.PEOPLE_REPORTED_BY, nodeName);

			// Activity Date
			vote.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE,
					new GregorianCalendar());

			// related to
			JcrUiUtils.addRefToMultiValuedProp(vote,
					PeopleNames.PEOPLE_RELATED_TO, poll);

			JcrUtils.updateLastModified(vote);

			if (rate != null)
				vote.setProperty(PeopleNames.PEOPLE_RATE, rate);
			updateAvgRatingCache(poll);
			return vote;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create vote on " + poll, re);
		}
	}

	public static void updateAvgRatingCache(Node poll) {
		try {
			double average = -1;

			// Session session = poll.getSession();
			// String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
			// + "] WHERE  ISDESCENDANTNODE('" + poll.getPath() + "/"
			// + PeopleNames.PEOPLE_RATES + "') ";
			// long nb = 0;
			// long total = 0;
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			// NodeIterator nit = query.execute().getNodes();

			long nb = 0;
			long total = 0;

			if (poll.hasNode(PeopleNames.PEOPLE_RATES)) {
				NodeIterator nit = poll.getNode(PeopleNames.PEOPLE_RATES)
						.getNodes();
				while (nit.hasNext()) {
					Node node = nit.nextNode();
					if (node.hasProperty(PeopleNames.PEOPLE_RATE)) {
						total += node.getProperty(PeopleNames.PEOPLE_RATE)
								.getLong();
						nb++;
					}
				}
			}

			if (nb > 0)
				average = (double) total / (double) nb;
			poll.setProperty(PeopleNames.PEOPLE_CACHE_AVG_RATE, average);
		} catch (RepositoryException e) {
			throw new PeopleException("unable to compute "
					+ "average rating for " + poll, e);
		}
	}

	public static String getAvgRating(Node poll) {
		try {
			// Session session = poll.getSession();
			// String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
			// + "] WHERE  ISDESCENDANTNODE('" + poll.getPath() + "/"
			// + PeopleNames.PEOPLE_RATES + "') ";
			// long nb = 0;
			// long total = 0;
			// Query query = session.getWorkspace().getQueryManager()
			// .createQuery(queryStr, Query.JCR_SQL2);
			// NodeIterator nit = query.execute().getNodes();
			// while (nit.hasNext()) {
			// Node node = nit.nextNode();
			// if (node.hasProperty(PeopleNames.PEOPLE_RATE)) {
			// total += node.getProperty(PeopleNames.PEOPLE_RATE)
			// .getLong();
			// nb++;
			// }
			// }

			Double avg = -1d;
			if (poll.hasProperty(PeopleNames.PEOPLE_CACHE_AVG_RATE))
				avg = poll.getProperty(PeopleNames.PEOPLE_CACHE_AVG_RATE)
						.getDouble();

			if (avg <= 0)
				return "(none yet)";
			else {
				// TODO enhance retrieval of vote count
				long nb = poll.getNode(PeopleNames.PEOPLE_RATES).getNodes()
						.getSize();
				String result = nbFormat.format(avg) + " (" + nb
						+ (nb == 1 ? " vote)" : " votes)");
				return result;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("unable to compute "
					+ "average rating for " + poll, e);
		}
	}

	// Prevents instantiation
	private ActivityUtils() {
	}
}