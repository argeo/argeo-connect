package org.argeo.connect.streams.backend;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

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

	private SAXParser saxParser = createSAXParser();

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
			if (log.isDebugEnabled())
				log.debug("Initializing feed at " + feedUrl.toURI());
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));
			String nodeName = JcrUtils.replaceInvalidChars(feed.getTitle());
			Node channelNode = adminSession.getNode(
					RssConstants.RSS_CHANNELS_BASE).addNode(nodeName,
					RssTypes.RSS_CHANNEL);

			channelNode.setProperty(RSS_URI, url);
			channelNode.setProperty(RSS_LINK, feed.getLink());

			Node channelInfoNode = channelNode.getNode(RSS_CHANNEL_INFO);
			channelInfoNode.setProperty(Property.JCR_TITLE,
					sanitize(feed.getTitle()));
			channelInfoNode.setProperty(Property.JCR_DESCRIPTION,
					sanitize(feed.getDescription()));

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
		itemNode.setProperty(Property.JCR_TITLE, sanitize(entry.getTitle()));

		String description = entry.getDescription().getValue();
		itemNode.setProperty(Property.JCR_DESCRIPTION, sanitize(description));

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

	/** Clean up string or return the empty string if it can't. */
	protected synchronized String sanitize(String original) {
		if (original == null)
			return null;
		String clean = removeTags(original);
		if (isTextValid(clean))
			return clean;
		else
			return "";
	}

	protected synchronized Boolean isTextValid(String str) {
		try {
			StringBuilder markup = new StringBuilder();
			markup.append("<html>");
			markup.append(str);
			markup.append("</html>");
			InputSource inputSource = new InputSource(new StringReader(
					markup.toString()));
			saxParser.parse(inputSource, new MarkupHandler());
			return true;
		} catch (Exception e) {
			log.error("Bad input (" + e + "):\n" + str + "\n");
			return false;
		}
	}

	protected String removeTags(String str) {
		StringBuilder clean = new StringBuilder("");
		boolean inTag = false;
		boolean inEntity = false;
		StringBuilder currEntity = null;
		for (char c : str.toCharArray()) {
			if (inTag) {// tags
				if (c == '>')
					inTag = false;
			} else {// text
				if (inEntity) {
					currEntity.append(c);
					if (c == ';') {
						inEntity = false;
						String entity = currEntity.toString();
						if (entity.charAt(1) == '#' || entity.equals("&quot;")
								|| entity.equals("&amp;")
								|| entity.equals("&apos;")
								|| entity.equals("&lt;")
								|| entity.equals("&gt;"))
							clean.append(entity);
					} else if (c == ' ') {// was not an entity
						inEntity = false;
						String entity = currEntity.toString();
						entity = entity.replace("&", "&amp;");
						clean.append(entity);
					} else {
						currEntity.append(c);
					}
				} else {
					if (c == '<')
						inTag = true;
					else if (c == '&') {
						inEntity = true;
						currEntity = new StringBuilder();
						currEntity.append(c);
					} else
						clean.append(c);
				}

			}
		}
		return clean.toString();
	}

	private static SAXParser createSAXParser() {
		SAXParser result = null;
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		try {
			result = parserFactory.newSAXParser();
		} catch (Exception exception) {
			throw new RuntimeException("Failed to create SAX parser", exception);
		}
		return result;
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

	private static class MarkupHandler extends DefaultHandler {
		private static final Map<String, String[]> SUPPORTED_ELEMENTS = createSupportedElementsMap();

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) {
			checkSupportedElements(name, attributes);
			checkSupportedAttributes(name, attributes);
			checkMandatoryAttributes(name, attributes);
		}

		private static void checkSupportedElements(String elementName,
				Attributes attributes) {
			if (!SUPPORTED_ELEMENTS.containsKey(elementName)) {
				throw new IllegalArgumentException(
						"Unsupported element in markup text: " + elementName);
			}
		}

		private static void checkSupportedAttributes(String elementName,
				Attributes attributes) {
			if (attributes.getLength() > 0) {
				List<String> supportedAttributes = Arrays
						.asList(SUPPORTED_ELEMENTS.get(elementName));
				int index = 0;
				String attributeName = attributes.getQName(index);
				while (attributeName != null) {
					if (!supportedAttributes.contains(attributeName)) {
						String message = "Unsupported attribute \"{0}\" for element \"{1}\" in markup text";
						message = MessageFormat.format(message, new Object[] {
								attributeName, elementName });
						throw new IllegalArgumentException(message);
					}
					index++;
					attributeName = attributes.getQName(index);
				}
			}
		}

		private static void checkMandatoryAttributes(String elementName,
				Attributes attributes) {
			checkIntAttribute(elementName, attributes, "img", "width");
			checkIntAttribute(elementName, attributes, "img", "height");
		}

		private static void checkIntAttribute(String elementName,
				Attributes attributes, String checkedElementName,
				String checkedAttributeName) {
			if (checkedElementName.equals(elementName)) {
				String attribute = attributes.getValue(checkedAttributeName);
				try {
					Integer.parseInt(attribute);
				} catch (NumberFormatException exception) {
					String message = "Mandatory attribute \"{0}\" for element \"{1}\" is missing or not a valid integer";
					Object[] arguments = new Object[] { checkedAttributeName,
							checkedElementName };
					message = MessageFormat.format(message, arguments);
					throw new IllegalArgumentException(message);
				}
			}
		}

		private static Map<String, String[]> createSupportedElementsMap() {
			Map<String, String[]> result = new HashMap<String, String[]>();
			result.put("html", new String[0]);
			result.put("br", new String[0]);
			result.put("b", new String[] { "style" });
			result.put("strong", new String[] { "style" });
			result.put("i", new String[] { "style" });
			result.put("em", new String[] { "style" });
			result.put("sub", new String[] { "style" });
			result.put("sup", new String[] { "style" });
			result.put("big", new String[] { "style" });
			result.put("small", new String[] { "style" });
			result.put("del", new String[] { "style" });
			result.put("ins", new String[] { "style" });
			result.put("code", new String[] { "style" });
			result.put("samp", new String[] { "style" });
			result.put("kbd", new String[] { "style" });
			result.put("var", new String[] { "style" });
			result.put("cite", new String[] { "style" });
			result.put("dfn", new String[] { "style" });
			result.put("q", new String[] { "style" });
			result.put("abbr", new String[] { "style", "title" });
			result.put("span", new String[] { "style" });
			result.put("img", new String[] { "style", "src", "width", "height",
					"title", "alt" });
			result.put("a", new String[] { "style", "href", "target", "title" });
			return result;
		}

	}

}
