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
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.ui.utils.RowViewerComparator;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.JcrUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

/** Basic implementation of a table that display persons */
public class PersonTableComposite extends Composite implements ArgeoNames,
		ITableProvider {
	private static final long serialVersionUID = 1262369448445021926L;

	private TableViewer personViewer;
	private Text filterTxt;

	private Text tagTxt;

	private final static String FILTER_HELP_MSG = "Type filter criterion "
			+ "separated by a space";
	private Session session;

	private int tableStyle;

	private boolean hasFilter = true;
	private boolean hasStaticFilter = true;
	private boolean hasSelectionColumn = false;

	private List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>();
	{
		colDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name", 300));
		colDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING, "Last name",
				120));
		colDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
				"First name", 120));
		colDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_TAGS, PropertyType.STRING, "Tags", 200));
	};

	/** Static FILTER */
	private void createStaticFilterPart(Composite parent) {
		// MainLayout
		Section headerSection = new Section(parent, Section.TITLE_BAR
				| Section.TWISTIE); // Section.DESCRIPTION
		headerSection.setText("Show more filters");
		headerSection.setExpanded(false);
		Composite body = new Composite(headerSection, SWT.NONE);
		body.setLayout(new RowLayout(SWT.HORIZONTAL));

		tagTxt = new Text(body, SWT.BORDER);
		tagTxt.setMessage("Filter by tag");
		tagTxt.setLayoutData(new RowData(120, SWT.DEFAULT));

		Button goBtn = new Button(body, SWT.PUSH);
		goBtn.setText("Search");
		goBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList();
			}

		});

		headerSection.setClient(body);
	}

	// CONSTRUCTORS

	/**
	 * Default table with no filter and no selection column that only display
	 * base info
	 * 
	 * Default selector is people:person
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public PersonTableComposite(Composite parent, int style, Session session) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
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
	public PersonTableComposite(Composite parent, int style, Session session,
			List<ColumnDefinition> colDefs, boolean addFilter,
			boolean addSelection) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		if (colDefs != null)
			this.colDefs = colDefs;
		this.hasFilter = addFilter;
		this.hasSelectionColumn = addSelection;
		populate();
	}

	protected void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		if (hasStaticFilter)
			createStaticFilterPart(parent);
		if (hasFilter)
			createFilterPart(parent);
		personViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(personViewer);
		personViewer.setContentProvider(new MyTableContentProvider());
		refreshFilteredList();
	}

	public List<Node> getSelectedUsers() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) personViewer)
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
		return personViewer;
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
					((CheckboxTableViewer) personViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		RowViewerComparator comparator = new RowViewerComparator();
		int i = offset;
		for (ColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(new CLProvider(colDef.getSelectorName(),
					colDef.getPropertyName()));
			column.getColumn().addSelectionListener(
					JcrUiUtils.getRowSelectionAdapter(i,
							colDef.getPropertyType(), colDef.getSelectorName(),
							colDef.getPropertyName(), comparator, viewer));
			i++;
		}

		// IMPORTANT: initialize comparator before setting it
		ColumnDefinition firstCol = colDefs.get(0);
		comparator.setColumn(firstCol.getPropertyType(),
				firstCol.getSelectorName(), firstCol.getPropertyName());
		viewer.setComparator(comparator);

		return viewer;
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
		List<Row> rows;
		try {
			rows = rowIteratorToList(listFilteredElements(session,
					hasFilter ? filterTxt.getText() : null));
			personViewer.setInput(rows.toArray());
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list users", e);
		}
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected RowIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(PeopleTypes.PEOPLE_PERSON,
				PeopleTypes.PEOPLE_PERSON);

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

		String tagStr = tagTxt.getText();
		if (CommonsJcrUtils.checkNotEmptyString(tagStr)) {
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_TAGS);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue("%" + tagStr + "%"));
			Constraint c2 = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_LIKE, statOp);
			if (defaultC == null)
				defaultC = c2;
			else
				defaultC = factory.and(defaultC, c2);
		}

		Ordering[] orderings = null;

		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);

		QueryResult result = query.execute();
		return result.getRows();
	}

	/** Convert a {@link NodeIterator} to a list of {@link Node} */
	private List<Row> rowIteratorToList(RowIterator rowIterator) {
		List<Row> rows = new ArrayList<Row>();
		while (rowIterator.hasNext()) {
			rows.add(rowIterator.nextRow());
		}
		return rows;
	}

	// /////////////////////////
	// LOCAL CLASSES
	private class CLProvider extends SimpleJcrRowLabelProvider {
		private static final long serialVersionUID = 1L;

		public CLProvider(String selectorName, String propertyName) {
			super(selectorName, propertyName);
		}

		// public String getToolTipText(Object element) {
		// return getText(element);
		// }

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

	// //////////////////////
	// Life cycle management
	@Override
	public boolean setFocus() {
		personViewer.getTable().setFocus();
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		refreshFilteredList();
	}

	// ///////////////////
	// Enable extraction
	@Override
	public List<ColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}

	@Override
	public RowIterator getRowIterator(String extractId) {
		try {
			String filter = hasFilter ? filterTxt.getText() : null;
			return listFilteredElements(session, filter);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get rows for the extract", re);
		}
	}
}