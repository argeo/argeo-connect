package org.argeo.connect.demo.gr.ui.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;


public class GrNodeLabelProvider extends DefaultNodeLabelProvider implements
		GrConstants {
	private final static Log log = LogFactory.getLog(GrNodeLabelProvider.class);

	// Images
	public final static Image networkFolderImg = GrUiPlugin
			.getImageDescriptor("icons/networkList.gif").createImage();
	public final static Image networkImg = GrUiPlugin.getImageDescriptor(
			"icons/network.png").createImage();

	public String getText(Object element) {
		// GPS connect Specific labels.
		String curText = super.getText(element);

		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				// Useless : we set network name as the rel path, we do not use
				// GR_NETWORK_NAME anymore
				//
				// if (node.getPrimaryNodeType().isNodeType(GrTypes.GR_NETWORK))
				// {
				// // Network
				// if (!"".equals(node.getProperty(GrTypes.GR_NETWORK_NAME)
				// .getString()))
				// curText = node.getProperty(GrTypes.GR_NETWORK_NAME)
				// .getString();
				// } else

				if (node.getPrimaryNodeType().isNodeType(NodeType.NT_FILE)) {
					// File
					if (!"".equals(node.getName()))
						curText = node.getName();
				}
			} catch (Exception e) {
				// silent
			}
		}

		return curText;
	}

	/** Specific label handling for connect GPS */
	protected String getText(Node node) throws RepositoryException {
		String label = node.getName();
		return label;
	}

	@Override
	public Image getImage(Object element) {
		Image curImg = super.getImage(element);

		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				// ref to files.
				if (node.getPrimaryNodeType().isNodeType(GrTypes.GR_NETWORK))
					curImg = networkImg;
			} catch (Exception e) {
				// silent
			}
		}
		return curImg;
	}
}
