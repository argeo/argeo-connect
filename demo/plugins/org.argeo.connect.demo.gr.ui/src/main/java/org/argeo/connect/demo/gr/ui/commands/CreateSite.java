package org.argeo.connect.demo.gr.ui.commands;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.editors.SiteEditor;
import org.argeo.connect.demo.gr.ui.editors.SiteEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Creates a new site */
public class CreateSite extends AbstractHandler implements GrNames {

	// private final static Log log = LogFactory.getLog(CreateSite.class);

	public final static String ID = GrUiPlugin.PLUGIN_ID + ".createSite";
	public final static String DEFAULT_ICON_REL_PATH = "icons/newSite.gif";
	public final static String DEFAULT_LABEL = GrMessages.get().createSite_lbl;
	public final static String PARAM_UID = GrUiPlugin.PLUGIN_ID + ".networkUid";

	/** DEPENDENCY INJECTION **/
	private GrBackend grBackend;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String networkUid = event.getParameter(PARAM_UID);
			Node network = grBackend.getCurrentSession().getNodeByIdentifier(
					networkUid);

			InputDialog idiag = new InputDialog(
					HandlerUtil.getActiveShell(event),
					GrMessages.get().dialog_createSite_title,
					GrMessages.get().dialog_createSite_msg, "", null);

			if (idiag.open() == org.eclipse.jface.window.Window.OK) {
				String siteName = idiag.getValue();

				if (siteName != null && !"".equals(siteName.trim())) {
					Node site = network.addNode(siteName, GrTypes.GR_SITE);
					site.addNode(GR_SITE_MAIN_POINT, GrTypes.GR_POINT);
					site.addNode(GR_SITE_COMMENTS, NodeType.NT_UNSTRUCTURED);

					JcrUtils.updateLastModified(network);
					site.getSession().save();

					// Open the corresponding editor
					HandlerUtil
							.getActiveWorkbenchWindow(event)
							.getActivePage()
							.openEditor(
									new SiteEditorInput(site.getIdentifier()),
									SiteEditor.ID);

				}
			}
			idiag.close();
		} catch (Exception e) {
			throw new ArgeoException("Cannot create network node", e);
		}
		return null;
	}

	/** DEPENDENCY INJECTION */
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}
}
