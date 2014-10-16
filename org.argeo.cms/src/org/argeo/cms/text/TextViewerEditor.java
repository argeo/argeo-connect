package org.argeo.cms.text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

public class TextViewerEditor extends ColumnViewerEditor {
	private static final long serialVersionUID = 2365418608344285437L;

	private final static Log log = LogFactory.getLog(TextViewerEditor.class);

	public TextViewerEditor(ColumnViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		super(viewer, editorActivationStrategy, feature);
	}

	@Override
	protected void setEditor(Control w, Item item, int fColumnNumber) {
		log.debug("setEditor on " + w + ", for " + item);

	}

	@Override
	protected void setLayoutData(LayoutData layoutData) {
		log.debug("setLayoutData " + layoutData);
	}

	@Override
	protected void updateFocusCell(ViewerCell focusCell,
			ColumnViewerEditorActivationEvent event) {
		
		log.debug("updateFocusCell " + focusCell + ", event " + event);
	}
}
