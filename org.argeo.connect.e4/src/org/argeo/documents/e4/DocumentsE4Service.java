package org.argeo.documents.e4;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsTypes;
import org.eclipse.swt.graphics.Image;

public class DocumentsE4Service implements AppE4Service {

	@Override
	public String getEntityEditorId(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE)) {
			// return FileEditor.ID;
			return "org.argeo.suite.e4.partdescriptor.file";
		} else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER)) {
			// return FolderEditor.ID;
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
		if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE))
			return ConnectImages.FILE;
		else if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return ConnectImages.FOLDER;
		else if (ConnectJcrUtils.isNodeType(entity, DocumentsTypes.DOCUMENTS_BOOKMARK))
			return ConnectImages.BOOKMARK;
		else
			return null;
	}

	@Override
	public void openEntityEditor(Node entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openSearchEntityView(String nodeType, String label) {
		// TODO Auto-generated method stub
		
	}
	
	
}
