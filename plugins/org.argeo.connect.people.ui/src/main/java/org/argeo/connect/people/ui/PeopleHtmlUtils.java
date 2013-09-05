package org.argeo.connect.people.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.people.PeopleException;

/** Some helper methods to generate html snippets */
public class PeopleHtmlUtils {
	private final static DateFormat df = new SimpleDateFormat(
			"EEE, dd MMM yyyy");

	/** shortcut to set form data while dealing with switching panel */
	public static String getLastUpdateSnippet(Node entity) {
		StringBuilder builder = new StringBuilder();
		try {
			Node node = (Node) entity;
			if (node.isNodeType(NodeType.MIX_VERSIONABLE)) {
				builder.append("<br/><br/><small><i>").append(
						"Last updated on ");
				builder.append(df.format(node
						.getProperty(Property.JCR_LAST_MODIFIED).getDate()
						.getTime()));
				builder.append(", by ");
				builder.append(node.getProperty(Property.JCR_LAST_MODIFIED_BY)
						.getString());
				builder.append("</i></small>");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

}
