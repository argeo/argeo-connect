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

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.commands.OpenCleanDataEditor;
import org.argeo.connect.ui.gps.commands.OpenLocalRepoEditor;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class GpsDoubleClickListener implements IDoubleClickListener {
	private FileHandler fileHandler;

	public GpsDoubleClickListener(FileHandler fileHandler) {
		this.fileHandler = fileHandler;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;

		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (!(obj instanceof Node))
			return;
		Node node = (Node) obj;

		try {

			if (node.isNodeType(NodeType.NT_FILE)) {
				// open the file
				String name = node.getName();
				String id = node.getPath();
				// TODO fix file download on RAP
				// fileHandler.openFile(name, id);
			} else if (node
					.isNodeType(ConnectTypes.CONNECT_CLEAN_TRACK_SESSION)) {
				// Call parameterized command "open Editor"
				IWorkbench iw = ConnectGpsUiPlugin.getDefault().getWorkbench();
				IHandlerService handlerService = (IHandlerService) iw
						.getService(IHandlerService.class);

				// get the command from plugin.xml
				IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
				ICommandService cmdService = (ICommandService) window
						.getService(ICommandService.class);
				Command cmd = cmdService.getCommand(OpenCleanDataEditor.ID);

				ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();

				// get the parameter
				IParameter iparam = cmd
						.getParameter(OpenCleanDataEditor.PARAM_UUID);

				Parameterization params = new Parameterization(iparam,
						node.getIdentifier());
				parameters.add(params);

				// build the parameterized command
				ParameterizedCommand pc = new ParameterizedCommand(cmd,
						parameters.toArray(new Parameterization[parameters
								.size()]));

				// execute the command
				handlerService = (IHandlerService) window
						.getService(IHandlerService.class);
				handlerService.executeCommand(pc, null);
			} else if (node.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORY)) {
				// Call parameterized command "open Editor"
				IWorkbench iw = ConnectGpsUiPlugin.getDefault().getWorkbench();
				IHandlerService handlerService = (IHandlerService) iw
						.getService(IHandlerService.class);

				// get the command from plugin.xml
				IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
				ICommandService cmdService = (ICommandService) window
						.getService(ICommandService.class);
				Command cmd = cmdService.getCommand(OpenLocalRepoEditor.ID);

				ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();

				// get the parameter
				IParameter iparam = cmd
						.getParameter(OpenLocalRepoEditor.PARAM_NAME);

				Parameterization params = new Parameterization(iparam,
						node.getName());
				parameters.add(params);

				// build the parameterized command
				ParameterizedCommand pc = new ParameterizedCommand(cmd,
						parameters.toArray(new Parameterization[parameters
								.size()]));

				// execute the command
				handlerService = (IHandlerService) window
						.getService(IHandlerService.class);
				handlerService.executeCommand(pc, null);
			}

		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Repository error while getting Node file info", re);
		} catch (Exception e) {
			throw new ArgeoException(
					"Error while handling the double click in the GPS browser view.",
					e);
		}
	}
}