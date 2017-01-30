package org.argeo.connect.people.workbench.rap;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.ui.ConnectWorkbenchService;
import org.eclipse.jface.wizard.Wizard;

/**
 * Centralize the definition of context specific parameters (Among other, the
 * name of the command to open editors or default editor
 */

public interface PeopleWorkbenchService extends ConnectWorkbenchService {
//	/**
//	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
//	 * and thus enable the opening plugin specific editors
//	 */
//	public String getOpenEntityEditorCmdId();
//
//	public String getOpenSearchEntityEditorCmdId();
//
//	public String getOpenFileCmdId();
//
//	public String getDefaultEditorId();
//
	/**
	 * Creates the correct wizard depending on the type of the given node.
	 * Overwrite to provide application specific wizard depending on the context
	 */
	public Wizard getCreationWizard(PeopleService peopleService, Node node);

	String getDefaultEditorId();

	// /** Centralize icon management for a given app */
	// public Image getIconForType(Node entity);
}
