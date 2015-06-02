package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;

/**
 * Draft methods that should be centralized in the activityService as soon as
 * stabilized
 */
public class ActivityUtils {

	public static NodeIterator getPolls(Node pollable, boolean onlyOpenPolls)
			throws RepositoryException {
		Session session = pollable.getSession();
		String queryStr = "SELECT * FROM [" + PeopleTypes.PEOPLE_POLL
				+ "] WHERE  ISDESCENDANTNODE('" + pollable.getPath() + "') ";
		if (onlyOpenPolls)
			throw new ArgeoException("Unimplemented ability");

		Query query = session.getWorkspace().getQueryManager()
				.createQuery(queryStr, Query.JCR_SQL2);
		return query.execute().getNodes();
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
			double avg = total / nb;
			String result = Math.round(avg) + " ( " + nb + " votes)";
			return result;
		}
	}

	// Prevents instantiation
	private ActivityUtils() {
	}
}