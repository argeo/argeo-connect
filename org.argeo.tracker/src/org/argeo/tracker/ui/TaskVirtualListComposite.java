package org.argeo.tracker.ui;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.workbench.util.JcrViewerDClickListener;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/** Canonical task list composite */
public class TaskVirtualListComposite extends Composite {
	private static final long serialVersionUID = 7277540413496825697L;

	private TableViewer tableViewer;
	private final int rowHeight;

	public TaskVirtualListComposite(Composite parent, int style, ILabelProvider labelProvider, int rowHeight) {
		super(parent, style);
		this.rowHeight = rowHeight;
		tableViewer = createListPart(this, labelProvider);
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
//		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
//		Composite tableComposite = new Composite(parent, SWT.NO_FOCUS);
//		tableComposite.setLayoutData(EclipseUiUtils.fillAll());

		int style = SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL;
		final TableViewer v = new TableViewer(parent, style);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		parent.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, rowHeight);

		v.setContentProvider(new ILazyContentProvider() {

			private static final long serialVersionUID = -3133493667354601923L;
			private Object[] elements;

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// IMPORTANT: don't forget this: an exception will be thrown if
				// a selected object is not part of the results anymore.
				viewer.setSelection(null);
				elements = (Object[]) newInput;
				int count = newInput != null ? elements.length : 0;
				((TableViewer) viewer).setItemCount(count);
				viewer.refresh();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void updateElement(int index) {
				v.replace(elements[index], index);
			}
		});
		v.addDoubleClickListener(new JcrViewerDClickListener());
		return v;
	}
}
