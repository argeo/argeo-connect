package org.argeo.connect.ui.workbench;

import javax.jcr.Node;

import org.argeo.connect.ui.AppUiService;
import org.argeo.eclipse.ui.specific.OpenFile;

/** Provide interface to manage a connect apps in a RCP/RAP Workbench */
public interface AppWorkbenchService extends AppUiService {
	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	default public String getOpenEntityEditorCmdId() {
		return null;
	}

	default public String getOpenSearchEntityEditorCmdId() {
		return null;
	}

	/**
	 * @return the ID of the relevant editor for this node within the current
	 *         context
	 */
	public String getEntityEditorId(Node entity);

	/**
	 * @return the ID of the relevant editor to search among nodes with this
	 *         NodeType
	 */
	public String getSearchEntityEditorId(String nodeType);

	default public String getOpenFileCmdId() {
		return OpenFile.ID;
	}

	default public String getDefaultEditorId() {
		return null;
	}
}
