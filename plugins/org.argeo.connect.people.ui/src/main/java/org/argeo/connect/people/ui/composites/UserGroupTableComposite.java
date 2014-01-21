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
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class UserGroupTableComposite extends Composite implements ArgeoNames {
	// private final static Log log =
	// LogFactory.getLog(UserTableComposite.class);

	private static final long serialVersionUID = -7385959046279360420L;

	private TableViewer userGroupViewer;
	private Text filterTxt;
	private Button displayUserChk;

	private final static String FILTER_HELP_MSG = "Search groups";
	private Session session;

	private boolean hasFilter;
	private boolean hasSelectionColumn;

	/**
	 * Overwrite to display other columns
	 */
	public List<ColumnDefinition> getColumnsDef() {
		List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();

		// Displayed name
		columnDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 120));

		// Description
		columnDefs.add(new ColumnDefinition(null, Property.JCR_DESCRIPTION,
				PropertyType.STRING, "Description", 200));

		// // Member nb
		// columnDefs.add(new ColumnDefinition(null, null,
		// PropertyType.STRING, "Nb of member", 50));
		return columnDefs;
	}

	public UserGroupTableComposite(Composite parent, int style, Session session) {
		super(parent, style);
		this.session = session;
	}

	/**
	 * 
	 * @param addFilter
	 *            choose to add a field to filter results or not
	 * @param addSelection
	 *            choose to add a column to select some of the displayed results
	 *            or not
	 */
	public void populate(boolean addFilter, boolean addSelection) {
		// initialization
		Composite parent = this;
		hasFilter = addFilter;
		hasSelectionColumn = addSelection;

		// Main Layout
		this.setLayout(new GridLayout(1, false));
		if (hasFilter)
			createFilterPart(parent);
		userGroupViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(userGroupViewer);
		userGroupViewer.setContentProvider(new UserGroupsContentProvider());
		refreshFilteredList();
	}

	public List<Node> getSelectedGroups() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) userGroupViewer)
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
		return userGroupViewer;
	}

	/** Returns filter String or null */
	protected String getFilterString() {
		return hasFilter ? filterTxt.getText() : null;
	}

	private TableViewer createTableViewer(final Composite parent) {
		int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
		if (hasSelectionColumn)
			style = style | SWT.CHECK;

		Table table = new Table(parent, style);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer viewer;
		if (hasSelectionColumn)
			viewer = new CheckboxTableViewer(table);
		else
			viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// pass a mapping between col index and property name to the comparator.
		// List<String> propertiesList = new ArrayList<String>();

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
					((CheckboxTableViewer) userGroupViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		// Create other columns
		List<ColumnDefinition> colDefs = getColumnsDef();

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
	}

	@Override
	public boolean setFocus() {
		userGroupViewer.getTable().setFocus();
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		refreshFilteredList();
	}

	private class UserGroupsContentProvider implements
			IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

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
		Composite composite = new Composite(parent, SWT.NO_FOCUS);
		composite.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));
		composite
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		displayUserChk = new Button(composite, SWT.CHECK);
		displayUserChk.setText("Display Users");

		displayUserChk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList();
			}
		});

		// Text Area for the filter
		filterTxt = new Text(composite, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalIndent = 10;
		filterTxt.setLayoutData(gd);
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/**
	 * Refresh the group list: caller might overwrite in order to display a
	 * subset of all groups, typically removing allready assigned groups to a
	 * given user
	 */
	protected void refreshFilteredList() {
		List<Node> nodes;
		try {
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session,
					hasFilter ? filterTxt.getText() : null));
			userGroupViewer.setInput(nodes.toArray());
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list users", e);
		}
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset of all groups
	 */
	protected final NodeIterator listFilteredElements(Session session,
			String filter) throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(PeopleTypes.PEOPLE_USER_GROUP,
				PeopleTypes.PEOPLE_USER_GROUP);

		// Dynamically build constraint depending on the filter String
		Constraint defaultC = null;
		if (filter != null && !"".equals(filter.trim())) {
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

		if (displayUserChk != null && !displayUserChk.getSelection()) {
			Constraint constraint = factory.propertyExistence(
					source.getSelectorName(),
					PeopleNames.PEOPLE_IS_SINGLE_USER_GROUP);
			constraint = factory.not(constraint);
			if (defaultC == null)
				defaultC = constraint;
			else
				defaultC = factory.and(defaultC, constraint);
		}

		Ordering order = factory.ascending(factory.propertyValue(
				source.getSelectorName(), Property.JCR_TITLE));
		Ordering[] orderings = { order };

		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);

		QueryResult result = query.execute();
		return result.getNodes();
	}
}