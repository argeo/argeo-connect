package org.argeo.connect.e4.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.eclipse.e4.core.di.annotations.Execute;

/** Open an editor to display a filtered table for a given JCR Node type */
public class OpenSearchEntityEditor {
	private final static Log log = LogFactory.getLog(OpenSearchEntityEditor.class);

	// public final static String ID = ConnectUiPlugin.PLUGIN_ID +
	// ".openSearchEntityEditor";

	public final static String PARAM_NODE_TYPE = "nodeType";
	public final static String PARAM_LABEL = "label";
	// public final static String PARAM_BASE_PATH = "basePath";

	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	@Execute
	public void execute(@Named(PARAM_NODE_TYPE) String entityType, @Named(PARAM_LABEL) String label) {
		// String entityType = event.getParameter(PARAM_NODE_TYPE);
		// String basePath = event.getParameter(PARAM_BASE_PATH);
		// String name = event.getParameter(PARAM_EDITOR_NAME);

		String editorId = systemWorkbenchService.getSearchEntityEditorId(entityType);
		if (editorId == null) {
			log.warn("No editor ID found for " + entityType);
			return;
		}
		systemWorkbenchService.openSearchEntityView(entityType, label);
		// try {
		// SearchNodeEditorInput eei = new SearchNodeEditorInput(entityType, basePath,
		// name);
		// HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().openEditor(eei,
		// editorId);
		// } catch (PartInitException pie) {
		// throw new ConnectException("Cannot open search editor for " + entityType,
		// pie);
		// }
		// return null;
	}

	// public void setSystemWorkbenchService(SystemWorkbenchService
	// systemWorkbenchService) {
	// this.systemWorkbenchService = systemWorkbenchService;
	// }
}
