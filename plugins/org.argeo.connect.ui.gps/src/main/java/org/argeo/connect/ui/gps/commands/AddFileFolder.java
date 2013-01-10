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
package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsImages;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Enable addition of file folders in the user defined tree structure.
 */
public class AddFileFolder extends AbstractHandler implements ConnectTypes {
	public final static String ID = ConnectGpsUiPlugin.ID + ".addFileFolder";
	public final static String DEFAULT_LABEL = "Add a sub folder";
	public final static ImageDescriptor DEFAULT_ICON = GpsImages.ICON_ADD_FOLDER;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		GpsBrowserView view = (GpsBrowserView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(HandlerUtil.getActivePartId(event));
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof Node) {
				String folderName = SingleValue.ask("Folder name",
						"Enter folder name");
				if (folderName != null) {
					Node parentNode = (Node) obj;
					try {
						Node newNode = parentNode.addNode(folderName,
								ConnectTypes.CONNECT_FILE_REPOSITORY);
						view.nodeAdded(parentNode, newNode);
						parentNode.getSession().save();
					} catch (RepositoryException e) {
						ErrorFeedback.show("Cannot create folder " + folderName
								+ " under " + parentNode, e);
					}
				}
			} else {
				ErrorFeedback.show("Can only add file folder to a node");
			}
		}
		return null;
	}
}