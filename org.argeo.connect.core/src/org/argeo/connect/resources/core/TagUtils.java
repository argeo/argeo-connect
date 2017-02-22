package org.argeo.connect.resources.core;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.util.ConnectJcrUtils;

public class TagUtils {
	public static Node retrieveTagParentFromTag(Node tag) {
		Node parent = tag;
		while (!ConnectJcrUtils.isNodeType(parent, ResourcesTypes.RESOURCES_TAG_PARENT))
			parent = ConnectJcrUtils.getParent(parent);
		return parent;
	}

	private TagUtils() {
	}
}
