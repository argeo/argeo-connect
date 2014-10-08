package org.argeo.connect.people.rap.commands;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleUiPlugin;
import org.argeo.connect.people.rap.editors.utils.IVersionedItemEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to check out a JCR item to edit it. A check to see if the
 * current user as the corresponding role to do so is done when called.
 */
public class CheckOutItem extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".checkOutItem";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getActivePart();

		try {
			if (iwp instanceof IVersionedItemEditor) {
				IVersionedItemEditor editor = (IVersionedItemEditor) iwp;
				if (editor.canBeCheckedOutByMe())
					editor.checkoutItem();
				else {
					MessageDialog dialog = new MessageDialog(
							HandlerUtil.getActiveShell(event), "Error", null,
							"Cannot check out current item",
							MessageDialog.INFORMATION, new String[] { "OK" }, 0);
					dialog.open();
				}

			}
		} catch (Exception e) {
			throw new PeopleException(
					"Unexpected error while checking out current entity", e);
		}
		return null;
	}
}
