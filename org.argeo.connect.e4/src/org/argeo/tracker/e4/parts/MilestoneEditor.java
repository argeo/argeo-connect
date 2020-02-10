package org.argeo.tracker.e4.parts;

import static org.argeo.activities.ActivitiesNames.ACTIVITIES_DUE_DATE;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.AssignedToLP;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectWorkbenchUtils;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.UserNameLP;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.TrackerLps;
import org.argeo.tracker.ui.controls.RepartitionChart;
import org.argeo.tracker.ui.dialogs.ConfigureMilestoneWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** Default editor to display and edit a Tracker's milestone */
public class MilestoneEditor extends AbstractTrackerEditor implements IJcrTableViewer {
	// private static final long serialVersionUID = -4872303290083584882L;

	// public static final String ID = TrackerUiPlugin.PLUGIN_ID +
	// ".milestoneEditor";

	// Context
	private Node project;
	private Node milestone;
	private String milestoneUid;

	// Ease implementation
	private Text filterTxt;
	private Button onlyMineBtn;
	private Button onlyOpenBtn;
	private Button overdueBtn;
	private AbstractFormPart issueListPart;

	// local parameters
	private final static int CHART_DATA_LIMIT = 8;
	private final static int CHART_WIDTH = 300;
	private final static int CHART_HEIGHT = 200;

	// @Override
	// public void init(IEditorSite site, IEditorInput input) throws
	// PartInitException {
	// super.init(site, input);
	// setTitleImage(ConnectImages.MILESTONE);
	// }

	@Override
	protected void addPages() {
		// Initialise local cache
		milestone = getNode();
		project = getAppService().getEntityByUid(ConnectJcrUtils.getSession(milestone), null,
				ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_PROJECT_UID));
		milestoneUid = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID);
		try {
			addPage(new MainPage(this));

			// if (isInRole(ROLE_ADMIN))
			// addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (Exception e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	private class MainPage extends AbstractEditorPage {
		public final static String PAGE_ID = ".mainPage";

		private Link projectLk;
		private Link managerLk;
		private Link overdueTasksLk;
		private Link dueDateLk;
		private Label dueDateLbl;
		private Composite chartCmp;
		private Label descLbl;

		private TableViewer tableViewer;

		public MainPage(AbstractTrackerEditor editor) {
			super(editor, PAGE_ID, "Overview");
		}

		protected void createFormContent(Composite body) {
			// ScrolledForm form = mf.getForm();
			// Composite body = form.getBody();
			// ScrolledComposite form = mf.getForm();
			// Composite body = new Composite(form, SWT.NONE);
			body.setLayout(new GridLayout());

//			new Label(body, SWT.BORDER).setText("TEST BODY");

			Composite overview = appendOverviewPart(body);
			// overview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendIssuesPart(tableCmp);
			GridData twd = new GridData(SWT.FILL, SWT.FILL, false, false);
			twd.heightHint = 300;
			tableCmp.setLayoutData(twd);

			// form.reflow(true);
		}

		private Composite appendOverviewPart(Composite parent) {
			FormToolkit tk = getFormToolkit();

			// final Section section = TrackerUiUtils.addFormSection(tk, parent,
			// getMilestoneTitle());

			Composite body = new Composite(parent, SWT.BORDER);
			body.setLayoutData(CmsUtils.fillAll());
			GridLayout layout = new GridLayout(2, false);
			// layout.numColumns = 2;
			body.setLayout(layout);

			// Project
			createFormBoldLabel(tk, body, "Project");
			projectLk = new Link(body, SWT.NONE);
			projectLk.setText("TEST LINK");
			projectLk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			configureOpenLink(projectLk, project);

			// Manager
			createFormBoldLabel(tk, body, "Manager");
			managerLk = new Link(body, SWT.NONE);
			managerLk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			// Chart
			chartCmp = new Composite(body, SWT.NO_FOCUS);
			chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			GridData twd = new GridData(SWT.FILL, SWT.FILL, false, false);
			twd.widthHint = CHART_WIDTH;
			twd.horizontalSpan = 2;
			twd.verticalSpan = 3;
			chartCmp.setLayoutData(twd);

			// Overdue tasks
			createFormBoldLabel(tk, body, "Overdue Tasks");
			overdueTasksLk = new Link(body, SWT.NONE);
			overdueTasksLk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			// Due Date
			dueDateLbl = createFormBoldLabel(tk, body, "Due Date");
			dueDateLk = new Link(body, SWT.NONE);
			dueDateLk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			// // Reported by
			// TrackerUiUtils.createFormBoldLabel(tk, body, "Reported by");
			// reporterLk = new Link(body, SWT.NONE);
			// reporterLk.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

			// TODO add linked documents

			// Description
			twd = (GridData) createFormBoldLabel(tk, body, "Details").getLayoutData();
			// twd.valign = GridData.TOP;
			descLbl = new Label(body, SWT.WRAP);
			twd = new GridData(SWT.FILL, SWT.FILL, false, false);
			// twd.colspan = 3;
			descLbl.setLayoutData(twd);

			SectionPart part = new SectionPart(body.getParent()) {

				@Override
				public void refresh() {
					String managerId = ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_MANAGER);
					if (EclipseUiUtils.notEmpty(managerId))
						managerLk.setText(getUserAdminService().getUserDisplayName(managerId));
					else
						managerLk.setText("");

					String dueDateStr = ConnectJcrUtils.getDateFormattedAsString(milestone,
							TrackerNames.TRACKER_TARGET_DATE, ConnectConstants.DEFAULT_DATE_TIME_FORMAT);
					if (EclipseUiUtils.notEmpty(dueDateStr))
						dueDateLk.setText(dueDateStr);

					String desc = ConnectJcrUtils.get(milestone, Property.JCR_DESCRIPTION);
					descLbl.setText(desc);

					// The chart
					CmsUtils.clear(chartCmp);
					GridData twd = (GridData) chartCmp.getLayoutData();
					Map<String, String> ot = TrackerUtils.getOpenTasksByAssignee(getUserAdminService(), project,
							milestoneUid, CHART_DATA_LIMIT);
					if (ot == null || ot.isEmpty()) {
						Label lbl = new Label(chartCmp, SWT.CENTER);
						lbl.setFont(EclipseUiUtils.getItalicFont(body));
						lbl.setText("No open task has been found for this milestone.");
						twd.heightHint = SWT.DEFAULT;
					} else {
						RepartitionChart coc = new RepartitionChart(chartCmp, SWT.NO_FOCUS);
						twd.heightHint = CHART_HEIGHT;
						coc.setLayoutData(EclipseUiUtils.fillAll());
						coc.setInput("Repartition by assignee - " + getOpenTaskNumber() + " open tasks", ot,
								CHART_WIDTH, CHART_HEIGHT);
					}

					// Overdue tasks
					Long nb = getOverdueTaskNumber();
					overdueTasksLk.setText(nb < 0 ? "-" : nb.toString());

					String closedOn = ConnectJcrUtils.getDateFormattedAsString(milestone,
							ConnectNames.CONNECT_CLOSE_DATE, ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

					if (EclipseUiUtils.notEmpty(closedOn)) {
						dueDateLbl.setText("Close date");
						dueDateLk.setText(closedOn);
					}

					parent.layout(true, true);
					body.setFocus();
					super.refresh();
				}
			};
			getManagedForm().addPart(part);
			// addMainSectionMenu(part);

			return body;
		}

		private long getOpenTaskNumber() {
			StringBuilder builder = new StringBuilder();
			try {
				builder.append(XPathUtils.descendantFrom(project.getPath()));
				builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_TASK).append(")");
				String milestoneCond = XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MILESTONE_UID, milestoneUid);
				String notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";
				builder.append("[").append(XPathUtils.localAnd(milestoneCond, notClosedCond)).append("]");
				Query query = XPathUtils.createQuery(getSession(), builder.toString());
				return query.execute().getNodes().getSize();
			} catch (RepositoryException e) {
				throw new ActivitiesException("Unable to get overdue tasks number for " + milestone);
			}
		}

		private long getOverdueTaskNumber() {
			StringBuilder builder = new StringBuilder();
			try {
				builder.append(XPathUtils.descendantFrom(project.getPath()));
				builder.append("//element(*, ").append(TrackerTypes.TRACKER_ISSUE).append(")");

				String milestoneCond = XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MILESTONE_UID, milestoneUid);

				// Past due date
				Calendar now = GregorianCalendar.getInstance();
				String overdueCond = XPathUtils.getPropertyDateComparaison(ACTIVITIES_DUE_DATE, now, "<");
				// Only opened tasks
				String notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";

				builder.append("[").append(XPathUtils.localAnd(milestoneCond, overdueCond, notClosedCond)).append("]");
				Query query = XPathUtils.createQuery(getSession(), builder.toString());
				return query.execute().getNodes().getSize();
			} catch (RepositoryException e) {
				throw new ActivitiesException("Unable to get overdue tasks number for " + milestone);
			}
		}

		/** Creates the list of issues relevant for this category */
		private void appendIssuesPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_ID), "ID", 40));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(Property.JCR_TITLE), "Title", 300));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS),
					"Status", 100));
			columnDefs.add(new ColumnDefinition(
					new TrackerLps().new DnLabelProvider(getUserAdminService(), ActivitiesNames.ACTIVITIES_ASSIGNED_TO),
					"Assignee", 160));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));

			// Create and configure the table
			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
					// String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
					// CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
					// ConnectEditor.PARAM_JCR_ID, jcrId);
					getAppWorkbenchService().openEntityEditor((Node) element);
				}
			});

			issueListPart = new AbstractFormPart() {
				@Override
				public void refresh() {
					refreshViewer(filterTxt.getText());
					// mf.getForm().reflow(true);
					super.refresh();
				}
			};
			getManagedForm().addPart(issueListPart);
		}

		public void setActive(boolean active) {
			issueListPart.markStale();
			super.setActive(active);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = getTasks();
			tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
			tableViewer.refresh();
		}

		// Add the filter ability
		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(4, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			filterTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 8130545587125370689L;

				public void modifyText(ModifyEvent event) {
					refreshViewer(filterTxt.getText());
				}
			});

			SelectionAdapter adapter = new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshViewer(filterTxt.getText());
				}
			};

			onlyMineBtn = new Button(parent, SWT.CHECK);
			onlyMineBtn.setText("Only mine ");
			onlyMineBtn.addSelectionListener(adapter);
			onlyOpenBtn = new Button(parent, SWT.CHECK);
			onlyOpenBtn.setText("Only open ");
			onlyOpenBtn.addSelectionListener(adapter);
			overdueBtn = new Button(parent, SWT.CHECK);
			overdueBtn.setText("Overdue ");
			overdueBtn.addSelectionListener(adapter);

		}

		// SECTION MENU
		private void addMainSectionMenu(SectionPart sectionPart) {
			ToolBarManager toolBarManager = TrackerUiUtils.addMenu(sectionPart.getSection());

			String tooltip = "Mark this milestone as closed";
			Action action = new CloseMilestone(tooltip, ConnectImages.IMG_DESC_CLOSE, sectionPart);
			toolBarManager.add(action);

			tooltip = "Edit the milestone main information";
			action = new OpenConfigureDialog(tooltip, ConnectImages.IMG_DESC_EDIT, sectionPart);
			toolBarManager.add(action);

			tooltip = "Add a task to this milestone";
			action = new AddTask(sectionPart.getSection().getShell(), tooltip, ConnectImages.IMG_DESC_ADD);
			toolBarManager.add(action);

			toolBarManager.update(true);

		}

		// MENU ACTIONS
		private class OpenConfigureDialog extends Action {
			private static final long serialVersionUID = -6798429720348536525L;
			private final SectionPart sectionPart;

			private OpenConfigureDialog(String name, ImageDescriptor img, SectionPart sectionPart) {
				super(name, img);
				this.sectionPart = sectionPart;
			}

			@Override
			public void run() {
				Shell currShell = sectionPart.getSection().getShell();
				ConfigureMilestoneWizard wizard = new ConfigureMilestoneWizard(getUserAdminService(),
						getTrackerService(), milestone);
				WizardDialog dialog = new WizardDialog(currShell, wizard);
				try {
					if (dialog.open() == Window.OK && milestone.getSession().hasPendingChanges()) {
						updatePartName();
						sectionPart.getSection().setText(getMilestoneTitle());
						sectionPart.refresh();
						sectionPart.markDirty();
						sectionPart.getSection().setFocus();
					}
				} catch (RepositoryException e) {
					throw new TrackerException("Cannot check session state on " + milestone, e);
				}
			}
		}

		private class CloseMilestone extends Action {
			private static final long serialVersionUID = -6798429720348536525L;
			private final SectionPart sectionPart;

			private CloseMilestone(String name, ImageDescriptor img, SectionPart sectionPart) {
				super(name, img);
				this.sectionPart = sectionPart;
			}

			@Override
			public void run() {
				Shell currShell = sectionPart.getSection().getShell();
				boolean doIt = MessageDialog.openConfirm(currShell, "Confirm before close",
						"Are you sure you want to close milestone ["
								+ ConnectJcrUtils.get(milestone, Property.JCR_TITLE) + "] ?");
				try {
					if (doIt) {
						milestone.setProperty(ConnectNames.CONNECT_CLOSE_DATE, new GregorianCalendar());
						milestone.setProperty(ConnectNames.CONNECT_CLOSED_BY, milestone.getSession().getUserID());
						sectionPart.getSection().setText(getMilestoneTitle());
						sectionPart.refresh();
						sectionPart.markDirty();
						sectionPart.getSection().setFocus();
					}
				} catch (RepositoryException e) {
					throw new TrackerException("Cannot check session state on " + milestone, e);
				}
			}
		}

		private class AddTask extends Action {
			private static final long serialVersionUID = 5112793747049604434L;
			final Shell shell;

			private AddTask(Shell shell, String name, ImageDescriptor img) {
				super(name, img);
				this.shell = shell;
			}

			@Override
			public void run() {
				Session session = ConnectJcrUtils.getSession(project);
				String mainMixin = ConnectJcrUtils.isNodeType(project, TrackerTypes.TRACKER_IT_PROJECT)
						? TrackerTypes.TRACKER_ISSUE
						: TrackerTypes.TRACKER_TASK;
				String propName1 = TrackerNames.TRACKER_PROJECT_UID;
				String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
				String propName2 = TrackerNames.TRACKER_MILESTONE_UID;
				String value2 = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID);

				String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(shell, session, getTrackerService(),
						getAppWorkbenchService(), mainMixin, propName1, value1, propName2, value2);
				if (EclipseUiUtils.notEmpty(pathCreated))
					refreshViewer(filterTxt.getText());
			}
		}
	}

	@Override
	public Object[] getElements(String extractId) {
		return JcrUtils.nodeIteratorToList(getTasks()).toArray(new Node[0]);
	}

	public NodeIterator getTasks() {
		String filter = filterTxt.getText();
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(project.getPath()));
			builder.append("//element(*, ").append(TrackerTypes.TRACKER_TASK).append(")");

			String milestoneCond = XPathUtils.getPropertyEquals(TrackerNames.TRACKER_MILESTONE_UID, milestoneUid);

			String onlyMineCond = null;
			if (onlyMineBtn.getSelection()) {
				List<String> normalisedRoles = new ArrayList<>();
				for (String role : CurrentUser.roles())
					normalisedRoles.add(TrackerUtils.normalizeDn(role));
				String[] nrArr = normalisedRoles.toArray(new String[0]);
				StringBuilder tmpBuilder = new StringBuilder();
				for (String role : nrArr) {
					String attrQuery = XPathUtils.getPropertyEquals(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, role);
					tmpBuilder.append(attrQuery).append(" or ");
				}
				if (tmpBuilder.length() > 4)
					onlyMineCond = "(" + tmpBuilder.substring(0, tmpBuilder.length() - 3) + ")";
			}

			String overdueCond = null;
			if (overdueBtn.getSelection()) {
				Calendar now = GregorianCalendar.getInstance();
				overdueCond = XPathUtils.getPropertyDateComparaison(ACTIVITIES_DUE_DATE, now, "<");
			}

			String notClosedCond = null;
			if (onlyOpenBtn.getSelection())
				notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";

			String ftcCond = null;
			if (EclipseUiUtils.notEmpty(filter))
				ftcCond = XPathUtils.getFreeTextConstraint(filter);

			String fullCond = XPathUtils.localAnd(milestoneCond, overdueCond, notClosedCond, ftcCond, onlyMineCond);
			builder.append("[").append(fullCond).append("]");
			builder.append(" order by @" + TrackerNames.TRACKER_ID);

			Query query = XPathUtils.createQuery(getSession(), builder.toString());
			return query.execute().getNodes();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to get filtered tasks for " + milestone + " with filter: " + filter, e);
		}
	}

	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();
		columns.add(new ConnectColumnDefinition("ID", new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_ID)));
		columns.add(
				new ConnectColumnDefinition("Status", new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS)));
		columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE)));
		columns.add(new ConnectColumnDefinition("Description", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
		columns.add(new ConnectColumnDefinition("Assigned To",
				new AssignedToLP(getActivitiesService(), null, Property.JCR_DESCRIPTION)));
		columns.add(
				new ConnectColumnDefinition("Due Date", new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE)));
		columns.add(new ConnectColumnDefinition("Importance", new TrackerLps().new ImportanceLabelProvider()));
		columns.add(new ConnectColumnDefinition("Priority", new TrackerLps().new PriorityLabelProvider()));
		columns.add(new ConnectColumnDefinition("Components",
				new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_COMPONENT_IDS)));

		columns.add(new ConnectColumnDefinition("Wake-Up Date",
				new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE)));
		columns.add(
				new ConnectColumnDefinition("Close Date", new JcrRowLabelProvider(ConnectNames.CONNECT_CLOSE_DATE)));
		columns.add(new ConnectColumnDefinition("Closed by",
				new UserNameLP(getUserAdminService(), null, ConnectNames.CONNECT_CLOSED_BY)));
		return columns;
	}

	/* LOCAL HELPERS */

	private String getMilestoneTitle() {
		String id = ConnectJcrUtils.get(getNode(), TrackerNames.TRACKER_ID);
		String name = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);

		String title = null;
		if (notEmpty(id) && notEmpty(id))
			if (id.equals(name))
				title = "v" + id;
			else
				title = "v" + id + "  aka " + name;
		else if (notEmpty(id))
			title = "v" + id;
		else
			title = name;

		if (notEmpty(title)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			title = title + (notEmpty(pname) ? " (" + pname + ")" : "");
		}

		String closedBy = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_CLOSED_BY);
		String closedOn = ConnectJcrUtils.getDateFormattedAsString(milestone, ConnectNames.CONNECT_CLOSE_DATE,
				ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

		if (EclipseUiUtils.notEmpty(closedOn))
			title += " closed on " + closedOn + " by " + getUserAdminService().getUserDisplayName(closedBy);
		return title;
	}

	// private Label createFormBoldLabel(FormToolkit toolkit, Composite parent,
	// String value) {
	// Label label = toolkit.createLabel(parent, " " + value, SWT.END);
	// label.setFont(EclipseUiUtils.getBoldFont(parent));
	// GridData twd = new GridData(SWT.FILL, SWT.FILL, false, false);
	// label.setLayoutData(twd);
	// return label;
	// }

	private void configureOpenLink(Link link, Node targetNode) {
		link.setText("<a>" + ConnectJcrUtils.get(targetNode, Property.JCR_TITLE) + "</a>");

		// Remove existing if necessary
		Listener[] existings = link.getListeners(SWT.Selection);
		for (Listener l : existings)
			link.removeListener(SWT.Selection, l);

		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				// CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
				// ConnectEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(targetNode));
				getAppWorkbenchService().openEntityEditor(targetNode);
			}
		});
	}
}
