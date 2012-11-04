package org.argeo.connect.demo.gr.ui.commands;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.editors.SiteEditor;
import org.argeo.connect.demo.gr.ui.editors.SiteEditorInput;
import org.argeo.gis.GisTypes;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateSite extends AbstractHandler implements GrNames {

	private final static Log log = LogFactory.getLog(CreateSite.class);

	public final static String ID = GrUiPlugin.PLUGIN_ID + ".createSite";
	public final static String DEFAULT_ICON_REL_PATH = "icons/newSite.gif";
	public final static String DEFAULT_LABEL = GrUiPlugin
			.getMessage("cmdLblCreateSite");
	public final static String PARAM_UID = "com.ignfi.gr.client.ui.networkUid";

	/** DEPENDENCY INJECTION **/
	private GrBackend grBackend;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String networkUid = event.getParameter(PARAM_UID);
			Node network = grBackend.getCurrentSession().getNodeByIdentifier(
					networkUid);

			InputDialog idiag = new InputDialog(
					HandlerUtil.getActiveShell(event),
					GrUiPlugin.getMessage("enterSiteNameDialogTitle"),
					GrUiPlugin.getMessage("enterSiteNameDialogLbl"), "",
					null);

			idiag.open();
			String siteName = idiag.getValue();
			idiag.close();

			Node site = network.addNode(siteName, GrTypes.GR_SITE);

			// Add compulory nodes :
			site.addNode(GR_SITE_MAIN_POINT, GrTypes.GR_POINT);
			site.addNode(GR_SITE_COMMENTS, NodeType.NT_UNSTRUCTURED);

			// We don't save at creation time.
			// parent.getSession().save();

			// Open the corresponding editor
			HandlerUtil
					.getActiveWorkbenchWindow(event)
					.getActivePage()
					.openEditor(new SiteEditorInput(site.getIdentifier()),
							SiteEditor.ID);

		} catch (Exception e) {
			throw new ArgeoException("Cannot create site node", e);
		}
		return null;
	}

	/** DEPENDENCY INJECTION */
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}
}
