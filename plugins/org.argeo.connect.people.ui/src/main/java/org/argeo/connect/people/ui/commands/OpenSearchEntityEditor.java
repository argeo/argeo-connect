package org.argeo.connect.people.ui.commands;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.SearchEntityEditorInput;
import org.argeo.connect.people.ui.editors.StaticSearchEntityEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

/**
 * Discard all pending changes of the current item and check it in.
 */
public class OpenSearchEntityEditor extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".openSearchEntityEditor";

	public final static String PARAM_ENTITY_TYPE = "param.entityType";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String entityType = event.getParameter(PARAM_ENTITY_TYPE);
		try {
			SearchEntityEditorInput eei = new SearchEntityEditorInput(
					entityType);
			PeopleUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.openEditor(eei, StaticSearchEntityEditor.ID);
		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}
		return null;
	}
}