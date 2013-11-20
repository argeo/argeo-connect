package org.argeo.connect.people.ui.composites;

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
import org.argeo.eclipse.ui.jcr.JcrUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class EntityTableComposite extends Composite implements ArgeoNames {
	// private final static Log log =
	// LogFactory.getLog(UserTableComposite.class);
	private static final long serialVersionUID = 1262369448445021926L;
	private TableViewer usersViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Type filter criterion "
			+ "separated by a space";
	private Session session;

	private boolean hasFilter = false;
	private boolean hasSelectionColumn = false;
	private int tableStyle;

	private final List<ColumnDefinition> colDefs;

	public List<ColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTORS

	/**
	 * Default table with no filter and no selection column that only display
	 * JCR_TITLES
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public EntityTableComposite(Composite parent, int style, Session session) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		colDefs = new ArrayList<ColumnDefinition>();
		// By default, it displays only title
		colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 300));
		this.session = session;
		populate();
	}

	/**
	 * Default table that only display JCR_TITLES. Caller can choose to add a
	 * filter and a selection column
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 * @param addFilter
	 * @param addSelection
	 */
	public EntityTableComposite(Composite parent, int style, Session session,
			boolean addFilter, boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		colDefs = new ArrayList<ColumnDefinition>();
		// By default, it displays only title
		colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 300));
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
	public EntityTableComposite(Composite parent, int style, Session session,
			List<ColumnDefinition> colDefs) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		this.colDefs = colDefs;
		populate();
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 * @param colDefs
	 * @param addFilter
	 * @param addSelection
	 */
	public EntityTableComposite(Composite parent, int style, Session session,
			List<ColumnDefinition> colDefs, boolean addFilter,
			boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		this.colDefs = colDefs;
		this.hasFilter = addFilter;
		this.hasSelectionColumn = addSelection;
		populate();
	}

	/**
	 * 
	 * @param addFilter
	 *            choose to add a field to filter results or not
	 * @param addSelection
	 *            choose to add a column to select some of the displayed results
	 *            or not
	 */
	public void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		this.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		if (hasFilter)
			createFilterPart(parent);
		usersViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(usersViewer);
		usersViewer.setContentProvider(new MyTableContentProvider());
		refreshFilteredList();
	}

	public List<Node> getSelectedUsers() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) usersViewer)
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
		return usersViewer;
	}

	private TableViewer createTableViewer(final Composite parent) {
		if (hasSelectionColumn)
			tableStyle = tableStyle | SWT.CHECK;

		Table table = new Table(parent, tableStyle);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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
					((CheckboxTableViewer) usersViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		NodeViewerComparator comparator = new NodeViewerComparator();
		int i = offset;
		for (ColumnDefinition colDef : colDefs) {
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
		ColumnDefinition firstCol = colDefs.get(0);
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

		// @Override
		// public Font getFont(Object elem) {
		// // self
		// String username = getProperty(elem, ARGEO_USER_ID);
		// if (username.equals(session.getUserID()))
		// return bold;
		//
		// // disabled
		// try {
		// Node userProfile = (Node) elem;
		// // Node userProfile = userHome.getNode(ARGEO_PROFILE);
		// if (!userProfile.getProperty(ARGEO_ENABLED).getBoolean())
		// return italic;
		// else
		// return null;
		// } catch (RepositoryException e) {
		// throw new ArgeoException("Cannot get font for " + username, e);
		// }
		// }
	}

	@Override
	public boolean setFocus() {
		usersViewer.getTable().setFocus();
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
		private static final long serialVersionUID = 7164029504991808317L;

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
		// if (hasSelectionColumn)
		// selectedItems.clear();

		List<Node> nodes;
		try {
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session,
					hasFilter ? filterTxt.getText() : null));
			usersViewer.setInput(nodes.toArray());
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
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(PeopleTypes.PEOPLE_ORGANIZATION,
				PeopleTypes.PEOPLE_ORGANIZATION);

		Constraint defaultC = null;

		// Build constraints based the textArea content
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

		// Ordering order = factory.ascending(factory.propertyValue(
		// source.getSelectorName(), ARGEO_USER_ID));
		Ordering[] orderings = null; // { order };

		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);

		QueryResult result = query.execute();
		return result.getNodes();
	}
}