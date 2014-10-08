package org.argeo.connect.people.rap;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleService;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralizes the definition of context specific parameters (Among other, the
 * name of the command to open editors or default editor
 */

public interface PeopleWorkbenchService {
	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId();

	public String getOpenSearchEntityEditorCmdId();

	// public String getOpenSearchByTagEditorCmdId();

	public String getOpenFileCmdId();

	public String getDefaultEditorId();

	/**
	 * Creates the correct wizard depending on the type of the given node.
	 * Overwrite to provide application specific wizard depending on the context
	 */
	public Wizard getCreationWizard(PeopleService peopleService, Node node);

	/** Centralize icon management for a given app */
	public Image getIconForType(Node entity);
}
