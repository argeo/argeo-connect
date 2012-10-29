package org.argeo.connect.demo.gr.ui.commands;

import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.views.NetworkBrowserView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Refresh the main EBI list. */
public class RefreshNetworkBrowserView extends AbstractHandler {
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".refreshNetworkBrowserView";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		NetworkBrowserView view = (NetworkBrowserView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(NetworkBrowserView.ID);
		view.refresh(null);
		return null;
	}

}