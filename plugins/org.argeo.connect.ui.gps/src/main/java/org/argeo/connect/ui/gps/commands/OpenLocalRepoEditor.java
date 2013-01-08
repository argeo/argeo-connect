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

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.editors.LocalRepoEditor;
import org.argeo.connect.ui.gps.editors.LocalRepoEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open an editor for the chosen local
 * repository.
 * 
 */

public class OpenLocalRepoEditor extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(OpenCleanDataEditor.class);

	public final static String ID = ConnectGpsUiPlugin.ID
			+ ".openLocalRepoEditor";
	public final static String PARAM_NAME = ConnectGpsUiPlugin.ID
			+ ".localRepoName";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String name = event.getParameter(PARAM_NAME);
		try {
			// Initializes the editor input.
			LocalRepoEditorInput lrei = new LocalRepoEditorInput(name);
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(lrei, LocalRepoEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}
}
