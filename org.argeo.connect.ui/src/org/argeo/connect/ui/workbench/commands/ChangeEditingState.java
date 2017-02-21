package org.argeo.connect.ui.workbench.commands;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ui.workbench.ConnectUiPlugin;
import org.argeo.connect.ui.workbench.parts.AbstractConnectEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/** Manage a form editor and the underlying node */
public class ChangeEditingState extends AbstractHandler {
	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".changeEditingState";

	private final static Log log = LogFactory.getLog(ChangeEditingState.class);

	// Parameters and corresponding possible values
	public final static String PARAM_NEW_STATE = "param.newEditingState";
	public final static String EDITING = "editing";
	public final static String NOT_EDITING = "notEditing";

	public final static String PARAM_PRIOR_ACTION = "param.priorAction";
	public final static String PRIOR_ACTION_SAVE = "save";
	public final static String PRIOR_ACTION_CHECKOUT = "checkout";
	public final static String PRIOR_ACTION_CANCEL = "cancel";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String priorAction = event.getParameter(PARAM_PRIOR_ACTION);
		String newState = event.getParameter(PARAM_NEW_STATE);
		IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();
		if (iwp instanceof AbstractConnectEditor) {
			AbstractConnectEditor editor = (AbstractConnectEditor) iwp;
			// prior action
			Node node = editor.getNode();
			if (PRIOR_ACTION_SAVE.equals(priorAction))
				ConnectJcrUtils.saveAndPublish(node, true);
			else if (PRIOR_ACTION_CANCEL.equals(priorAction))
				JcrUtils.discardUnderlyingSessionQuietly(node);
			else if (PRIOR_ACTION_CHECKOUT.equals(priorAction)) {
				if (!ConnectJcrUtils.checkCOStatusBeforeUpdate(node))
					log.warn("Referencing node " + node + " was checked in when we wanted to update");
			}
			// new State
			if (EDITING.equals(newState))
				editor.startEditing();
			else if (NOT_EDITING.equals(newState))
				editor.stopEditing();
		}
		return null;
	}
}
