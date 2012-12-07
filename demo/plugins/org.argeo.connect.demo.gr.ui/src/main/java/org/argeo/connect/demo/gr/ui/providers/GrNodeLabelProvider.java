/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

/** Label provider for the browser tree */
public class GrNodeLabelProvider extends DefaultNodeLabelProvider {

	// Helper to simplify retrieving of a user friendly name for the various
	// node types of GR Application
	public static String getName(Node node) {
		try {
			if (node.isNodeType(GrTypes.GR_SITE))
				return node.getIdentifier().substring(0, 8);
			else if (node.isNodeType(NodeType.MIX_TITLE))
				return node.getProperty(Property.JCR_TITLE).getString();
			else {
				String name = node.getName();
				if (GrNames.GR_NETWORKS.equals(name))
					return GrMessages.get().networksLbl;
				else
					return name;
			}

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
				return GrImages.ICON_NETWORK;
			else if (node.isNodeType(GrTypes.GR_SITE)) {
				String siteType = node.getProperty(GrNames.GR_SITE_TYPE)
						.getString();
				if (GrConstants.MONITORED.equals(siteType))
					return GrImages.ICON_MONITORED_TYPE;
				else if (GrConstants.VISITED.equals(siteType))
					return GrImages.ICON_VISITED_TYPE;
				else if (GrConstants.REGISTERED.equals(siteType))
					return GrImages.ICON_REGISTERED_TYPE;
			} else if (GrNames.GR_NETWORKS.equals(node.getName()))
				return GrImages.ICON_NETWORKS;

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