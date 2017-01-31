package org.argeo.connect.ui.workbench;

import javax.jcr.Node;

import org.argeo.eclipse.ui.specific.OpenFile;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/** Provide interface to manage a connect apps in a RCP/RAP Workbench */
public interface AppWorkbenchService {
	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	@Deprecated
	default public String getOpenEntityEditorCmdId() {
		return null;
	}

	@Deprecated
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

	/** Centralize icon management for a given app */
	default public Image getIconForType(Node entity) {
		return null;
	}

	/** Creates the correct wizard depending on the type of the given node. */
	default public Wizard getCreationWizard(Node node) {
		return null;
	}
}
