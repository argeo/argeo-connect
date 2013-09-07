package org.argeo.connect.streams;

/** Property Names for streams concepts. */
public interface RssNames {

	// CHANNEL
	public final static String RSS_LINK = "rss:link";
	public final static String RSS_URI = "rss:uri";

	// CHANNEL INFO - Path to the child that contains all versionable info
	public final static String RSS_CHANNEL_INFO = "rss:channelInfo";

	// public final static String RSS_TITLE = Property.JCR_TITLE;
	// public final static String RSS_DESCRIPTION = Property.JCR_DESCRIPTION;

	// ITEM
	public final static String RSS_CATEGORY = "rss:category";
	// Typically an image
	public final static String RSS_MEDIAS = "rss:medias";
	public final static String RSS_PUB_DATE = "rss:pubDate";
	public final static String RSS_UPDATE_DATE = " rss:updatedate";
}
