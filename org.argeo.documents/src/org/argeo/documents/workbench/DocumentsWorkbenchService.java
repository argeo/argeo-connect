package org.argeo.documents.workbench;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsTypes;
import org.argeo.documents.workbench.parts.FileEditor;
import org.argeo.documents.workbench.parts.FolderEditor;
import org.eclipse.swt.graphics.Image;

public class DocumentsWorkbenchService implements AppWorkbenchService {

	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE))
			return FileEditor.ID;
		else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return FolderEditor.ID;
		else
			return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE))
			return DocumentsImages.ICON_FILE;
		else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return DocumentsImages.ICON_FOLDER;
		else if (ConnectJcrUtils.isNodeType(entity, DocumentsTypes.DOCUMENTS_BOOKMARK))
			return DocumentsImages.ICON_BOOKMARK;
		else
			return null;
	}
}
