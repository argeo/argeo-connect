package org.argeo.connect.people.rap.toolkits;

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
import org.argeo.connect.people.rap.PeopleImages;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.PeopleUiService;
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
import org.argeo.connect.people.rap.utils.PeopleHtmlUtils;
import org.argeo.connect.people.rap.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
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
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different form panels for lists.
 */
public class ListToolkit {
	// private final static Log log = LogFactory.getLog(ListToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;
	private final String openEntityEditorCmdId;

	public ListToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
		this.openEntityEditorCmdId = peopleUiService.getOpenEntityEditorCmdId();
	}

	/**
	 * The jobs for a person
	 */
	public void populateJobsPanel(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout()); // PeopleUiUtils.gridLayoutNoBorder());
		AbstractPanelFormPart mainInfoPart = new JobsPanelPart(parent, entity);
		mainInfoPart.refresh();
		mainInfoPart.initialize(form);
		form.addPart(mainInfoPart);
	}

	private class JobsPanelPart extends AbstractPanelFormPart {
		private TableViewer jobsViewer;

		public JobsPanelPart(Composite parent, Node entity) {
			super(parent, entity);
		}

		protected void reCreateChildComposite(Composite panel, Node entity) {
			// Create new button
			final Button addBtn = isCurrentlyCheckedOut() ? toolkit
					.createButton(panel, "Add job", SWT.PUSH) : null;
			if (addBtn != null)
				configureAddJobBtn(addBtn, entity, "Add a new job");

			// Corresponding list
			Composite tableComp = toolkit.createComposite(panel);
			tableComp
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			jobsViewer = new TableViewer(tableComp);
			TableColumnLayout tableColumnLayout = createJobsTableColumns(
					entity, tableComp, jobsViewer, JobsPanelPart.this);
			tableComp.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(jobsViewer, 60);

			jobsViewer.setContentProvider(new BasicNodeListContentProvider());
			jobsViewer
					.addDoubleClickListener(new ListDoubleClickListener(false));
			refreshContent(panel, entity);
		}

		protected void refreshContent(Composite parent, Node entity) {
			try {
				if (!entity.hasNode(PeopleNames.PEOPLE_JOBS))
					return;
				NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_JOBS)
						.getNodes();
				jobsViewer.setInput(JcrUtils.nodeIteratorToList(ni));
				jobsViewer.refresh();
				// Fix problem when updating the job using a command.
				// Maybe not the best solution
				if (entity.getSession().hasPendingChanges())
					JobsPanelPart.this.markDirty();

			} catch (RepositoryException e) {
				throw new PeopleException("unable to refresh jobs for "
						+ entity, e);
			}
		}
	}

	// Jobs of a person
	private TableColumnLayout createJobsTableColumns(final Node entity,
			final Composite parent, final TableViewer viewer,
			AbstractFormPart part) {
		int[] bounds = { 150, 300 };
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn col;

		// Primary item
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.CENTER, 25);
		PrimaryEditingSupport editingSupport = new PrimaryEditingSupport(
				viewer, part, entity, PeopleNames.PEOPLE_IS_PRIMARY);
		col.setEditingSupport(editingSupport);
		col.setLabelProvider(new BooleanFlagLabelProvider(
				PeopleNames.PEOPLE_IS_PRIMARY, PeopleImages.PRIMARY_BTN,
				PeopleImages.PRIMARY_NOT_BTN));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				10, 15, true));

		// Role
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT,
				bounds[0]);
		col.setLabelProvider(new RoleListLabelProvider());
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				80, 20, true));

		// Company
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT,
				bounds[1]);
		col.setLabelProvider(new OrgOverviewLabelProvider(true, peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));

		// Edit & Remove links
		col = ViewerUtils.createTableViewerColumn(viewer, "Edit/Remove links",
				SWT.NONE, 60);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				40, 40, true));
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					// get the corresponding person
					Node link = (Node) element;
					Node person = link.getParent().getParent();
					if (CommonsJcrUtils.isNodeCheckedOutByMe(entity))
						return PeopleHtmlUtils.getEditJobSnippetForLists(link,
								false)
								+ " <br />"
								+ PeopleHtmlUtils
										.getRemoveReferenceSnippetForLists(
												link, person);
					else
						return "";

				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});
		// Add the magic listener that call a command with a link
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());
		return tableColumnLayout;
	}

	/**
	 * The employees of an organisation
	 */

	public void populateEmployeesPanel(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());
		AbstractPanelFormPart mainInfoPart = new EmployeesPanelPart(parent,
				entity);
		mainInfoPart.refresh();
		mainInfoPart.initialize(form);
		form.addPart(mainInfoPart);
	}

	private class EmployeesPanelPart extends AbstractPanelFormPart {
		private TableViewer employeesViewer;

		public EmployeesPanelPart(Composite parent, Node entity) {
			super(parent, entity);
		}

		protected void reCreateChildComposite(Composite panel, Node entity) {
			// Create new button
			Button addBtn = isCurrentlyCheckedOut() ? toolkit.createButton(
					panel, "Add Employee", SWT.PUSH) : null;
			if (addBtn != null)
				configureAddJobBtn(addBtn, entity,
						"Add an employee for this organisation");

			// Corresponding list
			Composite tableComp = toolkit.createComposite(panel);
			tableComp
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			employeesViewer = new TableViewer(tableComp);
			TableColumnLayout tableColumnLayout = createEmployeesTableColumns(
					entity, tableComp, employeesViewer);
			tableComp.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(employeesViewer, 70);

			employeesViewer
					.setContentProvider(new BasicNodeListContentProvider());
			employeesViewer.addDoubleClickListener(new ListDoubleClickListener(
					true));
			refreshContent(panel, entity);
		}

		protected void refreshContent(Composite parent, Node entity) {
			List<Node> employees = peopleService.getRelatedEntities(entity,
					PeopleTypes.PEOPLE_JOB, PeopleTypes.PEOPLE_PERSON);
			Collections.sort(employees, lastNameFirstNamePersonComparator);
			employeesViewer.setInput(employees);
			employeesViewer.refresh();

			// Try to force dirty state on refresh.
			// Does not work cause the link is saved
			// try {
			// if (entity.getSession().hasPendingChanges())
			// EmployeesPanelPart.this.markDirty();
			// } catch (RepositoryException e) {
			// throw new PeopleException("unable to refresh jobs for "
			// + entity, e);
			// }
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

	// private class MyNodeComparator extends Comparator<Node>

	// Employees of a company
	private TableColumnLayout createEmployeesTableColumns(final Node entity,
			final Composite parent, final TableViewer viewer) {
		int[] bounds = { 150, 300, 60 };
		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		// Role
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
				SWT.LEFT, bounds[0]);
		col.setLabelProvider(new RoleListLabelProvider());
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				80, 20, true));

		// Person
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT,
				bounds[1]);
		col.setLabelProvider(new PersonOverviewLabelProvider(
				PeopleUiConstants.LIST_TYPE_MEDIUM, peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));

		// Edit & Remove links
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());
		col = ViewerUtils.createTableViewerColumn(viewer, "Edit/Remove links",
				SWT.NONE, bounds[2]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				40, 40, true));
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					Node link = (Node) element;
					Node person = link.getParent().getParent();

					if (CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
						String tmp = PeopleHtmlUtils.getEditJobSnippetForLists(
								link, true)
								+ " <br />"
								+ PeopleHtmlUtils
										.getRemoveReferenceSnippetForLists(
												link, person);
						return tmp;
					} else
						return "";
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});
		return tableColumnLayout;

	}

	// ///////////////////////
	// HELPERS
	private void configureAddJobBtn(Button button, final Node relevantNode,
			String tooltip) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(EditJob.PUBLIC_RELEVANT_NODE_JCR_ID,
						CommonsJcrUtils.getIdentifier(relevantNode));
				CommandUtils.callCommand(EditJob.ID, params);
			}
		});
	}

	private class PrimaryEditingSupport extends BooleanEditingSupport {
		private static final long serialVersionUID = 4712271289751015687L;
		private final AbstractFormPart part;
		private final Node person;

		public PrimaryEditingSupport(TableViewer viewer, AbstractFormPart part,
				Node person, String propertyName) {
			super(viewer, propertyName);
			this.part = part;
			this.person = person;
		}

		@Override
		protected boolean canEdit(Object element) {
			return CommonsJcrUtils.isNodeCheckedOutByMe(person);
		}

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			if (((Boolean) value).booleanValue()
					&& PeopleJcrUtils.markAsPrimary(peopleService, person,
							currNode)) {
				// we refresh all parts to insure homogeneity.
				// TODO check if a simple part refresh is enough
				for (IFormPart part : form.getParts()) {
					part.refresh();
				}
				part.markDirty();
			}
		}
	}

	private class ListDoubleClickListener extends PeopleDoubleClickAdapter {
		private final boolean isBackward;

		public ListDoubleClickListener(boolean isBackward) {
			this.isBackward = isBackward;
		}

		@Override
		protected void processDoubleClick(Object obj) {
			if (obj instanceof Node) {
				Node link = (Node) obj;
				if (isBackward) {
					// we open editor for the parent entity
					try {
						link = link.getParent().getParent();
						CommandUtils.callCommand(openEntityEditorCmdId,
								OpenEntityEditor.PARAM_ENTITY_UID,
								CommonsJcrUtils.get(link,
										PeopleNames.PEOPLE_UID));
					} catch (RepositoryException e) {
						throw new PeopleException("unable to get related "
								+ "film for entity " + obj, e);
					}
				} else
					// We open editor for the referenced entity
					CommandUtils.callCommand(openEntityEditorCmdId,
							OpenEntityEditor.PARAM_ENTITY_UID, CommonsJcrUtils
									.get(link, PeopleNames.PEOPLE_REF_UID));
			}
		}
	}

}