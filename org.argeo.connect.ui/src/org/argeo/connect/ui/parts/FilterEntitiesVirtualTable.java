package org.argeo.connect.ui.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.util.TitleIconHtmlLP;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.util.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Almost canonical implementation of a table that display entities */
public class FilterEntitiesVirtualTable extends Composite {
	private static final long serialVersionUID = 1262369448445021926L;

	// Context
	private Session session;
	private AppWorkbenchService appWorkbenchService;

	// UI Objects
	private TableViewer entityViewer;
	private Text filterTxt;
	private int tableStyle;

	// Defaults
	private String nodeType = ConnectTypes.CONNECT_ENTITY;

	public FilterEntitiesVirtualTable(Composite parent, int style, Session session,
			AppWorkbenchService systemWorkbenchService, String nodeType) {
		this(parent, style, session, systemWorkbenchService, nodeType, false);
	}

	/**
	 * If lazy flag is set, the populate method must be explicitly called:
	 * Enable further configuration of the table before display, like typically
	 * definition of other column
	 */
	public FilterEntitiesVirtualTable(Composite parent, int style, Session session,
			AppWorkbenchService appWorkbenchService, String nodeType, boolean lazy) {
		super(parent, SWT.NONE);
		this.session = session;
		this.appWorkbenchService = appWorkbenchService;
		this.tableStyle = style;
		if (EclipseUiUtils.notEmpty(nodeType))
			this.nodeType = nodeType;
		if (!lazy)
			populate();
	}

	protected void populate() {
		Composite parent = this;
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		createFilterPart(parent);
		createTableViewer(parent);
		this.layout();
		EclipseUiSpecificUtils.enableToolTipSupport(entityViewer);

		if (!appWorkbenchService.lazyLoadLists())
			refreshFilteredList();
	}

	protected int getTableHeight() {
		return SWT.DEFAULT;
	}

	private void createTableViewer(final Composite parent) {
		Composite listCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = EclipseUiUtils.fillAll();
		listCmp.setLayoutData(gd);

		entityViewer = new TableViewer(listCmp, SWT.VIRTUAL | tableStyle);
		Table table = entityViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		CmsUiUtils.markup(table);
		CmsUiUtils.setItemHeight(table, 26);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn column;

		for (ConnectColumnDefinition colDef : getColumnsDef()) {
			column = ViewerUtils.createTableViewerColumn(entityViewer, colDef.getHeaderLabel(), SWT.NONE,
					colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
			tableColumnLayout.setColumnData(column.getColumn(),
					new ColumnWeightData(colDef.getColumnSize(), colDef.getColumnSize(), true));
		}
		listCmp.setLayout(tableColumnLayout);
		entityViewer.setContentProvider(new MyLazyCP(entityViewer));
	}

	private class MyLazyCP implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Object[] elements;

		public MyLazyCP(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	protected List<ConnectColumnDefinition> getColumnsDef() {
		List<ConnectColumnDefinition> colDefs;
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Name", new TitleIconHtmlLP(appWorkbenchService), 300));
		return colDefs;
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return entityViewer;
	}

	/* MANAGE FILTER */
	private void createFilterPart(Composite parent) {
		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
		layout.horizontalSpacing = 5;
		filterCmp.setLayout(layout);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());

		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL;
		Button okBtn = new Button(filterCmp, SWT.FLAT);
		okBtn.setText("Find");
		boolean isDyn = appWorkbenchService.queryWhenTyping();
//		if (isDyn)
//			filterTxt = new DelayedText(filterCmp, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
//		else
//			filterTxt = new Text(filterCmp, style);
//		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());
//
//
//		if (isDyn) {
//			final ServerPushSession pushSession = new ServerPushSession();
//			((DelayedText) filterTxt).addDelayedModifyListener(pushSession, new ModifyListener() {
//				private static final long serialVersionUID = 5003010530960334977L;
//
//				public void modifyText(ModifyEvent event) {
//					filterTxt.getDisplay().asyncExec(new Runnable() {
//						@Override
//						public void run() {
//							refreshFilteredList();
//						}
//					});
//					pushSession.stop();
//				}
//			});
//		}
		if (isDyn) {
			final DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
			final ServerPushSession pushSession = new ServerPushSession();
			(delayedText).addDelayedModifyListener(pushSession, new ModifyListener() {
				private static final long serialVersionUID = 5003010530960334977L;

				public void modifyText(ModifyEvent event) {
					delayedText.getText().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshFilteredList();
						}
					});
					pushSession.stop();
				}
			});
			filterTxt = delayedText.getText();
		}else {
			filterTxt = new Text(parent, style);
		}
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		filterTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 3946973977865345010L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					refreshFilteredList();
				}
			}
		});

		okBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 4305076157959928315L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList();
			}
		});
	}

	private void refreshFilteredList() {
		List<Node> nodes;
		try {
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session, filterTxt.getText()));
			entityViewer.setInput(nodes.toArray());
			entityViewer.setItemCount(nodes.size());
			entityViewer.refresh();
		} catch (RepositoryException e) {
			throw new ConnectException(
					"Unable to refresh filtered list of " + nodeType + " with filter " + filterTxt.getText(), e);
		}
	}

	/**
	 * Build repository request: caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter) throws RepositoryException {
		String xpathQueryStr = "//element(*, " + nodeType + ")";
		String attrQuery = XPathUtils.getFreeTextConstraint(filter);
		if (EclipseUiUtils.notEmpty(attrQuery))
			xpathQueryStr += "[" + attrQuery + "]";
		Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);
		xpathQuery.setLimit(ConnectUiConstants.SEARCH_DEFAULT_LIMIT);
		QueryResult result = xpathQuery.execute();
		return result.getNodes();

		// QueryManager queryManager = session.getWorkspace().getQueryManager();
		// QueryObjectModelFactory factory = queryManager.getQOMFactory();
		// Selector source = factory.selector(nodeType, nodeType);
		// Constraint defaultC = null;
		// // Build constraints based the textArea filter content
		// if (filter != null && !"".equals(filter.trim())) {
		// // Parse the String
		// String[] strs = filter.trim().split(" ");
		// for (String token : strs) {
		// StaticOperand so = factory.literal(session.getValueFactory()
		// .createValue("*" + token + "*"));
		// Constraint currC = factory.fullTextSearch(
		// source.getSelectorName(), null, so);
		// if (defaultC == null)
		// defaultC = currC;
		// else
		// defaultC = factory.and(defaultC, currC);
		// }
		// }

		// // Entity should normally always be a mix:title
		// Ordering order = factory.ascending(factory.propertyValue(
		// source.getSelectorName(), Property.JCR_TITLE));
		// Ordering[] orderings = { order };
		// QueryObjectModel query = factory.createQuery(source, defaultC,
		// orderings, null);
		// QueryObjectModel query = factory.createQuery(source, defaultC, null,
		// null);
		// query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
		// QueryResult result = query.execute();
		// return result.getNodes();
	}
}
