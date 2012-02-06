package org.argeo.connect.ui.gps.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;
import javax.jcr.Workspace;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.GpsUiGisServices;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.editors.LocalRepoEditor;
import org.argeo.connect.ui.gps.editors.LocalRepoEditorInput;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that enable deletion of the data pushed in a local repository
 * for a given "clean track session"
 * 
 * Corresponding session is then moved back to the root of the
 * "clean track session" repository and the read-only flag is removed.
 * 
 * Both browser view and local repo editor (if open) are refreshed.
 * 
 */

public class RemoveImportedData extends AbstractHandler {
	// private final static Log log =
	// LogFactory.getLog(OpenCleanDataEditor.class);

	public final static String ID = "org.argeo.connect.ui.gps.removeImportedData";
	public final static String DEFAULT_ICON_REL_PATH = "icons/sessionRemove.gif";
	public final static String DEFAULT_LABEL = "Remove corresponding data from the local repository";
	public final static String PARAM_SESSION_ID = "org.argeo.connect.ui.gps.sessionId";

	/* DEPENDENCY INJECTION */
	private GpsUiGisServices uiGisServices;
	private GpsUiJcrServices uiJcrServices;

	// Define here the default node name

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Retrieve the session node
			String modelId = event.getParameter(PARAM_SESSION_ID);
			// model Id is compulsory
			if (modelId == null)
				return null;
			Node currSess = uiJcrServices.getJcrSession().getNodeByIdentifier(
					modelId);

			// get linked local repository
			Node currRepo = currSess.getParent();

			List<String> segmentIds = new ArrayList<String>();
			NodeIterator ni = currSess.getNodes();
			while (ni.hasNext()) {
				Node tmpNode = ni.nextNode();
				if (tmpNode.isNodeType(ConnectTypes.CONNECT_FILE_TO_IMPORT)) {
					if (tmpNode.hasProperty(ConnectNames.CONNECT_SEGMENT_UUID)) {
						// sometimes no segment are created from a single gpx
						// file: all points have been cleaned out
						Value[] values = tmpNode.getProperty(
								ConnectNames.CONNECT_SEGMENT_UUID).getValues();
						for (int i = 0; i < values.length; i++) {
							segmentIds.add(values[i].getString());
						}
					}
				}
			}
			// Effective removal of the data
			uiGisServices.getTrackDao().deleteCleanPositions(
					currRepo.getName(), segmentIds);

			// Update JCR info
			currSess.setProperty(ConnectNames.CONNECT_IS_SESSION_COMPLETE,
					false);
			JcrUtils.updateLastModified(currSess);
			currSess.getSession().save();

			Node sessionRepo = uiJcrServices.getTrackSessionsParentNode();
			Workspace ws = currSess.getSession().getWorkspace();
			ws.move(currSess.getPath(),
					sessionRepo.getPath() + "/" + currSess.getName());

			// Get GpsBrowserView
			GpsBrowserView gbView = (GpsBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.findView(HandlerUtil.getActivePartId(event));
			// GpsBrowserView gbView = (GpsBrowserView) ConnectUiGpsPlugin
			// .getDefault().getWorkbench().getActiveWorkbenchWindow()
			// .getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(sessionRepo);
			gbView.refresh(currRepo);

			// Refresh local repo editor if open
			LocalRepoEditor fe = (LocalRepoEditor) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.findEditor(new LocalRepoEditorInput(currRepo.getName()));
			if (fe != null)
				fe.refresh();

		} catch (Exception e) {
			throw new ArgeoException("Cannot open editor", e);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setUiGisServices(GpsUiGisServices uiGisServices) {
		this.uiGisServices = uiGisServices;
		this.uiJcrServices = uiGisServices.getUiJcrServices();
	}
}