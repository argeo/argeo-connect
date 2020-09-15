package org.argeo.documents.e4.parts;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.api.NodeConstants;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.documents.DocumentsException;
import org.argeo.documents.DocumentsNames;
import org.argeo.documents.DocumentsService;
import org.argeo.documents.DocumentsTypes;
import org.argeo.documents.composites.BookmarksTableViewer;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.fs.FsTableViewer;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.fs.JcrPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Browse the node file system. */
public class MyFilesView implements IDoubleClickListener, Refreshable {
	// public final static String ID = DocumentsUiPlugin.PLUGIN_ID + ".myFilesView";

	@Inject
	private Repository repository;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private FileSystemProvider nodeFileSystemProvider;
	@Inject
	private DocumentsService documentsService;

	private Session homeSession;
	private Session groupSession;
	private Text filterTxt;
	private TableViewer searchResultsViewer;
	private Composite searchCmp;
	private Composite bookmarkCmp;

	@PostConstruct
	public void createPartControl(Composite parent) {
		homeSession = ConnectJcrUtils.login(repository, NodeConstants.HOME_WORKSPACE);
		groupSession = ConnectJcrUtils.login(repository, NodeConstants.SRV_WORKSPACE);
		// MainLayout
		parent.setLayout(new GridLayout());
//		addFilterPanel(parent);
//		searchCmp = new Composite(parent, SWT.NO_FOCUS);
//		searchCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
//		searchResultsViewer = createListPart(searchCmp, new DocumentsSingleColumnLP(systemWorkbenchService));
//		GridData gd = EclipseUiUtils.fillWidth();
//		gd.heightHint = 0;
//		searchCmp.setLayoutData(gd);

		bookmarkCmp = new Composite(parent, SWT.NO_FOCUS);
		bookmarkCmp.setLayoutData(EclipseUiUtils.fillAll());
		populateBookmarks(bookmarkCmp);
	}

	@Override
	public void forceRefresh(Object object) {
		populateBookmarks(bookmarkCmp);
		bookmarkCmp.getParent().layout(true, true);
	}

	public void addFilterPanel(Composite parent) {
		// Use a delayed text: the query won't be done until the user stop
		// typing for 800ms
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL;
		DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
		filterTxt = delayedText.getText();
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		// final ServerPushSession pushSession = new ServerPushSession();
		delayedText.addDelayedModifyListener(null, new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				delayedText.getText().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						int resultNb = refreshFilteredList();
						if (resultNb > 0)
							((GridData) searchCmp.getLayoutData()).heightHint = 120;
						else
							((GridData) searchCmp.getLayoutData()).heightHint = 0;
						parent.layout(true, true);
					}
				});
				// pushSession.stop();
			}
		});

		// Jump to the first item of the list using the down arrow
		filterTxt.addKeyListener(new KeyListener() {
			private static final long serialVersionUID = -4523394262771183968L;

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				// boolean altPressed = (e.stateMask & SWT.ALT) != 0;
				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.TAB) {
					Object first = searchResultsViewer.getElementAt(0);
					if (first != null) {
						searchResultsViewer.getTable().setFocus();
						searchResultsViewer.setSelection(new StructuredSelection(first), true);
					}
					e.doit = false;
				}
			}
		});
	}

	protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(100));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		CmsUiUtils.markup(table);
		CmsUiUtils.setItemHeight(table, 26);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(this);
		return v;
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(homeSession);
		JcrUtils.logoutQuietly(groupSession);
	}

	protected int refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			if (EclipseUiUtils.isEmpty(filter)) {
				searchResultsViewer.setInput(null);
				return 0;
			}
			// XPATH Query
			// String cf = XPathUtils.encodeXPathStringValue(filter);
			// String xpathQueryStr = "//element(*, nt:hierarchyNode)";
			// // + ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_FILE) + ")";
			// String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
			// if (notEmpty(xpathFilter))
			// xpathQueryStr += "[(" + xpathFilter + ") or "
			// //
			// + "(fn:name() = '" + cf + "' )" + "]";
			// QueryManager queryManager =
			// session.getWorkspace().getQueryManager();
			// Query xpathQuery = queryManager.createQuery(xpathQueryStr,
			// ConnectConstants.QUERY_XPATH);

			// SQL2 QUERY
			String cf = XPathUtils.encodeXPathStringValue(filter);
			String qStr = "SELECT * FROM [nt:hierarchyNode] WHERE UPPER(LOCALNAME()) LIKE '%" + cf.toUpperCase() + "%'";
			QueryManager queryManager = homeSession.getWorkspace().getQueryManager();
			Query xpathQuery = queryManager.createQuery(qStr, Query.JCR_SQL2);

			// xpathQuery.setLimit(TrackerUiConstants.SEARCH_DEFAULT_LIMIT);
			QueryResult result = xpathQuery.execute();
			NodeIterator nit = result.getNodes();
			searchResultsViewer.setInput(JcrUtils.nodeIteratorToList(nit));

			return (int) nit.getSize();
		} catch (RepositoryException e) {
			throw new DocumentsException("Unable to list files", e);
		}
	}

	private void populateBookmarks(Composite parent) {
		CmsUiUtils.clear(parent);
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);
		int bookmarkColWith = 200;

		CellLabelProvider lp = new MyLabelProvider();

		FsTableViewer homeViewer = new FsTableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL);
		Table table = homeViewer.configureDefaultSingleColumnTable(bookmarkColWith, lp);
		GridData gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 10;
		table.setLayoutData(gd);
		homeViewer.addDoubleClickListener(this);
		homeViewer.setPathsInput(documentsService.getMyDocumentsPath(nodeFileSystemProvider, homeSession));

		Path[] wkGpHomes = documentsService.getMyGroupsFilesPath(nodeFileSystemProvider, groupSession);
		if (wkGpHomes != null && wkGpHomes.length > 0) {
			appendTitle(parent, "Shared");
			FsTableViewer groupsViewer = new FsTableViewer(parent, SWT.SINGLE | SWT.NO_SCROLL);
			table = groupsViewer.configureDefaultSingleColumnTable(bookmarkColWith, lp);
			gd = EclipseUiUtils.fillWidth();
			gd.horizontalIndent = 10;
			table.setLayoutData(gd);
			groupsViewer.addDoubleClickListener(this);
			groupsViewer.setPathsInput(wkGpHomes);
		}

		appendTitle(parent, "Bookmarks");
		BookmarksTableViewer bookmarksViewer = new BookmarksTableViewer(parent, SWT.MULTI | SWT.NO_SCROLL,
				documentsService.getMyBookmarksParent(homeSession), documentsService, systemWorkbenchService);
		table = bookmarksViewer.configureDefaultSingleColumnTable(bookmarkColWith);
		gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 10;
		table.setLayoutData(gd);
		bookmarksViewer.addDoubleClickListener(this);
	}

	private class MyLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -3406927207142514035L;

		public MyLabelProvider() {
			super();
		}

		@Override
		public String getText(Object element) {
			Path curr = ((Path) element);
			try {
//				String path = curr.toString();
//				Node currNode = homeSession.getNode(path);
				// TODO do it via an attributes view
				JcrPath jcrPath = (JcrPath) curr;
				Node currNode = jcrPath.getNode();
//				Node parent = currNode.getParent();
//				if (currNode.isNodeType(NodeTypes.NODE_USER_HOME))
//					return currNode.getName();
//				else if (currNode.isNodeType(NodeTypes.NODE_GROUP_HOME))
//					return currNode.getProperty(NodeNames.LDAP_CN).getString();

				// FIXME make recognition of home and group home more robust
				if (currNode.hasProperty(Property.JCR_TITLE))
					return currNode.getProperty(Property.JCR_TITLE).getString();
				else if (currNode.hasProperty(Property.JCR_ID))
					return currNode.getProperty(Property.JCR_ID).getString();
				else
					return super.getText(element);
			} catch (RepositoryException e) {
				throw new DocumentsException("Cannot retrieve label for " + curr, e);
			}
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Path) {
				Path curr = ((Path) element);
//				String path = curr.toString();
//				Node currNode = ConnectJcrUtils.getNode(homeSession, path);
				// TODO do it via an attributes view
				JcrPath jcrPath = (JcrPath) curr;
				try {
					Node currNode = jcrPath.getNode();
					return systemWorkbenchService.getIconForType(currNode);
				} catch (RepositoryException e) {
					throw new IllegalStateException(e);
				}
			}
			return null;
		}
	}

	private Label appendTitle(Composite parent, String value) {
		Label titleLbl = new Label(parent, SWT.NONE);
		titleLbl.setText(value);
		titleLbl.setFont(EclipseUiUtils.getBoldFont(parent));
		GridData gd = EclipseUiUtils.fillWidth();
		gd.horizontalIndent = 5;
		gd.verticalIndent = 5;
		titleLbl.setLayoutData(gd);
		return titleLbl;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (selection.isEmpty())
			return;
		else {
			Object element = selection.getFirstElement();
			Node currNode;

			if (element instanceof Node) {
				Node tmpNode = (Node) element;
				if (ConnectJcrUtils.isNodeType(tmpNode, DocumentsTypes.DOCUMENTS_BOOKMARK)) {
					String uriStr = ConnectJcrUtils.get(tmpNode, DocumentsNames.DOCUMENTS_URI);
					try {
						Path currPath = documentsService.getPath(nodeFileSystemProvider, new URI(uriStr));
//						String jcrPath = currPath.toString();
						if (!Files.exists(currPath)) {
							String msg = "Bookmarked folder at URI " + uriStr
									+ " cannot be found. If it is a local folder "
									+ "it has been removed or renamed.\nDo you want to delete corresponding bookmark?";
							boolean remove = MessageDialog.openConfirm(event.getViewer().getControl().getShell(),
									"Missing target folder", msg);
							if (remove) {
								tmpNode.remove();
								homeSession.save();
								forceRefresh(null);
							}
							return;
						} else
							currNode = documentsService.getNode(homeSession.getRepository(), currPath);
						// TODO make it more portable
						RWT.getUISession().addUISessionListener(new UISessionListener() {
							private static final long serialVersionUID = 1L;

							public void beforeDestroy(UISessionEvent event) {
								Jcr.logout(Jcr.session(currNode));
							}
						});
					} catch (URISyntaxException | RepositoryException e) {
						throw new DocumentsException("Cannot get target node for bookmark " + tmpNode, e);
					}
				} else
					currNode = tmpNode;
			} else if (element instanceof Path) {
				Path currPath = (Path) element;
//				String jcrPath = currPath.toString();
//				// TODO rather directly use the jcrPath / an URI?
//				currNode = ConnectJcrUtils.getNode(homeSession, jcrPath);
				JcrPath jcrPath = (JcrPath) currPath;
				try {
					currNode = jcrPath.getNode();
				} catch (RepositoryException e) {
					throw new IllegalStateException(e);
				}

			} else
				throw new IllegalArgumentException("Cannot manage " + element + ", only Node and Path are supported.");
			// String nodeId = ConnectJcrUtils.getIdentifier(currNode);
			// FIXME hard coded parameter name
			// CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
			// "param.jcrId", nodeId);
			systemWorkbenchService.openEntityEditor(currNode);
		}
	}

	/* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }
	//
	// public void setSystemWorkbenchService(SystemWorkbenchService
	// systemWorkbenchService) {
	// this.systemWorkbenchService = systemWorkbenchService;
	// }
	//
	// public void setNodeFileSystemProvider(FileSystemProvider
	// nodeFileSystemProvider) {
	// this.nodeFileSystemProvider = nodeFileSystemProvider;
	// }
	//
	// public void setDocumentsService(DocumentsService documentsService) {
	// this.documentsService = documentsService;
	// }

	private class BasicNodeListContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;
		private List<Node> nodes;

		public void dispose() {
		}

		/** Expects a list of nodes as a new input */
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			nodes = (List<Node>) newInput;
		}

		public Object[] getElements(Object arg0) {
			return nodes.toArray();
		}
	}
}
