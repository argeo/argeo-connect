package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddFileFolder extends AbstractHandler implements ConnectTypes {
	public final static String ID = "org.argeo.connect.ui.gps.addFileFolder";
	public final static String DEFAULT_ICON_REL_PATH = "icons/addFolder.gif";
	public final static String DEFAULT_LABEL = "Add a sub folder";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		GpsBrowserView view = (GpsBrowserView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(HandlerUtil.getActivePartId(event));
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof Node) {
				String folderName = SingleValue.ask("Folder name",
						"Enter folder name");
				if (folderName != null) {
					Node parentNode = (Node) obj;
					try {
						Node newNode = parentNode.addNode(folderName,
								ConnectTypes.CONNECT_FILE_REPOSITORY);
						view.nodeAdded(parentNode, newNode);
						parentNode.getSession().save();
					} catch (RepositoryException e) {
						ErrorFeedback.show("Cannot create folder " + folderName
								+ " under " + parentNode, e);
					}
				}
			} else {
				ErrorFeedback.show("Can only add file folder to a node");
			}
		}
		return null;
	}

}
