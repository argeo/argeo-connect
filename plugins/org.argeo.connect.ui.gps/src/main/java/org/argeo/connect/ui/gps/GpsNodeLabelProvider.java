package org.argeo.connect.ui.gps;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.ui.ConnectUiPlugin;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

public class GpsNodeLabelProvider extends DefaultNodeLabelProvider {
	// Images
	public final static Image sessionFolder = ConnectUiPlugin
			.getImageDescriptor("icons/home.gif").createImage();
	public final static Image session = ConnectUiPlugin.getImageDescriptor(
			"icons/repository.gif").createImage();

	public String getText(Object element) {
		// GPS connect Specific labels.
		// Not yet used
		// if (element instanceof RepositoryRegister) {
		// return "Repositories";
		// }
		return super.getText(element);
	}

	/** Specific label handling for connect GPS */
	protected String getText(Node node) throws RepositoryException {
		String label = node.getName();
		
		return label; 
	}
	
	@Override
	public Image getImage(Object element) {
		// if (element instanceof RepositoryNode) {
		// if (((RepositoryNode) element).getDefaultSession() == null)
		// return RepositoryNode.REPOSITORY_DISCONNECTED;
		// else
		// return RepositoryNode.REPOSITORY_CONNECTED;
		// } else if (element instanceof WorkspaceNode) {
		// if (((WorkspaceNode) element).getSession() == null)
		// return WorkspaceNode.WORKSPACE_DISCONNECTED;
		// else
		// return WorkspaceNode.WORKSPACE_CONNECTED;
		// } else if (element instanceof RepositoryRegister) {
		// return REPOSITORIES;
		// }
		return super.getImage(element);
	}

}
