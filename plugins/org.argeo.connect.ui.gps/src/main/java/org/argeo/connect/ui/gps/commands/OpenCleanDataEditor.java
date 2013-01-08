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
import javax.jcr.Property;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open a CleanDataEditor.
 * 
 * Data set to clean and default parameters set to use might be added later as
 * parameters prior to open the editor.
 */

public class OpenCleanDataEditor extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(OpenCleanDataEditor.class);

	/* DEPENDENCY INJECTION */
	private GpsUiJcrServices uiJcrServices;

	public final static String ID = ConnectGpsUiPlugin.ID
			+ ".openCleanDataEditor";
	public final static String PARAM_UUID = ConnectGpsUiPlugin.ID
			+ ".connectSessionUuid";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String uuid = event.getParameter(PARAM_UUID);
		try {
			// Initializes the editor input.
			CleanDataEditorInput cdei = new CleanDataEditorInput(uuid);
			String nodeName;
			Node node = uiJcrServices.getJcrSession().getNodeByIdentifier(uuid);
			if (node.hasProperty(Property.JCR_NAME))
				nodeName = node.getProperty(Property.JCR_NAME).getString();
			else
				nodeName = node.getName();
			cdei.setName(nodeName);

			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(cdei, CleanDataEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setUiJcrServices(GpsUiJcrServices uiJcrServices) {
		this.uiJcrServices = uiJcrServices;
	}
}
