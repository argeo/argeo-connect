/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.ui.gps.editors;

import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * Add specific GPS UI cleaning session methods to the base FormPage abstract
 * class.
 */
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