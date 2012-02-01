package org.argeo.connect.ui.gps.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.commands.AddFileFolder;
import org.argeo.connect.ui.gps.commands.ImportDirectoryContent;
import org.argeo.connect.ui.gps.commands.NewCleanDataSession;
import org.argeo.connect.ui.gps.commands.OpenNewRepoWizard;
import org.argeo.connect.ui.gps.commands.RemoveImportedData;
import org.argeo.connect.ui.gps.providers.GpsDoubleClickListener;
import org.argeo.connect.ui.gps.providers.GpsNodeLabelProvider;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/** Specific Gps explorer View */
public class GpsBrowserView extends AbstractJcrBrowser implements ConnectNames,
		ConnectTypes { // extends ViewPart {

	private final static Log log = LogFactory.getLog(GpsBrowserView.class);
	public final static String ID = "org.argeo.connect.ui.gps.gpsBrowserView";

	/* DEPENDENCY INJECTION */
	// private Session jcrSession;
	// private TrackDao trackDao;
	private GpsUiJcrServices uiJcrServices;

	private TreeViewer nodesViewer;
	private SimpleNodeContentProvider nodeContentProvider;

	private FileHandler fileHandler;

	@Override
	public void createPartControl(Composite parent) {

		// Instantiate the generic object that fits for
		// both RCP & RAP, must be final to be accessed in the double click
		// listener.
		// Note that in RAP, it registers a service handler that provide the
		// access to the files.
		final SingleSessionFileProvider ssfp = new SingleSessionFileProvider(
				uiJcrServices.getJcrSession());
		fileHandler = new FileHandler(ssfp);

		parent.setLayout(new FillLayout());

		// String userHomePath = JcrUtils.getUserHomePath(username);

		// Creating base directories if they don't exists
		if (uiJcrServices.getGpxFilesDirectory() == null
				|| uiJcrServices.getLocalRepositoriesParentNode() == null
				|| uiJcrServices.getTrackSessionsParentNode() == null)
			uiJcrServices.initializeLocalRepository();

		String[] rootNodes = new String[3];
		try {
			rootNodes[0] = uiJcrServices.getLocalRepositoriesParentNode()
					.getPath();
			rootNodes[1] = uiJcrServices.getTrackSessionsParentNode().getPath();
			rootNodes[2] = uiJcrServices.getGpxFilesDirectory().getPath();
		} catch (RepositoryException re) {
			throw new ArgeoException("unexpected error while initializing"
					+ " roots of the view browser", re);
		}

		// Configure here useful view root nodes
		nodeContentProvider = new ViewContentProvider(
				uiJcrServices.getJcrSession(), rootNodes);
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
		getSite().registerContextMenu(menuManager, nodesViewer);

		// Selection Provider
		getSite().setSelectionProvider(nodesViewer);

		// add drag & drop support
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] tt = new Transfer[] { TextTransfer.getInstance() };
		nodesViewer.addDragSupport(operations, tt, new ViewDragListener());
	}

	protected TreeViewer createNodeViewer(Composite parent,
			ITreeContentProvider nodeContentProvider) {
		final TreeViewer tmpNodeViewer = new TreeViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		tmpNodeViewer.getTree().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		tmpNodeViewer.setContentProvider(nodeContentProvider);
		tmpNodeViewer.setLabelProvider(new GpsNodeLabelProvider());

		// Add Listeners
		tmpNodeViewer.addDoubleClickListener(new GpsDoubleClickListener(
				fileHandler));
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

		try {
			IWorkbenchWindow window = ConnectGpsUiPlugin.getDefault()
					.getWorkbench().getActiveWorkbenchWindow();

			// Please note that commands that are not subject to programmatic
			// conditions are directly define in the corresponding
			// menuContribution
			// of the plugin.xml.

			// Building conditions

			// Some commands are meaningless for multiple selection
			IStructuredSelection selection = (IStructuredSelection) nodesViewer
					.getSelection();
			boolean multipleSel = selection.size() > 1;

			boolean isFileRepo = false;
			boolean isSessionRepo = false;
			boolean isSession = false;
			boolean isLocalRepoParent = false;
			boolean isCompleted = false;
			Node curNode = null;

			if (!multipleSel && selection.getFirstElement() instanceof Node) {
				curNode = (Node) selection.getFirstElement();
				if (curNode.isNodeType(ConnectTypes.CONNECT_FILE_REPOSITORY))
					isFileRepo = true;
				if (curNode.isNodeType(ConnectTypes.CONNECT_SESSION_REPOSITORY))
					isSessionRepo = true;
				if (curNode.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORIES))
					isLocalRepoParent = true;
				if (curNode
						.isNodeType(ConnectTypes.CONNECT_CLEAN_TRACK_SESSION))
					isSession = true;

				if (curNode
						.hasProperty(ConnectNames.CONNECT_IS_SESSION_COMPLETE))
					isCompleted = curNode.getProperty(
							ConnectNames.CONNECT_IS_SESSION_COMPLETE)
							.getBoolean();
			}

			// Effective Refresh
			refreshCommand(menuManager, window, AddFileFolder.ID,
					AddFileFolder.DEFAULT_LABEL,
					AddFileFolder.DEFAULT_ICON_REL_PATH, isFileRepo, null);

			refreshCommand(menuManager, window, ImportDirectoryContent.ID,
					ImportDirectoryContent.DEFAULT_LABEL,
					ImportDirectoryContent.DEFAULT_ICON_REL_PATH, isFileRepo,
					null);

			Map<String, String> params = null;
			if (curNode != null) {
				params = new HashMap<String, String>();
				if (isSession) {
					// create session from current one
					params.put(NewCleanDataSession.PARAM_PARENT_ID, curNode
							.getParent().getIdentifier());
					params.put(NewCleanDataSession.PARAM_MODEL_ID,
							curNode.getIdentifier());
					refreshCommand(menuManager, window, NewCleanDataSession.ID,
							NewCleanDataSession.COPY_SESSION_LABEL,
							NewCleanDataSession.DEFAULT_ICON_REL_PATH,
							isSession, params);
				} else {
					params.put(NewCleanDataSession.PARAM_PARENT_ID,
							curNode.getIdentifier());
					// create session from default
					refreshCommand(menuManager, window, NewCleanDataSession.ID,
							NewCleanDataSession.DEFAULT_LABEL,
							NewCleanDataSession.DEFAULT_ICON_REL_PATH,
							isSessionRepo, null);
				}
			}

			params = new HashMap<String, String>();
			params.put(RemoveImportedData.PARAM_SESSION_ID,
					curNode.getIdentifier());
			refreshCommand(menuManager, window, RemoveImportedData.ID,
					RemoveImportedData.DEFAULT_LABEL,
					RemoveImportedData.DEFAULT_ICON_REL_PATH, isCompleted,
					params);

			refreshCommand(menuManager, window, OpenNewRepoWizard.ID,
					OpenNewRepoWizard.DEFAULT_LABEL,
					OpenNewRepoWizard.DEFAULT_ICON_REL_PATH, isLocalRepoParent,
					null);

		} catch (RepositoryException re) {
			throw new ArgeoException(
					"RepositoryException while refreshing context menu", re);
		}

	}

	protected void refreshParametrizedCommand(IMenuManager menuManager,
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
			contributionItemParameter.icon = ConnectGpsUiPlugin
					.getImageDescriptor(iconPath);

			CommandContributionItem cci = new CommandContributionItem(
					contributionItemParameter);
			cci.setId(cmdId);
			menuManager.add(cci);
		}
	}

	protected void refreshCommand(IMenuManager menuManager,
			IServiceLocator locator, String cmdId, String label,
			String iconPath, boolean showCommand, Map<String, String> params) {
		IContributionItem ici = menuManager.find(cmdId);
		if (ici != null)
			menuManager.remove(ici);
		CommandContributionItemParameter contributionItemParameter = new CommandContributionItemParameter(
				locator, null, cmdId, SWT.PUSH);

		if (showCommand) {
			// Set Params
			contributionItemParameter.label = label;
			contributionItemParameter.icon = ConnectGpsUiPlugin
					.getImageDescriptor(iconPath);

			if (params != null)
				contributionItemParameter.parameters = params;

			CommandContributionItem cci = new CommandContributionItem(
					contributionItemParameter);
			cci.setId(cmdId);
			menuManager.add(cci);
		}
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

				if (node.getPrimaryNodeType().isNodeType(NodeType.NT_RESOURCE)
						|| node.getPrimaryNodeType().isNodeType(
								CONNECT_CLEAN_PARAMETER)) {
					it.remove();
				}

			}
			return super.filterChildren(children);
		}

		@Override
		protected Object[] sort(Object parent, Object[] children) {
			Arrays.sort(children, new Comparator<Object>() {

				@Override
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

	class ViewDragListener implements DragSourceListener {

		public void dragStart(DragSourceEvent event) {
			if (log.isTraceEnabled())
				log.trace("Drag Start");
		}

		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) nodesViewer
					.getSelection();

			Object obj = selection.getFirstElement();
			// We ensure that current selection is valid.
			if (obj instanceof Node) {
				Node first = (Node) obj;

				try {
					// support multiple selection, but we insure that at least
					// first node is of the correct type
					if (first.getPrimaryNodeType().isNodeType(
							CONNECT_FILE_REPOSITORY)
							|| first.getPrimaryNodeType().isNodeType(
									NodeType.NT_FILE)) {

						Iterator<String> ids = getNodesIds(selection)
								.iterator();

						// We concatenate Ids since List is not supported by
						// drag & drop
						StringBuffer dataSB = new StringBuffer();
						while (ids.hasNext()) {
							dataSB.append(ids.next() + ";");
						}
						String dataS = dataSB.toString();
						if (dataS.lastIndexOf(";") == dataS.length() - 1)
							dataS = dataS.substring(0, dataS.length() - 1);

						event.data = dataS;

						// IHandlerService handlerService = (IHandlerService)
						// getSite()
						// .getService(IHandlerService.class);
						// handlerService.executeCommand(commandId, null);
					}
				} catch (RepositoryException e) {
					throw new ArgeoException(
							"Error while drag & dropping some files.", e);
				}
			}

		}

		private List<String> getNodesIds(IStructuredSelection selection) {
			Map<String, Node> nodeMap = new HashMap<String, Node>();
			Iterator<?> it = selection.iterator();
			while (it.hasNext()) {
				Node node = (Node) it.next();
				nodeToMap(nodeMap, node);
			}

			List<String> ids = new ArrayList<String>();
			for (String key : nodeMap.keySet()) {
				ids.add(key);
			}
			return ids;
		}

		/** recursively constructs a node map from a parent node */
		private void nodeToMap(Map<String, Node> map, Node curNode) {
			try {
				if (curNode.getPrimaryNodeType().isNodeType(NodeType.NT_FILE)) {
					// check if it already contains the current node
					if (!map.containsKey(curNode.getIdentifier()))
						map.put(curNode.getIdentifier(), curNode);
				}

				else if (curNode.getPrimaryNodeType().isNodeType(
						CONNECT_FILE_REPOSITORY)) {
					NodeIterator ni = curNode.getNodes();
					while (ni.hasNext()) {
						nodeToMap(map, ni.nextNode());
					}
				} else {
					if (log.isWarnEnabled())
						log.warn("Invalid node type ("
								+ curNode.getPrimaryNodeType().getName()
								+ ") encountered while building the nodeList.");
				}

			} catch (RepositoryException re) {
				throw new ArgeoException("Error while constructing NodeMap", re);

			}
		}

		public void dragFinished(DragSourceEvent event) {
			if (log.isTraceEnabled())
				log.trace("Finished Drag");
		}
	}

	/* DEPENDENCY INJECTION */
	public void setUiJcrServices(GpsUiJcrServices uiJcrServices) {
		this.uiJcrServices = uiJcrServices;
	}
}
