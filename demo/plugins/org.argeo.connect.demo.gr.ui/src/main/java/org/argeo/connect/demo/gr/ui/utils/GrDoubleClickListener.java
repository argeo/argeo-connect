package org.argeo.connect.demo.gr.ui.utils;

import java.io.File;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.service.GrBackend;
import org.argeo.connect.demo.gr.ui.commands.OpenNetworkEditor;
import org.argeo.connect.demo.gr.ui.commands.OpenSiteEditor;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;


public class GrDoubleClickListener implements IDoubleClickListener {
	private final static Log log = LogFactory
			.getLog(GrDoubleClickListener.class);
	private GrBackend grBackend;
	private FileHandler fileHandler;

	public GrDoubleClickListener(GrBackend grBackend) {
		this.grBackend = grBackend;
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
				String id = node.getPath();
				// fileHandler.openFile(name, id);
				File tmpFile = grBackend.getFileFromNode(node);
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
