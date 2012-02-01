package org.argeo.connect.ui.gps.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.gpx.utils.JcrSessionUtils;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.editors.CleanDataEditor;
import org.argeo.connect.ui.gps.editors.CleanDataEditorInput;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
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

public class NewCleanDataSession extends AbstractHandler {
	public final static String ID = "org.argeo.connect.ui.gps.newCleanDataSession";
	public final static String DEFAULT_ICON_REL_PATH = "icons/sessionAdd.gif";
	public final static String DEFAULT_LABEL = "Create a new clean data session";
	public final static String COPY_SESSION_LABEL = "Create a new session using this session params";
	public final static String PARAM_MODEL_ID = "org.argeo.connect.ui.gps.modelSessionId";
	public final static String PARAM_PARENT_ID = "org.argeo.connect.ui.gps.parentNodeId";

	/* DEPENDENCY INJECTION */
	private GpsUiJcrServices uiJcrServices;
	
	// Define here the default node name
	private DateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String modelId = event.getParameter(PARAM_MODEL_ID);
		String parNodeId = event.getParameter(PARAM_PARENT_ID);
		try {
			// Get GpsBrowserView
			GpsBrowserView view = (GpsBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.findView(HandlerUtil.getActivePartId(event));

			// Define parent Node
			Node parentNode;
			if (parNodeId == null) {
				// get the default parent
				parentNode = uiJcrServices.getTrackSessionsParentNode();
			} else
				parentNode = uiJcrServices.getJcrSession().getNodeByIdentifier(parNodeId);
			String nodeName = timeFormatter.format(new GregorianCalendar()
					.getTime());
			Node newNode = JcrSessionUtils.createNewSession(parentNode,
					nodeName);

			if (modelId != null) {
				Node modelNode = uiJcrServices.getJcrSession().getNodeByIdentifier(modelId);
				JcrSessionUtils.copyDataFromModel(modelNode, newNode);
			}

			view.nodeAdded(parentNode, newNode);
			CleanDataEditorInput cdei = new CleanDataEditorInput(
					newNode.getIdentifier());
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
