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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.composites.FilterEntitiesVirtualTable;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
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

	// draft workaround to prevent window close when the user presses return
	private Button dummyButton;

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
		return new Point(400, 615);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite main = new Composite(dialogArea, SWT.NO_FOCUS);
		main.setLayout(EclipseUiUtils.noSpaceGridLayout());
		main.setLayoutData(EclipseUiUtils.fillAll());
		
		int style = SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER;
		tableCmp = new MyFilterEntitiesVirtualTable(main, style, session,
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

		// draft workaround to prevent window close when the user presses return
		dummyButton = new Button(main, SWT.PUSH);
		dummyButton.setLayoutData(new GridData(1, 1));

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

	@Override
	public void create() {
		super.create();
		dummyButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7900611671119542857L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				// Do nothing, rather than call ok pressed
			}
		});
		Shell shell = getShell();
		shell.setDefaultButton(dummyButton);
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

	/** Overwrite and return true to provide specific filtering */
	protected boolean defineSpecificQuery() {
		return false;
	}

	/** Overwrite to provide specific filtering */
	protected NodeIterator query(Session session, String filter)
			throws RepositoryException {
		return null;
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

		protected NodeIterator listFilteredElements(Session session,
				String filter) throws RepositoryException {
			if (defineSpecificQuery())
				return query(session, filter);
			else
				return super.listFilteredElements(session, filter);
		}

		protected List<PeopleColumnDefinition> getColumnsDef() {
			if (colDefs == null)
				return super.getColumnsDef();
			else
				return colDefs;
		}
	}
}