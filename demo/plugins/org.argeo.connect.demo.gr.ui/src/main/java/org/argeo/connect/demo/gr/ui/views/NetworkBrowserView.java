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
package org.argeo.connect.demo.gr.ui.views;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.commands.CreateNetwork;
import org.argeo.connect.demo.gr.ui.commands.RefreshNetworkBrowserView;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.connect.demo.gr.ui.providers.GrTreeContentProvider;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;
import org.argeo.eclipse.ui.jcr.utils.NodeViewerComparer;
import org.argeo.eclipse.ui.jcr.views.AbstractJcrBrowser;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
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

/** Specific network explorer View */
public class NetworkBrowserView extends AbstractJcrBrowser implements GrNames,
		GrConstants {

	// private final static Log log =
	// LogFactory.getLog(NetworkBrowserView.class);
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".networkBrowserView";

	/* DEPENDENCY INJECTION */
	private GrBackend grBackend;

	// retrieved with the backend.
	private Repository repository;
	private Session session;

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

		// expends a little bit
		nodesViewer.expandToLevel(2);

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

		// Please note that commands that are not subject to programmatic
		// conditions are directly defined in the corresponding
		// menuContribution of the plugin.xml.

		// Building conditions
		boolean isAdmin = grBackend.isUserInRole(ROLE_ADMIN);

		// Some commands are meaningless for multiple selection
		boolean isFolder = true;

		// Effective Refresh of the menu

		// Create new network
		CommandUtils.refreshCommand(menuManager, window, CreateNetwork.ID,
				CreateNetwork.DEFAULT_LABEL, CreateNetwork.DEFAULT_ICON_DESC,
				isFolder && isAdmin);

		// Add a refresh command so that context menu is not empty for non-admin
		// users
		CommandUtils.refreshCommand(menuManager, window,
				RefreshNetworkBrowserView.ID,
				RefreshNetworkBrowserView.DEFAULT_LABEL,
				RefreshNetworkBrowserView.DEFAULT_ICON_DESC, true);

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

	/* DEPENDENCY INJECTION */
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
		this.repository = grBackend.getRepository();
	}
}