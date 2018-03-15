package org.argeo.people.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.people.PeopleException;

/** Methods to ease implementation of People Web UI */
public class PeopleWebSnippets {
	/** a snippet to display tags that are linked to the current entity */
	public static String getTagLikeValues(ResourcesService tagService, String tagId, Node taggableNode,
			String taggablePropName, String prefix) {
		StringBuilder builder = new StringBuilder();
		try {
			Session session = taggableNode.getSession();
			Node tagParent = tagService.getTagLikeResourceParent(session, tagId);
			// String tagParentPath = tagParent.getPath();
			if (taggableNode.hasProperty(taggablePropName)) {
				for (Value value : taggableNode.getProperty((taggablePropName)).getValues()) {
					String valueStr = value.getString();
					Node tag = tagService.getRegisteredTag(tagParent, valueStr);
					builder.append(prefix);
					builder.append("<a href=\"#" + tag.getPath() + "\">").append(valueStr).append("</a> ");
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Error while tag like property " + taggablePropName + " values for node " + taggableNode, e);
		}
		return ConnectUtils.replaceAmpersand(builder.toString().trim());
	}
}
