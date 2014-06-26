package org.argeo.connect.people.ui.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.utils.ActivityViewerComparator;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.ui.utils.Refreshable;
import org.argeo.connect.people.utils.ActivityJcrUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.JcrUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

public class MyTasksView extends ViewPart implements Refreshable {
	public static final String ID = PeopleUiPlugin.PLUGIN_ID + ".myTasksView";

	/* DEPENDENCY INJECTION */
	// private PeopleService peopleService;
	private Session session;
	private ActivityService activityService;
	private PeopleUiService peopleUiService;

	// private String openEntityEditorCmdId = OpenEntityEditor.ID;

	private TableViewer tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);
		tableViewer.setContentProvider(new MyTableContentProvider());
		tableViewer.addDoubleClickListener(new ViewDoubleClickListener());

		refreshFilteredList();

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

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
		int tableStyle = SWT.SINGLE;
		Table table = new Table(parent, tableStyle);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;

		Map<String, ColumnLabelProvider> lpMap = new HashMap<String, ColumnLabelProvider>();
		lpMap.put(PeopleNames.PEOPLE_ASSIGNED_TO, new AssignedToLabelProvider());
		lpMap.put(PeopleNames.PEOPLE_RELATED_TO, new RelatedToLabelProvider());

		ActivityViewerComparator comparator = new ActivityViewerComparator(
				activityService, lpMap);

		// TODO : add an icon depending on the task type and/or status ??
		// column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
		// 22);
		// column.setLabelProvider(new TypeLabelProvider());

		// Date
		column = ViewerUtils.createTableViewerColumn(viewer, "Due Date",
				SWT.RIGHT, 72);
		column.setLabelProvider(new DateLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(0, PropertyType.DATE,
						ActivityViewerComparator.RELEVANT_DATE, comparator,
						viewer));

		// Title / description
		column = ViewerUtils.createTableViewerColumn(viewer, "What", SWT.NONE,
				360);
		column.setLabelProvider(new TitleDescLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(1, PropertyType.STRING,
						Property.JCR_TITLE, comparator, viewer));

		// Assigned to
		column = ViewerUtils.createTableViewerColumn(viewer, "Assigned to",
				SWT.NONE, 100);
		column.setLabelProvider(new AssignedToLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(2, PropertyType.STRING,
						PeopleNames.PEOPLE_ASSIGNED_TO, comparator, viewer));

		// Related to
		column = ViewerUtils.createTableViewerColumn(viewer, "Related to",
				SWT.NONE, 140);
		column.setLabelProvider(new RelatedToLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(3, PropertyType.STRING,
						PeopleNames.PEOPLE_RELATED_TO, comparator, viewer));

		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE);
		// 2 times to force descending
		// comparator.setColumn(PropertyType.DATE,
		// MyTaskListComparator.RELEVANT_DATE);

		viewer.setComparator(comparator);

		return viewer;
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		List<Node> tasks = activityService.getMyTasks(session, true);
		tableViewer.setInput(tasks.toArray());
	}

	private class AssignedToLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					return ActivityJcrUtils.getAssignedToDisplayName(currNode);
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
					return ActivityJcrUtils
							.getActivityManagerDisplayName(currNode);
				}
				return "";
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to get date from node "
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
								CommonsJcrUtils.get(currReferenced,
										Property.JCR_TITLE)).append(", ");
					}
					return builder.toString();
				}
				return "";
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to get date from node "
						+ element, re);
			}
		}
	}

	private class TitleDescLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;

				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					String desc = CommonsJcrUtils.get(currNode,
							Property.JCR_TITLE)
							+ " - "
							+ CommonsJcrUtils.get(currNode,
									Property.JCR_DESCRIPTION);
					return desc;
				}
				return "";
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to get date from node "
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

	private class MyTableContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 7164029504991808317L;

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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
							peopleUiService.getOpenEntityEditorCmdId(),
							paramName, jcrId);
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot open user editor", e);
				}
			}
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.activityService = peopleService.getActivityService();
	}

	public void setRepository(Repository repository) {
		this.session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

}