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
import javax.jcr.PropertyType;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.composites.SimpleJcrTableComposite;
import org.argeo.connect.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to choose a country
 */
public class PickUpCountryDialog extends TrayDialog {
	private static final long serialVersionUID = 3766899676609659573L;

	// Business objects
	private PeopleService peopleService;
	private final Session session;
	private Node selectedNode;

	// this page widgets and UI objects
	private SimpleJcrTableComposite tableCmp;
	private final String title;

	private List<JcrColumnDefinition> colDefs = new ArrayList<JcrColumnDefinition>();
	{ // By default, it displays only title
		colDefs.add(new JcrColumnDefinition(null, PeopleNames.PEOPLE_CODE,
				PropertyType.STRING, "Iso Code", 100));
		colDefs.add(new JcrColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Label", 240));
	};

	public PickUpCountryDialog(Shell parentShell, PeopleService peopleService,
			Session session, String title) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.peopleService = peopleService;
	}

	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Node tagParent = peopleService.getResourceService()
				.getTagLikeResourceParent(session,
						PeopleConstants.RESOURCE_COUNTRY);

		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
		tableCmp = new SimpleJcrTableComposite(dialogArea, style, session,
				JcrUiUtils.getPath(tagParent),
				PeopleTypes.PEOPLE_TAG_ENCODED_INSTANCE, colDefs, true, false);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(
				new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(
				new MySelectionChangedListener());

		parent.pack();
		return dialogArea;
	}

	public String getSelected() {
		if (selectedNode != null)
			return JcrUiUtils.get(selectedNode, PeopleNames.PEOPLE_CODE);
		else
			return null;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty()) {
				selectedNode = null;
				return;
			}

			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty()) {
				selectedNode = null;
				return;
			}

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
				okPressed();
			}
		}
	}
}