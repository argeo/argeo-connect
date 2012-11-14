package org.argeo.connect.demo.gr.ui.views;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.commands.CreateNetwork;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.connect.demo.gr.ui.providers.GrTreeContentProvider;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;
import org.argeo.eclipse.ui.jcr.utils.NodeViewerComparer;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/** Specific network explorer View */
public class NetworkBrowserView extends AbstractJcrBrowser implements GrNames,
		GrConstants {

	// private final static Log log =
	// LogFactory.getLog(NetworkBrowserView.class);
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".networkBrowserView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	// private GrBackend grBackend;

	// UI management
	private TreeViewer nodesViewer;
	private SimpleNodeContentProvider nodeContentProvider;

	private Node networksRootNode;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		// Configure here useful view root nodes
		try {
			session = repository.login();
			nodeContentProvider = new GrTreeContentProvider(session,
					new String[] { GR_NETWORKS_BASE_PATH });
			networksRootNode = session.getNode(GR_NETWORKS_BASE_PATH);
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot get root networks node", re);
		}
		// nodes viewer
		nodesViewer = createNodeViewer(parent, nodeContentProvider);
		nodesViewer.setComparer(new NodeViewerComparer());
		nodesViewer.setInput(getViewSite());

		// context menu
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(nodesViewer.getTree());
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		nodesViewer.getTree().setMenu(menu);

	}

	protected TreeViewer createNodeViewer(Composite parent,
			ITreeContentProvider nodeContentProvider) {
		final TreeViewer tmpNodeViewer = new TreeViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		// tmpNodeViewer.getTree().setLayoutData(
		// new FillData(SWT.FILL, SWT.FILL, true, true));

		tmpNodeViewer.setContentProvider(nodeContentProvider);
		tmpNodeViewer.setLabelProvider(new GrNodeLabelProvider());

		// Add Listeners
		// tmpNodeViewer.addDoubleClickListener(new GrDoubleClickListener(
		// fileHandler));
		tmpNodeViewer.addDoubleClickListener(new GrDoubleClickListener());
		tmpNodeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						// Does nothing for the time being
					}
				});

		return tmpNodeViewer;
	}

	@Override
	public void setFocus() {
		nodesViewer.getTree().setFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	public void refresh(Object obj) {
		if (obj == null) {
			nodesViewer.refresh(networksRootNode);
		} else
			nodesViewer.refresh(obj);
	}

	public void nodeAdded(Node parentNode, Node newNode) {
		nodesViewer.refresh(parentNode);
		nodesViewer.expandToLevel(newNode, 0);
	}

	// Manage context menu
	/**
	 * Defines the commands that will pop up in the context menu.
	 **/
	protected void contextMenuAboutToShow(IMenuManager menuManager) {

		IWorkbenchWindow window = GrUiPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();

		// Please note that commands that are not subject to programatic
		// conditions are directly defined in the corresponding
		// menuContribution of the plugin.xml.

		// Building conditions

		// Some commands are meaningless for multiple selection
		boolean isFolder = true;

		// Effective Refresh
		refreshCommand(menuManager, window, CreateNetwork.ID,
				CreateNetwork.DEFAULT_LABEL,
				CreateNetwork.DEFAULT_ICON_REL_PATH, isFolder);

	}

	protected void refreshCommand(IMenuManager menuManager,
			IServiceLocator locator, String cmdId, String label,
			String iconPath, boolean showCommand) {
		IContributionItem ici = menuManager.find(cmdId);
		if (ici != null)
			menuManager.remove(ici);
		CommandContributionItemParameter contributionItemParameter = new CommandContributionItemParameter(
				locator, null, cmdId, SWT.PUSH);

		if (showCommand) {
			// Set Params
			contributionItemParameter.label = label;
			contributionItemParameter.icon = GrUiPlugin
					.getImageDescriptor(iconPath);

			CommandContributionItem cci = new CommandContributionItem(
					contributionItemParameter);
			cci.setId(cmdId);
			menuManager.add(cci);
		}
	}

	public TreeViewer getTreeViewer() {
		return nodesViewer;
	}

	// abstract methods that must be overwritten
	@Override
	protected int[] getWeights() {
		// Useles : current view is not a sash form
		return null;
	}

	@Override
	protected TreeViewer getNodeViewer() {
		return nodesViewer;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/** DEPENDENCY INJECTION */
	// public void setJcrSession(Session jcrSession) {
	// this.jcrSession = jcrSession;
	// }

	// public void setGrBackend(GrBackend grBackend) {
	// this.grBackend = grBackend;
	// }
}