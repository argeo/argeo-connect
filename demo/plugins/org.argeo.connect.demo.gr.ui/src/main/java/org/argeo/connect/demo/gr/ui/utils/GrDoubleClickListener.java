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

import java.io.File;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackendImpl;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.commands.OpenNetworkEditor;
import org.argeo.connect.demo.gr.ui.commands.OpenSiteEditor;
import org.argeo.connect.demo.gr.ui.providers.TmpFileProvider;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/** Double-click management */
public class GrDoubleClickListener implements IDoubleClickListener {
	// private final static Log log = LogFactory
	// .getLog(GrDoubleClickListener.class);
	//private GrBackend grBackend;
	private FileHandler fileHandler;

	public GrDoubleClickListener() {
		//this.grBackend = grBackend;
		TmpFileProvider tfp = new TmpFileProvider();
		fileHandler = new FileHandler(tfp);
	}

	/**
	 * Call a specific command depending on the NodeType on which the
	 * doubleclick has been done.
	 * 
	 * */
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
				// fileHandler.openFile(name, id);
				File tmpFile = GrBackendImpl.getFileFromNode(node);
				tmpFile.deleteOnExit();
				fileHandler.openFile(name, tmpFile.getPath());
			} else if (node.isNodeType(GrTypes.GR_NETWORK)) {
				CommandUtils.CallCommandWithOneParameter(OpenNetworkEditor.ID,
						OpenNetworkEditor.PARAM_UID, node.getIdentifier());
			} else if (node.isNodeType(GrTypes.GR_SITE)) {
				CommandUtils.CallCommandWithOneParameter(OpenSiteEditor.ID,
						OpenSiteEditor.PARAM_UID, node.getIdentifier());
			}
		} catch (Exception e) {
			throw new ArgeoException(
					"Error while handling the double click in the network browser view.",
					e);
		}
	}

}
