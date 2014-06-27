package org.argeo.connect.people.ui.commands;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.SearchByTagEditor;
import org.argeo.connect.people.ui.editors.utils.SearchNodeEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Open an editor that display a table filtered by tag
 */
public class OpenSearchByTagEditor extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".openSearchByTagEditor";

	public final static String PARAM_TAG_VALUE = "param.tagValue";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String tagValue = event.getParameter(PARAM_TAG_VALUE);
		try {
			SearchNodeEditorInput eei = new SearchNodeEditorInput(
					PeopleTypes.PEOPLE_ENTITY);

			IEditorPart part = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().openEditor(eei, getEditorId());

			SearchByTagEditor editor = (SearchByTagEditor) part;
			if (editor != null)
				editor.setTagValue(tagValue == null ? "" : tagValue);

		} catch (PartInitException pie) {
			throw new PeopleException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}
		return null;
	}

	protected String getEditorId() {
		return SearchByTagEditor.ID;
	}
}