package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
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

/** Almost canonical implementation of a table that display entities */
public class FilteredVirtualEntityTable extends Composite implements ArgeoNames {
	// private final static Log log =
	// LogFactory.getLog(EntityTableComposite.class);
	private static final long serialVersionUID = 1262369448445021926L;

	private TableViewer entityViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Type filter criterion "
			+ "separated by a space";
	private Session session;

	private int tableStyle;

	private boolean hasFilter = false;
	private boolean hasSelectionColumn = false;
	private List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>();
	{ // By default, it displays only title
		colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 300));
	};
	private String nodeType = PeopleTypes.PEOPLE_ENTITY;

	public List<ColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTORS

	/**
	 * Default table with no filter and no selection column that only display
	 * JCR_TITLES
	 * 
	 * Default selector is people:entity
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public FilteredVirtualEntityTable(Composite parent, int style,
			Session session) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		populate();
	}

	/**
	 * Default table that only display JCR_TITLES of an entity table. Caller can
	 * choose to add a filter and a selection column
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 * @param addFilter
	 * @param addSelection
	 */
	public FilteredVirtualEntityTable(Composite parent, int style,
			Session session, boolean addFilter, boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		this.hasFilter = addFilter;
		this.hasSelectionColumn = addSelection;
		populate();
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 * @param colDefs
	 */
	public FilteredVirtualEntityTable(Composite parent, int style,
			Session session, List<ColumnDefinition> colDefs) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		this.colDefs = colDefs;
		populate();
	}

	/**
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
	public FilteredVirtualEntityTable(Composite parent, int style,
			Session session, String nodeType, List<ColumnDefinition> colDefs,
			boolean addFilter, boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		if (colDefs != null)
			this.colDefs = colDefs;
		if (nodeType != null)
			this.nodeType = nodeType;
		this.hasFilter = addFilter;
		this.hasSelectionColumn = addSelection;
		populate();
	}

	protected void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		GridLayout layout = PeopleUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		if (hasFilter)
			createFilterPart(parent);
		entityViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(entityViewer);
		entityViewer
				.setContentProvider(new MyLazyContentProvider(entityViewer));
		refreshFilteredList();

		if (hasFilter)
			filterTxt.setFocus();
	}

	public List<Node> getSelectedEntities() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) entityViewer)
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
		return entityViewer;
	}

	private TableViewer createTableViewer(final Composite parent) {
		tableStyle = tableStyle | SWT.VIRTUAL;

		if (hasSelectionColumn)
			tableStyle = tableStyle | SWT.CHECK;

		Table table = new Table(parent, tableStyle);
		table.setLayoutData(EclipseUiUtils.fillAll());
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewer viewer;
		if (hasSelectionColumn)
			viewer = new CheckboxTableViewer(table);
		else
			viewer = new TableViewer(table);

		// TableViewer viewer;
		// if (hasSelectionColumn)
		// viewer = new CheckboxTableViewer(table); // parent, SWT.VIRTUAL |
		// tableStyle);
		// else
		// viewer = new TableViewer(parent, SWT.VIRTUAL | tableStyle);
		// Table table = viewer.getTable();
		//
		// table.setLayoutData(EclipseUiUtils.fillAll());
		// table.setLinesVisible(true);
		// table.setHeaderVisible(true);
		// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		TableViewerColumn column;
		// int offset = 0;
		if (hasSelectionColumn) {
			// offset = 1;
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
					((CheckboxTableViewer) entityViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		// NodeViewerComparator comparator = new NodeViewerComparator();
		// int i = offset;
		for (ColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(new CLProvider(colDef.getPropertyName()));
			// column.getColumn().addSelectionListener(
			// JcrUiUtils.getNodeSelectionAdapter(i,
			// colDef.getPropertyType(), colDef.getPropertyName(),
			// comparator, viewer));
			// i++;
		}

		// IMPORTANT: initialize comparator before setting it
		// ColumnDefinition firstCol = colDefs.get(0);
		// comparator.setColumn(firstCol.getPropertyType(),
		// firstCol.getPropertyName());
		// viewer.setComparator(comparator);

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
		entityViewer.getTable().setFocus();
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		refreshFilteredList();
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Object[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
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
			entityViewer.setInput(nodes.toArray());
			entityViewer.setItemCount(nodes.size());
			entityViewer.refresh();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list users", e);
		}
	}

	protected void setViewerInput(Object[] elements) {
		entityViewer.setInput(elements);
		// we must explicitly set the items count
		entityViewer.setItemCount(elements.length);
		entityViewer.refresh();
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(nodeType, nodeType);

		Constraint defaultC = null;

		// Build constraints based the textArea filter content
		if (filter != null && !"".equals(filter.trim())) {
			// Parse the String
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
		}
		// Entity should normally always be a mix:title
		Ordering order = factory.ascending(factory.propertyValue(
				source.getSelectorName(), Property.JCR_TITLE));
		Ordering[] orderings = { order };
		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);
		// TODO rather implement a virtual viewer
		// query.setLimit(100);
		QueryResult result = query.execute();
		return result.getNodes();
	}
}