package org.argeo.connect.people.workbench.rap.views;

import static org.argeo.eclipse.ui.jcr.JcrUiUtils.getNodeSelectionAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.workbench.rap.util.ActivityViewerComparator;
import org.argeo.connect.people.workbench.rap.util.Refreshable;
import org.argeo.connect.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

public class MyTasksView extends ViewPart implements Refreshable {
	public static final String ID = PeopleRapPlugin.PLUGIN_ID + ".myTasksView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// Local cache
	private ActivityService activityService;
	private UserAdminService userAdminService;

	private TableViewer tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		// Finalise initialisation
		session = JcrUiUtils.login(repository);
		activityService = peopleService.getActivityService();
		userAdminService = peopleService.getUserAdminService();

		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);
		tableViewer.setContentProvider(new MyLazyCP(tableViewer));
		tableViewer.addDoubleClickListener(new ViewDoubleClickListener());

		refreshFilteredList();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void forceRefresh(Object object) {
		refreshFilteredList();
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private TableViewer createTableViewer(final Composite parent) {
		int tableStyle = SWT.SINGLE | SWT.VIRTUAL;
		Table table = new Table(parent, tableStyle);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;

		Map<String, ColumnLabelProvider> lpMap = new HashMap<String, ColumnLabelProvider>();
		lpMap.put(PeopleNames.PEOPLE_ASSIGNED_TO, new AssignedToLabelProvider());
		lpMap.put(PeopleNames.PEOPLE_RELATED_TO, new RelatedToLabelProvider());

		ActivityViewerComparator comparator = new ActivityViewerComparator(
				activityService, lpMap);

		// Date
		column = ViewerUtils.createTableViewerColumn(viewer, "Date",
				SWT.RIGHT, 80);
		column.setLabelProvider(new DateLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(0, PropertyType.DATE,
						ActivityViewerComparator.RELEVANT_DATE, comparator,
						viewer));

		// Title / description
		column = ViewerUtils.createTableViewerColumn(viewer, "What", SWT.NONE,
				360);
		column.setLabelProvider(new TitleDescLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(1, PropertyType.STRING,
						Property.JCR_TITLE, comparator, viewer));

		// Assigned to
		column = ViewerUtils.createTableViewerColumn(viewer, "Assigned to",
				SWT.NONE, 150);
		column.setLabelProvider(new AssignedToLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(2, PropertyType.STRING,
						PeopleNames.PEOPLE_ASSIGNED_TO, comparator, viewer));

		// Related to
		column = ViewerUtils.createTableViewerColumn(viewer, "Related to",
				SWT.NONE, 250);
		column.setLabelProvider(new RelatedToLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(3, PropertyType.STRING,
						PeopleNames.PEOPLE_RELATED_TO, comparator, viewer));
		// Status
		column = ViewerUtils.createTableViewerColumn(viewer, "Status",
				SWT.NONE, 250);
		column.setLabelProvider(new StatusLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(4, PropertyType.STRING,
						PeopleNames.PEOPLE_TASK_STATUS, comparator, viewer));

		// Warning: initialise comparator before setting it
		comparator.setColumn(PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE);

		viewer.setComparator(comparator);

		return viewer;
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		NodeIterator tasks = activityService.getMyTasks(session, false);
		setInput(tasks);
	}

	protected final void setInput(NodeIterator tasks) {
		tableViewer.setInput(JcrUtils.nodeIteratorToList(tasks).toArray());
		tableViewer.setItemCount((int) tasks.getSize());
		tableViewer.refresh();
	}

	private class AssignedToLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					return activityService.getAssignedToDisplayName(currNode);
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
					String id = JcrUiUtils.get(currNode,
							PeopleNames.PEOPLE_REPORTED_BY);
					if (EclipseUiUtils.notEmpty(id))
						return userAdminService.getUserDisplayName(id);
				}
				return "";
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get date from node "
						+ element, re);
			}
		}
	}

	private class RelatedToLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.hasProperty(PeopleNames.PEOPLE_RELATED_TO)) {
					StringBuilder builder = new StringBuilder();
					Value[] refs = currNode.getProperty(
							PeopleNames.PEOPLE_RELATED_TO).getValues();
					for (Value value : refs) {
						String id = value.getString();
						Node currReferenced = session.getNodeByIdentifier(id);
						builder.append(
								JcrUiUtils.get(currReferenced,
										Property.JCR_TITLE)).append(", ");
					}
					return builder.toString();
				}
				return "";
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get date from node "
						+ element, re);
			}
		}
	}

	private class StatusLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			return JcrUiUtils.get((Node) element,
					PeopleNames.PEOPLE_TASK_STATUS);
		}
	}

	private class TitleDescLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;

				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					String title = JcrUiUtils.get(currNode, Property.JCR_TITLE);

					String desc = JcrUiUtils.get(currNode,
							Property.JCR_DESCRIPTION);
					return JcrUiUtils.concatIfNotEmpty(title, desc, " - ");
				}
				return "";
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get date from node "
						+ element, re);
			}
		}
	}

	private class DateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		private DateFormat todayFormat = new SimpleDateFormat("HH:mm");
		private DateFormat inMonthFormat = new SimpleDateFormat("dd MMM");
		private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

		@Override
		public String getText(Object element) {
			Node currNode = (Node) element;

			Calendar dateToDisplay = activityService
					.getActivityRelevantDate(currNode);
			if (dateToDisplay == null)
				return "";

			Calendar now = GregorianCalendar.getInstance();
			if (dateToDisplay.get(Calendar.YEAR) == now.get(Calendar.YEAR)
					&& dateToDisplay.get(Calendar.MONTH) == now
							.get(Calendar.MONTH))
				if (dateToDisplay.get(Calendar.DAY_OF_MONTH) == now
						.get(Calendar.DAY_OF_MONTH))
					return todayFormat.format(dateToDisplay.getTime());
				else
					return inMonthFormat.format(dateToDisplay.getTime());
			else
				return dateFormat.format(dateToDisplay.getTime());
		}
	}

	private class MyLazyCP implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Object[] elements;

		public MyLazyCP(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	class ViewDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				try {
					String jcrId = ((Node) obj).getIdentifier();
					String paramName = OpenEntityEditor.PARAM_JCR_ID;
					CommandUtils.callCommand(
							peopleWorkbenchService.getOpenEntityEditorCmdId(),
							paramName, jcrId);
				} catch (RepositoryException e) {
					throw new PeopleException("Cannot open user editor", e);
				}
			}
		}
	}

	protected Session getSession() {
		return session;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}
