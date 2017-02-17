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
package org.argeo.connect.people.workbench.rap.dialogs;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.composites.FilterEntitiesVirtualTable;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to add reference to some related business
 * entity(ies)
 */
public class PickUpRelatedDialog extends TrayDialog {
	private static final long serialVersionUID = -2526572299370624808L;

	// Context
	private final Session session;
	private final AppWorkbenchService peopleWorkbenchService;
	private Node selectedNode;

	// this page widgets and UI objects
	private FilterEntitiesVirtualTable tableCmp;
	private final String title;

	// draft workaround to prevent window close when the user presses return
	private Button dummyButton;

	public PickUpRelatedDialog(Shell parentShell, String title, Session session,
			AppWorkbenchService peopleWorkbenchService, Node referencingNode) {
		super(parentShell);
		this.title = title;
		// this.referencingNode = referencingNode;
		this.session = session;
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	protected Point getInitialSize() {
		return new Point(400, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		// final Button seeAllChk = new Button(dialogArea, SWT.CHECK);
		// seeAllChk.setText("See all organisation");

		Composite bodyCmp = new Composite(dialogArea, SWT.NO_FOCUS);
		bodyCmp.setLayoutData(EclipseUiUtils.fillAll());
		bodyCmp.setLayout(new GridLayout());

		int style = SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
		tableCmp = new FilterEntitiesVirtualTable(bodyCmp, style, session, peopleWorkbenchService,
				PeopleTypes.PEOPLE_ENTITY);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(new MySelectionChangedListener());

		// draft workaround to prevent window close when the user presses return
		dummyButton = new Button(dialogArea, SWT.PUSH);
		dummyButton.setVisible(false);

		parent.pack();
		return dialogArea;
	}

	public Node getSelected() {
		return selectedNode;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	public void create() {
		super.create();
		getShell().setDefaultButton(dummyButton);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
				okPressed();
			}
		}
	}
}