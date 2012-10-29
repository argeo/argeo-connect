package org.argeo.connect.demo.gr.ui.commands;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.service.GrBackend;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.views.NetworkBrowserView;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateNetwork extends AbstractHandler implements GrNames {
	public final static String ID = GrUiPlugin.PLUGIN_ID + ".createNetwork";
	public final static String DEFAULT_ICON_REL_PATH = "icons/newNetwork.gif";
	public final static String DEFAULT_LABEL = GrUiPlugin
			.getMessage("cmdLblCreateNetwork");

	/** DEPENDENCY INJECTION **/
	private GrBackend grBackend;

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {

			NetworkBrowserView nbv = (NetworkBrowserView) HandlerUtil
					.getActiveWorkbenchWindow(event).getActivePage()
					.showView(NetworkBrowserView.ID);

			TreeViewer tv = nbv.getTreeViewer();
			IStructuredSelection selection = (IStructuredSelection) tv
					.getSelection();

			Node parent = (Node) selection.getFirstElement();

			InputDialog idiag = new InputDialog(
					HandlerUtil.getActiveShell(event),
					GrUiPlugin.getMessage("enterNetworkNameDialogTitle"),
					GrUiPlugin.getMessage("enterNetworkNameDialogLbl"), "",
					null);

			idiag.open();
			String networkName = idiag.getValue();
			idiag.close();

			Node network = parent.addNode(networkName, GrTypes.GR_NETWORK);
			// network.addMixin(GrTypes.GR_NETWORK);

			JcrUtils.updateLastModified(network);
			// network.setProperty(GR_NETWORK_LAST_UPDATE, new
			// GregorianCalendar());
			// network.setProperty(GR_NETWORK_LAST_USER,
			// grBackend.getCurrentUserId());
			parent.getSession().save();

			nbv.refresh(parent);
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
