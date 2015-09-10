package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.ArgeoException;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.providers.TitleIconLP;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
public class FilterEntitiesVirtualTable extends Composite implements ArgeoNames {
	private static final long serialVersionUID = 1262369448445021926L;

	// Context
	private Session session;
	private PeopleWorkbenchService peopleWorkbenchService;

	// UI Objects
	private TableViewer entityViewer;
	private Text filterTxt;
	private int tableStyle;

	// Defaults
	private String nodeType = PeopleTypes.PEOPLE_ENTITY;

	public FilterEntitiesVirtualTable(Composite parent, int style,
			Session session, PeopleWorkbenchService peopleWorkbenchService,
			String nodeType) {
		this(parent, style, session, peopleWorkbenchService, nodeType, false);
	}

	/**
	 * If lazy flag is set, the populate method must be explicitly called:
	 * Enable further configuration of the table before display, like typically
	 * definition of other column
	 */
	public FilterEntitiesVirtualTable(Composite parent, int style,
			Session session, PeopleWorkbenchService peopleWorkbenchService,
			String nodeType, boolean lazy) {
		super(parent, SWT.NONE);
		this.session = session;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.tableStyle = style;
		if (CommonsJcrUtils.checkNotEmptyString(nodeType))
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
		this.pack();
		this.layout();
		EclipseUiSpecificUtils.enableToolTipSupport(entityViewer);
		// Do not execute the query by default
		// refreshFilteredList();
	}

	protected int getTableHeight() {
		return 449;
	}

	private void createTableViewer(final Composite parent) {
		Composite listCmp = new Composite(parent, SWT.NO_FOCUS);
		// TODO Workaround to force display of the scroll bar
		GridData gd = EclipseUiUtils.fillWidth();
		gd.heightHint = getTableHeight();
		listCmp.setLayoutData(gd);

		entityViewer = new TableViewer(listCmp, SWT.VIRTUAL | tableStyle);
		Table table = entityViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 26);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn column;

		for (PeopleColumnDefinition colDef : getColumnsDef()) {
			column = ViewerUtils.createTableViewerColumn(entityViewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
			tableColumnLayout.setColumnData(
					column.getColumn(),
					new ColumnWeightData(colDef.getColumnSize(), colDef
							.getColumnSize(), true));
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

	protected List<PeopleColumnDefinition> getColumnsDef() {
		List<PeopleColumnDefinition> colDefs;
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", new TitleIconLP(
						peopleWorkbenchService, Property.JCR_TITLE), 300));
		return colDefs;
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return entityViewer;
	}

	/* MANAGE FILTER */
	private void createFilterPart(Composite parent) {
		// Text Area for the filter

		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2,
				false));
		layout.horizontalSpacing = 5;
		filterCmp.setLayout(layout);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
		filterTxt = new Text(filterCmp, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		Button okBtn = new Button(filterCmp, SWT.FLAT);
		okBtn.setText("Find");

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
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session,
					filterTxt.getText()));
			entityViewer.setInput(nodes.toArray());
			entityViewer.setItemCount(nodes.size());
			entityViewer.refresh();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to refresh filtered list of "
					+ nodeType + " with filter " + filterTxt.getText(), e);
		}
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		String xpathQueryStr = "//element(*, " + nodeType + ")";
		String attrQuery = XPathUtils.getFreeTextConstraint(filter);
		if (CommonsJcrUtils.checkNotEmptyString(attrQuery))
			xpathQueryStr += "[" + attrQuery + "]";
		Query xpathQuery = queryManager.createQuery(xpathQueryStr,
				PeopleConstants.QUERY_XPATH);
		xpathQuery.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
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