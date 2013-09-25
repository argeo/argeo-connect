package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.commands.AddEntityReferenceWithPosition;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.ui.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.FilmOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.RoleListLabelProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
	private final PeopleUiService peopleUiService;

	public ListToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
	}

	/**
	 * The jobs for a person
	 */

	public void populateJobsPanel(Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());
		// Create new button
		final Button addBtn = toolkit.createButton(panel, "Add job", SWT.PUSH);
		configureAddReferenceButton(addBtn, entity, "Add a new job", false,
				PeopleTypes.PEOPLE_ORGANIZATION);

		// Corresponding list
		Composite tableComp = toolkit.createComposite(panel);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer viewer = new TableViewer(tableComp);
		TableColumnLayout tableColumnLayout = createJobsTableColumns(tableComp,
				viewer);
		tableComp.setLayout(tableColumnLayout);
		PeopleUiUtils.setTableDefaultStyle(viewer, 60);

		// compulsory content provider
		viewer.setContentProvider(new BasicNodeListContentProvider());
		try {
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_JOBS));
		} catch (RepositoryException re) {
			throw new PeopleException("Error adding double click on job list",
					re);
		}

		// Add life cycle management
		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					addBtn.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));

					List<Node> jobs = new ArrayList<Node>();
					if (!entity.hasNode(PeopleNames.PEOPLE_JOBS)) // No job to
																	// display
						return;
					NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_JOBS)
							.getNodes();
					while (ni.hasNext()) {
						// Check if have the right type of node
						Node currJob = ni.nextNode();
						if (currJob.isNodeType(PeopleTypes.PEOPLE_JOB)) {
							jobs.add(currJob);
						}
					}
					viewer.setInput(jobs);
				} catch (RepositoryException re) {
					throw new PeopleException("Cannot refresh jobs list", re);
				}
			}
		};
		sPart.initialize(form);
		form.addPart(sPart);
	}

	public void populateEmployeesPanel(Composite panel, final Node entity) {
		try {
			panel.setLayout(new GridLayout());
			// Create new button
			final Button addBtn = toolkit.createButton(panel, "Add Employee",
					SWT.PUSH);
			configureAddReferenceButton(addBtn, entity,
					"Add an employee for this organisation", true,
					PeopleTypes.PEOPLE_PERSON);

			// Corresponding list
			Composite tableComp = toolkit.createComposite(panel);
			tableComp
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			final TableViewer viewer = new TableViewer(tableComp);
			TableColumnLayout tableColumnLayout = createEmployeesTableColumns(
					tableComp, viewer);
			tableComp.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 70);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_EMPLOYEES));

			// Add life cycle management
			final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
				public void refresh() {
					super.refresh();
					addBtn.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					viewer.setInput(peopleService.getRelatedEntities(entity,
							PeopleTypes.PEOPLE_JOB, PeopleTypes.PEOPLE_PERSON));
				}
			};
			sPart.initialize(form);
			form.addPart(sPart);
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot populate employee panel ", re);
		}
	}

	public void populateFilmsPanel(Composite panel, final Node entity) {
		try {
			panel.setLayout(new GridLayout());
			// Create new button
			final Button addBtn = toolkit.createButton(panel, "Add Film",
					SWT.PUSH);
			configureAddReferenceButton(addBtn, entity,
					"Add a film participation", true, FilmTypes.FILM);

			// Corresponding list
			Composite tableComp = toolkit.createComposite(panel);
			tableComp
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			final TableViewer viewer = new TableViewer(tableComp, SWT.V_SCROLL);
			TableColumnLayout tableColumnLayout = createProductionsTableColumns(
					tableComp, viewer);
			tableComp.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 60);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_PRODUCTIONS));

			// Add life cycle management
			final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
				public void refresh() {
					super.refresh();
					addBtn.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					viewer.setInput(peopleService.getRelatedEntities(entity,
							PeopleTypes.PEOPLE_MEMBER, FilmTypes.FILM));
				}
			};
			sPart.initialize(form);
			form.addPart(sPart);

		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations list", re);
		}
	}

	public void populateMembersPanel(Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());
		// Create new button
		final Button addBtn = toolkit.createButton(panel, "Add member",
				SWT.PUSH);
		configureAddReferenceButton(addBtn, entity, "Add a new group member",
				false, PeopleTypes.PEOPLE_PERSON);

		// Corresponding list
		Composite tableComp = toolkit.createComposite(panel);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer viewer = new TableViewer(tableComp, SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = createMembersTableColumns(
				tableComp, viewer);
		tableComp.setLayout(tableColumnLayout);
		PeopleUiUtils.setTableDefaultStyle(viewer, 60);

		viewer.setContentProvider(new BasicNodeListContentProvider());
		try {

			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_MEMBERS));
		} catch (RepositoryException re) {
			throw new PeopleException("Error adding double clic on the "
					+ "film participation list", re);
		}

		// Add life cycle management
		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					addBtn.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					List<Node> members = new ArrayList<Node>();
					if (!entity.hasNode(PeopleNames.PEOPLE_MEMBERS))
						return; // No member to display
					NodeIterator ni = entity
							.getNode(PeopleNames.PEOPLE_MEMBERS).getNodes();
					while (ni.hasNext()) {
						// Check relevant nodeType
						Node currMember = ni.nextNode();
						if (currMember.isNodeType(PeopleTypes.PEOPLE_MEMBER)) {
							members.add(currMember);
						}
					}
					viewer.setInput(members);
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Cannot refresh film participation list", re);
				}
			}
		};
		sPart.initialize(form);
		form.addPart(sPart);
	}

	private TableColumnLayout createJobsTableColumns(final Composite parent,
			final TableViewer viewer) {
		int[] bounds = { 150, 300 };
		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		// Role
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
				SWT.LEFT, bounds[0]);
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
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());
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

					return PeopleHtmlUtils.getEditWithPosSnippetForLists(link,
							false, PeopleTypes.PEOPLE_ORGANIZATION)
							+ " <br />"
							+ PeopleHtmlUtils
									.getRemoveReferenceSnippetForLists(link,
											person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});

		return tableColumnLayout;
	}

	private TableColumnLayout createEmployeesTableColumns(
			final Composite parent, final TableViewer viewer) {
		int[] bounds = { 150, 300 };
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

					return PeopleHtmlUtils.getEditWithPosSnippetForLists(link,
							true, PeopleTypes.PEOPLE_PERSON)
							+ " <br />"
							+ PeopleHtmlUtils
									.getRemoveReferenceSnippetForLists(link,
											person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});
		return tableColumnLayout;

	}

	private TableColumnLayout createProductionsTableColumns(Composite parent,
			TableViewer viewer) {
		int[] bounds = { 150, 300 };
		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		// Role
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
				SWT.LEFT, bounds[0]);
		col.setLabelProvider(new RoleListLabelProvider());
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				80, 20, true));

		// Film
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT,
				bounds[1]);
		col.setLabelProvider(new FilmOverviewLabelProvider(true, peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));

		// Edit & Remove links
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());
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

					return PeopleHtmlUtils.getEditWithPosSnippetForLists(link,
							true, FilmTypes.FILM)
							+ " <br />"
							+ PeopleHtmlUtils
									.getRemoveReferenceSnippetForLists(link,
											person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});

		return tableColumnLayout;
	}

	private TableColumnLayout createMembersTableColumns(Composite parent,
			TableViewer viewer) {
		int[] bounds = { 150, 300 };
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

					return PeopleHtmlUtils.getEditWithPosSnippetForLists(link,
							false, PeopleTypes.PEOPLE_PERSON)
							+ " <br />"
							+ PeopleHtmlUtils
									.getRemoveReferenceSnippetForLists(link,
											person);
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

	private void configureAddReferenceButton(Button button,
			final Node targetNode, String tooltip, final boolean isBackward,
			final String nodeTypeToSearch) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					if (isBackward)
						params.put(
								AddEntityReferenceWithPosition.PARAM_REFERENCED_JCR_ID,
								targetNode.getIdentifier());
					else
						params.put(
								AddEntityReferenceWithPosition.PARAM_REFERENCING_JCR_ID,
								targetNode.getIdentifier());
					params.put(
							AddEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE,
							nodeTypeToSearch);

					CommandUtils.callCommand(AddEntityReferenceWithPosition.ID,
							params);
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to get parent Jcr identifier", e1);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

}