package org.argeo.connect.streams;

import javax.jcr.Property;

/** Property Names for streams concepts. */
public interface RssNames {

	//
	public final static String RSS_TITLE = Property.JCR_TITLE;
	public final static String RSS_DESCRIPTION = Property.JCR_DESCRIPTION;
	public final static String RSS_LINK = "rss:link";
	public final static String RSS_URI = "rss:uri";

	// ITEM
	public final static String RSS_CATEGORY = "rss:category";
	// Typically an image
	public final static String RSS_ENCLOSURE = "rss:enclosure";
	public final static String RSS_PUB_DATE = "rss:pubDate";
	public final static String RSS_UPDATE_DATE = " rss:updatedate";
}
