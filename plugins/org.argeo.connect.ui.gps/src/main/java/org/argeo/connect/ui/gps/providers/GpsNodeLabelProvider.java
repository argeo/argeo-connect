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
package org.argeo.connect.ui.gps.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

public class GpsNodeLabelProvider extends DefaultNodeLabelProvider implements
		ConnectTypes, ConnectNames, ConnectGpsLabels {
	// private final static Log log = LogFactory
	// .getLog(GpsNodeLabelProvider.class);

	// Images
	public final static Image sessionFolder = ConnectGpsUiPlugin
			.getImageDescriptor("icons/home.gif").createImage();
	public final static Image session = ConnectGpsUiPlugin.getImageDescriptor(
			"icons/session.gif").createImage();
	public final static Image sessionDone = ConnectGpsUiPlugin
			.getImageDescriptor("icons/sessionDone.gif").createImage();
	public final static Image repos = ConnectGpsUiPlugin.getImageDescriptor(
			"icons/repos.gif").createImage();
	public final static Image repo = ConnectGpsUiPlugin.getImageDescriptor(
			"icons/repo.gif").createImage();

	public final static Image fileNewImg = ConnectGpsUiPlugin
			.getImageDescriptor("icons/file_new.gif").createImage();
	public final static Image fileProcessedImg = ConnectGpsUiPlugin
			.getImageDescriptor("icons/file_processed.gif").createImage();

	public String getText(Object element) {
		String curText = super.getText(element);
		// GPS connect Specific labels.
		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				if (node.hasProperty(Property.JCR_TITLE))
					curText = node.getProperty(Property.JCR_TITLE).getString();
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_SESSION_REPOSITORY)) {
					// Parent Session Node
					curText = ConnectGpsUiPlugin
							.getGPSMessage(SESSION_REPOSITORY_LBL);
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
				// Files.
				if (node.getPrimaryNodeType()
						.isNodeType(CONNECT_FILE_TO_IMPORT))
					if (node.getProperty(CONNECT_ALREADY_PROCESSED)
							.getBoolean())
						curImg = fileProcessedImg;
					else
						curImg = fileNewImg;
				// Sessions
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_CLEAN_TRACK_SESSION))
					if (node.getProperty(CONNECT_IS_SESSION_COMPLETE)
							.getBoolean())
						curImg = sessionDone;
					else
						curImg = session;
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_LOCAL_REPOSITORY))
					curImg = repo;
				// Parent folder
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_SESSION_REPOSITORY))
					curImg = sessionFolder;
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_LOCAL_REPOSITORIES))
					curImg = repos;

			} catch (Exception e) {
				// silent
			}
		}
		return curImg;
	}
}