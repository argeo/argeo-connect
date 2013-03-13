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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.gps.CleaningSessionUtils;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsImages;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open a CleanDataEditor.
 * 
 * Data set to clean and default parameters set to use might be added later as
 * parameters prior to open the editor.
 */

public class NewCleanDataSession extends AbstractHandler {
	public final static String ID = ConnectGpsUiPlugin.ID
			+ ".newCleanDataSession";
	public final static String PARAM_MODEL_ID = ConnectGpsUiPlugin.ID
			+ ".modelSessionId";
	public final static String PARAM_PARENT_ID = ConnectGpsUiPlugin.ID
			+ ".parentNodeId";

	public final static ImageDescriptor DEFAULT_ICON = GpsImages.ICON_ADD_CLEANING_SESSION;
	public final static String DEFAULT_LABEL = "Create session...";
	public final static String COPY_SESSION_LABEL = "Create session with this params...";

	/* DEPENDENCY INJECTION */
	private GpsUiJcrServices uiJcrServices;

	// Define here the default node name
	private DateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String modelId = event.getParameter(PARAM_MODEL_ID);
		String parNodeId = event.getParameter(PARAM_PARENT_ID);
		try {
			// Get GpsBrowserView
			GpsBrowserView view = (GpsBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.findView(GpsBrowserView.ID);

			// Define parent Node
			Node parentNode = null;
			if (parNodeId != null)
				parentNode = uiJcrServices.getJcrSession().getNodeByIdentifier(
						parNodeId);

			if (parentNode == null || // get the params from a read-only session
					parentNode
							.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORY))
				// use default parent
				parentNode = uiJcrServices.getTrackSessionsParentNode();

			String nodeName = timeFormatter.format(new GregorianCalendar()
					.getTime());
			Node newNode = CleaningSessionUtils.createNewSession(parentNode,
					nodeName);

			if (modelId != null) {
				Node modelNode = uiJcrServices.getJcrSession()
						.getNodeByIdentifier(modelId);
				CleaningSessionUtils.copyDataFromModel(modelNode, newNode);
			}

			view.nodeAdded(parentNode, newNode);
			CleanDataEditorInput cdei = new CleanDataEditorInput(
					newNode.getIdentifier());
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
