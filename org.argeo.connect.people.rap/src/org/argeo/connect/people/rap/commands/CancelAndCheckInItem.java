package org.argeo.connect.people.rap.commands;

import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.editors.utils.IVersionedItemEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CancelAndCheckInItem {}

///**
// * Discard all pending changes of the current item and check it in.
// */
//public class CancelAndCheckInItem extends AbstractHandler {
//	public final static String ID = PeopleRapPlugin.PLUGIN_ID
//			+ ".cancelAndCheckInItem";
//
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
//				.getActivePage().getActivePart();
//		if (iwp instanceof IVersionedItemEditor) {
//			IVersionedItemEditor editor = (IVersionedItemEditor) iwp;
//			editor.cancelAndCheckInItem();
//		}
//		
//		if (iwp instanceof IVersionedItemEditor) {
//			IVersionedItemEditor editor = (IVersionedItemEditor) iwp;
//			editor.cancelAndCheckInItem();
//		}
//		return null;
//	}
//}
