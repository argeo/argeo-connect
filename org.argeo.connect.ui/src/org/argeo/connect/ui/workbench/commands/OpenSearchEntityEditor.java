package org.argeo.connect.ui.workbench.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectUiPlugin;
import org.argeo.connect.ui.workbench.util.SearchNodeEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Open an editor to display a filtered table for a given JCR Node type */
public class OpenSearchEntityEditor extends AbstractHandler {
	private final static Log log = LogFactory.getLog(OpenSearchEntityEditor.class);

	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".openSearchEntityEditor";

	public final static String PARAM_NODE_TYPE = "param.nodeType";
	public final static String PARAM_EDITOR_NAME = "param.editorName";
	public final static String PARAM_BASE_PATH = "param.basePath";

	private AppWorkbenchService appWorkbenchService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String entityType = event.getParameter(PARAM_NODE_TYPE);
		String basePath = event.getParameter(PARAM_BASE_PATH);
		String name = event.getParameter(PARAM_EDITOR_NAME);

		String editorId = appWorkbenchService.getSearchEntityEditorId(entityType);
		if (editorId == null) {
			log.warn("No editor ID found for " + entityType);
			return null;
		}
		try {
			SearchNodeEditorInput eei = new SearchNodeEditorInput(entityType, basePath, name);
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().openEditor(eei, editorId);
		} catch (PartInitException pie) {
			throw new ConnectException("Cannot open search editor for " + entityType, pie);
		}
		return null;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
