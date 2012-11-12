package org.argeo.connect.demo.gr.ui.views;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.commands.CreateNetwork;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.argeo.connect.demo.gr.ui.utils.GrNodeLabelProvider;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;
import org.argeo.eclipse.ui.jcr.utils.NodeViewerComparer;
import org.argeo.eclipse.ui.jcr.utils.SingleSessionFileProvider;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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

	private final static Log log = LogFactory.getLog(NetworkBrowserView.class);
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".networkBrowserView";

	/* DEPENDENCY INJECTION */
	private Session jcrSession;
	private GrBackend grBackend;

	// UI management
	private TreeViewer nodesViewer;
	private SimpleNodeContentProvider nodeContentProvider;
	private FileHandler fileHandler;

	private Node networksRootNode;

	@Override
	public void createPartControl(Composite parent) {

		final SingleSessionFileProvider ssfp = new SingleSessionFileProvider(
				jcrSession);
		fileHandler = new FileHandler(ssfp);

		parent.setLayout(new FillLayout());

		// Configure here useful view root nodes
		nodeContentProvider = new ViewContentProvider(jcrSession,
				new String[] { GR_NETWORKS_BASE_PATH });
		try {
			networksRootNode = jcrSession.getNode(GR_NETWORKS_BASE_PATH);
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
		tmpNodeViewer.addDoubleClickListener(new GrDoubleClickListener(
				grBackend));
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

	public void refresh(Object obj) {
		if (obj == null) {
			nodesViewer.refresh(networksRootNode);
			log.debug("refreshing the whole tree");
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

		// Please note that commands that are not subject to programmatic
		// conditions are directly defined in the corresponding
		// menuContribution of the plugin.xml.

		// Building conditions

		// Some commands are meaningless for multiple selection
		IStructuredSelection selection = (IStructuredSelection) nodesViewer
				.getSelection();
		boolean multipleSel = selection.size() > 1;
		boolean isFolder = true;

		// //boolean isFolder = false;
		//
		// // We add networks only under NT_FOLDER nodes
		// try {
		// if (!multipleSel && selection.getFirstElement() instanceof Node) {
		// Node node = (Node) selection.getFirstElement();
		// if (node.getPrimaryNodeType().getName()
		// .equals(NodeType.NT_FOLDER))
		// isFolder = true;
		// }
		// } catch (RepositoryException re) {
		// throw new ArgeoException(
		// "RepositoryException while refreshing context menu", re);
		// }

		// Effective Refresh
		refreshCommand(menuManager, window, CreateNetwork.ID,
				CreateNetwork.DEFAULT_LABEL,
				CreateNetwork.DEFAULT_ICON_REL_PATH, isFolder);

		// refreshCommand(menuManager, window, ImportDirectoryContent.ID,
		// ImportDirectoryContent.DEFAULT_LABEL,
		// ImportDirectoryContent.DEFAULT_ICON_REL_PATH, isFileRepo);
		//
		// refreshCommand(menuManager, window, NewCleanDataSession.ID,
		// NewCleanDataSession.DEFAULT_LABEL,
		// NewCleanDataSession.DEFAULT_ICON_REL_PATH, isSessionRepo);
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

	// Add specific behaviours to the node provider
	class ViewContentProvider extends SimpleNodeContentProvider {

		public ViewContentProvider(Session session, String[] basePaths) {
			super(session, basePaths);
		}

		@Override
		protected List<Node> filterChildren(List<Node> children)
				throws RepositoryException {
			for (Iterator<Node> it = children.iterator(); it.hasNext();) {
				Node node = it.next();

				if (node.getPrimaryNodeType().isNodeType(NodeType.NT_RESOURCE)) {
					it.remove();
				}
				if (node.getPrimaryNodeType().isNodeType(GrTypes.GR_SITE)) {
					it.remove();
				}

			}
			return super.filterChildren(children);
		}

		@Override
		protected Object[] sort(Object parent, Object[] children) {
			Arrays.sort(children, new Comparator<Object>() {

				public int compare(Object o1, Object o2) {
					Node node1 = (Node) o1;
					Node node2 = (Node) o2;
					try {
						return node1.getPath().compareTo(node2.getPath());
					} catch (RepositoryException e) {
						throw new ArgeoException("Cannot compare " + node1
								+ " and " + node2, e);
					}
				}

			});
			return children;
		}

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

	/** DEPENDENCY INJECTION */
	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}

	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}

}
