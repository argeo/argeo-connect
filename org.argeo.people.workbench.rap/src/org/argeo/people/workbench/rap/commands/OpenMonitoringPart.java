package org.argeo.people.workbench.rap.commands;

import org.argeo.people.PeopleException;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.argeo.people.workbench.rap.editors.util.SingletonEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/** Workaround to enable opening of a default editor */
public class OpenMonitoringPart extends AbstractHandler {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".openMonitoringView";

	public final static String PARAM_PART_ID = "param.partId";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String partId = event.getParameter(PARAM_PART_ID);
		try {
			IWorkbenchPage iwPage = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage();

			SingletonEditorInput eei = new SingletonEditorInput(partId);
			IEditorPart iep = iwPage.findEditor(eei);
			if (iep == null) {
				iwPage.openEditor(eei, partId);
			} else
				iwPage.activate(iep);

		} catch (PartInitException e) {
			throw new PeopleException("Unable to part with id " + partId, e);
		}
		return null;
	}
}
