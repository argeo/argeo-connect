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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
import org.argeo.connect.people.workbench.rap.PeopleRapConstants;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.workbench.rap.providers.TitleIconRowLP;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog with a filtered list to add some members in a mailing list
 */
public class PickUpContactableDialog extends TrayDialog {
	private static final long serialVersionUID = -2526572299370624808L;

	// Business objects
	private final Session session;
	private Node selectedNode;
	private String nodeType;

	// this page widgets and UI objects
	// private EntityTableComposite tableCmp;
	private final String title;

	private List<PeopleColumnDefinition> colDefs;
	private Text filterTxt;
	private TableViewer tableViewer;

	public PickUpContactableDialog(Shell parentShell, String title,
			Session session, PeopleWorkbenchService peopleUiService,
			String nodeType) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.nodeType = nodeType;

		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition("Display Name",
				new TitleIconRowLP(peopleUiService, null, Property.JCR_TITLE),
				300));
	}

	protected Point getInitialSize() {
		return new Point(400, 650);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		createFilterPart(dialogArea);
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(dialogArea,
				SWT.SINGLE | SWT.BORDER, colDefs);
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		tableViewer.addDoubleClickListener(new MyDoubleClickListener());
		tableViewer
				.addSelectionChangedListener(new MySelectionChangedListener());
		parent.pack();
		refreshFilteredList();
		filterTxt.setFocus();
		return dialogArea;
	}

	public Node getSelected() {
		return selectedNode;
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		tableViewer.setInput(rows);
		// we must explicitly set the items count
		tableViewer.setItemCount(rows.length);
		tableViewer.refresh();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty()) {
				// selectedNode = null;
				return;
			}

			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof Row) {
				selectedNode = JcrUiUtils.getNode((Row) obj, null);
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof Row) {
				JcrUiUtils.getNode((Row) obj, null);
				okPressed();
			}
		}
	}

	private void createFilterPart(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			String xpathQueryStr = "//element(*, " + nodeType + ")";
			String attrQuery = XPathUtils.getFreeTextConstraint(filterTxt
					.getText());
			if (EclipseUiUtils.notEmpty(attrQuery))
				xpathQueryStr += "[" + attrQuery + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr,
					PeopleConstants.QUERY_XPATH);
			QueryResult result = xpathQuery.execute();

			// QueryManager queryManager = session.getWorkspace()
			// .getQueryManager();
			// QueryObjectModelFactory factory = queryManager.getQOMFactory();
			// Selector source = factory.selector(nodeType, nodeType);
			//
			// Constraint defaultC = JcrUiUtils.getFreeTextConstraint(
			// session, factory, source, filterTxt.getText());
			//
			// Ordering order = factory.ascending(factory.propertyValue(
			// source.getSelectorName(), Property.JCR_TITLE));
			// Ordering[] orderings = { order };
			// QueryObjectModel query = factory.createQuery(source, defaultC,
			// orderings, null);
			// // TODO rather implement a virtual viewer
			// query.setLimit(100);
			// QueryResult result = query.execute();
			Row[] rows = JcrUiUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + nodeType
					+ " entities", e);
		}
	}
}