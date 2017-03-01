package org.argeo.connect.resources.workbench;

import static org.argeo.connect.resources.ResourcesNames.RESOURCES_TAGGABLE_PROP_NAME;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.core.TagUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class OtherTagsLabelProvider extends ColumnLabelProvider {

	// private final String tagTypeId;
	private final String taggablePropName;
	private final String currentTag;
	private Node tagLikeResPar;
	private String selectorName;

	public OtherTagsLabelProvider(ResourcesService resourcesService, Session session, String tagId, String currentTag) {
		tagLikeResPar = resourcesService.getTagLikeResourceParent(session, tagId);
		taggablePropName = ConnectJcrUtils.get(tagLikeResPar, ResourcesNames.RESOURCES_TAGGABLE_PROP_NAME);
		this.currentTag = currentTag;
	}

	public OtherTagsLabelProvider(Node tageInstance, String selectorName) {
		this.selectorName = selectorName;
		tagLikeResPar = TagUtils.retrieveTagParentFromTag(tageInstance);
		taggablePropName = ConnectJcrUtils.getMultiAsString(tagLikeResPar, RESOURCES_TAGGABLE_PROP_NAME, ",");
		currentTag = TagUtils.retrieveTagId(tageInstance);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getText(Object element) {
		// try {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		// TODO also handle encoded tag case.
		StringBuilder builder = new StringBuilder();
		List<String> currTags = ConnectJcrUtils.getMultiAsList(currNode, taggablePropName);
		loop: for (String tag : currTags) {
			if (tag.equals(currentTag))
				continue loop;
			// TODO rather display links to open corresponding tag editor
			builder.append(tag).append(", ");
		}

		if (builder.length() > 2)
			return builder.substring(0, builder.length() - 2);
		else
			return "";
		// } catch (RepositoryException re) {
		// throw new ActivitiesException("Unable to get date from node " +
		// element, re);
		// }
	}
}
