package org.argeo.connect.people.rap.composites;

import java.util.List;

import javax.jcr.query.Row;

import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/** Utility class that wraps a virtual table viewer to display JCR rows */
public class VirtualRowTableViewer extends Composite implements ArgeoNames {
	private static final long serialVersionUID = 1L;
	private TableViewer viewer;

	private List<PeopleColumnDefinition> colDefs;
	private int tableStyle;

	private boolean hasCheckBoxes = false;

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
		GridLayout layout = PeopleUiUtils.noSpaceGridLayout();
		this.setLayout(layout);
		createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(viewer);
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return viewer;
	}

	private void createTableViewer(final Composite parent) {
		// Creates objects

		int swtStyle = tableStyle | SWT.VIRTUAL;

		if (hasCheckBoxes)
			swtStyle = swtStyle | SWT.CHECK;

		Table table = new Table(parent, swtStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(PeopleUiUtils.fillGridData());
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		if (hasCheckBoxes)
			viewer = new CheckboxTableViewer(table);
		else
			viewer = new TableViewer(table);

		// Create columns
		TableViewerColumn column;
		for (PeopleColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(viewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(colDef.getColumnLabelProvider());
		}

		viewer.setContentProvider(new MyLazyContentProvider(viewer));
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Row[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);

			this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
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
}