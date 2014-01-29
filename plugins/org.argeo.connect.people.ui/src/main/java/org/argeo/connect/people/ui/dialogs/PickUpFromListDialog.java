/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.ui.dialogs;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Simple dialog to pick up a string value from a list
 */
public class PickUpFromListDialog extends TrayDialog {
	private static final long serialVersionUID = 1L;

	// this page widgets and UI objects
	private TableViewer viewer;
	private final MyFilter viewerFilter;
	private final String title;

	// the displayed strings
	private final String[] elements;

	private String selectedValue;

	public PickUpFromListDialog(Shell parentShell, String title,
			String[] elements) {
		super(parentShell);
		this.title = title;
		this.elements = elements;
		viewerFilter = new MyFilter();
	}

	protected Point getInitialSize() {
		return new Point(250, 400);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new GridLayout());

		Text txtFilter = new Text(dialogArea, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		txtFilter
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtFilter.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				viewerFilter.setText(text.getText());
				viewer.refresh();
			}
		});

		createViewer(dialogArea);

		viewer.setInput("init");

		// Add listeners
		viewer.addDoubleClickListener(new MyDoubleClickListener());
		viewer.addSelectionChangedListener(new MySelectionChangedListener());

		parent.pack();
		return dialogArea;
	}

	private void createViewer(Composite parent) {
		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
		viewer = new TableViewer(parent, style);
		viewer.setContentProvider(new MyProvider());
		createColumn();
		viewer.addFilter(viewerFilter);
		viewer.getTable().setHeaderVisible(false);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(tableData);
	}

	private TableViewerColumn createColumn() {
		TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
		result.setLabelProvider(new ColumnLabelProvider());
		TableColumn column = result.getColumn();
		column.setWidth(170);
		column.setMoveable(true);
		return result;
	}

	private class MyProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

		public Object[] getElements(Object inputElement) {
			return PickUpFromListDialog.this.elements;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
			// do nothing
		}
	}

	public String getSelected() {
		return selectedValue;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	private static final class MyFilter extends ViewerFilter {
		private static final long serialVersionUID = 1L;
		private String text;

		public void setText(final String string) {
			text = string;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			boolean result = true;
			String person = (String) element;
			if (text != null && text.length() > 0) {
				String personText = person.toLowerCase();
				result = personText.indexOf(text.toLowerCase()) != -1;
			}
			return result;
		}

		@Override
		public boolean isFilterProperty(Object element, String prop) {
			return true;
		}
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty()) {
				selectedValue = "";
				return;
			}

			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof String) {
				selectedValue = (String) obj;
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty()) {
				selectedValue = "";
				return;
			}

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof String) {
				selectedValue = (String) obj;
			}
			okPressed();
		}
	}
}