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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.FilterEntitiesVirtualTable;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to add reference to some "business" jcr Node.
 * Choosable nodes are all nodes of the NodeType that is given upon creation.
 * This nodeType must inherit from people:base nodeType, so that the created
 * table finds information it expects to display in the table.
 */
public class PickUpByNodeTypeDialog extends TrayDialog {
	private static final long serialVersionUID = -2526572299370624808L;

	// Business objects
	private final Session session;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final String nodeType;
	private Node selectedNode;
	private List<PeopleColumnDefinition> colDefs;

	// this page widgets and UI objects
	private FilterEntitiesVirtualTable tableCmp;
	private final String title;

	public PickUpByNodeTypeDialog(Shell parentShell, String title,
			Session session, PeopleWorkbenchService peopleWorkbenchService,
			String nodeType) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.nodeType = nodeType;
	}

	public PickUpByNodeTypeDialog(Shell parentShell, String title,
			Session session, PeopleWorkbenchService peopleWorkbenchService,
			String nodeType, List<PeopleColumnDefinition> colDefs) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.nodeType = nodeType;
		this.colDefs = colDefs;
	}

	protected Point getInitialSize() {
		return new Point(400, 605);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		int style = SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER;
		tableCmp = new MyFilterEntitiesVirtualTable(dialogArea, style, session,
				peopleWorkbenchService, nodeType, colDefs);
		GridData gd = EclipseUiUtils.fillAll();
		tableCmp.setLayoutData(gd);

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(
				new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(
				new MySelectionChangedListener());

		tableCmp.getTableViewer().getTable().pack();
		tableCmp.getTableViewer().getTable().layout();
		tableCmp.layout();
		// dialogArea.pack();

		dialogArea.layout();
		return dialogArea;
	}

	public Node getSelected() {
		return selectedNode;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
				okPressed();
			}
		}
	}

	// Add the ability to provide a business specific label provider for the
	// given entity type
	private class MyFilterEntitiesVirtualTable extends
			FilterEntitiesVirtualTable {
		private static final long serialVersionUID = 3122449385321832511L;
		private List<PeopleColumnDefinition> colDefs;

		public MyFilterEntitiesVirtualTable(Composite parent, int style,
				Session session, PeopleWorkbenchService peopleWorkbenchService,
				String nodeType) {
			super(parent, style, session, peopleWorkbenchService, nodeType);
		}

		public MyFilterEntitiesVirtualTable(Composite parent, int style,
				Session session, PeopleWorkbenchService peopleWorkbenchService,
				String nodeType, List<PeopleColumnDefinition> colDefs) {
			super(parent, style, session, peopleWorkbenchService, nodeType,
					true);
			this.colDefs = colDefs;
			populate();
		}

		protected List<PeopleColumnDefinition> getColumnsDef() {
			if (colDefs == null)
				return super.getColumnsDef();
			else
				return colDefs;
		}
	}
}