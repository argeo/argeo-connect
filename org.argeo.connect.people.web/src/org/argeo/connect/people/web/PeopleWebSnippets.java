package org.argeo.connect.people.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.ui.ConnectUiUtils;

public class PeopleWebSnippets {
	/** a snippet to display tags that are linked to the current entity */
	public static String getTagLikeValues(PeopleService peopleService, String tagId, Node taggableNode,
			String taggablePropName, String prefix) {
		StringBuilder builder = new StringBuilder();

		ResourceService tagService = peopleService.getResourceService();
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
		return ConnectUiUtils.replaceAmpersand(builder.toString().trim());
	}
}
