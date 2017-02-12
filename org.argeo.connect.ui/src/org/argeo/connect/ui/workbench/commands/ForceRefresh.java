package org.argeo.connect.ui.workbench.commands;

import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.Refreshable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Force refreshment of the active part if it implements
 * <Code>Refreshable</code> interface.
 */
public class ForceRefresh extends AbstractHandler {

	public final static String ID = AppWorkbenchService.CONNECT_WORKBENCH_ID_PREFIX + ".forceRefresh";
	public final static String PARAM_VIEW_ID = "param.viewId";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String partId = event.getParameter(PARAM_VIEW_ID);

		IWorkbenchWindow iww = HandlerUtil.getActiveWorkbenchWindow(event);
		if (iww == null)
			return null;

		IWorkbenchPage activePage = iww.getActivePage();

		if (partId != null) {
			IViewPart viewPart = activePage.findView(partId);
			if (viewPart != null && viewPart instanceof Refreshable)
				((Refreshable) viewPart).forceRefresh(null);
		} else {
			IWorkbenchPart part = activePage.getActivePart();
			if (part instanceof Refreshable)
				((Refreshable) part).forceRefresh(null);
		}

		return null;
	}
}