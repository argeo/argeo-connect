package org.argeo.connect.people.rap.commands;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.editors.DefaultSearchEntityEditor;
import org.argeo.connect.people.rap.editors.SearchPersonEditor;
import org.argeo.connect.people.rap.editors.utils.SearchNodeEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Open an editor to display a filtered table for a given JCR Node type */
public class OpenSearchEntityEditor extends AbstractHandler {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".openSearchEntityEditor";

	public final static String PARAM_NODE_TYPE = "param.nodeType";
	public final static String PARAM_EDITOR_NAME = "param.editorName";
	public final static String PARAM_BASE_PATH = "param.basePath";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String entityType = event.getParameter(PARAM_NODE_TYPE);
		String basePath = event.getParameter(PARAM_BASE_PATH);
		String name = event.getParameter(PARAM_EDITOR_NAME);

		try {
			SearchNodeEditorInput eei = new SearchNodeEditorInput(entityType,
					basePath, name);

			if (entityType.equals(PeopleTypes.PEOPLE_PERSON)) {
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.openEditor(eei, SearchPersonEditor.ID);
			} else
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
						.openEditor(eei, DefaultSearchEntityEditor.ID);
		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}
		return null;
	}
}