package org.argeo.connect.demo.gr.ui.commands;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.editors.NetworkEditor;
import org.argeo.connect.demo.gr.ui.editors.NetworkEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenNetworkEditor extends AbstractHandler {
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".openNetworkEditor";
	public final static String PARAM_UID = GrUiPlugin.PLUGIN_ID
			+ ".networkUid";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String uid = event.getParameter(PARAM_UID);

		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(new NetworkEditorInput(uid), NetworkEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}

}
