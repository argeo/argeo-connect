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
import org.argeo.connect.ui.ConnectUiPlugin;
import org.argeo.connect.ui.gps.GpsNodeLabelProvider;
import org.argeo.connect.ui.gps.commands.AddFileFolder;
import org.argeo.connect.ui.gps.commands.ImportDirectoryContent;
import org.argeo.connect.ui.gps.commands.NewCleanDataSession;
import org.argeo.connect.ui.gps.commands.OpenCleanDataEditor;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;
import org.argeo.eclipse.ui.jcr.utils.NodeViewerComparer;
import org.argeo.eclipse.ui.jcr.utils.SingleSessionFileProvider;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.argeo.eclipse.ui.jcr.views.GenericJcrBrowser;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/** Specific Gps explorer View */
public class GpsBrowserView extends AbstractJcrBrowser implements ConnectNames,
		ConnectTypes { // extends ViewPart {

	private final static Log log = LogFactory.getLog(GenericJcrBrowser.class);
	public final static String ID = "org.argeo.connect.ui.gps.gpsBrowserView";

	// TODO : HARD CODED VARIABLES, MUST BE CLEANLY IMPLEMENTED LATER
	private final static String trackSessionRelPath = "/.connect/importTrackSessions";
	private final static String gpxFileDirectoryPath = "/connect/gpx";

	private Session jcrSession;

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
				jcrSession);
		fileHandler = new FileHandler(ssfp);

		parent.setLayout(new FillLayout());

		String userHomePath = JcrUtils.getUserHomePath(jcrSession);

		try {
			// Creating base directories if they don't exists
			if (!jcrSession.nodeExists(gpxFileDirectoryPath)) {
				int lastIndex = gpxFileDirectoryPath.lastIndexOf("/");
				Node parFolder = JcrUtils.mkdirs(jcrSession,
						gpxFileDirectoryPath.substring(0, lastIndex));
				parFolder.addNode(
						gpxFileDirectoryPath.substring(lastIndex + 1),
						CONNECT_FILE_REPOSITORY);
			}

			String sessionbasePath = userHomePath + trackSessionRelPath;
			if (!jcrSession.nodeExists(sessionbasePath)) {
				int lastIndex = sessionbasePath.lastIndexOf("/");
				Node parFolder = JcrUtils.mkdirs(jcrSession,
						sessionbasePath.substring(0, lastIndex));
				if (log.isTraceEnabled())
					log.debug("par folder node type"
							+ parFolder.getPrimaryNodeType().getName());
				parFolder.addNode(sessionbasePath.substring(lastIndex + 1),
						CONNECT_SESSION_REPOSITORY);
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Error while initializing jcr repository",
					re);
		}

		// Configure here useful view root nodes
		nodeContentProvider = new ViewContentProvider(jcrSession, new String[] {
				userHomePath + trackSessionRelPath, gpxFileDirectoryPath });
		// home also
		// nodeContentProvider = new ViewContentProvider(jcrSession, new
		// String[] {
		// userHomePath, userHomePath + trackSessionRelPath,
		// gpxFileDirectoryPath });

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

	private class GpsDoubleClickListener implements IDoubleClickListener {
		private FileHandler fileHandler;

		public GpsDoubleClickListener(FileHandler fileHandler) {
			this.fileHandler = fileHandler;
		}

		public void doubleClick(DoubleClickEvent event) {
			if (event.getSelection() == null || event.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (!(obj instanceof Node))
				return;
			Node node = (Node) obj;

			try {
				if (node.isNodeType(NodeType.NT_FILE)) {
					// open the file
					String name = node.getName();
					String id = node.getPath();
					fileHandler.openFile(name, id);
				} else if (node
						.isNodeType(ConnectTypes.CONNECT_CLEAN_TRACK_SESSION)) {
					// Call parameterized command "open Editor"
					IWorkbench iw = ConnectUiPlugin.getDefault().getWorkbench();
					IHandlerService handlerService = (IHandlerService) iw
							.getService(IHandlerService.class);

					// get the command from plugin.xml
					IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
					ICommandService cmdService = (ICommandService) window
							.getService(ICommandService.class);
					Command cmd = cmdService.getCommand(OpenCleanDataEditor.ID);

					ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();

					// get the parameter
					IParameter iparam = cmd
							.getParameter(OpenCleanDataEditor.PARAM_UUID);

					Parameterization params = new Parameterization(iparam,
							node.getIdentifier());
					parameters.add(params);

					// build the parameterized command
					ParameterizedCommand pc = new ParameterizedCommand(cmd,
							parameters.toArray(new Parameterization[parameters
									.size()]));

					// execute the command
					handlerService = (IHandlerService) window
							.getService(IHandlerService.class);
					handlerService.executeCommand(pc, null);

					// open the corresponding session
					node.isNodeType(NodeType.NT_FILE);
				}

			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Repository error while getting Node file info", re);
			} catch (Exception e) {
				throw new ArgeoException(
						"Error while handling the double click in the GPS browser view.",
						e);
			}
		}

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

		IWorkbenchWindow window = ConnectUiPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();

		// Please note that commands that are not subject to programatic
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
		// We want to add GPX file only in the right place of the repository
		try {
			if (!multipleSel && selection.getFirstElement() instanceof Node) {
				Node node = (Node) selection.getFirstElement();
				if (node.getPrimaryNodeType().getName()
						.equals(ConnectTypes.CONNECT_FILE_REPOSITORY))
					isFileRepo = true;
				if (node.getPrimaryNodeType().getName()
						.equals(ConnectTypes.CONNECT_SESSION_REPOSITORY))
					isSessionRepo = true;
			}

		} catch (RepositoryException re) {
			throw new ArgeoException(
					"RepositoryException while refreshing context menu", re);
		}

		// Effective Refresh
		refreshCommand(menuManager, window, AddFileFolder.ID,
				AddFileFolder.DEFAULT_LABEL,
				AddFileFolder.DEFAULT_ICON_REL_PATH, isFileRepo);

		refreshCommand(menuManager, window, ImportDirectoryContent.ID,
				ImportDirectoryContent.DEFAULT_LABEL,
				ImportDirectoryContent.DEFAULT_ICON_REL_PATH, isFileRepo);

		refreshCommand(menuManager, window, NewCleanDataSession.ID,
				NewCleanDataSession.DEFAULT_LABEL,
				NewCleanDataSession.DEFAULT_ICON_REL_PATH, isSessionRepo);
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
			contributionItemParameter.icon = ConnectUiPlugin
					.getImageDescriptor(iconPath);

			// if (!REMOVE_CMD_ID.equals(cmdId)) {
			// Map<String, String> params = new HashMap<String, String>();
			// params.put(UUID_PARAM_ID, selectedRa.getUuid());
			// params.put(NAME_PARAM_ID,
			// (selectedRa.getAttributes().get("testCase") == null) ? null
			// : selectedRa.getAttributes().get("testCase"));
			// contributionItemParameter.parameters = params;
			// }

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

	// abstrat methods that must be overwritten
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
					// support multiple selecttion, but we insure that at least
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
			Iterator it = selection.iterator();
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

	// IoC
	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}

}
