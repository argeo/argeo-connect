package org.argeo.connect.people.rap.editors.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.EditJob;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.editors.utils.BooleanEditingSupport;
import org.argeo.connect.people.rap.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.rap.listeners.PeopleDoubleClickAdapter;
import org.argeo.connect.people.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.rap.providers.BooleanFlagLabelProvider;
import org.argeo.connect.people.rap.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.rap.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.rap.providers.RoleListLabelProvider;
import org.argeo.connect.people.rap.utils.AbstractPanelFormPart;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that displays an editable list of jobs
 * or employees
 */
public class JobList extends Composite {
	private static final long serialVersionUID = -4736848221960630767L;

	private final FormToolkit toolkit;
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final Node entity;
	private final boolean isBackward;

	// this page UI Objects
	private MyFormPart myFormPart;

	public JobList(FormToolkit toolkit, IManagedForm form, Composite parent,
			int style, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node entity) {
		super(parent, style);
		this.toolkit = toolkit;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.entity = entity;

		// Participations are stored in the projects.
		isBackward = CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG);

		// Populate
		myFormPart = new MyFormPart(this);
		myFormPart.initialize(form);
		form.addPart(myFormPart);
	}

	private class MyFormPart extends AbstractPanelFormPart {
		private TableViewer itemViewer;

		public MyFormPart(Composite parent) {
			super(parent, entity);
		}

		protected void reCreateChildComposite(Composite panel, Node entity) {
			// Add button if needed
			Button addBtn = null;
			if (isCurrentlyCheckedOut()) {
				panel.setLayout(new GridLayout());
				addBtn = toolkit.createButton(panel, "", SWT.PUSH);
				configureAddBtn(addBtn);
			} else {
				panel.setLayout(PeopleUiUtils.noSpaceGridLayout());
			}

			// Item list
			Composite tableComp = toolkit.createComposite(panel);
			itemViewer = createItemViewer(tableComp);
			tableComp.setLayoutData(PeopleUiUtils.fillGridData());
			refreshContent(panel, entity);
		}

		protected void refreshContent(Composite parent, Node entity) {
			if (isBackward) {
				List<Node> employees = peopleService.getRelatedEntities(entity,
						PeopleTypes.PEOPLE_JOB, PeopleTypes.PEOPLE_PERSON);
				Collections.sort(employees, lastNameFirstNamePersonComparator);
				itemViewer.setInput(employees);
				// itemViewer.refresh();
			} else {
				try {
					List<Node> jobs = new ArrayList<Node>();
					if (!entity.hasNode(PeopleNames.PEOPLE_JOBS))
						return; // No member to display
					NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_JOBS)
							.getNodes();
					while (ni.hasNext()) {
						// Check relevant nodeType
						Node currNode = ni.nextNode();
						if (currNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
							jobs.add(currNode);
						}
					}
					itemViewer.setInput(jobs);
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Cannot refresh person job list for " + entity, re);
				}
			}
		}
	}

	private TableViewer createItemViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.V_SCROLL);
		PeopleRapUtils.setTableDefaultStyle(viewer, 60);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn col;

		// Primary job for persons only
		if (!isBackward) {
			col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.CENTER,
					25);
			PrimaryEditingSupport editingSupport = new PrimaryEditingSupport(
					viewer, PeopleNames.PEOPLE_IS_PRIMARY);
			col.setEditingSupport(editingSupport);
			col.setLabelProvider(new BooleanFlagLabelProvider(
					PeopleNames.PEOPLE_IS_PRIMARY, PeopleRapImages.PRIMARY_BTN,
					PeopleRapImages.PRIMARY_NOT_BTN));
			tableColumnLayout.setColumnData(col.getColumn(),
					new ColumnWeightData(10, 15, true));
		}

		// Role
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT, 150);
		col.setLabelProvider(new RoleListLabelProvider());
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				80, 20, true));

		// Linked entity
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT, 300);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));
		if (isBackward)
			col.setLabelProvider(new PersonOverviewLabelProvider(
					PeopleRapConstants.LIST_TYPE_MEDIUM, peopleService,
					peopleWorkbenchService));
		else
			col.setLabelProvider(new OrgOverviewLabelProvider(true,
					peopleService, peopleWorkbenchService));

		// Edit & Remove links
		if (CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
			col = ViewerUtils.createTableViewerColumn(viewer,
					"Edit/Remove links", SWT.NONE, 60);
			tableColumnLayout.setColumnData(col.getColumn(),
					new ColumnWeightData(40, 40, true));
			col.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					Node link = (Node) element;
					return PeopleRapSnippets.getEditJobSnippetForLists(link,
							isBackward)
							+ " <br />"
							+ PeopleRapSnippets
									.getRemoveReferenceSnippetForLists(link,
											entity);
				}
			});
		}

		// Providers and listeners
		viewer.setContentProvider(new BasicNodeListContentProvider());
		viewer.addDoubleClickListener(new ListDoubleClickListener());
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());

		// Important don't forget this.
		parent.setLayout(tableColumnLayout);
		return viewer;
	}

	private void configureAddBtn(Button button) {
		String tooltip = "", text = "";
		if (isBackward) {
			text = "Add an employee";
			tooltip = "Register the position of a person in this organisation";
		} else {
			text = "Add job";
			tooltip = "Register a position in an organisation";
		}

		button.setToolTipText(tooltip);
		button.setText(text);

		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(EditJob.PARAM_RELEVANT_NODE_JCR_ID,
						CommonsJcrUtils.getIdentifier(entity));
				CommandUtils.callCommand(EditJob.ID, params);
			}
		});
	}

	private class ListDoubleClickListener extends PeopleDoubleClickAdapter {
		@Override
		protected void processDoubleClick(Object obj) {
			if (obj instanceof Node) {
				Node link = (Node) obj;
				Node toOpen;
				if (isBackward) {
					toOpen = CommonsJcrUtils.getParent(CommonsJcrUtils
							.getParent(link));
				} else {
					toOpen = peopleService.getEntityByUid(CommonsJcrUtils
							.getSession(entity), CommonsJcrUtils.get(link,
							PeopleNames.PEOPLE_REF_UID));
				}
				CommandUtils.callCommand(
						peopleWorkbenchService.getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID,
						CommonsJcrUtils.getIdentifier(toOpen));
			}
		}
	}

	private class PrimaryEditingSupport extends BooleanEditingSupport {
		private static final long serialVersionUID = 1L;

		public PrimaryEditingSupport(TableViewer viewer, String propertyName) {
			super(viewer, propertyName);
		}

		@Override
		protected boolean canEdit(Object element) {
			return CommonsJcrUtils.isNodeCheckedOutByMe(entity);
		}

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			if (((Boolean) value).booleanValue()
					&& PeopleJcrUtils.markAsPrimary(peopleService, entity,
							currNode)) {
				myFormPart.refresh();
				myFormPart.markDirty();
			}
		}
	}

	private static Comparator<Node> lastNameFirstNamePersonComparator = new Comparator<Node>() {

		public int compare(Node node1, Node node2) {
			try {
				int rc = 0;
				// We have a job, we want to compare corresponding parent person
				if (node1 != null)
					node1 = node1.getParent().getParent();
				if (node2 != null)
					node2 = node2.getParent().getParent();
				String lastName1 = CommonsJcrUtils.get(node1,
						PeopleNames.PEOPLE_LAST_NAME).toLowerCase();
				String lastName2 = CommonsJcrUtils.get(node2,
						PeopleNames.PEOPLE_LAST_NAME).toLowerCase();
				String firstName1 = CommonsJcrUtils.get(node1,
						PeopleNames.PEOPLE_FIRST_NAME).toLowerCase();
				String firstName2 = CommonsJcrUtils.get(node2,
						PeopleNames.PEOPLE_FIRST_NAME).toLowerCase();
				rc = lastName1.compareTo(lastName2);
				if (rc == 0)
					rc = firstName1.compareTo(firstName2);
				return rc;
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to compare " + node1
						+ " with " + node2, e);
			}
		}
	};

}