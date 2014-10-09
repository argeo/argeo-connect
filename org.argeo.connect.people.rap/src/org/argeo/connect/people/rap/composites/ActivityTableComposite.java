package org.argeo.connect.people.rap.composites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
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
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.ActivitiesImages;
import org.argeo.connect.people.rap.utils.ActivityViewerComparator;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.ActivityJcrUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.JcrUiUtils;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/** Basic implementation of a table that displays both activities and tasks */
public class ActivityTableComposite extends Composite implements ArgeoNames {
	private static final long serialVersionUID = 1L;

	private TableViewer tableViewer;
	private Session session;
	private int tableStyle;
	private ActivityService activityService;

	// CONSTRUCTORS

	/**
	 * Default table with a filter
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public ActivityTableComposite(Composite parent, int style, Session session,
			ActivityService activityService) {
		super(parent, SWT.NONE);
		this.tableStyle = style;
		this.session = session;
		this.activityService = activityService;
	}

	/** Must be called immediately after creation */
	public void populate() {
		Composite parent = this;
		GridLayout layout = PeopleUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		tableViewer = createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(tableViewer);
		tableViewer.setContentProvider(new MyTableContentProvider());
		refreshFilteredList();
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private TableViewer createTableViewer(final Composite parent) {
		Table table = new Table(parent, tableStyle);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;

		Map<String, ColumnLabelProvider> lpMap = new HashMap<String, ColumnLabelProvider>();
		lpMap.put(PeopleNames.PEOPLE_ASSIGNED_TO, new ManagerLabelProvider());
		lpMap.put(PeopleNames.PEOPLE_RELATED_TO, new RelatedToLabelProvider());

		ActivityViewerComparator comparator = new ActivityViewerComparator(
				activityService, lpMap);

		// Activity type: mail, note... todo or task
		// TODO add icon to display activity type :
		column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 22);
		column.setLabelProvider(new TypeLabelProvider());

		int colIndex = 1; // eases change of below columns order

		// Date
		column = ViewerUtils.createTableViewerColumn(viewer, "Date", SWT.RIGHT,
				75);
		column.setLabelProvider(new DateLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(colIndex++,
						PropertyType.DATE,
						ActivityViewerComparator.RELEVANT_DATE, comparator,
						viewer));

		// Manager
		column = ViewerUtils.createTableViewerColumn(viewer, "Manager",
				SWT.NONE, 100);
		column.setLabelProvider(new ManagerLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(colIndex++,
						PropertyType.STRING, PeopleNames.PEOPLE_ASSIGNED_TO,
						comparator, viewer));

		// Related to
		column = ViewerUtils.createTableViewerColumn(viewer, "Related to",
				SWT.NONE, 140);
		column.setLabelProvider(new RelatedToLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(colIndex++,
						PropertyType.STRING, PeopleNames.PEOPLE_RELATED_TO,
						comparator, viewer));

		// Title / description
		column = ViewerUtils.createTableViewerColumn(viewer, "Content",
				SWT.NONE, 360);
		column.setLabelProvider(new TitleDescLabelProvider());
		column.getColumn().addSelectionListener(
				JcrUiUtils.getNodeSelectionAdapter(colIndex++,
						PropertyType.STRING, Property.JCR_TITLE, comparator,
						viewer));

		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE);
		// 2 times to force descending
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
		try {
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

		Selector source = factory.selector(PeopleTypes.PEOPLE_ACTIVITY,
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

	// /////////////////////////
	// LOCAL CLASSES
	private class TypeLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public Image getImage(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					return ActivitiesImages.TODO;
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_NOTE)) {
					return ActivitiesImages.NOTE;
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_SENT_EMAIL)) {
					return ActivitiesImages.SENT_MAIL;
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_CALL)) {
					return ActivitiesImages.PHONE_CALL;
				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_SENT_FAX)) {
					return ActivitiesImages.SENT_FAX;
				} else
					return null;
				// TODO implement all types.
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to get date from node "
						+ element, re);
			}
		}

		@Override
		public String getText(Object element) {
			return "";
		}
	}

	private class ManagerLabelProvider extends ColumnLabelProvider {
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
					if (refs.length > 0) {
						for (Value value : refs) {
							String id = value.getString();
							Node currReferenced = session
									.getNodeByIdentifier(id);
							builder.append(
									CommonsJcrUtils.get(currReferenced,
											Property.JCR_TITLE)).append(", ");
						}
						return builder.substring(0, builder.lastIndexOf(", "));
					}

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

				if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
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

	// //////////////////////
	// Life cycle management
	@Override
	public boolean setFocus() {
		tableViewer.getTable().setFocus();
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void refresh() {
		refreshFilteredList();
	}
}