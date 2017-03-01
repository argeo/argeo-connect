package org.argeo.connect.resources.core;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.util.ConnectJcrUtils;

public class TagUtils {
	public static Node retrieveTagParentFromTag(Node tag) {
		Node parent = tag;
		while (!ConnectJcrUtils.isNodeType(parent, ResourcesTypes.RESOURCES_TAG_PARENT))
			parent = ConnectJcrUtils.getParent(parent);
		return parent;
	}

	public static String retrieveTagId(Node tag) {
		if (ConnectJcrUtils.isNodeType(tag, ResourcesTypes.RESOURCES_TAG))
			return ConnectJcrUtils.get(tag, Property.JCR_TITLE);
		else if (ConnectJcrUtils.isNodeType(tag, ResourcesTypes.RESOURCES_ENCODED_TAG))
			return ConnectJcrUtils.get(tag, ResourcesNames.RESOURCES_TAG_CODE);
		else
			throw new ConnectException(tag + " is not a valid tag");
	}

	private TagUtils() {
	}
}
