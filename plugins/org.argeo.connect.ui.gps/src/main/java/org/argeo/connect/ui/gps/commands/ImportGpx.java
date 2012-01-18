package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;

import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.ui.gps.wizards.ImportGpxWizard;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Open a JCR query editor. */
public class ImportGpx extends AbstractHandler {
	// private final static Log log = LogFactory.getLog(ImportGpx.class);
	private TrackDao trackDao;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		AbstractJcrBrowser view = (AbstractJcrBrowser) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.findView(HandlerUtil.getActivePartId(event));
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			try {
				if (obj instanceof Node) {
					Node baseNode = (Node) obj;
					ImportGpxWizard wizard = new ImportGpxWizard(trackDao,
							baseNode);
					WizardDialog dialog = new WizardDialog(
							HandlerUtil.getActiveShell(event), wizard);
					dialog.open();
					view.refresh(baseNode);
				} else {
					ErrorFeedback.show("Can only import to a node");
				}
			} catch (Exception e) {
				ErrorFeedback.show("Cannot import files to " + obj, e);
			}
		}
		return null;
	}

	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

}
