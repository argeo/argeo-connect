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
package org.argeo.connect.demo.gr.ui.utils;

import java.util.ArrayList;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;


public class CommandUtils {

	/**
	 * Factorizes command call that is quite verbose and always the same
	 * 
	 * NOTE that none of the parameter can be null
	 */
	public static void CallCommandWithOneParameter(String commandId,
			String paramId, String paramValue) {
		try {
			IWorkbench iw = GrUiPlugin.getDefault().getWorkbench();

			IHandlerService handlerService = (IHandlerService) iw
					.getService(IHandlerService.class);

			// get the command from plugin.xml
			IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
			ICommandService cmdService = (ICommandService) window
					.getService(ICommandService.class);

			Command cmd = cmdService.getCommand(commandId);

			ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();

			// get the parameter
			IParameter iparam = cmd.getParameter(paramId);

			Parameterization params = new Parameterization(iparam, paramValue);
			parameters.add(params);

			// build the parameterized command
			ParameterizedCommand pc = new ParameterizedCommand(cmd,
					parameters.toArray(new Parameterization[parameters.size()]));

			// execute the command
			handlerService = (IHandlerService) window
					.getService(IHandlerService.class);
			handlerService.executeCommand(pc, null);
		} catch (Exception e) {
			throw new ArgeoException("Error while calling command of id :"
					+ commandId, e);
		}
	}

}
