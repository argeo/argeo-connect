package org.argeo.connect.resources.workbench;

import static org.argeo.connect.resources.ResourcesNames.RESOURCES_TAGGABLE_PROP_NAME;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.core.TagUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * A label provider to display clickable tag-like resources like in
 * markup-enabled viewers
 */
public class OtherTagsLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = -3486673830618518227L;

	// private final String tagTypeId;
	// Context
	private final Session session;
	private final ResourcesService resourcesService;
	private final AppWorkbenchService appWorkbenchService;
	private String tagId;

	private final String taggablePropName;
	private final String currentTag;
	private Node tagLikeResPar;
	private String selectorName;

	// enhance labels
	private String labelPrefix = "#";

	public OtherTagsLabelProvider(ResourcesService resourcesService, AppWorkbenchService appWorkbenchService,
			Session session, String tagId, String currentTag) {
		this.resourcesService = resourcesService;
		this.appWorkbenchService = appWorkbenchService;
		this.session = session;
		this.tagId = tagId;
		this.currentTag = currentTag;
		tagLikeResPar = resourcesService.getTagLikeResourceParent(session, tagId);
		if(tagLikeResPar!=null){// workaround
		taggablePropName = ConnectJcrUtils.getMultiAsString(tagLikeResPar, RESOURCES_TAGGABLE_PROP_NAME, ",");
		}else{
			taggablePropName = null;
		}
	}

	public OtherTagsLabelProvider(ResourcesService resourcesService, AppWorkbenchService appWorkbenchService,
			Node tagInstance, String selectorName) {
		this.session = ConnectJcrUtils.getSession(tagInstance);
		this.resourcesService = resourcesService;
		this.appWorkbenchService = appWorkbenchService;
		this.selectorName = selectorName;
		tagLikeResPar = TagUtils.retrieveTagParentFromTag(tagInstance);
		tagId = ConnectJcrUtils.get(tagLikeResPar, ResourcesNames.RESOURCES_TAG_ID);
		taggablePropName = ConnectJcrUtils.getMultiAsString(tagLikeResPar, RESOURCES_TAGGABLE_PROP_NAME, ",");
		currentTag = TagUtils.retrieveTagId(tagInstance);
	}

	public void setLabelPrefix(String labelPrefix) {
		this.labelPrefix = labelPrefix;
	}

	@Override
	public String getText(Object element) {
//		if(true)
//			return "";
		// try {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		// TODO also handle encoded tag case.
		StringBuilder builder = new StringBuilder();
		List<String> currTags = ConnectJcrUtils.getMultiAsList(currNode, taggablePropName);
		loop: for (String tag : currTags) {
			if (EclipseUiUtils.notEmpty(currentTag) && tag.equals(currentTag))
				continue loop;
			// TODO rather display links to open corresponding tag editor
			String value = ConnectWorkbenchUtils.getTagLink(session, resourcesService, appWorkbenchService, tagId, tag);
			builder.append(labelPrefix).append(value).append(" ");
		}

		if (builder.length() > 2)
			return builder.toString();
		else
			return "";
		// } catch (RepositoryException re) {
		// throw new ActivitiesException("Unable to get date from node " +
		// element, re);
		// }
	}
}
