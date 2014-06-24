package org.argeo.connect.streams.web;

/** Styles references in the CSS for forms. */
public interface StreamsWebStyles {

	// General
	public final static String STREAMS_MAIN_COMPOSITE = "streams_main_composite";
	public final static String STREAMS_TITLE = "streams_title";
	public final static String STREAMS_DESCRIPTION = "streams_description";
	public final static String STREAMS_PUBLICATION_DATE = "streams_publication_date";
	public final static String STREAMS_TAG = "streams_tag";
	public final static String STREAMS_ITEM_EXCERPT = "streams_item_excerpt";

	// WARNING:
	// We cannot use the usual CSS classes to apply styling on labels and link
	// that are items of a styled jface table viewer. Corresponding style must
	// be described here.
	public final static String STREAMS_TAG_STYLE = "style='color:#383838; font-decoration:none; font-decoration:none;'";
	public final static String STREAMS_URL_STYLE = "style='color:#383838; font-decoration:none;'";
	public final static String STREAMS_LIST_ITEM_STYLE = "style='float:left;padding:0px;white-space:pre-wrap;'";
	
	
	// Below alternative throws following error: 'Unsupported attribute "class"
	// for element "a" in markup text'
	// public final static String CSS_URL_STYLE = "class='testClass'";
}
