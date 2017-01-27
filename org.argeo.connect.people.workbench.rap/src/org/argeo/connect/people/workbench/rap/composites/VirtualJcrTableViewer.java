package org.argeo.connect.people.workbench.rap.composites;

import java.util.ArrayList;
import java.util.List;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.workbench.rap.PeopleRapImages;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
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

/**
 * Utility class that wraps a virtual table viewer to display JCR rows or nodes
 */
public class VirtualJcrTableViewer extends Composite {
	// private final static Log log = LogFactory
	// .getLog(VirtualJcrTableViewer.class);

	private static final long serialVersionUID = 1L;
	private TableViewer viewer;

	private List<PeopleColumnDefinition> colDefs;
	private int tableStyle;

	// Management of displayed row and selection
	private Object[] elements;
	// Work around : keep a flag to know which elements have already been loaded
	// or not
	private boolean[] loadedFlags;

	private List<Object> selectedElements = new ArrayList<Object>();
	private boolean hasCheckBoxes = false;

	private MyLazyContentProvider lazyContentProvider;

	public List<PeopleColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	// CONSTRUCTOR
	public VirtualJcrTableViewer(Composite parent, int style, List<PeopleColumnDefinition> columns) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.colDefs = columns;
		populate();
	}

	public VirtualJcrTableViewer(Composite parent, int style, List<PeopleColumnDefinition> columns,
			boolean addCheckBoxes) {
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
		// GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
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
			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 20);
			column.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				public String getText(Object element) {
					return null;
				}

				public Image getImage(Object element) {
					if (selectedElements.contains(element)) {
						return PeopleRapImages.CHECK_SELECTED;
					} else {
						return PeopleRapImages.CHECK_UNSELECTED;
					}
				}
			});
			column.setEditingSupport(new SelectionEditingSupport());
			tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(20, 24, true));
		}

		for (PeopleColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer, colDef.getHeaderLabel(), SWT.NONE,
					colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
			tableColumnLayout.setColumnData(column.getColumn(),
					new ColumnWeightData(colDef.getColumnSize(), colDef.getColumnSize(), true));
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
			elements = (Object[]) newInput;
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
			return selectedElements.contains(element);
		}

		@Override
		protected void setValue(Object element, Object value) {
			Boolean selected = (Boolean) value;
			if (selected && !selectedElements.contains(element)) {
				selectedElements.add(element);
			} else if (!selected && selectedElements.contains(element)) {
				selectedElements.remove(element);
			}
		}
	}

	public void setAllChecked(boolean checked) {
		selectedElements.clear();
		if (checked)
			for (Object currEl : elements) {
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

	public Object[] getSelectedElements() {
		return selectedElements.toArray();
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
