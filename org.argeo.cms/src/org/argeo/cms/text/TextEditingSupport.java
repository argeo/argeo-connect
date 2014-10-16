package org.argeo.cms.text;

import javax.jcr.Node;

import org.argeo.cms.viewers.CompositeItem;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

public class TextEditingSupport extends EditingSupport {
	private static final long serialVersionUID = -5224731756574839664L;

	private TextInterpreter textInterpreter;

	private TextCellEditor cellEditor;

	public TextEditingSupport(ColumnViewer viewer,
			TextInterpreter textInterpreter) {
		super(viewer);
		this.textInterpreter = textInterpreter;
		cellEditor = new TextCellEditor();
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return textInterpreter.read((Node) element);
	}

	@Override
	protected void setValue(Object element, Object value) {
		textInterpreter.write((Node) element, value.toString());
	}

	@Override
	protected void initializeCellEditorValue(CellEditor cellEditor,
			ViewerCell cell) {
		CompositeItem item = (CompositeItem) cell.getItem();
		// for (Control child : item.getComposite().getChildren())
		// child.dispose();

		cellEditor.setStyle(SWT.MULTI | SWT.WRAP);
		cellEditor.create(item.getComposite());
		Text text = (Text) cellEditor.getControl();
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.setText("COUCOU");
		text.setVisible(true);
		text.moveAbove(null);
		item.getComposite().getParent().layout();
		super.initializeCellEditorValue(cellEditor, cell);
	}

	@Override
	protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		super.saveCellEditorValue(cellEditor, cell);
		CompositeItem item = (CompositeItem) cell.getItem();
		cellEditor.dispose();
		item.getComposite().layout();
		getViewer().refresh(cell.getElement());
	}

}
