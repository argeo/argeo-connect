package org.argeo.connect.people.workbench.rap.editors.parts;

import static org.argeo.eclipse.ui.jcr.JcrUiUtils.getNodeSelectionAdapter;

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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.util.ActivityUtils;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleRapSnippets;
import org.argeo.connect.people.workbench.rap.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.workbench.rap.util.ActivityViewerComparator;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/** Basic implementation of a table that displays both activities and tasks */
public class ActivityTable extends Composite {
	private static final long serialVersionUID = 1L;

	private TableViewer tableViewer;
	private Session session;
	private Node entity;
	// private PeopleService peopleService;
	private ResourceService resourceService;
	private PeopleWorkbenchService peopleWorkbenchService;
	private ActivityService activityService;
	private UserAdminService userAdminService;

	// CONSTRUCTORS

	/**
	 * Default table with a filter
	 * 
	 * @param parent
	 * @param style
	 *            the style of the table
	 * @param session
	 */
	public ActivityTable(Composite parent, int style,
			PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node entity) {
		super(parent, SWT.NONE);
		session = ConnectJcrUtils.getSession(entity);
		// this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		activityService = peopleService.getActivityService();
		resourceService = peopleService.getResourceService();
		userAdminService = peopleService.getUserAdminService();
		this.entity = entity;

		this.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite tableComp = new Composite(this, SWT.NO_FOCUS);
		tableViewer = createActivityViewer(tableComp, style);
		tableComp.setLayoutData(EclipseUiUtils.fillAll());
		// refreshFilteredList();
	}

	private TableViewer createActivityViewer(final Composite parent, int style) {
		TableViewer viewer = new TableViewer(parent, SWT.V_SCROLL | style);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		Table table = viewer.getTable();
		table.setLayoutData(EclipseUiUtils.fillAll());
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 54);
		table.setHeaderVisible(false);
		table.setLinesVisible(true);

		Map<String, ColumnLabelProvider> lpMap = new HashMap<String, ColumnLabelProvider>();
		lpMap.put(PeopleNames.PEOPLE_ASSIGNED_TO, new UsersLabelProvider());
		lpMap.put(PeopleNames.PEOPLE_RELATED_TO, new AlsoRelatedToLP());

		ActivityViewerComparator comparator = new ActivityViewerComparator(
				activityService, lpMap);

		TableColumn col;
		TableViewerColumn tvCol;
		int colIndex = 0;

		// Types
		col = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(col,
				new ColumnWeightData(30, 120, true));
		tvCol = new TableViewerColumn(viewer, col);
		tvCol.setLabelProvider(new TypeLabelProvider());
		col.addSelectionListener(getNodeSelectionAdapter(colIndex++,
				PropertyType.STRING, Property.JCR_PRIMARY_TYPE, comparator,
				viewer));

		// Dates
		col = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(col,
				new ColumnWeightData(60, 145, true));
		tvCol = new TableViewerColumn(viewer, col);
		tvCol.setLabelProvider(new DateLabelProvider());
		col.addSelectionListener(getNodeSelectionAdapter(colIndex++,
				PropertyType.DATE, ActivityViewerComparator.RELEVANT_DATE,
				comparator, viewer));

		// relevant users
		col = new TableColumn(table, SWT.LEFT);
		col.setText("Reported by");
		col.addSelectionListener(getNodeSelectionAdapter(colIndex++,
				PropertyType.STRING, PeopleNames.PEOPLE_ASSIGNED_TO,
				comparator, viewer));
		tableColumnLayout.setColumnData(col,
				new ColumnWeightData(60, 180, true));
		tvCol = new TableViewerColumn(viewer, col);
		tvCol.setLabelProvider(new UsersLabelProvider());

		// Also related to
		col = new TableColumn(table, SWT.LEFT | SWT.WRAP);
		col.setText("Also related to");
		tableColumnLayout
				.setColumnData(col, new ColumnWeightData(80, 80, true));
		tvCol = new TableViewerColumn(viewer, col);
		tvCol.setLabelProvider(new AlsoRelatedToLP());
		col.setToolTipText("Also related to these entities");

		// Title / description
		col = new TableColumn(table, SWT.LEFT | SWT.WRAP);
		tableColumnLayout.setColumnData(col, new ColumnWeightData(200, 150,
				true));
		// col.addSelectionListener(ConnectJcrUtils.getNodeSelectionAdapter(colIndex++,
		// PropertyType.STRING, Property.JCR_TITLE, comparator, viewer));
		tvCol = new TableViewerColumn(viewer, col);
		tvCol.setLabelProvider(new TitleDescLabelProvider());

		//
		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE);
		// 2 times to force descending
		comparator.setColumn(PropertyType.DATE,
				ActivityViewerComparator.RELEVANT_DATE);
		viewer.setComparator(comparator);

		table.addSelectionListener(new HtmlListRwtAdapter());
		// EclipseUiSpecificUtils.enableToolTipSupport(viewer);
		viewer.setContentProvider(new MyTableContentProvider());

		// Warning: don't forget to set the generated layout.
		parent.setLayout(tableColumnLayout);
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
			throw new PeopleException("Unable to list activities", e);
		}
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		String xpathQueryStr = "//element(*, " + PeopleTypes.PEOPLE_ACTIVITY
				+ ")";
		String attrQuery = XPathUtils.getFreeTextConstraint(filter);
		if (EclipseUiUtils.notEmpty(attrQuery))
			xpathQueryStr += "[" + attrQuery + "]";
		Query xpathQuery = queryManager.createQuery(xpathQueryStr,
				ConnectConstants.QUERY_XPATH);
		QueryResult result = xpathQuery.execute();
		return result.getNodes();
	}

	// /////////////////////////
	// LOCAL CLASSES
	private class TypeLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node currNode = (Node) element;

			try {
				StringBuilder builder = new StringBuilder();
				if (currNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					builder.append("<b>");
					builder.append(resourceService
							.getItemDefaultEnLabel(currNode
									.getPrimaryNodeType().getName()));
					builder.append("</b>");
					builder.append("<br />");
					builder.append(ConnectJcrUtils.get(currNode,
							PeopleNames.PEOPLE_TASK_STATUS));

				} else if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
					builder.append("<b>");
					builder.append(activityService.getActivityLabel(currNode));
					// specific for rate
					if (currNode.isNodeType(PeopleTypes.PEOPLE_RATE)) {
						Long rate = ConnectJcrUtils.getLongValue(currNode,
								PeopleNames.PEOPLE_RATE);
						if (rate != null)
							builder.append(": " + rate);
					}
					builder.append("</b>");
				}
				return builder.toString();
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get type snippet for "
						+ currNode, re);
			}
		}
	}

	private class DateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node activityNode = (Node) element;
			try {
				Calendar date = null;
				StringBuilder builder = new StringBuilder();
				// VARIOUS WF LAYOUT
				if (activityNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					// done task
					if (activityNode.hasProperty(PeopleNames.PEOPLE_CLOSE_DATE)) {
						date = activityNode.getProperty(
								PeopleNames.PEOPLE_CLOSE_DATE).getDate();
						builder.append(funkyFormat(date))
								.append(" (Done date)").append("<br />");

						if (activityNode
								.hasProperty(PeopleNames.PEOPLE_DUE_DATE)) {
							date = activityNode.getProperty(
									PeopleNames.PEOPLE_DUE_DATE).getDate();
							builder.append(funkyFormat(date)).append(
									" (Due date)");

						}
					} else if (activityNode
							.hasProperty(PeopleNames.PEOPLE_DUE_DATE)) {
						date = activityNode.getProperty(
								PeopleNames.PEOPLE_DUE_DATE).getDate();
						builder.append(funkyFormat(date)).append(" (Due date)")
								.append("<br />");

						boolean sleeping = false;
						if (activityNode
								.hasProperty(PeopleNames.PEOPLE_WAKE_UP_DATE)) {
							date = activityNode.getProperty(
									PeopleNames.PEOPLE_WAKE_UP_DATE).getDate();
							Calendar now = GregorianCalendar.getInstance();
							if (date.after(now)) {
								builder.append(funkyFormat(date)).append(
										" (Sleep until)");
								sleeping = true;
							}
						}

						if (activityNode
								.hasProperty(Property.JCR_LAST_MODIFIED)
								&& !sleeping) {
							date = activityNode.getProperty(
									Property.JCR_LAST_MODIFIED).getDate();
							builder.append(funkyFormat(date)).append(
									" (Last update)");
						}
					} else {
						if (activityNode
								.hasProperty(Property.JCR_LAST_MODIFIED)) {
							date = activityNode.getProperty(
									Property.JCR_LAST_MODIFIED).getDate();
							builder.append(funkyFormat(date))
									.append(" (Last update)").append("<br />");
						}

						if (activityNode
								.hasProperty(PeopleNames.PEOPLE_ACTIVITY_DATE)) {
							date = activityNode.getProperty(
									PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
							builder.append(funkyFormat(date)).append(
									" (Creation date)");
						} else if (activityNode
								.hasProperty(Property.JCR_CREATED)) {
							date = activityNode.getProperty(
									Property.JCR_CREATED).getDate();
							builder.append(funkyFormat(date)).append(
									" (Creation date)");
						}
					}
				} else if (activityNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
					Calendar happened = null;
					// Calendar created = null;
					String happenedLbl = "";
					Calendar lastMod = null;
					if (activityNode.hasProperty(Property.JCR_LAST_MODIFIED))
						lastMod = activityNode.getProperty(
								Property.JCR_LAST_MODIFIED).getDate();

					if (activityNode
							.hasProperty(PeopleNames.PEOPLE_ACTIVITY_DATE)) {
						happened = activityNode.getProperty(
								PeopleNames.PEOPLE_ACTIVITY_DATE).getDate();
						happenedLbl = " (Done date)";
					} else if (activityNode.hasProperty(Property.JCR_CREATED)) {
						happened = activityNode.getProperty(
								Property.JCR_CREATED).getDate();
						happenedLbl = " (Creation date)";
					}
					boolean addUpdateDt = happened == null;
					if (!addUpdateDt) {
						date = (Calendar) happened.clone();
						date.add(Calendar.MINUTE, 5);
						if (lastMod != null)
							addUpdateDt = lastMod.after(date);
					}
					if (addUpdateDt)
						builder.append(funkyFormat(lastMod))
								.append(" (Last update)").append("<br />");
					if (happened != null)
						builder.append(funkyFormat(happened)).append(
								happenedLbl);
				}
				return builder.toString();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to get date label for "
						+ activityNode, e);
			}
		}
	}

	private String getDisplayName(String id) {
		return userAdminService.getUserDisplayName(id);
	}

	private String getDNameFromProp(Node node, String propName) {
		String id = ConnectJcrUtils.get(node, propName);
		if (EclipseUiUtils.notEmpty(id))
			return userAdminService.getUserDisplayName(id);
		return "";
	}

	private class UsersLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node activityNode = (Node) element;
			try {
				String value = "";
				StringBuilder builder = new StringBuilder();
				if (activityNode.isNodeType(PeopleTypes.PEOPLE_TASK)) {
					// done task
					if (activityService.isTaskDone(activityNode)) {
						value = getDNameFromProp(activityNode,
								PeopleNames.PEOPLE_CLOSED_BY);
						if (EclipseUiUtils.notEmpty(value))
							builder.append(value).append(" (Closed by)")
									.append("<br />");
						value = activityService
								.getAssignedToDisplayName(activityNode);
						if (EclipseUiUtils.notEmpty(value))
							builder.append(value).append(" (Assignee)")
									.append("<br />");
					} else {
						value = activityService
								.getAssignedToDisplayName(activityNode);
						if (EclipseUiUtils.notEmpty(value))
							builder.append(value).append(" (Assignee)")
									.append("<br />");

						value = ConnectJcrUtils.get(activityNode,
								Property.JCR_LAST_MODIFIED_BY);
						if (EclipseUiUtils.notEmpty(value))
							builder.append(getDisplayName(value)).append(
									" (Last updater)");
					}
				} else if (activityNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)) {
					String reporter = getDNameFromProp(activityNode,
							PeopleNames.PEOPLE_REPORTED_BY);
					String updater = getDNameFromProp(activityNode,
							Property.JCR_LAST_MODIFIED_BY);

					if (EclipseUiUtils.isEmpty(reporter))
						reporter = getDNameFromProp(activityNode,
								Property.JCR_CREATED_BY);

					if (EclipseUiUtils.notEmpty(reporter))
						builder.append(reporter).append(" (Reporter)")
								.append("<br />");
					if (EclipseUiUtils.notEmpty(updater)
							&& (reporter == null || !reporter.equals(updater)))
						builder.append(updater).append(" (Last updater)")
								.append("<br />");
				}
				return builder.toString();
			} catch (RepositoryException e) {
				throw new PeopleException(
						"Unable to get related users snippet for "
								+ activityNode, e);
			}
		}
	}

	private class AlsoRelatedToLP extends ColumnLabelProvider {
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
						String currEntityId = null;
						if (entity != null)
							currEntityId = entity.getIdentifier();
						for (Value value : refs) {
							String id = value.getString();
							if (!id.equals(currEntityId)) {
								Node currReferenced = session
										.getNodeByIdentifier(id);
								String label = PeopleRapSnippets
										.getOpenEditorSnippet(
												peopleWorkbenchService
														.getOpenEntityEditorCmdId(),
												currReferenced, ConnectJcrUtils.get(
														currReferenced,
														Property.JCR_TITLE));
								builder.append(label).append(", ");
							}
						}
						if (builder.lastIndexOf(", ") != -1) {
							String value = ConnectUiUtils
									.replaceAmpersand(builder.substring(0,
											builder.lastIndexOf(", ")));
							return wrapThis(value);
						}
					}

				}
				return "";
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get date from node "
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
					String title = ConnectJcrUtils.get(currNode, Property.JCR_TITLE);
					// Specific behaviour for polls
					if (currNode.isNodeType(PeopleTypes.PEOPLE_POLL)) {
						title = ConnectJcrUtils.get(currNode,
								PeopleNames.PEOPLE_POLL_NAME)
								+ ": "
								+ ActivityUtils.getAvgRating(currNode);
					}

					String desc = ConnectJcrUtils.get(currNode,
							Property.JCR_DESCRIPTION);
					String res = ConnectJcrUtils
							.concatIfNotEmpty(title, desc, " - ");
					return wrapThis(res);
				}
				return "";
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to get date from node "
						+ element, re);
			}
		}
	}

	private DateFormat todayFormat = new SimpleDateFormat("HH:mm");
	private DateFormat inMonthFormat = new SimpleDateFormat("dd MMM");
	private DateFormat dateFormat = new SimpleDateFormat(
			ConnectUiConstants.DEFAULT_SHORT_DATE_FORMAT);

	private String funkyFormat(Calendar date) {
		Calendar now = GregorianCalendar.getInstance();
		if (date.get(Calendar.YEAR) == now.get(Calendar.YEAR)
				&& date.get(Calendar.MONTH) == now.get(Calendar.MONTH))
			if (date.get(Calendar.DAY_OF_MONTH) == now
					.get(Calendar.DAY_OF_MONTH))
				return todayFormat.format(date.getTime());
			else
				return inMonthFormat.format(date.getTime());
		else
			return dateFormat.format(date.getTime());

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

	private final String LIST_WRAP_STYLE = "style='float:left;padding:0px;white-space:pre-wrap;'";

	private String wrapThis(String value) {
		String wrapped = "<span " + LIST_WRAP_STYLE + " >"
				+ ConnectUiUtils.replaceAmpersand(value) + "</span>";
		return wrapped;
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