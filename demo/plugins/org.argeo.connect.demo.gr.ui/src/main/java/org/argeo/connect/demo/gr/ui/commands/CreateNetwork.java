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
package org.argeo.connect.demo.gr.ui.commands;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.views.NetworkBrowserView;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.handlers.HandlerUtil;

/** Creates a new network */
public class CreateNetwork extends AbstractHandler implements GrNames {
	public final static String ID = GrUiPlugin.PLUGIN_ID + ".createNetwork";
	public final static String DEFAULT_ICON_REL_PATH = "icons/newNetwork.gif";
	public final static String DEFAULT_LABEL = GrMessages.get().createNetwork_lbl;

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			NetworkBrowserView nbv = (NetworkBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.showView(NetworkBrowserView.ID);

			TreeViewer tv = nbv.getTreeViewer();
			IStructuredSelection selection = (IStructuredSelection) tv
					.getSelection();

			Node parent = (Node) selection.getFirstElement();
			InputDialog idiag = new InputDialog(
					HandlerUtil.getActiveShell(event),
					GrMessages.get().dialog_createNetwork_title,
					GrMessages.get().dialog_createNetwork_msg, "", null);

			if (idiag.open() == org.eclipse.jface.window.Window.OK) {
				String networkName = idiag.getValue();

				if (networkName != null && !"".equals(networkName.trim())) {
					Node network = parent.addNode(networkName,
							GrTypes.GR_NETWORK);
					JcrUtils.updateLastModified(network);
					parent.getSession().save();
					nbv.refresh(parent);
				}
			}
			idiag.close();
		} catch (Exception e) {
			throw new ArgeoException("Cannot create network node", e);
		}
		return null;
	}
}