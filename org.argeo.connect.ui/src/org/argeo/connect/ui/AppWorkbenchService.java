package org.argeo.connect.ui;

import javax.jcr.Node;

import org.argeo.eclipse.ui.specific.OpenFile;

/** Provide interface to manage a Connect App in a RCP/RAP Workbench */
public interface AppWorkbenchService extends AppUiService {

	/**
	 * @return the relevant OpenEntityEditor command ID in the current context
	 */
	default public String getOpenEntityEditorCmdId() {
		// return OpenEntityEditor.ID;
		return "org.argeo.connect.ui.workbench.openEntityEditor";
	}

	/**
	 * @return the relevant OpenSearchEntityEditor command ID in the current context
	 */
	default public String getOpenSearchEntityEditorCmdId() {
		// return OpenSearchEntityEditor.ID;
		return "org.argeo.connect.ui.workbench.openSearchEntityEditor";
	}

	public void openEntityEditor(Node entity);

//	public void openEntityEditor(String entityId);

	public void openSearchEntityView(String nodeType, String label);

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

	/**
	 * @return the ID of the relevant OpenFile command ID for current context
	 */
	default public String getOpenFileCmdId() {
		return OpenFile.ID;
	}

	/**
	 * @return the ID of the default Editor for current context
	 */
	default public String getDefaultEditorId() {
		return null;
	}
}
