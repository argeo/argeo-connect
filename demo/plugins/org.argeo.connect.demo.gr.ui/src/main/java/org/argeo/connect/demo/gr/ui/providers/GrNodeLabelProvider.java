package org.argeo.connect.demo.gr.ui.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrImages;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

/** Label provider for the browser tree */
public class GrNodeLabelProvider extends DefaultNodeLabelProvider implements
		GrConstants {
	// /private final static Log log =
	// LogFactory.getLog(GrNodeLabelProvider.class);

	// Helper to simplify retrieving of a user friendly name for the various
	// node types of GR Application
	public static String getName(Node node) {
		try {
			if (node.isNodeType(GrTypes.GR_SITE))
				return node.getIdentifier().substring(0, 7);
			else if (node.isNodeType(NodeType.MIX_TITLE))
				return node.getProperty(Property.JCR_TITLE).getString();
			else
				return node.getName();
		} catch (RepositoryException e) {
			throw new GrException("Cannot retrieve name for node", e);
		}
	}

	// Helper to simplify retrieving of an icon user friendly name for the
	// various
	// node types of GR Application
	public static Image getIcon(Node node) {
		try {
			if (node.isNodeType(GrTypes.GR_NETWORK))
				return GrImages.networkImg;
			else if (node.isNodeType(GrTypes.GR_SITE)) {
				String siteType = node.getProperty(GrNames.GR_SITE_TYPE)
						.getString();
				if (GrConstants.NATIONAL.equals(siteType))
					return GrImages.ICON_NATIONAL_TYPE;
				else if (GrConstants.BASE.equals(siteType))
					return GrImages.ICON_BASE_TYPE;
				else if (GrConstants.NORMAL.equals(siteType))
					return GrImages.ICON_NORMAL_TYPE;
			}
		} catch (RepositoryException e) {
			throw new GrException("Cannot retrieve name for node", e);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof Node) {
			return getName((Node) element);
		} else
			return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		Image curImg = null;

		// Try to get a specific images for the current product
		if (element instanceof Node) {
			curImg = getIcon((Node) element);
		}

		// Try to get a default image
		if (curImg == null)
			curImg = super.getImage(element);

		return curImg;
	}
}