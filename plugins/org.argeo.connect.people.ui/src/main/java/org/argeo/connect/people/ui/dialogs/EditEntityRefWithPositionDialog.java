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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog with a filtered list to edit a named linked reference. Note that both
 * referenced and referencing entities must be given in order to eventually
 * remove old reference
 */
public class EditEntityRefWithPositionDialog extends TrayDialog {
	private static final long serialVersionUID = -3534660152626908662L;
	// The various field
	private Text positionTxt;
	private final String positionLbl = "Role";
	private Text selectedItemTxt;
	private Node selectedItem = null;
	private final String chosenItemLbl = "Chosen item";

	// The search list
	private Text filterTxt;
	private TableViewer entityViewer;

	private String value;
	private final String title;

	private Session session;
	private boolean isBackward;
	private String toSearchNodeType;
	private Node oldLinkNode;
	private PeopleService peopleService;

	// caches old info as a shortcut
	private String oldPosition;
	private Node oldReferencing;
	private Node oldReferenced;

	/**
	 * 
	 * @param parentShell
	 * @param title
	 * @param peopleService
	 * @param referencingNode
	 * @param referencedNode
	 * @param toSearchNodeType
	 * @param isBackward
	 *            tells if we must remove referenced (if true) or referencing
	 *            (if false) node
	 */
	public EditEntityRefWithPositionDialog(Shell parentShell, String title,
			PeopleService peopleService, Node oldLink, String toSearchNodeType,
			boolean isBackward) {
		super(parentShell);
		this.title = title;
		this.peopleService = peopleService;
		this.toSearchNodeType = toSearchNodeType;
		this.oldLinkNode = oldLink;
		this.isBackward = isBackward;

		// Try to initiallize our shortcuts
		try {
			session = CommonsJcrUtils.login(peopleService.getRepository());
			oldPosition = CommonsJcrUtils.get(oldLinkNode,
					PeopleNames.PEOPLE_ROLE);
			oldReferencing = oldLink.getParent().getParent();
			oldReferenced = peopleService.getEntityByUid(session, oldLink
					.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
		} catch (RepositoryException e) {
			throw new PeopleException("unable to initialize link edition", e);
		}

	}

	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogarea.setLayout(new GridLayout());

		// the text fields
		Composite textFieldsCmp = new Composite(dialogarea, SWT.NONE);
		textFieldsCmp.setLayout(new GridLayout(2, false));
		textFieldsCmp
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		positionTxt = createLT(textFieldsCmp, positionLbl);
		positionTxt.setText(oldPosition);

		selectedItemTxt = createLT(textFieldsCmp, chosenItemLbl);
		selectedItemTxt.setEnabled(false);

		if (isBackward) {
			selectedItemTxt.setText(CommonsJcrUtils.get(oldReferencing,
					Property.JCR_TITLE));
		} else
			selectedItemTxt.setText(CommonsJcrUtils.get(oldReferenced,
					Property.JCR_TITLE));

		// the filter
		Composite filterCmp = new Composite(dialogarea, SWT.NONE);
		filterCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		addFilterPanel(filterCmp);
		// the list
		Composite listCmp = new Composite(dialogarea, SWT.NONE);
		listCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		entityViewer = createListPart(listCmp,
				new EntitySingleColumnLabelProvider(peopleService));
		refreshFilteredList(toSearchNodeType);

		parent.pack();
		return dialogarea;
	}

	/**
	 * Override this to perform the real addition
	 * 
	 * @return
	 */
	protected boolean performFinish() {
		// String msg = null;

		// TODO clean check for edit use case
		// if (CommonsJcrUtils.isEmptyString(positionTxt.getText())
		// && CommonsJcrUtils.isEmptyString(oldPosition))
		// msg = "Please enter a role for current position.";
		// else if (selectedItem == null && )
		// msg = "Please select an entity.";

		// if (msg != null) {
		// MessageDialog.openError(getShell(), "Non valid information", msg);
		// return false;
		// } else {
		Node srcNode = null;
		Node targetNode = null;
		try {
			if (isBackward) {
				if (selectedItem == null)
					srcNode = oldReferencing;
				else
					srcNode = selectedItem;
				targetNode = oldReferenced;
				// First remove old entity reference
				boolean wasCheckedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(oldReferencing);
				if (!wasCheckedOut)
					CommonsJcrUtils.checkout(oldReferencing);
				oldLinkNode.remove();
				if (wasCheckedOut)
					oldReferencing.getSession().save();
				else
					CommonsJcrUtils.saveAndCheckin(oldReferencing);
				peopleService.createEntityReference(srcNode, targetNode,
						positionTxt.getText());
			} else {
				// just edit current link
				if (selectedItem == null)
					targetNode = oldReferenced;
				else
					targetNode = selectedItem;

				boolean wasCheckedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(oldReferencing);
				if (!wasCheckedOut)
					CommonsJcrUtils.checkout(oldReferencing);
				oldLinkNode.setProperty(PeopleNames.PEOPLE_ROLE,
						positionTxt.getText());
				oldLinkNode.setProperty(PeopleNames.PEOPLE_REF_UID, targetNode
						.getProperty(PeopleNames.PEOPLE_UID).getString());
				if (wasCheckedOut)
					oldReferencing.getSession().save();
				else
					CommonsJcrUtils.saveAndCheckin(oldReferencing);
			}

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to update link reference", e);
		}
		return true;
		// }
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

	public String getString() {
		return value;
	}

	public void addFilterPanel(Composite parent) {
		parent.setLayout(new GridLayout());
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList(toSearchNodeType);
			}
		});
	}

	protected TableViewer createListPart(Composite parent,
			ILabelProvider labelProvider) {
		parent.setLayout(new FillLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableViewer v = new TableViewer(tableComposite, SWT.V_SCROLL
				| SWT.SINGLE);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.LEFT);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(PeopleUiConstants.CUSTOM_ITEM_HEIGHT, Integer.valueOf(20));

		// Providers and listeners
		v.setContentProvider(new BasicNodeListContentProvider());
		v.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				try {
					// Only single selection is enabled
					Node selectedEntity = (Node) ((IStructuredSelection) event
							.getSelection()).getFirstElement();
					if (selectedEntity.isNodeType(NodeType.MIX_TITLE))
						selectedItemTxt.setText(CommonsJcrUtils.get(
								selectedEntity, Property.JCR_TITLE));
					selectedItem = selectedEntity;
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to manage selected item",
							e);
				}
			}
		});

		return v;
	}

	protected void refreshFilteredList(String nodeType) {
		try {
			String filter = filterTxt.getText();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(nodeType, "selector");

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*"));
				defaultC = factory.fullTextSearch("selector", null, so);
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
			entityViewer
					.setInput(JcrUtils.nodeIteratorToList(result.getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

	/** Overwrite to close session */
	public boolean close() {
		JcrUtils.logoutQuietly(session);
		return super.close();
	}

}
