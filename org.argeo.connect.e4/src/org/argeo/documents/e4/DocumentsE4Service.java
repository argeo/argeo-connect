package org.argeo.documents.e4;

import static org.argeo.connect.util.ConnectJcrUtils.isNodeType;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.documents.DocumentsTypes;
import org.argeo.node.NodeTypes;
import org.eclipse.swt.graphics.Image;

public class DocumentsE4Service implements AppE4Service {

	@Override
	public String getEntityEditorId(Node entity) {
		if (isFileNodeType(entity)) {
			return "org.argeo.suite.e4.partdescriptor.file";
		} else if (isFolderNodeType(entity)) {
			return "org.argeo.suite.e4.partdescriptor.folder";
		} else
			return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		if (isFileNodeType(entity))
			return ConnectImages.FILE;
		else if (isFolderNodeType(entity))
			return ConnectImages.FOLDER;
		else if (isNodeType(entity, DocumentsTypes.DOCUMENTS_BOOKMARK))
			return ConnectImages.BOOKMARK;
		else
			return null;
	}

	private static boolean isFolderNodeType(Node entity) {
		return isNodeType(entity, NodeType.NT_FOLDER) || isNodeType(entity, NodeTypes.NODE_USER_HOME)
				|| isNodeType(entity, NodeTypes.NODE_GROUP_HOME);
	}

	private static boolean isFileNodeType(Node entity) {
		return isNodeType(entity, NodeType.NT_FILE) || isNodeType(entity, NodeType.NT_LINKED_FILE);
	}
}
