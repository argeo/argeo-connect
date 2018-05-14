package org.argeo.connect.e4.resources;

import javax.jcr.Node;

import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.swt.graphics.Image;

public class ResourcesE4Service implements AppE4Service {

	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return "org.argeo.suite.e4.partdescriptor.tagEditor";
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (ResourcesTypes.RESOURCES_TAG.equals(nodeType))
			return null;
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return ConnectImages.TAG;
		return null;
	}
}
