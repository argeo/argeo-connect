package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.JcrUiUtils;
import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.NodeViewerComparator;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Canonical implementation of a table that display Jcr nodes */
public class SimpleJcrTableComposite extends Composite implements ArgeoNames {

	private static final long serialVersionUID = 3476100511651849356L;
	private TableViewer tableViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Type filter criterion "
			+ "separated by a space";
	private Session session;

	private int tableStyle;

	private boolean hasFilter = false;
	private boolean hasSelectionColumn = false;

	private List<JcrColumnDefinition> colDefs = new ArrayList<JcrColumnDefinition>();
	{ // By default, it displays only title
		colDefs.add(new JcrColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 300));
	};
	private String nodeType = NodeType.NT_UNSTRUCTURED;
	private String parentPath = "/";

	public List<JcrColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTORS

	/**
	 * Default table with no filter and no selection column that only display
	 * JCR_TITLE.
	 * 
	 * Default selector is NodeType.NT_UNSTRUCTURED, default parent path is the
	 * root path
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public SimpleJcrTableComposite(Composite parent, int style, Session session) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		colDefs = new ArrayList<JcrColumnDefinition>();
		this.session = session;
		populate();
	}

	/**
	 * 
	 * Caller might define column that are displayed, choose to add a filter and
	 * a selection column, define a parent path and/or a type for nodes that has
	 * to be listed
	 * 
	 * if some parameters are null, we use default values instead
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 * @param colDefs
	 * @param addFilter
	 * @param addSelection
	 */
	public SimpleJcrTableComposite(Composite parent, int style,
			Session session, String parentPath, String nodeType,
			List<JcrColumnDefinition> colDefs, boolean addFilter,
			boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		if (colDefs != null)
			this.colDefs = colDefs;
		if (nodeType != null)
			this.nodeType = nodeType;
		if (parentPath != null)
			this.parentPath = parentPath;

		this.hasFilter = addFilter;
		this.hasSelectionColumn = addSelection;
		populate();
	}

	protected void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		GridLayout gl = EclipseUiUtils.noSpaceGridLayout();
		gl.verticalSpacing = 5;
		this.setLayout(gl);
		if (hasFilter)
			createFilterPart(parent);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);
		tableViewer.setContentProvider(new MyTableContentProvider());
		refreshFilteredList();

		if (hasFilter)
			filterTxt.setFocus();
	}

	public List<Node> getSelectedNodes() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) tableViewer)
					.getCheckedElements();

			List<Node> result = new ArrayList<Node>();
			for (Object obj : elements) {
				result.add((Node) obj);
			}
			return result;
		} else
			throw new ArgeoException("Unvalid request: no selection column "
					+ "has been created for the current table");
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private TableViewer createTableViewer(final Composite parent) {
		// TODO make it virtual
		// tableStyle = tableStyle | SWT.VIRTUAL;

		if (hasSelectionColumn)
			tableStyle = tableStyle | SWT.CHECK;

		Table table = new Table(parent, tableStyle);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer;
		if (hasSelectionColumn)
			viewer = new CheckboxTableViewer(table);
		else
			viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;
		int offset = 0;
		if (hasSelectionColumn) {
			offset = 1;
			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
					25);
			column.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					return null;
				}
			});
			SelectionAdapter selectionAdapter = new SelectionAdapter() {
				private static final long serialVersionUID = 1L;
				boolean allSelected = false;

				@Override
				public void widgetSelected(SelectionEvent e) {
					allSelected = !allSelected;
					((CheckboxTableViewer) tableViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		NodeViewerComparator comparator = new NodeViewerComparator();
		int i = offset;
		for (JcrColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(new CLProvider(colDef.getPropertyName()));
			column.getColumn().addSelectionListener(
					JcrUiUtils.getNodeSelectionAdapter(i,
							colDef.getPropertyType(), colDef.getPropertyName(),
							comparator, viewer));
			i++;
		}

		// IMPORTANT: initialize comparator before setting it
		JcrColumnDefinition firstCol = colDefs.get(0);
		comparator.setColumn(firstCol.getPropertyType(),
				firstCol.getPropertyName());
		viewer.setComparator(comparator);

		return viewer;
	}

	private class CLProvider extends SimpleJcrNodeLabelProvider {
		private static final long serialVersionUID = 1L;

		public CLProvider(String propertyName) {
			super(propertyName);
		}

		public String getToolTipText(Object element) {
			return getText(element);
		}
	}

	@Override
	public boolean setFocus() {
		tableViewer.getTable().setFocus();
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		refreshFilteredList();
	}

	private class MyTableContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 5933809732799991342L;

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/* MANAGE FILTER */
	private void createFilterPart(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		List<Node> nodes;
		try {
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session,
					hasFilter ? filterTxt.getText() : null));
			tableViewer.setInput(nodes.toArray());
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list users", e);
		}
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		String xpathQueryStr = XPathUtils.descendantFrom(parentPath)
				+ "//element(*, " + nodeType + ")";
		String attrQuery = XPathUtils.getFreeTextConstraint(filter);
		if (CommonsJcrUtils.checkNotEmptyString(attrQuery))
			xpathQueryStr += "[" + attrQuery + "]";
		Query xpathQuery = queryManager.createQuery(xpathQueryStr,
				PeopleConstants.QUERY_XPATH);
		QueryResult result = xpathQuery.execute();
		return result.getNodes();
	}
}