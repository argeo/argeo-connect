package org.argeo.activities.workbench.parts;

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

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.activities.workbench.util.ActivityViewerComparator;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

public class MyTodoListView extends ViewPart implements Refreshable {
	public static final String ID = ActivitiesUiPlugin.PLUGIN_ID + ".myTodoListView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	private UserAdminService userAdminService;
	private ActivitiesService activitiesService;
	private SystemWorkbenchService systemWorkbenchService;

	private TableViewer tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		// Finalise initialisation
		session = ConnectJcrUtils.login(repository);

		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);

		tableViewer.setContentProvider(new MyLazyCP(tableViewer));
		tableViewer.addDoubleClickListener(new ViewDoubleClickListener());

		// The context menu
		final TaskViewerContextMenu contextMenu = new TaskViewerContextMenu(tableViewer, session, activitiesService);
		tableViewer.getTable().addMouseListener(new MouseAdapter() {
			private static final long serialVersionUID = 6737579410648595940L;

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					// contextMenu.setCurrFolderPath(currDisplayedFolder);
					contextMenu.show(tableViewer.getTable(), new Point(e.x, e.y),
							(IStructuredSelection) tableViewer.getSelection());
				}
			}
		});
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
		int tableStyle = SWT.MULTI | SWT.VIRTUAL;
		Table table = new Table(parent, tableStyle);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;

		Map<String, ColumnLabelProvider> lpMap = new HashMap<String, ColumnLabelProvider>();
		lpMap.put(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, new AssignedToLabelProvider());
		lpMap.put(ActivitiesNames.ACTIVITIES_RELATED_TO, new RelatedToLabelProvider());

		ActivityViewerComparator comparator = new ActivityViewerComparator(activitiesService, lpMap);

		// Date
		column = ViewerUtils.createTableViewerColumn(viewer, "Date", SWT.RIGHT, 80);
		column.setLabelProvider(new DateLabelProvider());
		column.getColumn().addSelectionListener(getNodeSelectionAdapter(0, PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE, comparator, viewer));

		// Title / description
		column = ViewerUtils.createTableViewerColumn(viewer, "What", SWT.NONE, 360);
		column.setLabelProvider(new TitleDescLabelProvider());
		column.getColumn().addSelectionListener(
				getNodeSelectionAdapter(1, PropertyType.STRING, Property.JCR_TITLE, comparator, viewer));

		// Assigned to
		column = ViewerUtils.createTableViewerColumn(viewer, "Assigned to", SWT.NONE, 150);
		column.setLabelProvider(new AssignedToLabelProvider());
		column.getColumn().addSelectionListener(getNodeSelectionAdapter(2, PropertyType.STRING,
				ActivitiesNames.ACTIVITIES_ASSIGNED_TO, comparator, viewer));

		// Related to
		column = ViewerUtils.createTableViewerColumn(viewer, "Related to", SWT.NONE, 250);
		column.setLabelProvider(new RelatedToLabelProvider());
		column.getColumn().addSelectionListener(getNodeSelectionAdapter(3, PropertyType.STRING,
				ActivitiesNames.ACTIVITIES_RELATED_TO, comparator, viewer));
		// Status
		column = ViewerUtils.createTableViewerColumn(viewer, "Status", SWT.NONE, 250);
		column.setLabelProvider(new StatusLabelProvider());
		column.getColumn().addSelectionListener(getNodeSelectionAdapter(4, PropertyType.STRING,
				ActivitiesNames.ACTIVITIES_TASK_STATUS, comparator, viewer));

		// Warning: initialise comparator before setting it
		comparator.setColumn(PropertyType.DATE, ActivityViewerComparator.RELEVANT_DATE);

		viewer.setComparator(comparator);

		return viewer;
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		NodeIterator tasks = activitiesService.getMyTasks(session, true);
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
				if (currNode.isNodeType(ActivitiesTypes.ACTIVITIES_TASK)) {
					return activitiesService.getAssignedToDisplayName(currNode);
				} else if (currNode.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY)) {
					String id = ConnectJcrUtils.get(currNode, ActivitiesNames.ACTIVITIES_REPORTED_BY);
					if (EclipseUiUtils.notEmpty(id))
						return userAdminService.getUserDisplayName(id);
				}
				return "";
			} catch (RepositoryException re) {
				throw new ActivitiesException("Unable to get date from node " + element, re);
			}
		}
	}

	private class RelatedToLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.hasProperty(ActivitiesNames.ACTIVITIES_RELATED_TO)) {
					StringBuilder builder = new StringBuilder();
					Value[] refs = currNode.getProperty(ActivitiesNames.ACTIVITIES_RELATED_TO).getValues();
					for (Value value : refs) {
						String id = value.getString();
						Node currReferenced = session.getNodeByIdentifier(id);
						builder.append(ConnectJcrUtils.get(currReferenced, Property.JCR_TITLE)).append(", ");
					}
					return builder.toString();
				}
				return "";
			} catch (RepositoryException re) {
				throw new ActivitiesException("Unable to get date from node " + element, re);
			}
		}
	}

	private class StatusLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			return ConnectJcrUtils.get((Node) element, ActivitiesNames.ACTIVITIES_TASK_STATUS);
		}
	}

	private class TitleDescLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;

				if (currNode.isNodeType(ActivitiesTypes.ACTIVITIES_TASK)) {
					String title = ConnectJcrUtils.get(currNode, Property.JCR_TITLE);

					String desc = ConnectJcrUtils.get(currNode, Property.JCR_DESCRIPTION);
					return ConnectJcrUtils.concatIfNotEmpty(title, desc, " - ");
				}
				return "";
			} catch (RepositoryException re) {
				throw new ActivitiesException("Unable to get date from node " + element, re);
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

			Calendar dateToDisplay = activitiesService.getActivityRelevantDate(currNode);
			if (dateToDisplay == null)
				return "";

			Calendar now = GregorianCalendar.getInstance();
			if (dateToDisplay.get(Calendar.YEAR) == now.get(Calendar.YEAR)
					&& dateToDisplay.get(Calendar.MONTH) == now.get(Calendar.MONTH))
				if (dateToDisplay.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH))
					// "today, at " +
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

			Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
			if (obj instanceof Node) {
				try {
					String jcrId = ((Node) obj).getIdentifier();
					String paramName = OpenEntityEditor.PARAM_JCR_ID;
					CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(), paramName, jcrId);
				} catch (RepositoryException e) {
					throw new ActivitiesException("Cannot open user editor", e);
				}
			}
		}
	}

	protected Session getSession() {
		return session;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}
