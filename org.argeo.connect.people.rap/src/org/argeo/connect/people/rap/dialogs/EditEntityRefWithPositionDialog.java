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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
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

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.rap.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.people.rap.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog with a filtered list to create or edit a position for a given person
 * in an organisation. If editing an existing position, note that both
 * referenced and referencing entities must be given in order to eventually
 * remove old reference
 */

@Deprecated
public class EditEntityRefWithPositionDialog extends TrayDialog {
	private static final long serialVersionUID = -3534660152626908662L;

	/* DEPENDENCY INJECTION */
	// private Repository repository;
	private PeopleService peopleService;
	private Session session;

	// The various field
	private Text positionTxt;
	private Text selectedItemTxt;
	private Node selectedItem = null;
	private Text departmentTxt;
	private Button isPrimaryBtn;

	// Labels
	private final String positionLbl = "Role";
	private final String chosenItemLbl = "Chosen item";
	private final String departmentLbl = "Department";
	private final String primaryLbl = "Is primary";

	// The search list
	private Text filterTxt;
	private TableViewer entityViewer;

	// private String value;
	private final String title;

	private boolean isBackward;
	private String toSearchNodeType;
	private Node oldLinkNode;

	// caches old info to initialise widgets if needed
	private String oldPosition = "";
	private String oldDepartment = "";
	private boolean wasPrimary = false;
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
			Repository repository, PeopleService peopleService, Node oldLink,
			boolean isBackward) {
		// , String toSearchNodeType
		super(parentShell);
		this.title = title;
		// this.repository = repository;
		this.peopleService = peopleService;
		this.oldLinkNode = oldLink;
		this.isBackward = isBackward;
		if (isBackward)
			toSearchNodeType = PeopleTypes.PEOPLE_PERSON;
		else
			toSearchNodeType = PeopleTypes.PEOPLE_ORG;

		if (oldLink != null) {
			// Try to initiallize our shortcuts
			try {
				session = CommonsJcrUtils.login(repository);
				oldPosition = CommonsJcrUtils.get(oldLinkNode,
						PeopleNames.PEOPLE_ROLE);
				oldDepartment = CommonsJcrUtils.get(oldLinkNode,
						PeopleNames.PEOPLE_DEPARTMENT);
				Boolean tmp = CommonsJcrUtils.getBooleanValue(oldLink,
						PeopleNames.PEOPLE_IS_PRIMARY);
				if (tmp != null)
					wasPrimary = tmp;

				oldReferencing = oldLink.getParent().getParent();
				oldReferenced = peopleService.getEntityByUid(session, oldLink
						.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
			} catch (RepositoryException e) {
				throw new PeopleException("unable to initialize link edition",
						e);
			}
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogarea.setLayout(new GridLayout(2, false));

		// the filter
		Composite filterCmp = new Composite(dialogarea, SWT.NONE);
		filterCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		addFilterPanel(filterCmp);

		// the list
		Composite listCmp = new Composite(dialogarea, SWT.NONE);
		listCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		entityViewer = createListPart(listCmp,
				new EntitySingleColumnLabelProvider(peopleService, null));
		refreshFilteredList(toSearchNodeType);

		// An empty line to give some air to the dialog
		Label dummyLbl = new Label(dialogarea, SWT.NONE);
		dummyLbl.setText("");
		dummyLbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));

		// The chosen item
		selectedItemTxt = createLT(dialogarea, chosenItemLbl);
		selectedItemTxt.setEnabled(false);
		selectedItemTxt.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CLASS_FORCE_BORDER);

		if (isBackward) {
			selectedItemTxt.setText(CommonsJcrUtils.get(oldReferencing,
					Property.JCR_TITLE));
		} else
			selectedItemTxt.setText(CommonsJcrUtils.get(oldReferenced,
					Property.JCR_TITLE));

		// Role if needed
		positionTxt = createLT(dialogarea, positionLbl);
		positionTxt.setText(oldPosition);

		// Department
		departmentTxt = createLT(dialogarea, departmentLbl);
		departmentTxt.setText(oldDepartment);

		// TODO display this only when adding a position for a person
		// Is primary
		if (!isBackward) {
			isPrimaryBtn = createLC(dialogarea, primaryLbl);
			isPrimaryBtn.setSelection(wasPrimary);
		}
		parent.pack();

		// Set the focus on the first field.
		filterTxt.setFocus();

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

	// This dialog life cycle

	@Override
	protected void okPressed() {
		if (performFinish())
			super.okPressed();
	}

	protected Point getInitialSize() {
		return new Point(400, 500);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/** Overwrite to close session */
	public boolean close() {
		JcrUtils.logoutQuietly(session);
		return super.close();
	}

	// Specific widgets management

	/** Creates label and text. */
	protected Text createLT(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.RIGHT);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return text;
	}

	/** Creates label and check box. */
	protected Button createLC(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.RIGHT);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Button btn = new Button(parent, SWT.CHECK);
		btn.setText("");
		btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return btn;
	}

	protected void addFilterPanel(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage("Search and choose a corresponding entity");
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
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

					// Sets the focus to next usefull field
					positionTxt.setFocus();

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
			// Kind of small hack: display only 15 results so that we avoid the
			// scroll bar bug; that is: showing the vertical scroll always
			// triggers appearance of horizontal scroll bar
			if (isBackward)
				query.setLimit(13); // no primary check box, the table gets
									// bigger
			else
				query.setLimit(12);

			QueryResult result = query.execute();
			entityViewer
					.setInput(JcrUtils.nodeIteratorToList(result.getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}
}