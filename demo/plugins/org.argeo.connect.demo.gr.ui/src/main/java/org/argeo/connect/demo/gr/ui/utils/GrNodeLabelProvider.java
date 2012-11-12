package org.argeo.connect.demo.gr.ui.utils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

/** Label provider for the browser tree */
public class GrNodeLabelProvider extends DefaultNodeLabelProvider implements
		GrConstants {
	// /private final static Log log =
	// LogFactory.getLog(GrNodeLabelProvider.class);

	// Images
	public final static Image networkFolderImg = GrUiPlugin.getImageDescriptor(
			"icons/networkList.gif").createImage();
	public final static Image networkImg = GrUiPlugin.getImageDescriptor(
			"icons/network.png").createImage();

	public String getText(Object element) {
		String curText = super.getText(element);

		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				if (node.isNodeType(NodeType.MIX_TITLE)) {
					if (node.hasProperty(Property.JCR_TITLE))
						return node.getProperty(Property.JCR_TITLE).getString();
					else
						return node.getName();
				}
			} catch (RepositoryException e) {
				throw new GrException("Cannot get text", e);
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
