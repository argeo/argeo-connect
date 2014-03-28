package org.argeo.connect.people.ui.commands;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.DefaultSearchEntityEditor;
import org.argeo.connect.people.ui.editors.SearchPersonEditor;
import org.argeo.connect.people.ui.editors.utils.SearchEntityEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;

/**
 * Open an editor that display a filtered table for a given JCR Node type
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

			if (entityType.equals(PeopleTypes.PEOPLE_PERSON)) {
				PeopleUiPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.openEditor(eei, SearchPersonEditor.ID);
			} else
				PeopleUiPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.openEditor(eei, DefaultSearchEntityEditor.ID);
		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}
		return null;
	}
}