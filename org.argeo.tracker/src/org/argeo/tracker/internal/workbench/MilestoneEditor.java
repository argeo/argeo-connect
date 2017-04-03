package org.argeo.tracker.internal.workbench;

import static org.argeo.activities.ActivitiesNames.ACTIVITIES_DUE_DATE;
import static org.argeo.cms.auth.CurrentUser.isInRole;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;
import static org.argeo.node.NodeConstants.ROLE_ADMIN;
import static org.eclipse.ui.forms.widgets.TableWrapData.BOTTOM;
import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

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
import org.argeo.cms.ArgeoNames;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.UserNameLP;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.connect.workbench.TechnicalInfoPage;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.TrackerLps;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.controls.RepartitionChart;
import org.argeo.tracker.internal.ui.dialogs.ConfigureMilestoneWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureTaskWizard;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/** Default editor to display and edit a Tracker's milestone */
public class MilestoneEditor extends AbstractTrackerEditor implements IJcrTableViewer {
	private static final long serialVersionUID = -4872303290083584882L;

	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".milestoneEditor";

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

	@Override
	protected void addPages() {
		// Initialise local cache
		milestone = getNode();
		project = getAppService().getEntityByUid(ConnectJcrUtils.getSession(milestone), null,
				ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_PROJECT_UID));
		milestoneUid = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID);
		try {

			addPage(new MainPage(this));

			if (isInRole(ROLE_ADMIN))
				addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	private class MainPage extends FormPage implements ArgeoNames {
		public final static String PAGE_ID = MilestoneEditor.ID + ".mainPage";

		private Link projectLk;
		private Link managerLk;
		private Link overdueTasksLk;
		private Link dueDateLk;
		private Composite chartCmp;
		private Label descLbl;

		private TableViewer tableViewer;

		public MainPage(FormEditor editor) {
			super(editor, PAGE_ID, "Overview");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			body.setLayout(new TableWrapLayout());

			Composite overview = appendOverviewPart(body, mf);
			overview.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

			Composite issues = appendIssuePart(body, mf);
			issues.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

			form.reflow(true);
		}

		private Composite appendOverviewPart(Composite parent, IManagedForm mf) {
			FormToolkit tk = mf.getToolkit();

			final Section section = TrackerUiUtils.addFormSection(tk, parent, getMilestoneTitle());
			// section.setLayoutData(new TableWrapData(FILL_GRAB));

			Composite body = (Composite) section.getClient();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 5;
			body.setLayout(layout);

			// Project
			createFormBoldLabel(tk, body, "Project");
			projectLk = new Link(body, SWT.NONE);
			projectLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));
			configureOpenLink(projectLk, project);

			// Manager
			createFormBoldLabel(tk, body, "Manager");
			managerLk = new Link(body, SWT.NONE);
			managerLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Chart
			chartCmp = new Composite(body, SWT.NO_FOCUS);
			chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			TableWrapData twd = new TableWrapData(TableWrapData.CENTER, TableWrapData.MIDDLE);
			twd.maxWidth = CHART_WIDTH;
			twd.rowspan = 3;
			chartCmp.setLayoutData(twd);

			// Overdue tasks
			TrackerUiUtils.createFormBoldLabel(tk, body, "Overdue Tasks");
			overdueTasksLk = new Link(body, SWT.NONE);
			overdueTasksLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Due Date
			TrackerUiUtils.createFormBoldLabel(tk, body, "Due Date");
			dueDateLk = new Link(body, SWT.NONE);
			dueDateLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// // Reported by
			// TrackerUiUtils.createFormBoldLabel(tk, body, "Reported by");
			// reporterLk = new Link(body, SWT.NONE);
			// reporterLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// TODO add linked documents

			// Description
			twd = (TableWrapData) TrackerUiUtils.createFormBoldLabel(tk, body, "Details").getLayoutData();
			twd.valign = TableWrapData.TOP;
			descLbl = new Label(body, SWT.WRAP);
			twd = new TableWrapData(FILL_GRAB, TableWrapData.TOP);
			twd.colspan = 3;
			descLbl.setLayoutData(twd);

			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					String managerId = ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_MANAGER);
					if (EclipseUiUtils.notEmpty(managerId))
						managerLk.setText(getUserAdminService().getUserDisplayName(managerId));
					else
						managerLk.setText("");

					String dueDateStr = ConnectJcrUtils.getDateFormattedAsString(milestone,
							TrackerNames.TRACKER_TARGET_DATE, ConnectUiConstants.DEFAULT_DATE_TIME_FORMAT);
					if (EclipseUiUtils.notEmpty(dueDateStr))
						dueDateLk.setText(dueDateStr);

					String desc = ConnectJcrUtils.get(milestone, Property.JCR_DESCRIPTION);
					descLbl.setText(desc);

					// The chart
					CmsUtils.clear(chartCmp);
					TableWrapData twd = (TableWrapData) chartCmp.getLayoutData();
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
						coc.setInput("Open tasks by assignee", ot, CHART_WIDTH, CHART_HEIGHT);
					}

					// Overdue tasks
					Long nb = getOverdueTaskNumber();
					overdueTasksLk.setText(nb < 0 ? "-" : nb.toString());

					parent.layout(true, true);
					section.setFocus();
					super.refresh();
				}
			};
			getManagedForm().addPart(part);
			addMainSectionMenu(part);

			return section;
		}

		private long getOverdueTaskNumber() {
			StringBuilder builder = new StringBuilder();
			try {
				builder.append(XPathUtils.descendantFrom(project.getPath()));
				builder.append("//element(*, ").append(ActivitiesTypes.ACTIVITIES_TASK).append(")");
				// Past due date
				Calendar now = GregorianCalendar.getInstance();
				String overdueCond = XPathUtils.getPropertyDateComparaison(ACTIVITIES_DUE_DATE, now, "<");
				// Only opened tasks
				String notClosedCond = "not(@" + ConnectNames.CONNECT_CLOSE_DATE + ")";
				builder.append("[").append(XPathUtils.localAnd(overdueCond, notClosedCond)).append("]");
				Query query = XPathUtils.createQuery(getSession(), builder.toString());
				return query.execute().getNodes().getSize();
			} catch (RepositoryException e) {
				throw new ActivitiesException("Unable to get overdue tasks number for " + milestone);
			}
		}

		private Composite appendIssuePart(Composite parent, IManagedForm mf) {
			Composite body = new Composite(parent, SWT.NO_FOCUS);
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
			layout.verticalSpacing = 5;
			body.setLayout(layout);
			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendIssuesPart(tableCmp, mf);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			return body;
		}

		/** Creates the list of issues relevant for this category */
		private void appendIssuesPart(Composite parent, IManagedForm mf) {
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
					String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
					CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, jcrId);
				}
			});

			issueListPart = new AbstractFormPart() {
				@Override
				public void refresh() {
					refreshViewer(filterTxt.getText());
					mf.getForm().reflow(true);
					super.refresh();
				}
			};
			mf.addPart(issueListPart);
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
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

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

			String tooltip = "Edit the milestone main information";
			Action action = new OpenConfigureDialog(tooltip, TrackerImages.IMG_DESC_EDIT, sectionPart);
			toolBarManager.add(action);

			tooltip = "Add the task to this milestone";
			action = new AddTask(sectionPart.getSection().getShell(), tooltip, TrackerImages.IMG_DESC_ADD);
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

		private class AddTask extends Action {
			private static final long serialVersionUID = 5112793747049604434L;
			final Shell shell;

			private AddTask(Shell shell, String name, ImageDescriptor img) {
				super(name, img);
				this.shell = shell;
			}

			@Override
			public void run() {
				Session tmpSession = null;
				try {
					AppService as = getAppService();
					tmpSession = milestone.getSession().getRepository().login();
					Node draftTask = as.createDraftEntity(tmpSession, TrackerTypes.TRACKER_TASK);
					draftTask.setProperty(TrackerNames.TRACKER_PROJECT_UID,
							ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID));
					draftTask.setProperty(TrackerNames.TRACKER_MILESTONE_UID,
							ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID));

					ConfigureTaskWizard wizard = new ConfigureTaskWizard(getUserAdminService(), getTrackerService(),
							draftTask);

					WizardDialog dialog = new WizardDialog(shell, wizard);
					if (dialog.open() == Window.OK) {
						String issueBasePath = as.getBaseRelPath(TrackerTypes.TRACKER_TASK);
						Node parent = tmpSession.getNode("/" + issueBasePath);
						Node task = as.publishEntity(parent, TrackerTypes.TRACKER_TASK, draftTask);
						task = as.saveEntity(task, false);
						milestone.getSession().refresh(true);
						refreshViewer(filterTxt.getText());
					}
				} catch (RepositoryException e1) {
					throw new TrackerException("Unable to create issue on " + project, e1);
				} finally {
					JcrUtils.logoutQuietly(tmpSession);
				}
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
		if (notEmpty(name)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return "#" + id + " " + name;
	}

	private Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
		Label label = toolkit.createLabel(parent, " " + value, SWT.END);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		TableWrapData twd = new TableWrapData(TableWrapData.RIGHT, TableWrapData.BOTTOM);
		label.setLayoutData(twd);
		return label;
	}

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
				CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(targetNode));
			}
		});
	}
}
