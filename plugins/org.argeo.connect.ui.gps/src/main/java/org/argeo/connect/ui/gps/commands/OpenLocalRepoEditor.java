package org.argeo.connect.ui.gps.commands;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.editors.LocalRepoEditor;
import org.argeo.connect.ui.gps.editors.LocalRepoEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open an editor for the chosen local
 * repository.
 * 
 */

public class OpenLocalRepoEditor extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(OpenCleanDataEditor.class);

	public final static String ID = "org.argeo.connect.ui.gps.openLocalRepoEditor";
	public final static String PARAM_NAME = "org.argeo.connect.ui.gps.localRepoName";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String name = event.getParameter(PARAM_NAME);
		try {
			// Initializes the editor input.
			LocalRepoEditorInput lrei = new LocalRepoEditorInput(name);
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(lrei, LocalRepoEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}
}
