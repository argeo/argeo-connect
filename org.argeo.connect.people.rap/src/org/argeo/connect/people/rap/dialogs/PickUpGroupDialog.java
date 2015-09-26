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
package org.argeo.connect.people.rap.dialogs;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.composites.GroupsTableViewer;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.useradmin.Group;

/** Dialog with a group list to pick up one */
public class PickUpGroupDialog extends TrayDialog {
	private static final long serialVersionUID = -1420106871173920369L;

	// Business objects
	private final PeopleService peopleService;
	private Group selectedGroup;

	// this page widgets and UI objects
	private GroupsTableViewer tableCmp;
	private String title;

	public PickUpGroupDialog(Shell parentShell, String title,
			PeopleService peopleService) {
		super(parentShell);
		this.title = title;
		this.peopleService = peopleService;
	}

	protected Point getInitialSize() {
		return new Point(400, 500);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new FillLayout());

		Composite bodyCmp = new Composite(dialogArea, SWT.NO_FOCUS);
		bodyCmp.setLayout(new GridLayout());

		tableCmp = new GroupsTableViewer(bodyCmp, SWT.NO_FOCUS,
				peopleService.getUserAdminService());
		tableCmp.populate(true, false);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		tableCmp.refresh();

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(
				new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(
				new MySelectionChangedListener());

		parent.pack();
		return dialogArea;
	}

	public String getSelected() {
		if (selectedGroup == null)
			return null;
		else
			return selectedGroup.getName();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof Group) {
				selectedGroup = (Group) obj;
				okPressed();
			}
		}
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty()) {
				selectedGroup = null;
				return;
			}
			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof Group) {
				selectedGroup = (Group) obj;
			}
		}
	}
}