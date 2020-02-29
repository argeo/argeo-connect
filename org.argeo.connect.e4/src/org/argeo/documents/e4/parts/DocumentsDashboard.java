package org.argeo.documents.e4.parts;

import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.util.BasicNodeListContentProvider;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.documents.DocumentsException;
import org.argeo.documents.ui.DocumentsSingleColumnLP;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class DocumentsDashboard implements IDoubleClickListener {
	@Inject
	private Repository repository;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	private Session session;
	private Text filterTxt;
	private TableViewer searchResultsViewer;
	private Composite searchCmp;

	@PostConstruct
	public void createPartControl(Composite parent) {
		session = ConnectJcrUtils.login(repository);
		// MainLayout
		parent.setLayout(new GridLayout());
		addFilterPanel(parent);
		searchCmp = new Composite(parent, SWT.NO_FOCUS);
		searchCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		searchResultsViewer = createListPart(searchCmp, new DocumentsSingleColumnLP(systemWorkbenchService));
		GridData gd = EclipseUiUtils.fillWidth();
		gd.heightHint = 0;
		searchCmp.setLayoutData(gd);
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
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 26);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(this);
		return v;
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
			QueryManager queryManager = session.getWorkspace().getQueryManager();
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

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (selection.isEmpty())
			return;
		else {
			Object element = selection.getFirstElement();
			Node currNode;

			if (element instanceof Node) {
				currNode = (Node) element;
			} else if (element instanceof Path) {
				Path currPath = (Path) element;
				String jcrPath = currPath.toString();
				// TODO rather directly use the jcrPath / an URI?
				currNode = ConnectJcrUtils.getNode(session, jcrPath);
			} else
				throw new IllegalArgumentException("Cannot manage " + element + ", only Node and Path are supported.");
			systemWorkbenchService.openEntityEditor(currNode);
		}
	}

	@Focus
	public void setFocus() {
		// RAP specific
		RWT.getClient().getService(BrowserNavigation.class).pushState("~", "Docs");
	}

	@PreDestroy
	public void destroy() {
		JcrUtils.logoutQuietly(session);
	}
}
