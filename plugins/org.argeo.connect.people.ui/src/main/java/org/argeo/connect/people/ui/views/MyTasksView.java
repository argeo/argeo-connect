package org.argeo.connect.people.ui.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

public class MyTasksView extends ViewPart {
	public static final String ID = PeopleUiPlugin.PLUGIN_ID + ".myTasksView";

	/* DEPENDENCY INJECTION */
	// private PeopleService peopleService;
	private Session session;
	private ActivityService activityService;

	private TableViewer tableViewer;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder();
		layout.verticalSpacing = 5;
		parent.setLayout(layout);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);
		tableViewer.setContentProvider(new MyTableContentProvider());
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

		// TODO : add an icon depending on the task type and/or status ??
		// column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
		// 22);
		// column.setLabelProvider(new TypeLabelProvider());

		// Date
		column = ViewerUtils.createTableViewerColumn(viewer, "Due Date", SWT.RIGHT,
				65);
		column.setLabelProvider(new DateLabelProvider());

		// Title / description
		column = ViewerUtils.createTableViewerColumn(viewer, "What", SWT.NONE,
				360);
		column.setLabelProvider(new TitleDescLabelProvider());

		// Related to
		column = ViewerUtils.createTableViewerColumn(viewer, "Related to",
				SWT.NONE, 140);
		column.setLabelProvider(new RelatedToLabelProvider());

		return viewer;
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		try {

			// TODO implement getting my tasks only

			List<Node> nodes = JcrUtils
					.nodeIteratorToList(listFilteredElements(session, null));
			tableViewer.setInput(nodes.toArray());

		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list activities", e);
		}
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(PeopleTypes.PEOPLE_TASK,
				PeopleTypes.PEOPLE_ACTIVITY);

		Constraint defaultC = null;

		// Build constraints based the textArea filter content
		if (filter != null && !"".equals(filter.trim())) {
			// Parse the String
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
		}

		Ordering[] orderings = null;

		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);

		QueryResult result = query.execute();
		return result.getNodes();
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

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.activityService = peopleService.getActivityService();
	}

	public void setRepository(Repository repository) {
		this.session = CommonsJcrUtils.login(repository);
	}

}
