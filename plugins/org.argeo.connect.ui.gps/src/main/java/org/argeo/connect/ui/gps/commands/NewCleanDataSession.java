package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.commons.JcrSessionUtils;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
			// String uuid = UUID.randomUUID().toString();
			Node parentNode;

			// Retrieve parent node.
			ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getSelection();
			GpsBrowserView view = (GpsBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.findView(HandlerUtil.getActivePartId(event));
			if (selection != null && !selection.isEmpty()
					&& selection instanceof IStructuredSelection) {

				// Command is visible only when a single item is selected in the
				// tree node.
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof Node) {

					parentNode = (Node) obj;

					Node newNode = JcrSessionUtils.createNewSession(parentNode,
							"new Session");

					view.nodeAdded(parentNode, newNode);
					parentNode.getSession().save();

					HandlerUtil
							.getActiveWorkbenchWindow(event)
							.getActivePage()
							.openEditor(
									new CleanDataEditorInput(
											newNode.getIdentifier()),
									CleanDataEditor.ID);
				}
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}
}
