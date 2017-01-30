package org.argeo.connect.ui;

import javax.jcr.Node;

import org.eclipse.swt.graphics.Image;

/** Provide interface to manage a connect apps in a RCP/RAP Workbench */
public interface ConnectWorkbenchService {
	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId();

	public String getOpenSearchEntityEditorCmdId();

	public String getOpenFileCmdId();

	public String getDefaultEditorId();

	/** Centralize icon management for a given app */
	public Image getIconForType(Node entity);
}
