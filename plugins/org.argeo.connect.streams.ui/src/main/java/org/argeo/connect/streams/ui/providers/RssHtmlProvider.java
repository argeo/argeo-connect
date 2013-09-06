package org.argeo.connect.streams.ui.providers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssNames;

/** Some helper methods to generate html snippets */

public class RssHtmlProvider implements RssNames {
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy");

	/** shortcut to set form data while dealing with switching panel */
	public static String pubDateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			if (entity.hasProperty(RSS_PUB_DATE)) {
				builder.append("<small><i>");
				builder.append(df.format(entity.getProperty(RSS_PUB_DATE)
						.getDate().getTime()));
				builder.append("</i></small>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot create organizations content", re);
		}
	}
}
