package org.argeo.connect.ui.gps.commands;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to set visible or open a CleanDataEditor.
 * 
 * Data set to clean and default parameters set to use might be added later as
 * parameters prior to open the editor.
 */

public class OpenCleanDataEditor extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(OpenCleanDataEditor.class);

	/* DEPENDENCY INJECTION */
	private GpsUiJcrServices uiJcrServices;

	public final static String ID = "org.argeo.connect.ui.gps.openCleanDataEditor";
	public final static String PARAM_UUID = "org.argeo.connect.ui.gps.connectSessionUuid";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String uuid = event.getParameter(PARAM_UUID);
		try {
			// Initializes the editor input.
			CleanDataEditorInput cdei = new CleanDataEditorInput(uuid);
			String nodeName;
			Node node = uiJcrServices.getJcrSession().getNodeByIdentifier(uuid);
			if (node.hasProperty(Property.JCR_NAME))
				nodeName = node.getProperty(Property.JCR_NAME).getString();
			else
				nodeName = node.getName();
			cdei.setName(nodeName);

			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.openEditor(cdei, CleanDataEditor.ID);
		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setUiJcrServices(GpsUiJcrServices uiJcrServices) {
		this.uiJcrServices = uiJcrServices;
	}
}
