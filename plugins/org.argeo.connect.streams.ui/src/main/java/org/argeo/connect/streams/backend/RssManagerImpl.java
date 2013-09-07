package org.argeo.connect.streams.backend;

import java.io.InputStream;
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
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssConstants;
import org.argeo.connect.streams.RssManager;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.jcr.JcrUtils;
import org.jdom.Element;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/** Retrieve items and manages them in the repository. */
public class RssManagerImpl implements RssNames, RssManager {
	private final static Log log = LogFactory.getLog(RssManagerImpl.class);

	private Repository repository;

	private Session adminSession;

	private Map<String, List<String>> defaultChannels = new HashMap<String, List<String>>();

	private Integer pollingPeriod = 0;
	private Thread pollingThread = null;

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
					getOrCreateChannel(adminSession, url);
				} catch (Exception e) {
					log.error("Cannot register " + url, e);
				}
			}

			// Load latest
			retrieveItems();

			// Polling htread
			if (pollingPeriod > 0) {
				pollingThread = new PollingThread(SecurityContextHolder
						.getContext().getAuthentication());
				pollingThread.start();
			}
		} catch (RepositoryException e) {
			JcrUtils.logoutQuietly(adminSession);
			throw new ArgeoException("Cannot login to repository", e);
		}
	}

	public synchronized void destroy() {
		JcrUtils.logoutQuietly(adminSession);
		adminSession = null;// used as marker by the polling thread
		notifyAll();
		if (pollingThread != null)
			pollingThread.interrupt();
	}

	public Node getOrCreateChannel(Session session, String url) {
		try {
			for (NodeIterator nit = session.getNode(
					RssConstants.RSS_CHANNELS_BASE).getNodes(); nit.hasNext();) {
				Node channelNode = nit.nextNode();
				String channelUrl = channelNode.getProperty(RSS_URI)
						.getString();
				if (channelUrl.equals(url))
					return channelNode;
			}
			URL feedUrl = new URL(url);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));
			String nodeName = JcrUtils.replaceInvalidChars(feed.getTitle());
			Node channelNode = adminSession.getNode(
					RssConstants.RSS_CHANNELS_BASE).addNode(nodeName,
					RssTypes.RSS_CHANNEL);

			channelNode.setProperty(RSS_URI, url);
			channelNode.setProperty(RSS_LINK, feed.getLink());

			Node channelInfoNode = channelNode.getNode(RSS_CHANNEL_INFO);
			channelInfoNode.setProperty(Property.JCR_TITLE, feed.getTitle());
			channelInfoNode.setProperty(Property.JCR_DESCRIPTION,
					feed.getDescription());

			if (log.isDebugEnabled())
				log.debug("Registered channel: '" + feed.getTitle() + "' ("
						+ url + ")");
			adminSession.save();
			return channelNode;
		} catch (Exception e) {
			throw new ArgeoException("Cannot create channel", e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void retrieveItems() {
		try {
			for (NodeIterator nit = adminSession.getNode(
					RssConstants.RSS_CHANNELS_BASE).getNodes(); nit.hasNext();) {
				Node channelNode = nit.nextNode();
				String title = channelNode.getNode(RSS_CHANNEL_INFO)
						.getProperty(Property.JCR_TITLE).getString();
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
		String nodeName = JcrUtils.replaceInvalidChars(entry.getTitle()).trim();
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

		String description = entry.getDescription().getValue();
		itemNode.setProperty(Property.JCR_DESCRIPTION, description);

		itemNode.setProperty(RSS_PUB_DATE, publishedDate);
		if (updatedDate != null)
			itemNode.setProperty(RSS_UPDATE_DATE, updatedDate);

		// Categories
		List<String> categories = new ArrayList<String>();
		for (SyndCategory syndCategory : (List<SyndCategory>) entry
				.getCategories()) {
			categories.add(syndCategory.getName());
		}
		itemNode.setProperty(RSS_CATEGORY,
				categories.toArray(new String[categories.size()]));
		itemNode.getSession().save();

		// MEDIAS

		// Enclosure
		for (SyndEnclosure enclosure : (List<SyndEnclosure>) entry
				.getEnclosures()) {
			// String type = enclosure.getType();
			String url = enclosure.getUrl();
			if (url != null)
				addMedia(itemNode, url, null);
		}

		// Foreign markup
		List<Element> foreignMarkup = (List<Element>) entry.getForeignMarkup();
		String url = null;
		String urlDesc = null;
		for (Element element : foreignMarkup) {
			if (element.getQualifiedName().equals("media:content")) {
				url = element.getAttributeValue("url");
			}
			if (element.getQualifiedName().equals("media:description")) {
				urlDesc = element.getText();
			}
		}
		if (url != null)
			addMedia(itemNode, url, urlDesc);

		return true;
	}

	protected void addMedia(Node itemNode, String url, String description)
			throws RepositoryException {
		InputStream in = null;
		try {
			URL u = new URL(url);
			in = u.openStream();
			Node mediasNode = JcrUtils.getOrAdd(itemNode, RSS_MEDIAS);
			String fileName = JcrUtils.lastPathElement(u.getPath());

			Node fileNode = JcrUtils.copyStreamAsFile(mediasNode, fileName, in);
			fileNode.addMixin(NodeType.MIX_TITLE);
			fileNode.setProperty(Property.JCR_TITLE, fileName);
			if (description != null)
				fileNode.setProperty(Property.JCR_DESCRIPTION, fileName);
			itemNode.getSession().save();
		} catch (Exception e) {
			log.error("Cannot add media " + url, e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setDefaultChannels(Map<String, List<String>> defaultChannels) {
		this.defaultChannels = defaultChannels;
	}

	/** Polling period, in s. 0 is no polling. */
	public void setPollingPeriod(Integer pollingPeriod) {
		this.pollingPeriod = pollingPeriod;
	}

	private class PollingThread extends Thread {
		private final Authentication authentication;

		public PollingThread(Authentication authentication) {
			super("RSS Polling");
			this.authentication = authentication;
		}

		public void run() {
			SecurityContextHolder.getContext()
					.setAuthentication(authentication);
			if (log.isDebugEnabled())
				log.debug("Started " + getName() + " thread");
			try {
				Thread.sleep(pollingPeriod * 1000);
			} catch (InterruptedException e) {
				// silent
			}

			while (adminSession != null) {
				retrieveItems();
				try {
					Thread.sleep(pollingPeriod * 1000);
				} catch (InterruptedException e) {
					// silent
				}
			}
		}
	}
}
