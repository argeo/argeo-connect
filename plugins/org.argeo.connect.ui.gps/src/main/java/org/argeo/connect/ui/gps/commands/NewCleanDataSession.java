package org.argeo.connect.ui.gps.commands;

import java.util.UUID;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open a CleanDataEditor.
 * 
 * Data set to clean and default parameters set to use might be added later as
 * parameters prior to open the editor.
 */

public class NewCleanDataSession extends AbstractHandler {
	public final static String ID = "org.argeo.connect.ui.gps.newCleanDataSession";
	public final static String DEFAULT_ICON_REL_PATH = "icons/newSession.gif";
	public final static String DEFAULT_LABEL = "Create a new clean data session";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			String uuid = UUID.randomUUID().toString();
			HandlerUtil
					.getActiveWorkbenchWindow(event)
					.getActivePage()
					.openEditor(new CleanDataEditorInput(uuid),
							CleanDataEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}
}
