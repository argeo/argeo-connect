package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.query.Row;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/** Utility class that wraps a virtual table viewer to display JCR rows */
public class VirtualRowTableViewer extends Composite implements ArgeoNames {
	// private final static Log log = LogFactory
	// .getLog(VirtualRowTableViewer.class);

	private static final long serialVersionUID = 1L;
	private TableViewer viewer;

	private List<PeopleColumnDefinition> colDefs;
	private int tableStyle;

	// Management of displayed row and selection
	private Row[] elements;
	// Work around : keep a flag to know which elements have already been loade
	// or not
	private boolean[] loadedFlags;

	private List<Row> selectedElements = new ArrayList<Row>();
	private boolean hasCheckBoxes = false;

	private MyLazyContentProvider lazyContentProvider;

	public List<PeopleColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTOR
	public VirtualRowTableViewer(Composite parent, int style,
			List<PeopleColumnDefinition> columns) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.colDefs = columns;
		populate();
	}

	public VirtualRowTableViewer(Composite parent, int style,
			List<PeopleColumnDefinition> columns, boolean addCheckBoxes) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.colDefs = columns;
		hasCheckBoxes = addCheckBoxes;
		populate();
	}

	protected void populate() {
		// initialization
		Composite parent = this;
		// Main Layout
		// GridLayout layout = PeopleUiUtils.noSpaceGridLayout();
		// this.setLayout(layout);
		createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(viewer);
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return viewer;
	}

	private void createTableViewer(final Composite parent) {
		int swtStyle = tableStyle | SWT.VIRTUAL;
		Table table = new Table(parent, swtStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		CmsUtils.markup(table);

		TableViewerColumn column;
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		viewer = new TableViewer(table);

		if (hasCheckBoxes) {
			// check column
			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
					20);
			column.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				public String getText(Object element) {
					return null;
				}

				public Image getImage(Object element) {
					Row row = (Row) element;
					if (selectedElements.contains(row)) {
						return PeopleRapImages.CHECK_SELECTED;
					} else {
						return PeopleRapImages.CHECK_UNSELECTED;
					}
				}
			});
			column.setEditingSupport(new SelectionEditingSupport());
			tableColumnLayout.setColumnData(column.getColumn(),
					new ColumnWeightData(20, 20, true));
		}

		for (PeopleColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
			tableColumnLayout.setColumnData(
					column.getColumn(),
					new ColumnWeightData(colDef.getColumnSize(), colDef
							.getColumnSize(), true));
		}

		lazyContentProvider = new MyLazyContentProvider(viewer);
		viewer.setContentProvider(lazyContentProvider);
		parent.setLayout(tableColumnLayout);
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			selectedElements.clear();
			elements = (Row[]) newInput;
			loadedFlags = new boolean[elements == null ? 0 : elements.length];
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
			loadedFlags[index] = true;
		}
	}

	/** Select the columns by editing the checkbox in the first column */
	class SelectionEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 398089475969012249L;

		public SelectionEditingSupport() {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			Row row = (Row) element;
			return selectedElements.contains(row);
		}

		@Override
		protected void setValue(Object element, Object value) {
			Boolean selected = (Boolean) value;
			Row row = (Row) element;
			if (selected && !selectedElements.contains(row)) {
				selectedElements.add(row);
			} else if (!selected && selectedElements.contains(row)) {
				selectedElements.remove(row);
			}
		}
	}

	public void setAllChecked(boolean checked) {
		selectedElements.clear();
		if (checked)
			for (Row currEl : elements) {
				selectedElements.add(currEl);
			}
		// Cannot violently refresh. loop issues
		// refresh();
		// workaround
		int count = viewer.getTable().getItemCount();
		for (int i = 0; i < count; i++)
			if (loadedFlags[i])
				lazyContentProvider.updateElement(i);
	}

	public Row[] getSelectedElements() {
		return selectedElements.toArray(new Row[0]);
	}

	public void refresh() {
		viewer.refresh();
	}

	@Override
	public boolean setFocus() {
		viewer.getTable().setFocus();
		return true;
	}
}