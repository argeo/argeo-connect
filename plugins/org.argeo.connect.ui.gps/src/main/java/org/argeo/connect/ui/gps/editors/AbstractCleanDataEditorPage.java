package org.argeo.connect.ui.gps.editors;

import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.ConnectUiPlugin;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public abstract class AbstractCleanDataEditorPage extends FormPage implements
		ConnectNames, ConnectTypes, ConnectGpsLabels {

	// Images
	protected final static Image CHECKED = ConnectUiPlugin.getImageDescriptor(
			"icons/checked.gif").createImage();
	protected final static Image UNCHECKED = ConnectUiPlugin
			.getImageDescriptor("icons/unchecked.gif").createImage();

	public AbstractCleanDataEditorPage(FormEditor editor, String id,
			String title) {
		super(editor, id, title);
		// TODO Auto-generated constructor stub
	}

	public CleanDataEditor getEditor() {
		return (CleanDataEditor) super.getEditor();
	}

	/** Factorize the creation of table columns */
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
		try {
			// Cannot edit a completed session
			return getEditor().getCurrentSessionNode()
					.getProperty(CONNECT_IS_COMPLETE).getBoolean();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot access node to see if it has already been imported.");
		}
	}

}
