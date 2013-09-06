package org.argeo.connect.streams.backend;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssNames;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/** Retrieve items and manages them in the repository. */
public class RssManagerImpl implements RssNames {
	private Repository repository;

	private Session adminSession;

	public void init() {
		try {
			adminSession = repository.login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot login to repository", e);
		}
	}

	public void destroy() {
		JcrUtils.logoutQuietly(adminSession);
	}

	@SuppressWarnings("unchecked")
	public void updateStreams() {
		String url = "";
		try {
			URL feedUrl = new URL(url);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			entries: for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
				if (entry.getTitle() == null
						|| entry.getTitle().trim().equals(""))
					continue entries;
				entries.add(entry);
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot read feed", e);
		}

	}

	protected void saveEntry(Session session, SyndEntry entry)
			throws RepositoryException {
		Node linkNode;

		String url = entry.getLink();
		String linkPath = null;
		if (session.itemExists(linkPath)
				&& session.getNode(linkPath).getProperty(ArgeoNames.ARGEO_URI)
						.equals(url)) {
			linkNode = session.getNode(linkPath);
		}

		Calendar publishedDate = new GregorianCalendar();
		publishedDate.setTime(entry.getPublishedDate());
		linkNode = JcrUtils.mkdirs(session, linkPath);
		linkNode.addMixin(RSS_LINK);
		linkNode.setProperty(RSS_LINK, url);
		linkNode.setProperty(Property.JCR_TITLE, entry.getTitle());
		linkNode.setProperty(Property.JCR_DESCRIPTION, entry.getDescription()
				.getValue());
		// linkNode.setProperty(ConnectNames.CONNECT_AUTHOR, (String[]) entry
		// .getAuthors().toArray(new String[entry.getAuthors().size()]));
		// linkNode.setProperty(ConnectNames.CONNECT_PUBLISHED_DATE,
		// publishedDate);
		// linkNode.setProperty(ConnectNames.CONNECT_UPDATED_DATE,
		// publishedDate);
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
