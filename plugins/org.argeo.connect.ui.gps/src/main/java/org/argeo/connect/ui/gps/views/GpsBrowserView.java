/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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
import org.argeo.connect.ui.gps.commands.DeleteNodesExt;
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
import org.argeo.eclipse.ui.utils.CommandUtils;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;

/** Specific GPS explorer View */
public class GpsBrowserView extends AbstractJcrBrowser implements ConnectNames,
		ConnectTypes { // extends ViewPart {

	private final static Log log = LogFactory.getLog(GpsBrowserView.class);
	public final static String ID = ConnectGpsUiPlugin.ID + ".gpsBrowserView";

	/* DEPENDENCY INJECTION */
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

		// Create base directories if they don't exists
		if (uiJcrServices.getGpxFilesDirectory() == null
				|| uiJcrServices.getLocalRepositoriesParentNode() == null
				|| uiJcrServices.getTrackSessionsParentNode() == null)
			uiJcrServices.initializeLocalRepository();

		// Create UI objects
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
		nodeContentProvider = new ViewContentProvider(
				uiJcrServices.getJcrSession(), rootNodes);

		// TreeViewer
		parent.setLayout(new GridLayout());
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
			// Note that commands that are not subject to programmatic
			// conditions are directly define in the corresponding
			// menuContribution of the plugin.xml.

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
			boolean canBeDeleted = false;
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

				if (isCompleted || isSessionRepo || isLocalRepoParent)
					canBeDeleted = false;
				else
					canBeDeleted = true;
			}

			// Effective Refresh
			CommandUtils.refreshCommand(menuManager, window, AddFileFolder.ID,
					AddFileFolder.DEFAULT_LABEL, AddFileFolder.DEFAULT_ICON,
					isFileRepo);

			CommandUtils.refreshCommand(menuManager, window,
					ImportDirectoryContent.ID,
					ImportDirectoryContent.DEFAULT_LABEL,
					ImportDirectoryContent.DEFAULT_ICON, isFileRepo);

			Map<String, String> params = null;
			if (curNode != null) {
				params = new HashMap<String, String>();
				if (isSession) {
					// create session from current one
					params.put(NewCleanDataSession.PARAM_PARENT_ID, curNode
							.getParent().getIdentifier());
					params.put(NewCleanDataSession.PARAM_MODEL_ID,
							curNode.getIdentifier());
					CommandUtils
							.refreshParametrizedCommand(menuManager, window,
									NewCleanDataSession.ID,
									NewCleanDataSession.COPY_SESSION_LABEL,
									NewCleanDataSession.DEFAULT_ICON,
									isSession, params);
				} else {
					params.put(NewCleanDataSession.PARAM_PARENT_ID,
							curNode.getIdentifier());
					// create session from default
					CommandUtils.refreshCommand(menuManager, window,
							NewCleanDataSession.ID,
							NewCleanDataSession.DEFAULT_LABEL,
							NewCleanDataSession.DEFAULT_ICON, isSessionRepo);
				}

				params = new HashMap<String, String>();
				params.put(RemoveImportedData.PARAM_SESSION_ID,
						curNode.getIdentifier());
				CommandUtils.refreshParametrizedCommand(menuManager, window,
						RemoveImportedData.ID,
						RemoveImportedData.DEFAULT_LABEL,
						RemoveImportedData.DEFAULT_ICON, isCompleted, params);
			}

			CommandUtils.refreshCommand(menuManager, window,
					OpenNewRepoWizard.ID, OpenNewRepoWizard.DEFAULT_LABEL,
					OpenNewRepoWizard.DEFAULT_ICON, isLocalRepoParent);

			CommandUtils.refreshCommand(menuManager, window, DeleteNodesExt.ID,
					DeleteNodesExt.DEFAULT_LABEL, DeleteNodesExt.DEFAULT_ICON,
					canBeDeleted);

		} catch (RepositoryException re) {
			throw new ArgeoException(
					"RepositoryException while refreshing context menu", re);
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