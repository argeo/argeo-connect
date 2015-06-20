package org.argeo.connect.people.utils;

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
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;

/**
 * Draft methods that should be centralized in the activityService as soon as
 * stabilized
 */
public class ActivityUtils {

	// private final static Log log = LogFactory.getLog(ActivityUtils.class);

	private final static NumberFormat nbFormat = DecimalFormat.getInstance();

	public static NodeIterator getPolls(Node pollable, boolean onlyOpenPolls) {
		try {
			Session session = pollable.getSession();
			String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_POLL
					+ "] WHERE  ISDESCENDANTNODE('" + pollable.getPath() + "')";
			if (onlyOpenPolls)
				throw new ArgeoException("Unimplemented ability");

			Query query = session.getWorkspace().getQueryManager()
					.createQuery(queryStr, Query.JCR_SQL2);
			return query.execute().getNodes();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get polls for " + pollable);
		}
	}

	public static NodeIterator getRates(Node pollable) {
		try {
			Session session = pollable.getSession();
			String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
					+ "] WHERE  ISDESCENDANTNODE('" + pollable.getPath() + "')";
			Query query = session.getWorkspace().getQueryManager()
					.createQuery(queryStr, Query.JCR_SQL2);
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
		return createVote(poll, null);
	}

	public static Node createVote(Node poll, String userID) {
		try {
			Node parent = JcrUtils.mkdirs(poll, PeopleNames.PEOPLE_RATES,
					NodeType.NT_UNSTRUCTURED);

			String nodeName = CommonsJcrUtils.isEmptyString(userID) ? poll
					.getSession().getUserID() : userID;

			Node vote = parent.addNode(nodeName, PeopleTypes.PEOPLE_ACTIVITY);
			vote.addMixin(PeopleTypes.PEOPLE_RATE);
			vote.setProperty(PeopleNames.PEOPLE_REPORTED_BY, nodeName);

			// Activity Date
			vote.setProperty(PeopleNames.PEOPLE_ACTIVITY_DATE,
					new GregorianCalendar());

			// related to
			CommonsJcrUtils.addRefToMultiValuedProp(vote,
					PeopleNames.PEOPLE_RELATED_TO, poll);
			JcrUtils.updateLastModified(vote);
			return vote;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create vote on " + poll, re);
		}
	}

	public static String getAvgRating(Node poll) throws RepositoryException {
		Session session = poll.getSession();
		String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_RATE
				+ "] WHERE  ISDESCENDANTNODE('" + poll.getPath() + "') ";
		long nb = 0;
		long total = 0;
		Query query = session.getWorkspace().getQueryManager()
				.createQuery(queryStr, Query.JCR_SQL2);
		NodeIterator nit = query.execute().getNodes();
		while (nit.hasNext()) {
			Node node = nit.nextNode();
			if (node.hasProperty(PeopleNames.PEOPLE_RATE)) {
				total += node.getProperty(PeopleNames.PEOPLE_RATE).getLong();
				nb++;
			}
		}

		if (nb == 0)
			return "(none yet)";
		else {
			double avg = (double) total / (double) nb;
			// Math.round(avg)
			String result = nbFormat.format(avg) + " (" + nb
					+ (nb == 1 ? " vote)" : " votes)");
			return result;
		}
	}

	// Prevents instantiation
	private ActivityUtils() {
	}
}