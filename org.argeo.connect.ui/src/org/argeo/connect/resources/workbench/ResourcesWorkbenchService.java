package org.argeo.connect.resources.workbench;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectWorkbenchImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.swt.graphics.Image;

public class ResourcesWorkbenchService implements AppWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return TagEditor.ID;
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (ResourcesTypes.RESOURCES_TAG.equals(nodeType))
			return SearchTagsEditor.ID;
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, ResourcesTypes.RESOURCES_TAG))
			return ConnectWorkbenchImages.ICON_TAG;
		return null;
	}
}
