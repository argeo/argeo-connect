package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.FilmOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.RoleListLabelProvider;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different form panels for lists.
 */
public class ListToolkit {
	// private final static Log log = LogFactory.getLog(ListToolkit.class);

	// private final FormToolkit toolkit;
	// private final IManagedForm form;
	private final PeopleService peopleService;
	private final PeopleUiService peopleUiService;

	public ListToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		// this.toolkit = toolkit;
		// this.form = form;
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
	}

	public void populateJobsPanel(Composite panel, final Node entity) {
		try {
			TableViewer viewer = new TableViewer(panel);
			TableColumnLayout tableColumnLayout = createJobsTableColumns(panel,
					viewer);
			panel.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 60);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_JOBS));

			List<Node> jobs = new ArrayList<Node>();
			if (!entity.hasNode(PeopleNames.PEOPLE_JOBS)) // No job to display
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
			throw new PeopleException("Cannot create organizations list", re);
		}
	}

	public void populateEmployeesPanel(Composite panel, final Node entity) {
		try {
			TableViewer viewer = new TableViewer(panel);
			TableColumnLayout tableColumnLayout = createEmployeesTableColumns(
					panel, viewer);
			panel.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 70);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_EMPLOYEES));

			viewer.setInput(peopleService.getRelatedEntities(entity,
					PeopleTypes.PEOPLE_JOB, PeopleTypes.PEOPLE_PERSON));

		} catch (RepositoryException re) {
			throw new PeopleException("Cannot populate employee panel ", re);
		}
	}

	public void populateFilmsPanel(Composite panel, final Node entity) {
		try {
			TableViewer viewer = new TableViewer(panel, SWT.V_SCROLL);
			TableColumnLayout tableColumnLayout = createProductionsTableColumns(
					panel, viewer);
			panel.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 60);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_PRODUCTIONS));

			viewer.setInput(peopleService.getRelatedEntities(entity,
					PeopleTypes.PEOPLE_MEMBER, FilmTypes.FILM));
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations list", re);
		}
	}

	public void populateMembersPanel(Composite panel, final Node entity) {
		try {
			TableViewer viewer = new TableViewer(panel, SWT.V_SCROLL);
			TableColumnLayout tableColumnLayout = createMembersTableColumns(
					panel, viewer);
			panel.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 60);

			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName(),
							PeopleUiConstants.PANEL_MEMBERS));

			List<Node> members = new ArrayList<Node>();
			if (!entity.hasNode(PeopleNames.PEOPLE_MEMBERS))
				return; // No member to display
			NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_MEMBERS)
					.getNodes();
			while (ni.hasNext()) {
				// Check relevant nodeType
				Node currMember = ni.nextNode();
				if (currMember.isNodeType(PeopleTypes.PEOPLE_MEMBER)) {
					members.add(currMember);
				}
			}
			viewer.setInput(members);
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations list", re);
		}
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
		col.setLabelProvider(new PersonOverviewLabelProvider(true,
				peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));
		return tableColumnLayout;
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
		col.setLabelProvider(new PersonOverviewLabelProvider(true,
				peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));
		return tableColumnLayout;
	}

}