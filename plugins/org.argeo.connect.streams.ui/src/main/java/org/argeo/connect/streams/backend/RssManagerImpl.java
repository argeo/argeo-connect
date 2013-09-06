package org.argeo.connect.streams.backend;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssConstants;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.jcr.JcrUtils;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/** Retrieve items and manages them in the repository. */
public class RssManagerImpl implements RssNames {
	private final static Log log = LogFactory.getLog(RssManagerImpl.class);

	private Repository repository;

	private Session adminSession;

	private Map<String, List<String>> defaultChannels = new HashMap<String, List<String>>();

	public void init() {
		try {
			adminSession = repository.login();
			JcrUtils.mkdirs(adminSession, RssConstants.RSS_CHANNELS_BASE);
			// Register default streams if needed
			Set<String> notRegisteredUrls = new HashSet<String>(
					defaultChannels.keySet());
			for (NodeIterator nit = adminSession.getNode(
					RssConstants.RSS_CHANNELS_BASE).getNodes(); nit.hasNext();) {
				Node channelNode = nit.nextNode();
				String url = channelNode.getProperty(RSS_URI).getString();
				notRegisteredUrls.remove(url);
			}

			for (String url : notRegisteredUrls) {
				try {
					URL feedUrl = new URL(url);
					SyndFeedInput input = new SyndFeedInput();
					SyndFeed feed = input.build(new XmlReader(feedUrl));
					createChannel(adminSession, feed, url);
					adminSession.save();
				} catch (Exception e) {
					log.error("Cannot register " + url, e);
				}
			}

			// Load latest
			updateStreams();
		} catch (RepositoryException e) {
			JcrUtils.logoutQuietly(adminSession);
			throw new ArgeoException("Cannot login to repository", e);
		}
	}

	public void destroy() {
		JcrUtils.logoutQuietly(adminSession);
	}

	@SuppressWarnings("unchecked")
	public void updateStreams() {
		try {
			for (NodeIterator nit = adminSession.getNode(
					RssConstants.RSS_CHANNELS_BASE).getNodes(); nit.hasNext();) {
				Node channelNode = nit.nextNode();
				String title = channelNode.getProperty(Property.JCR_TITLE)
						.getString();
				String url = channelNode.getProperty(RSS_URI).getString();
				try {
					URL feedUrl = new URL(url);
					SyndFeedInput input = new SyndFeedInput();
					SyndFeed feed = input.build(new XmlReader(feedUrl));
					int count = 0;
					entries: for (SyndEntry entry : (List<SyndEntry>) feed
							.getEntries()) {
						if (entry.getTitle() == null
								|| entry.getTitle().trim().equals(""))
							continue entries;

						Boolean newItem = saveEntry(channelNode, entry);
						if (newItem)
							count++;
					}
					if (log.isDebugEnabled() && count != 0)
						log.debug("Retrieved " + count + " items from '"
								+ title + "' (" + url + ")");
				} catch (Exception e) {
					log.error("Cannot retrieve " + url + " to " + channelNode,
							e);
				}
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot read feeds", e);
		}

	}

	protected void createChannel(Session session, SyndFeed feed, String url)
			throws RepositoryException {
		String nodeName = JcrUtils.replaceInvalidChars(feed.getTitle());
		Node channelNode = adminSession.getNode(RssConstants.RSS_CHANNELS_BASE)
				.addNode(nodeName);
		channelNode.setProperty(Property.JCR_TITLE, feed.getTitle());
		channelNode.setProperty(RSS_URI, url);
		channelNode.setProperty(RSS_LINK, feed.getLink());
		if (log.isDebugEnabled())
			log.debug("Registered channel: '" + feed.getTitle() + "' (" + url
					+ ")");
	}

	@SuppressWarnings("unchecked")
	protected Boolean saveEntry(Node channelNode, SyndEntry entry)
			throws RepositoryException {
		Calendar publishedDate = new GregorianCalendar();
		publishedDate.setTime(entry.getPublishedDate() != null ? entry
				.getPublishedDate() : new Date());
		Calendar updatedDate = null;
		if (entry.getUpdatedDate() != null) {
			updatedDate = new GregorianCalendar();
			updatedDate.setTime(entry.getUpdatedDate());
		}
		// path
		String datePath = JcrUtils.dateAsPath(publishedDate);
		String nodeName = JcrUtils.replaceInvalidChars(entry.getTitle());
		String relPath = datePath + nodeName;

		Node itemNode;
		// TODO check update date
		if (channelNode.hasNode(relPath)) {
			return false;
		} else {
			itemNode = JcrUtils.mkdirs(channelNode.getSession(),
					channelNode.getPath() + '/' + datePath);
			itemNode = channelNode.addNode(relPath, RssTypes.RSS_ITEM);
		}
		itemNode.setProperty(RSS_LINK, entry.getLink());
		itemNode.setProperty(Property.JCR_TITLE, entry.getTitle());
		itemNode.setProperty(Property.JCR_DESCRIPTION, entry.getDescription()
				.getValue());
		// linkNode.setProperty(ConnectNames.CONNECT_AUTHOR, (String[])
		// entry
		// .getAuthors().toArray(new String[entry.getAuthors().size()]));
		itemNode.setProperty(RSS_PUB_DATE, publishedDate);
		// linkNode.setProperty(ConnectNames.CONNECT_UPDATED_DATE,
		// publishedDate);
		List<String> categories = new ArrayList<String>();
		for (SyndCategory syndCategory : (List<SyndCategory>) entry
				.getCategories()) {
			categories.add(syndCategory.getName());
		}
		itemNode.setProperty(RSS_CATEGORY,
				categories.toArray(new String[categories.size()]));

		itemNode.getSession().save();
		return true;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setDefaultChannels(Map<String, List<String>> defaultChannels) {
		this.defaultChannels = defaultChannels;
	}

}
