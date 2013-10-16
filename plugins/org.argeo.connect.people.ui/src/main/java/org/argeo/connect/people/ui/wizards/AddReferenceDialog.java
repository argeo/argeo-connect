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
package org.argeo.connect.people.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.toolkits.MailingListToolkit;
import org.argeo.connect.people.ui.utils.ColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Parent for a dialog with a filtered list to add some references
 */
public abstract class AddReferenceDialog extends TrayDialog {

	private static final long serialVersionUID = 8032393989247669006L;
	// this page widgets and UI objects
	private TableViewer entityViewer;
	private final String title;
	private MailingListToolkit mlToolkit;

	// business objects
	private List<Row> selectedItems = new ArrayList<Row>();
	private Session session;
	private String[] toSearchNodeTypes;

	public AddReferenceDialog(Shell parentShell, String title,
			PeopleService peopleService, String[] toSearchNodeTypes) {
		super(parentShell);
		this.mlToolkit = new MailingListToolkit();
		this.title = title;
		this.toSearchNodeTypes = toSearchNodeTypes;
		session = CommonsJcrUtils.login(peopleService.getRepository());
	}

	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add the filtered / selectable list
		entityViewer = mlToolkit.createItemsViewerWithCheckBox(dialogArea,
				selectedItems, getColumnsDef());
		entityViewer.setContentProvider(new MyContentProvider());
		entityViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				if (obj instanceof Row) {
					List<Row> rows = new ArrayList<Row>();
					rows.add((Row) obj);
					performAddition(rows);
				}
			}
		});

		parent.pack();

		// initialize with no filter
		entityViewer.setInput("");
		return dialogArea;
	}

	protected abstract List<ColumnDefinition> getColumnsDef();

	protected abstract boolean performAddition(List<Row> items);

	private List<Row> getSelectedItems() {
		return selectedItems;
	}

	@Override
	protected void okPressed() {
		if (performAddition(getSelectedItems()))
			super.okPressed();
	}

	/** Creates label and text. */
	protected Text createLT(Composite parent, String label) {
		new Label(parent, SWT.NONE).setText(label);
		Text text = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return text;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	protected RowIterator refreshFilteredList(String filter, String nodeType) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(nodeType, nodeType);

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*"));
				defaultC = factory.fullTextSearch(source.getSelectorName(),
						null, so);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}

			// Order by default by JCR TITLE
			// TODO check if node definition has MIX_TITLE mixin
			// TODO Apparently case insensitive ordering is not implemented in
			// current used JCR implementation
			Ordering order = factory
					.ascending(factory.upperCase(factory.propertyValue(
							source.getSelectorName(), Property.JCR_TITLE)));
			QueryObjectModel query;
			query = factory.createQuery(source, defaultC,
					new Ordering[] { order }, null);
			query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);

			QueryResult result = query.execute();
			return result.getRows();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

	/** Overwrite to close session */
	public boolean close() {
		JcrUtils.logoutQuietly(session);
		return super.close();
	}

	/**
	 * Specific content provider for this Part
	 */
	private class MyContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;
		private String filter;

		public void dispose() {
		}

		/** Expects a filter text as a new input */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			filter = (String) newInput;
			if (newInput != null)
				viewer.refresh();
		}

		public Object[] getElements(Object arg0) {
			// TODO support multiple node types.
			RowIterator ri = refreshFilteredList(filter, toSearchNodeTypes[0]);
			// FIXME will not work for big resultset
			Object[] result = new Object[(int) ri.getSize()];
			int i = 0;
			while (ri.hasNext()) {
				result[i] = ri.nextRow();
				i++;
			}
			return result;
		}
	}
}