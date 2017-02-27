package org.argeo.people.workbench.rap.composites;

import java.util.List;

import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/** Utility class that wraps a table viewer to display JCR rows */
public class PeopleTableViewer extends Composite {
	private static final long serialVersionUID = 1L;
	private TableViewer viewer;
	private MyViewerComparator comparator;

	private List<ConnectColumnDefinition> colDefs;
	private int tableStyle;

	public List<ConnectColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTOR
	public PeopleTableViewer(Composite parent, int style,
			List<ConnectColumnDefinition> columns) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.colDefs = columns;
		populate();
	}

	protected void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		this.setLayout(layout);
		createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(viewer);
		viewer.setContentProvider(new MyTableContentProvider());
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return viewer;
	}

	private void createTableViewer(final Composite parent) {
		// Creates objects
		Table table = new Table(parent, tableStyle);
		viewer = new TableViewer(table);
		comparator = new MyViewerComparator();

		// Configure
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create columns
		TableViewerColumn column;
		int i = 0;
		for (ConnectColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
			column.getColumn().addSelectionListener(
					getSelectionAdapter(column.getColumn(), i));
			i++;
		}

		viewer.setComparator(comparator);
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
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

	/* Add sort abilities */
	private class MyViewerComparator extends ViewerComparator {
		private static final long serialVersionUID = 1L;

		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = 0;

		public MyViewerComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			ColumnLabelProvider labelProvider = colDefs.get(propertyIndex)
					.getColumnLabelProvider();
			int rc = labelProvider.getText(e1).compareTo(
					labelProvider.getText(e2));

			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}

	public void refresh() {
		viewer.refresh();
	}

	@Override
	public boolean setFocus() {
		viewer.getTable().setFocus();
		return true;
	}

	// @Override
	// public void dispose() {
	// super.dispose();
	// }
}
