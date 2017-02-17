package org.argeo.connect.tracker.ui.commands;

import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.connect.tracker.internal.ui.parts.AllProjectsEditor;
import org.argeo.connect.tracker.ui.TrackerUiPlugin;
import org.argeo.connect.ui.workbench.commands.OpenSearchEntityEditor;
import org.argeo.connect.ui.workbench.parts.SearchNodeEditorInput;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Extends People command to add tracker specific search editors*/
public class OpenTrackerSearchEntityEditor extends OpenSearchEntityEditor {
	public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".openTrackerSearchEntityEditor";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String nodeType = event.getParameter(PARAM_NODE_TYPE);
		String name = event.getParameter(PARAM_EDITOR_NAME);
		String basePath = event.getParameter(PARAM_BASE_PATH);

		IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		try {
			SearchNodeEditorInput eei = new SearchNodeEditorInput(nodeType, basePath, name);
			if (nodeType.equals(TrackerTypes.TRACKER_PROJECT))
				activePage.openEditor(eei, AllProjectsEditor.ID);
			else
				return super.execute(event);
		} catch (PartInitException pie) {
			throw new TrackerException("Unexpected PartInitException while opening entity editor for type " + nodeType
					+ " at path " + basePath, pie);
		}
		return null;
	}
}
