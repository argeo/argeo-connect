package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;

import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.jcr.ui.explorer.wizards.ImportFileSystemWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportDirectoryContent extends AbstractHandler {
	// private static Log log = LogFactory.getLog(ImportDirectoryContent.class);

	public final static String ID = "org.argeo.connect.ui.gps.importDirectoryContent";
	public final static String DEFAULT_ICON_REL_PATH = "icons/import_fs.png";
	public final static String DEFAULT_LABEL = "Upload GPS data to repository";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		GpsBrowserView view = (GpsBrowserView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(HandlerUtil.getActivePartId(event));

		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			try {
				if (obj instanceof Node) {
					Node folder = (Node) obj;
					// if (!folder.getPrimaryNodeType().getName()
					// .equals(NodeType.NT_FOLDER)) {
					// Error.show("Can only import to a folder node");
					// return null;
					// }
					ImportFileSystemWizard wizard = new ImportFileSystemWizard(
							folder);
					WizardDialog dialog = new WizardDialog(
							HandlerUtil.getActiveShell(event), wizard);
					dialog.open();
					view.refresh(folder);
				} else {
					ErrorFeedback.show("Can only import to a node");
				}
			} catch (Exception e) {
				ErrorFeedback.show("Cannot import files to " + obj, e);
			}
		}
		return null;
	}
}
