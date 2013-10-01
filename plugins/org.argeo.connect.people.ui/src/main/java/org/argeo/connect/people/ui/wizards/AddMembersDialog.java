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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.toolkits.MailingListToolkit;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
 * Dialog with a filtered list to add some members in a mailing list
 */
public class AddMembersDialog extends TrayDialog {
	private static final long serialVersionUID = 5641280645351822123L;

	// this page widgets and UI objects
	private TableViewer entityViewer;
	private final String title;
	private MailingListToolkit mlToolkit;

	// business objects
	private final List<Row> selectedItems = new ArrayList<Row>();
	private Session session;
	private Node referencingNode;
	private String[] toSearchNodeTypes;
	private PeopleService peopleService;

	public AddMembersDialog(Shell parentShell, String title,
			PeopleService peopleService, Node referencingNode,
			String[] toSearchNodeTypes) {
		super(parentShell);
		this.mlToolkit = new MailingListToolkit();
		this.title = title;
		this.peopleService = peopleService;
		this.referencingNode = referencingNode;
		this.toSearchNodeTypes = toSearchNodeTypes;
		session = CommonsJcrUtils.login(peopleService.getRepository());
	}

	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add the filtered / selectable list
		entityViewer = mlToolkit.createControl(dialogarea, selectedItems);
		entityViewer.setContentProvider(new MyContentProvider());
		// entityViewer
		// .addDoubleClickListener(new mlToolkit.AddDoubleClickListener() {
		// @Override
		// protected void add(List<Node> nodes) {
		// // String defaultMail = PeopleJcrUtils
		// // .getDefaultContactValue(person,
		// // PeopleTypes.PEOPLE_EMAIL);
		// // if (CommonsJcrUtils.isEmptyString(defaultMail))
		// // skippedPerson
		// // .append(PersonJcrUtils
		// // .getPersonDisplayName(person))
		// // .append("; ");
		// // else {
		// // // Node createdNode =
		// // peopleService.createEntityReference(
		// // referencingNode, person, defaultMail);
		// // }
		// }
		// });
		parent.pack();
		// initialize with no filter
		entityViewer.setInput("");
		return dialogarea;
	}

	protected boolean performFinish() {
		StringBuilder skippedPerson = new StringBuilder();

		for (Row personRow : selectedItems) {
			Node person;
			try {
				person = personRow.getNode(PeopleTypes.PEOPLE_PERSON);
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to get person node from row",
						e);
			}
			String defaultMail = PeopleJcrUtils.getDefaultContactValue(person,
					PeopleTypes.PEOPLE_EMAIL);
			if (CommonsJcrUtils.isEmptyString(defaultMail))
				skippedPerson.append(
						PersonJcrUtils.getPersonDisplayName(person)).append(
						"; ");
			else {
				// Node createdNode =
				peopleService.createEntityReference(referencingNode, person,
						defaultMail);
			}
		}

		if (skippedPerson.length() > 0) {
			skippedPerson.substring(0, skippedPerson.length() - 2);
			String msg = "Following persons have no defined mail adress, "
					+ "they could not be added: ";

			MessageDialog.openError(getShell(), "Non valid information", msg
					+ skippedPerson.toString());
		}
		return true;
	}

	@Override
	protected void okPressed() {
		if (performFinish())
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
			QueryObjectModel query;
			query = factory.createQuery(source, defaultC, null, null);
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
