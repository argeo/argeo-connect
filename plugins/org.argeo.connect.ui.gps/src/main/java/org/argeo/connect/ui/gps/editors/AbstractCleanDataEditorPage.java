package org.argeo.connect.ui.gps.editors;

import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public abstract class AbstractCleanDataEditorPage extends FormPage {

	// Images
	protected final static Image CHECKED = ConnectGpsUiPlugin
			.getImageDescriptor("icons/checked.gif").createImage();
	protected final static Image UNCHECKED = ConnectGpsUiPlugin
			.getImageDescriptor("icons/unchecked.gif").createImage();

	public AbstractCleanDataEditorPage(FormEditor editor, String id,
			String title) {
		super(editor, id, title);
	}

	public CleanDataEditor getEditor() {
		return (CleanDataEditor) super.getEditor();
	}

	/** Factorizes the creation of table columns */
	protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
			String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Check if the current session is already completed and thus not editable
	 * anymore
	 */
	protected boolean isSessionAlreadyComplete() {
		return getEditor().getUiJcrServices().isSessionComplete(
				getEditor().getCurrentCleanSession());
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		((CleanDataEditor) getEditor()).refreshReadOnlyState();
	}
}