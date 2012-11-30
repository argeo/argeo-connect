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

import java.util.Iterator;

import javax.jcr.Node;

import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.eclipse.ui.jcr.commands.DeleteNodes;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteNodesExt extends DeleteNodes {
	public final static String ID = "org.argeo.connect.ui.gps.deleteNodes";
	public final static String DEFAULT_ICON_REL_PATH = "icons/remove.gif";
	public final static String DEFAULT_LABEL = "Delete selected nodes";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		AbstractJcrBrowser view = (AbstractJcrBrowser) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(HandlerUtil.getActivePartId(event));

		if (selection != null && selection instanceof IStructuredSelection) {
			Iterator<?> it = ((IStructuredSelection) selection).iterator();
			Object obj = null;
			Node ancestor = null;
			try {
				while (it.hasNext()) {
					obj = it.next();
					if (obj instanceof Node) {
						Node node = (Node) obj;
						Node parentNode = node.getParent();
						remove(node);
						node.getSession().save();
						ancestor = getOlder(ancestor, parentNode);

					}
				}
				if (ancestor != null)
					view.nodeRemoved(ancestor);
			} catch (Exception e) {
				ErrorFeedback.show("Cannot delete node " + obj, e);
			}
		}
		return null;
	}

	protected void remove(Node node) throws Exception {
		if (node.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORY))
			ErrorFeedback
					.show("Local repository deletion is not yet implemented");
		else if (node.isNodeType(ConnectTypes.CONNECT_FILE_TO_IMPORT)
				&& node.hasProperty(ConnectNames.CONNECT_ALREADY_PROCESSED)
				&& node.getProperty(ConnectNames.CONNECT_ALREADY_PROCESSED)
						.getBoolean())
			ErrorFeedback.show("This file has already been "
					+ "processed. It cannot be removed.");
		else {
			// close editor before removing the node.
			if (node.isNodeType(ConnectTypes.CONNECT_CLEAN_TRACK_SESSION)) {
				IWorkbenchWindow iww = ConnectGpsUiPlugin.getDefault()
						.getWorkbench().getActiveWorkbenchWindow();

				IEditorPart fe = (IEditorPart) iww.getActivePage().findEditor(
						new CleanDataEditorInput(node.getIdentifier()));
				if (fe != null)
					iww.getActivePage().closeEditor(fe, false);
			}
			node.remove();
		}
	}
}