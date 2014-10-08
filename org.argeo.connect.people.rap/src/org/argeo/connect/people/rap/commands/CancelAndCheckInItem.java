package org.argeo.connect.people.ui.commands;

import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.IVersionedItemEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Discard all pending changes of the current item and check it in.
 */
public class CancelAndCheckInItem extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".cancelAndCheckInItem";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getActivePart();
		if (iwp instanceof IVersionedItemEditor) {
			IVersionedItemEditor editor = (IVersionedItemEditor) iwp;
			editor.cancelAndCheckInItem();
		}
		return null;
	}
}
