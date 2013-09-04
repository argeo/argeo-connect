package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.FilmOverviewLabelProvider;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
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
public class ListPanelToolkit {
	private final static Log log = LogFactory.getLog(ListPanelToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;
	private final PeopleUiService peopleUiService;

	public ListPanelToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
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
							.getPrimaryNodeType().getName()));

			List<Node> jobs = new ArrayList<Node>();

			if (!entity.hasNode(PeopleNames.PEOPLE_JOBS)) // No job to display
				return;

			// Session session = entity.getSession();
			NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_JOBS)
					.getNodes();
			while (ni.hasNext()) {
				// Check if have the right type of node
				Node currJob = ni.nextNode();
				if (currJob.isNodeType(PeopleTypes.PEOPLE_JOB)) {
					// Node linkedOrg = peopleService.getEntityById(session,
					// currJob.getProperty(PeopleNames.PEOPLE_ORG_ID).getString());
					// if (linkedOrg != null)
					jobs.add(currJob);
				}
			}
			viewer.setInput(jobs);

		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations list", re);
		}

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

	public void populateFilmsPanel(Composite panel, final Node entity) {
		try {
			TableViewer viewer = new TableViewer(panel);
			TableColumnLayout tableColumnLayout = createProductionsTableColumns(
					panel, viewer);
			panel.setLayout(tableColumnLayout);
			PeopleUiUtils.setTableDefaultStyle(viewer, 60);

			// compulsory content provider
			viewer.setContentProvider(new BasicNodeListContentProvider());
			viewer.addDoubleClickListener(peopleUiService
					.getNewNodeListDoubleClickListener(peopleService, entity
							.getPrimaryNodeType().getName()));

			List<Node> productions = new ArrayList<Node>();

			// if (!entity.hasNode(PeopleNames.PEOPLE_JOBS)) // No job to
			// display
			// return;
			//
			// // Session session = entity.getSession();
			// NodeIterator ni = entity.getNode(PeopleNames.PEOPLE_JOBS)
			// .getNodes();
			// while (ni.hasNext()) {
			// // Check if have the right type of node
			// Node currJob = ni.nextNode();
			// if (currJob.isNodeType(PeopleTypes.PEOPLE_JOB)) {
			// // Node linkedOrg = peopleService.getEntityById(session,
			// // currJob.getProperty(PeopleNames.PEOPLE_ORG_ID).getString());
			// // if (linkedOrg != null)
			// jobs.add(currJob);
			// }
			// }
			viewer.setInput(productions);

		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations list", re);
		}

	}

	private TableColumnLayout createProductionsTableColumns(
			final Composite parent, final TableViewer viewer) {
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
		col.setLabelProvider(new FilmOverviewLabelProvider(true, peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));
		return tableColumnLayout;
	}

}