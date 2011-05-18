package org.argeo.connect.ui.gps;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.ConnectUiPlugin;
import org.argeo.eclipse.ui.jcr.DefaultNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

public class GpsNodeLabelProvider extends DefaultNodeLabelProvider implements
		ConnectTypes, ConnectNames, ConnectGpsLabels {
	private final static Log log = LogFactory
			.getLog(GpsNodeLabelProvider.class);

	// Images
	public final static Image sessionFolder = ConnectUiPlugin
			.getImageDescriptor("icons/home.gif").createImage();
	public final static Image session = ConnectUiPlugin.getImageDescriptor(
			"icons/repository.gif").createImage();

	public final static Image fileNewImg = ConnectUiPlugin.getImageDescriptor(
			"icons/file_new.gif").createImage();

	public final static Image fileProcessedImg = ConnectUiPlugin
			.getImageDescriptor("icons/file_processed.gif").createImage();

	public String getText(Object element) {
		// GPS connect Specific labels.
		String curText = super.getText(element);

		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				if (node.getPrimaryNodeType().isNodeType(
						CONNECT_CLEAN_TRACK_SESSION)) {
					// Session
					if (!"".equals(node.getProperty(CONNECT_NAME).getString()))
						curText = node.getProperty(CONNECT_NAME).getString();
				} else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_FILE_TO_IMPORT)) {
					// File
					if (!"".equals(node.getProperty(CONNECT_LINKED_FILE_NAME)
							.getString()))
						curText = node.getProperty(CONNECT_LINKED_FILE_NAME)
								.getString();
				} else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_SESSION_REPOSITORY)) {
					log.debug("Here & label = "
							+ ConnectUiPlugin
									.getGPSMessage(SESSION_REPOSITORY_LBL));
					// Parent Session Node
					curText = ConnectUiPlugin
							.getGPSMessage(SESSION_REPOSITORY_LBL);
				}
			} catch (Exception e) {
				// silent
			}
		}

		return curText;
	}

	/** Specific label handling for connect GPS */
	protected String getText(Node node) throws RepositoryException {
		String label = node.getName();
		return label;
	}

	@Override
	public Image getImage(Object element) {
		Image curImg = super.getImage(element);

		if (element instanceof Node) {
			Node node = (Node) element;
			try {
				// ref to files.
				if (node.getPrimaryNodeType()
						.isNodeType(CONNECT_FILE_TO_IMPORT))
					if (node.getProperty(CONNECT_ALREADY_PROCESSED)
							.getBoolean())
						curImg = fileProcessedImg;
					else
						curImg = fileNewImg;
				else if (node.getPrimaryNodeType().isNodeType(
						CONNECT_SESSION_REPOSITORY))
					curImg = sessionFolder;
			} catch (Exception e) {
				// silent
			}
		}
		return curImg;
	}
}
