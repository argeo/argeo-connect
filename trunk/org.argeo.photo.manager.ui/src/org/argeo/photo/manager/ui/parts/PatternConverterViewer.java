package org.argeo.photo.manager.ui.parts;

import org.argeo.photo.manager.PatternConverter;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class PatternConverterViewer {
	private TableViewer viewer;

	private ScrolledComposite scroll;

	public PatternConverterViewer(Composite parent) {
		// Scroll
		scroll = new ScrolledComposite(parent, SWT.BORDER);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinSize(300, 100);

		viewer = new TableViewer(scroll, SWT.MULTI | SWT.V_SCROLL);
		final Table table = viewer.getTable();
		scroll.setContent(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableViewerColumn colFrom = new TableViewerColumn(viewer, SWT.LEFT);
		// TableColumn colFrom = new TableColumn(table, SWT.LEFT);
		colFrom.getColumn().setText("From");
		colFrom.getColumn().setWidth(300);

		TableViewerColumn colTo = new TableViewerColumn(viewer, SWT.LEFT);
		// TableColumn colTo = new TableColumn(table, SWT.LEFT);
		colTo.getColumn().setText("To");
		colTo.getColumn().setWidth(300);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		colFrom.setEditingSupport(new EditingSupport(viewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(table);
			}

			protected Object getValue(Object element) {
				return ((PatternConverter.Patterns) element).getFromPattern();
			}

			protected void setValue(Object element, Object value) {
				((PatternConverter.Patterns) element).setFromPattern(value
						.toString());
				getViewer().update(element, null);
			}
		});

		colTo.setEditingSupport(new EditingSupport(viewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(table);
			}

			protected Object getValue(Object element) {
				return ((PatternConverter.Patterns) element).getToPattern();
			}

			protected void setValue(Object element, Object value) {
				((PatternConverter.Patterns) element).setToPattern(value
						.toString());
				getViewer().update(element, null);
			}
		});
		//
		// // Create the cell editors
		// CellEditor[] editors = new CellEditor[2];
		//
		// {
		// // Column 1 : From Pattern
		// TextCellEditor textEditor = new TextCellEditor(table);
		// ((Text) textEditor.getControl()).setTextLimit(60);
		// editors[0] = textEditor;
		// }
		//
		// {
		// // Column 2 : Description (Free text)
		// TextCellEditor textEditor = new TextCellEditor(table);
		// ((Text) textEditor.getControl()).setTextLimit(60);
		// editors[1] = textEditor;
		// }
		// viewer.setCellEditors(editors);
		// viewer.setCellModifier(this);
	}

	public void setLayoutData(Object layoutData) {
		scroll.setLayoutData(layoutData);
	}

	public void setInput(PatternConverter obj) {
		viewer.setInput(obj);
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return ((PatternConverter) inputElement).getConversionPatterns()
					.toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			PatternConverter.Patterns patterns = (PatternConverter.Patterns) obj;
			if (index == 0) {
				return patterns.getFromPattern();
			} else if (index == 1) {
				return patterns.getToPattern();
			} else {
				return null;
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

	}

}
